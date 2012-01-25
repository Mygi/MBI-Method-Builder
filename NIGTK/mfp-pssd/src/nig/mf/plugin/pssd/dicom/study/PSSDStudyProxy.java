package nig.mf.plugin.pssd.dicom.study;


import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.Session;
import arc.mf.plugin.dicom.*;
import arc.xml.*;
import nig.iio.dicom.StudyMetadata;
import nig.mf.plugin.pssd.util.MailHandler;
import nig.mf.plugin.util.DateUtil;
import nig.mf.pssd.plugin.util.CiteableIdUtil;


/**
 * The PSSD study occurs under an ex-method. The incoming configuration
 * can connect to either:
 * 
 *  1. The subject, in which case the ex-method and study may need to 
 *     be created. If existing method, then or directly to the study.
 *     
 *  2. The study, which has already been created. In that case, there
 *     is nothing to do.
 *     
 * @author Jason
 *
 */
public class PSSDStudyProxy extends StudyProxy {

	private String _ns;                     // namespace to put data in; not currently used
	private String _subject;                // cid of Subject
	private String _exmethod;               // cid of Ex-Method
	private String _study;                  // cid of Study
	private String _methodStep;             // The STep in the Method. Not supplied in the constructor.
	private StudyMetadata _sm;
	private boolean _createdStudy;
	private String _subjectMetaService;     // Service to set research domain-specific meta-data

	/**
	 * Constructor.  We expect 1) just the Subject, 2) the Subject and ExMethod or 3) The Subject,
	 * ExMethod and Study CIDs to be given.
	 *     
	 * 
	 * @param studyId
	 */
	public PSSDStudyProxy(String namespace, String studyUID, String subject, String exmethod,
			String study, StudyMetadata sm, String subjectMetaService) {
		super(studyUID);
		_ns      = namespace; 
		_subject = subject; 
		_exmethod  = exmethod; 
		_methodStep = null; 
		_study   = study;   
		_sm      = sm;
		_createdStudy = false;
		_subjectMetaService = subjectMetaService;
	}

	public String id() {
		return _study;
	}

	public String subject() {
		return _subject;
	}
	public String exMethod() {
		return _exmethod;
	}

	public String methodStep() {
		return _methodStep;
	}

	public StudyMetadata metaData() {
		return _sm;
	}

	public long createAsset(ServiceExecutor executor) throws Throwable {
		// Find the Ex-Method object that is registered with this Subject
		//  If not specified, then should return the
		// primary method for the subject.
		if ( _exmethod == null ) {
			findExMethod(executor);
		}

		// If the DICOM meta-data and server configuration do not specify
		// a Study CID, then _study will be null. Find or create the 
		// study to which this DICOM Study will be attached.  
		if ( _study == null ) {
			findOrCreateStudy(executor);
		}

		// If the Study was created by this DICOM server, the _methodStep will
		// be filled in.  If the Study has been supplied by CID, or a pre-existing
		// DICOM/Bruker Study found in findOrCreateStudy, we still need
		// to find the method step of that study and make sure it's consistent
		// with the modality of the DICOM data.
		if ( _methodStep == null ) {
			findMethodStep(executor);
		}

		// At this point the STudy has either been created by this class
		// or it pre-existed.   Now add the DICOM meta-data
		// to the Study.  Overwrite if it pre-exists
		addStudyMetaData(executor);

		// Attach domain-dependent meta-data (e.g. neuroimaging) via the specified service (DICOM control)
		if (_subjectMetaService!=null) {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("id", _subject);

			// Convert container to XML.  The parent is "dicom" ready to be added to the "dicom" element of the service.
			XmlDoc.Element m = _sm.toXML();
			if (m!=null) {
				dm.add(m);
				try {
					executor.execute(_subjectMetaService, dm.root());
				} catch (Throwable t) {
					// If it fails, we don't want to throw an exception. Just write to logfile
					System.out.println("Failed to set domain-specific subject meta-data with service " + _subjectMetaService + " : " + t.getMessage());
				}
			}
		}


		// We need to convert the meta-data container to an XmlDoc.ELement and pass in
		// to this service

		// Get ID of Study
		String studyId = nig.mf.pssd.plugin.util.CiteableIdUtil.cidToId(executor, _study);
		return  (long)Integer.valueOf(studyId);
	}

	/**
	 * Find the Method to which the study belongs. We may (or may not) 
	 * have a Method specified.
	 * 
	 * @param executor
	 * @throws Throwable
	 */
	private void findExMethod(ServiceExecutor executor) throws Throwable {
		if ( _exmethod != null ) {
			return;
		}

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id",_subject);

		// Find the Method object registered in the Subject
		XmlDoc.Element r = executor.execute("om.pssd.object.describe",dm.root());
		String mid = r.value("object/method/id");	
		if ( mid == null ) {
			throw new Exception("There is no Method for PSSD subject " + _subject);
		}

		// Find the Ex-Method within the subject that is executing the primary
		// method. It's an error if there is no ex-method.
		dm = new XmlDocMaker("args");
		dm.add("id",_subject);
		dm.add("method",mid);	
		r = executor.execute("om.pssd.subject.method.find",dm.root());
		_exmethod = r.value("id");

		if ( _exmethod == null ) {
			throw new Exception("There is no instantiated ex-method for PSSD subject " + _subject);
		}
	}

	private void findMethodStep(ServiceExecutor executor) throws Throwable {
		if ( _methodStep != null ) {
			return;
		}

		if ( _study == null ) {
			return;
		}

		// Find the Step in the Method that was used to create this Study
		// If a study is pre-created, the step should be in the Study meta-data
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id",_study);
		XmlDoc.Element r = executor.execute("om.pssd.object.describe",dm.root());
		_methodStep = r.value("object/method/step");
		if (_methodStep==null) {
			// The flow of the DICOM server means we shouldn't get to this point.
			// But if we do somehow then we default to the first step in Method that has
			// a Study TYPE that matches the DICOM modality.
			// CLients should fill in the method step when creating Studies
			_methodStep = getFirstStepPath (executor, _exmethod, _sm.modality());
		} else {
			// Having obtained the Method step, we now need to validate that
			// this step is consistent with the DICOM modality. 
			
			// Find all the steps implicitly consistent with this DICOM modality
			Boolean explicit = false;
			Collection<XmlDoc.Element> steps = getMethodStudySteps (executor, _exmethod, _sm.modality(), explicit);
			Boolean found = false;
			for (XmlDoc.Element step : steps) {
				if (step.value().equals(_methodStep)) {
					found = true;
					break;
				}
			}
			if (!found) {
				throwModalityError (executor, _exmethod, _methodStep, _sm.modality());
			}
		}
	}

	private void findOrCreateStudy(ServiceExecutor executor) throws Throwable {
		if ( _study != null ) {
			return;
		}

		XmlDocMaker dm = new XmlDocMaker("args");

		// We need the ExMethod to create a new Study for us unless there is a Study 
		// with the same UID.  If it exists, we use it. Note that this Study might have 
		// been created by the Bruker client or a DICOM client.  The goal is to have one
		// STudy with either/both DICOM and Bruker meta-data attached and both DICOM and Bruker format
		// DataSets. The DICOM DataSets are derivations of the Bruker DataSets
		String query = "cid starts with '" + _exmethod + "' and xpath(mf-dicom-study/uid)='" + super.studyUID() + "'";
		dm.add("where",query);
		dm.add("action","get-cid");	
		dm.add("pdist", 0);      // Force local
		XmlDoc.Element r = executor.execute("asset.query",dm.root());
		String sid= r.value("cid");

		// If no DICOM try Bruker Study.   Could enhance by testing for both...but what
		// would we do if we had both ?
		if (sid==null) {
			dm = new XmlDocMaker("args");
			query = "cid starts with '" + _exmethod + "' and xpath(hfi-bruker-study/uid)='" + super.studyUID() + "'";
			dm.add("where",query);
			dm.add("action","get-cid");	
			dm.add("pdist", 0);      // Force local
			r = executor.execute("asset.query",dm.root());
			sid = r.value("cid");
		}
		if (sid!=null) {
			_study = sid;
			return;
		}

		// There is no pre-existing DICOM or Bruker study to be updated. In that case, 
		// ask the ExMethod to create a new Study of the correct type and modality.
		// Defer the addition of the optional meta-data to later
		String modality = _sm.modality();
		_methodStep = getFirstStepPath (executor, _exmethod, modality);

		// Now create the Study for the specified step
		// There is not really a piece of DICOM meta-data
		// that is appropriate for the name. Perhaps the description, 
		// although for most of our RCH data this is always the same string
		dm = new XmlDocMaker("args");
		dm.add("pid",_exmethod);
		dm.add("step",_methodStep);
		if (_sm.description() != null) {
			dm.add("description", _sm.description());
		}

		r = executor.execute("om.pssd.study.create",dm.root());
		_study = r.value("id");

		_createdStudy = true;
	}


	private String getFirstStepPath (ServiceExecutor executor,  String exMethodCid, String modality) throws Throwable {

		// The DICOM modality is no-longer equated to Study type. We must find the
		// first Step in the Method that specifies DICOM data of the current modality
		// is acceptable.  No specification in the Method means that any DICOM modality
		// is allowed.
		/*
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id",exMethodCid);
		//dm.add("type",modality);	
		// Implicit search; will find Steps with no modality since this means allow any modality
		dm.add("dicom-modality", new String[] {"explicit", "false"}, modality);  
		XmlDoc.Element r = executor.execute("om.pssd.ex-method.study.step.find",dm.root());	
		String step  = r.value("ex-method/step[0]");          // First Step path

		*/
		
		Collection<XmlDoc.Element> steps = getMethodStudySteps (executor, exMethodCid, modality, false);
		Iterator<XmlDoc.Element> it = steps.iterator();

		// Get the first step
		XmlDoc.Element stepEl = it.next();
		String step = stepEl.value();
		// System.out.println("getFirstStepPath : found first step " + step);

		// It is quite possible there is more than Step in the Method that makes
		// a Study of this kind.  This server will only find the first.
		// To be smarter, the Study would need to be pre-created and the
		// CID specified in the DICOM meta-data. Many of our projects
		// now do this. 
		if ( step == null ) throwModalityError (executor, exMethodCid, null, modality);
		return step;
	}
	
	private Collection<XmlDoc.Element> getMethodStudySteps (ServiceExecutor executor, String exMethodCid, String modality, Boolean explicit) throws Throwable {

		// The DICOM modality is no-longer equated to Study type. We must find the
		// first Step in the Method that specifies DICOM data of the current modality
		// is acceptable.  No specification in the Method means that any DICOM modality
		// is allowed.
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id",exMethodCid);
		//dm.add("type",modality);	
		// Implicit search; will find Steps with no modality since this means allow any modality
		dm.add("dicom-modality", new String[] {"explicit", explicit.toString()}, modality);  
		XmlDoc.Element r = executor.execute("om.pssd.ex-method.study.step.find",dm.root());
		if (r==null) return null;
		Collection<XmlDoc.Element> steps = r.elements("ex-method/step");
		return steps;
	}

	
	private void throwModalityError (ServiceExecutor executor, String exMethodCid, String stepPath, String modality) throws Throwable {
		// Send an email to the admins. This is a bit easier than faffing about with
		// notifications and triggers.  Since it's an error condition it's ok to 
		// do this without any end-user configuration (see pssd-notification)
		String projectId = CiteableIdUtil.getProjectCID(exMethodCid);
		String msg = "The ex-method " + exMethodCid + " does not have a study which supports DICOM modality : " + modality;
		if (stepPath!=null) {
			msg = "The ex-method " + exMethodCid + " for step " + stepPath + " does not have a study which supports DICOM modality : " + modality;
		}
		String subject = "Failed DICOM upload for project " + projectId;
		MailHandler.sendAdminMessage (executor, projectId, subject, msg);

		// Generate exception
		throw new Exception(msg);
	}


	private void addStudyMetaData (ServiceExecutor executor) throws Throwable {

		// Retrieve meta-data on PSSD Study object
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id",_study);
		XmlDoc.Element r = executor.execute("om.pssd.object.describe",dm.root());
		XmlDoc.Element object = r.element("object");

		// Copy "interface" arguments to output document 
		dm = new XmlDocMaker("args");
		dm.add("id", _study);
		dm.add("type", object.value("type"));
		//
		String name = object.value("name");
		if (name != null) dm.add("name", name);
		String desc = object.value("description");
		if (desc != null) dm.add("description", desc);

		// Now add in element "meta/mf-dicom-study". 
		XmlDoc.Element oldMeta = object.element("meta");
		fillStudyMetadata(dm, oldMeta);

		// Set the meta-data
		executor.execute("om.pssd.study.update",dm.root());
	}

	private void fillStudyMetadata(XmlDocMaker dm, XmlDoc.Element oldMeta) throws Throwable {

		// Format the new date in MF date format
		Date newDate = _sm.date();
		String newDateStr = DateUtil.formatDate(newDate);

		// We will  *add* the new metadata (not replace) but handle the case
		// of an overwrite (i.e. mf-dicom-study already exists for this Study)
		// By adding the mf-dicom-study meta-data, we will help ourselves if we end up
		// with wrong additional data in a Study somehow ; we will have more of the
		// audit trail with an extra mf-dicom-study
		String oldUID = null;
		String oldID = null;
		String oldDate = null;
		String oldDesc = null;
		if (oldMeta != null) {
			oldUID = oldMeta.value("mf-dicom-study/uid");
			oldID = oldMeta.value("mf-dicom-study/id");
			oldDate= oldMeta.value("mf-dicom-study/sdate");
			oldDesc = oldMeta.value("mf-dicom-study/description");

			// This should be enough...
			if(oldUID != null && oldID != null && oldDate != null && oldDesc != null) {
				if (oldUID.equals(_sm.UID()) &&
						oldID.equals(_sm.id()) &&
						oldDate.equals(newDateStr) &&
						oldDesc.equals(_sm.description())) {	
					return;
				}
			}

		}

		// Add new
		dm.push("meta", new String[] {"action", "add"});
		dm.push("mf-dicom-study",new String[] { "ns", "dicom" });

		if ( _sm.UID() != null ) {
			dm.add("uid",_sm.UID());
		}

		if ( _sm.id() != null ) {
			dm.add("id",_sm.id());
		}

		dm.push("ingest");
		dm.add("date","now");
		dm.add("domain",Session.user().domain());
		dm.add("user",Session.user().name());
		dm.pop();

		if ( _sm.institution() != null || _sm.station() != null ) {
			dm.push("location");
			if ( _sm.institution() != null ) {
				dm.add("institution",_sm.institution());
			}

			if ( _sm.station() != null ) {
				dm.add("station",_sm.station());
			}

			dm.pop();
		}


		if ( _sm.description() != null ) {
			dm.add("description",_sm.description());
		}

		if ( _sm.date() != null ) {
			dm.add("sdate", _sm.date());
		}

		if ( _sm.rpn() != null ) {
			dm.add("rpn",_sm.rpn());
		}

		if ( _sm.havePatientDetails() ) {
			dm.push("subject");

			if ( _sm.patientSex() != null ) {
				dm.add("sex",_sm.patientSex());
			}

			if ( _sm.patientAge() != -1 ) {
				dm.add("age",_sm.patientAge());
			}

			if ( _sm.patientWeight() != -1 ) {
				dm.add("weight",_sm.patientWeight());
			}

			if ( _sm.patientLength() != -1 ) {
				dm.add("size",_sm.patientLength());
			}

			dm.pop();
		}

		dm.pop();
	}


	public void destroyAsset(ServiceExecutor executor) throws Throwable {
		if ( !_createdStudy ) {
			return;
		}

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id",_study);

		executor.execute("om.pssd.object.destroy",dm.root());

		_createdStudy = false;
	}
}
