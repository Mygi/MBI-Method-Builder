package nig.mf.plugin.pssd.dicom.study;

import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dicom.*;
import nig.mf.plugin.pssd.dicom.DicomIngestControls;
import nig.mf.plugin.pssd.util.MailHandler;
import nig.mf.pssd.plugin.util.CiteableIdUtil;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.xml.*;

import nig.iio.dicom.DicomElements;
import nig.iio.dicom.StudyMetadata;


public class StudyProxyFactory {

	public static final String UNKNOWN_PROJECT    = "UnknownProject";
	public static final String UNKNOWN_SUBJECT    = "UnknownSubject";
	public static final String UNKNOWN_PATIENT_ID = "UnknownPatientID";
	public static final String UNKNOWN_STUDY      = "UnknownStudy";
	private static final Integer MIN_CID_DEPTH = 4;


	/**
	 * Creates the "right" (PSSD or DICOM/PSS) study proxy for the incoming study.
	 * 
	 * @param executor
	 * @param studyUID
	 * @param dem
	 * @param ic
	 * @return
	 * @throws Throwable
	 */
	public static StudyProxy createStudyProxy(ServiceExecutor executor,DicomEngineContext ec,String studyUID, DataElementMap dem,DicomIngestControls ic) throws Throwable {


		// Look for citeable id first..
		StudyProxy study = createPSSDStudy(executor,studyUID,dem,ic);
		if ( study != null ) {
			return study;
		}
		DicomAssetEngine pss = ec.engine("pss");
		if ( pss == null ) {
			throw new Exception("No DICOM engine found to process the data.");
		}
		System.out.println("StudyProxyFactory: Fall through to PSS engine");
		return pss.createStudyProxy(executor, studyUID, dem);
	}

	private static StudyProxy createPSSDStudy(ServiceExecutor executor,String studyUID,DataElementMap dem,DicomIngestControls ic) throws Throwable {

		// Do we have a citeable ID anywhere (as configured by the engine) in the DICOM meta-data ?
		String cid = generateCiteableID(executor,dem,ic);
		System.out.println("StudyProxyFactory::createPSSDStudy:  cid = " + cid);
		if ( cid == null ) {
			// This will cause it to fall through to next Engine if any
			return null;
		}
		String projectId = CiteableIdUtil.getProjectCID(cid);


		// Create the metadata to be stored with the study from the given element map
		// This also contains subject-specific information
		StudyMetadata meta = StudyMetadata.createFrom(dem);

		// The next question.. what type of PSSD object is specified by this CID ?
		try {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("id",cid);

			// We have a CID, see if the object exists for it.
			XmlDoc.Element r = executor.execute("om.pssd.object.exists",dm.root());
			if ( !r.booleanValue("exists") ) {

				// See if this CID is for a Subject based on its depth.
				// Try to auto-create the Subject if requested in the DICOM controls
				if (nig.mf.pssd.CiteableIdUtil.isSubjectId(cid)) {
					if (ic.autoSubjectCreate()) {
						try {
							createSubject (executor, cid, meta);
						} catch (Throwable t) {
							if (projectId != null) {
								String subject = "PSSD DICOM Engine - auto Subject create failed" + cid;
								String msg = "Auto-creation of a Subject with citable identifier '" + cid + "' \n failed with message: " + t.getMessage() + "\n The process may fall through to the standard Mediaflux DICOM data model engine if configured."; 
								MailHandler.sendAdminMessage(executor, projectId, subject, msg);
							}
							return null; 
						}
					} else {
						if (projectId != null) {
							String subject = "PSSD DICOM Engine - citable identifier has no asset" + cid;
							String msg = "The citable identifier '" + cid + "' represents a Subject but it does not exist \n and subject auto-creation is not enabled (DICOM server control). \n  The process may fall through to the standard Mediaflux DICOM data model engine if configured."; 
							MailHandler.sendAdminMessage(executor, projectId, subject, msg);
						}
						return null; 
					}
				}
			}

			r = executor.execute("om.pssd.object.type",dm.root());
			String pssdType = r.stringValue("type","unknown");
			if ( pssdType.equals("unknown") ) {
				return null;
			}

			// Now create/update the Study depending on the type  of object
			// that the CID represents
			String subjectMetaService = ic.subjectMetaService();
			if ( pssdType.equalsIgnoreCase("subject") ) {
				return new PSSDStudyProxy(ic.namespace(),studyUID,cid,null,null,meta, subjectMetaService);
			}

			if ( pssdType.equalsIgnoreCase("ex-method") ) {
				String sid = parentId(cid);
				return new PSSDStudyProxy(ic.namespace(),studyUID,sid,cid,null,meta, subjectMetaService);
			}

			if ( pssdType.equalsIgnoreCase("study") ) {
				String mid = parentId(cid);
				String sid = parentId(mid);
				return new PSSDStudyProxy(ic.namespace(),studyUID,sid,mid,cid,meta, subjectMetaService);
			}

			// The CID does not refer to an object of type that we can handle.  Send a message
			// to the admin of the project if possible (i.e. if we can find the project cid).
			if (projectId != null) {
				String subject = "PSSD DICOM Engine - unhandled citable identifier " + cid;
				String msg = "The citable identifier '" + cid + "' does not represent an object that the PSSD DICOM engine can upload data to.\n The CID should be for a Subject, ExMethod or Study. \n The process may fall through to the standard Mediaflux DICOM data model engine if configured."; 
				MailHandler.sendAdminMessage(executor, projectId, subject, msg);		
			}
			return null;
		} catch ( Throwable t ) {
			System.out.println(t.getMessage() + " (cid=" + cid + ")");
			// TODO: remove
			// log error to debug.log
			logError(executor, t);
			throw t;
		}		
	}


	/**
	 * Function to try to auto-create a Subject of the given CID, if it is of the correct depth
	 * 
	 * 
	 * @param cid
	 * @param sm
	 * @return
	 * @throws Throwable
	 */
	private static void createSubject (ServiceExecutor executor, String cid, StudyMetadata sm) throws Throwable {


		// Get Project CID and get Methods
		String pid = nig.mf.pssd.plugin.util.CiteableIdUtil.getProjectCID(cid);
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", pid);
		XmlDoc.Element r = executor.execute("om.pssd.object.describe", dm.root());
		Collection<XmlDoc.Element> methods = r.elements("object/method");

		// We can't proceed if there are no Methods or more than one (how could we choose ?)
		if (methods==null) {
			// There is nothing we can do but throw an exception
			throw new Exception ("There is no Method registered with the Project - cannot auto-create Subject");
		}
		if (methods.size()>1) {
			// There is nothing we can do but throw an exception
			throw new Exception ("There is more than 1  Method registered with the Project; cannot select for Subject auto-creation");
		}

		// Get the only bMethod CID
		Iterator<XmlDoc.Element> it = methods.iterator();
		XmlDoc.Element method = it.next();
		String mid = method.value("id");
		if (mid==null) {
			// There is nothing we can do but throw an exception
			throw new Exception ("There is no Method registered with the Project - cannot auto-create Subject");
		}

		// If the Subject CID has not been allocated, import it.  
		DistributedAsset dID = new DistributedAsset (null, cid);
		if (!nig.mf.pssd.plugin.util.CiteableIdUtil.cidExists(executor, dID)) {
			nig.mf.pssd.plugin.util.CiteableIdUtil.importCid(executor, null, cid);
		}

		// We already know the asset does not exist so now we can try to create it
		String subjectNumber = nig.mf.pssd.CiteableIdUtil.getLastSection(cid);
		dm = new XmlDocMaker("args");
		dm.add("pid", pid);
		dm.add("subject-number", subjectNumber);
		dm.add("method", mid);

		// If it fails we will catch (outside this function) and send a message to the admin
		executor.execute("om.pssd.subject.create", dm.root());

		// We don't have to bother with any domain-specific meta-data
		// because the Study creation process actually checks the Subject
		// and sets any desired domain-specific meta-data based on the Method
	}



	//TODO: remove
	// the method below is to write some log for debugging the problem of losing perms.
	private static void logError(ServiceExecutor executor, Throwable t) {

		try {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("app", "debug");
			dm.add("event", "error");
			dm.add("msg", t.getMessage());
			executor.execute("server.log", dm.root());
		} catch (Throwable t1){
			t1.printStackTrace(System.out);
		}

	}


	/**
	 * If possible, generate a citeable identifier from the
	 * 
	 * @param dem
	 * @return
	 * @throws Throwable
	 */
	private static String generateCiteableID(ServiceExecutor executor,DataElementMap dem,DicomIngestControls ic) throws Throwable {
		// If CID is specified directly by configuration we are done...	
		if (ic.citableID() != null) {
			String sid = ic.citableID();
			if ( ic.cidPrefix() != null ) {
				sid = ic.cidPrefix() + "." + sid;       // Stick on the server.namespace prefix
			}
			if ( isValidCiteableID(sid) ) {
				if ( depthOf(sid)>= MIN_CID_DEPTH) {
					return sid;
				} else {
					return null;
				}
			} else {
				return null;
			}
		}

		// Continue and see if can extract from DICOM meta-data
		if ( ic.cidElements() == null ) {
			return null;
		}
		// Loop over the configured DICOM ingest controls 
		// and try to extract a CID from the DICOM meta-data
		for ( int i=0; i < ic.cidElements().length; i++ ) {
			String sid = extractID(ic.cidElements()[i],dem,ic.ignoreNonDigits());

			// The study part is optional:
			//
			// project.subject.ex-method[.study]
			//
			if ( sid != null ) {
				
				// Stick on the server.namespace prefix. Do this first
				// else a bare integer (e.g. CID = subject) won't be valid
				if ( ic.cidPrefix() != null ) {
					sid = ic.cidPrefix() + "." + sid;   
				}
				if ( isValidCiteableID(sid) ) {
					if ( depthOf(sid)>= MIN_CID_DEPTH) {
						return sid;
					}
				} else {
					System.out.println("sid is not valid");
				}

			}
		}

		return null;
	}

	/**
	 * Identifies whether the given identifier is valid or not.
	 * 
	 * @param id
	 * @return
	 */
	public static boolean isValidCiteableID(String id) {
		// Handle case of leading or trailing  dot (not valid) here
		int l = id.length();
		String first = id.substring(0);
		String last = id.substring(l-1);
		if (first.equals(".") || last.equals(".")) {
			return false;   
		}
		
		// Handle rest
		boolean wantDot = false;
		String dot = ".";

		StringTokenizer t = new StringTokenizer(id,dot,true);
		while ( t.hasMoreTokens() ) {
			String idp = t.nextToken();
			if ( idp.equals(dot) ) {
				if ( !wantDot ) {
					return false;
				}

				wantDot = false;
			} else {
				if ( !isNumber(idp) ) {
					return false;
				}

				wantDot = true;
			}
		}

		return true;
	}

	private static boolean isNumber(String id) {
		for ( int i=0; i < id.length(); i++ ) {
			if ( !Character.isDigit(id.charAt(i)) ) {
				return false;
			}
		}

		return true;
	}

	private static int depthOf(String cid) throws Throwable {
		int depth = 1;
		int idx = 0;
		idx = cid.indexOf('.',0);
		while ( idx != -1 ) {
			depth++;
			idx = cid.indexOf('.',idx+1);
		}

		return depth;
	}

	private static String parentId(String cid) throws Throwable {
		int idx = cid.lastIndexOf('.');
		if ( idx == -1 ) {
			return null;
		}

		return cid.substring(0,idx);
	}


	/**
	 * This function has a kludge in it to handle the case when patient names
	 * are incorrectly encoded in the DICOM header. The DICOM patient name field
	 * should be of the form "Last^Middle2^Middle1^First".  However, it is  not
	 * uncommon (e.g. RCH DICOM client) to find first and middle names separated 
	 * only by spaces. Thus "Last^First Middle".  Here, in the
	 * PSSD engine we are really only concerned with CIDs (not the actual names)
	 * which we are expecting to find in the as configured DICOM tags (e.g.first name),
	 * so there is  not really a lot of code to write.  The PSS engine needs more.
	 * 
	 * @param ele
	 * @param dem
	 * @param stripLeadingNonDigits
	 * @return
	 * @throws Throwable
	 */
	private static String extractID(int ele, DataElementMap dem, boolean stripLeadingNonDigits) throws Throwable {
		switch ( ele ) {
		case DicomIngestControls.ID_NONE: return null;

		case DicomIngestControls.ID_BY_PATIENT_FULL_NAME: {
			DicomPersonName pn = (DicomPersonName)dem.valueOf(DicomElements.PATIENT_NAME);
			if (pn==null) return null;

			String fullName = pn.fullName();
			System.out.println("StudyProxyFactory::extractID:Full Name = " + fullName);
			if (stripLeadingNonDigits) fullName = nig.mf.pssd.CiteableIdUtil.removeLeadingNonDigits(fullName);
			return fullName;                // Full names should be ok
		}

		case DicomIngestControls.ID_BY_PATIENT_LAST_NAME: {
			DicomPersonName pn = (DicomPersonName)dem.valueOf(DicomElements.PATIENT_NAME);
			if (pn == null) return null;	
			String lastName = pn.last();           // Last names should be ok
			System.out.println("StudyProxyFactory::extractID:Last Name = " + lastName);
			if (stripLeadingNonDigits) lastName = nig.mf.pssd.CiteableIdUtil.removeLeadingNonDigits(lastName);
			return lastName;
		}

		case DicomIngestControls.ID_BY_PATIENT_FIRST_NAME: {
			DicomPersonName pn = (DicomPersonName)dem.valueOf(DicomElements.PATIENT_NAME);
			if (pn == null ) return null;
			String firstName = pn.first();	         // May be a combination of first and middle
			System.out.println("StudyProxyFactory::extractID:First Name = " + firstName);
			if (stripLeadingNonDigits) firstName = nig.mf.pssd.CiteableIdUtil.removeLeadingNonDigits(firstName);
			if (firstName == null) return null;

			// If the middle names are concatenated we expect the name 
			// to be of the form "First  Middle" so just grab the first token
			StringTokenizer st = new StringTokenizer (firstName);
			String name = null;
			if (st.hasMoreTokens()) name = st.nextToken();
			return name;
		}

		case DicomIngestControls.ID_BY_PATIENT_ID: {
			String id = dem.stringValue(DicomElements.PATIENT_ID,UNKNOWN_PATIENT_ID);
			System.out.println("StudyProxyFactory::extractID:Patient ID = " + id);
			if (stripLeadingNonDigits) id = nig.mf.pssd.CiteableIdUtil.removeLeadingNonDigits(id);
			return id;
		}

		case DicomIngestControls.ID_BY_STUDY_ID: {
			String id = dem.stringValue(DicomElements.STUDY_ID,UNKNOWN_STUDY);
			System.out.println("StudyProxyFactory::extractID:Study ID = " + id);
			if (stripLeadingNonDigits) id = nig.mf.pssd.CiteableIdUtil.removeLeadingNonDigits(id);
			return id;
		}

		default: return null;
		}
	}
}
