package nig.mf.plugin.pssd.ni;

import java.util.Collection;
import java.util.Vector;

import nig.mf.MimeTypes;
import nig.mf.pssd.plugin.util.PSSDUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcPSSDIdentityGrab extends PluginService {
	private Interface _defn;

	public SvcPSSDIdentityGrab() {

		_defn = new Interface();
		_defn.add(new Element("project", CiteableIdType.DEFAULT,
				"The citeable id of the local PSSD project (to operate on all Subjects).", 0, 1));
		_defn.add(new Element("subject", CiteableIdType.DEFAULT,
				"The citeable id of the local PSSD subject (to operate on one Subject).", 0, 1));
	}

	public String name() {

		return "nig.pssd.identity.grab";

	}

	public String description() {

		return "This service extracts the Patient ID out of the DICOM meta-data and insert into the hfi.pssd.identity meta-data on the PSSD Subject";

	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {

		String projectCid = args.value("project");
		String subjectCid = args.value("subject");

		if (projectCid != null && subjectCid != null) {
			throw new Exception("You cannot specify both project and subject.");
		}

		if (projectCid != null) {
			if (PSSDUtil.isReplica(executor(), projectCid)) {
				throw new Exception ("The given Project is a replica. Cannot modify");
			}
			rebuildProjectIdentities(projectCid, w);
		}

		if (subjectCid != null) {
			if (PSSDUtil.isReplica(executor(), subjectCid)) {
				throw new Exception ("The given subject is a replica. Cannot modify");
			}
			rebuildSubjectIdentities(subjectCid, w);
		}
		
		if (projectCid==null&&subjectCid==null){
			
			XmlDocMaker doc = new XmlDocMaker("args");
			doc.add("where","model='om.pssd.subject'");
			doc.add("size","infinity");
			doc.add("action","get-meta");
			doc.add("pdist", 0);         // Force local
			XmlDoc.Element r1 = executor().execute("asset.query", doc.root(), null,
					null);
			Collection<String> projectCids = r1.values("asset/cid");
			if(projectCids!=null){
				for (String cid : projectCids) {
					rebuildProjectIdentities(cid,w);
				}
			}	
		}
	}
	
	private void rebuildProjectIdentities(String projectCid, XmlWriter w) throws Throwable {
		
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("cid",projectCid);
		doc.add("pdist", 0);                   // Force local
		XmlDoc.Element r1 = executor().execute("asset.get", doc.root(), null,
				null);
		
		if(r1.value("asset/model")==null){
			throw new Exception(projectCid + " is not a valid pssd project.");
		}
		if(!r1.value("asset/model").equals("om.pssd.project")){
			throw new Exception(projectCid + " is not a valid pssd project.");
		}
		
		doc = new XmlDocMaker("args");
		doc.add("where","cid in '" + projectCid + "' and model='om.pssd.subject'");
		doc.add("size","infinity");
		doc.add("action","get-meta");
		doc.add("pdist", 0);
		XmlDoc.Element r2 = executor().execute("asset.query", doc.root(), null,
				null);
		Collection<String> subjectCids = r2.values("asset/cid");
		if(subjectCids!=null){
			for (String subjCid : subjectCids) {
				rebuildSubjectIdentities(subjCid,w);
			}
		}
		
	}

	private void rebuildSubjectIdentities(String subjectCid, XmlWriter w) throws Throwable {
		
		// asset.get :cid $subjectCid
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("cid", subjectCid);
		doc.add("pdist", 0);
		XmlDoc.Element r1 = executor().execute("asset.get", doc.root(), null,
				null);
		if(r1.value("asset/model")==null){
			throw new Exception(subjectCid + " is not a valid pssd subject.");
		}
		if(!r1.value("asset/model").equals("om.pssd.subject")){
			throw new Exception(subjectCid + " is not a valid pssd subject.");
		}
		Collection<XmlDoc.Element> PSSDIdentities = r1.elements("asset/meta/hfi.pssd.identity[@ns='pssd.public']");
		Collection<String[]> patientIds = getPatientIds(subjectCid);
		if(patientIds==null){
			return;
		}
		
		for (String[] pIdInfo : patientIds) {
			String institution = pIdInfo[0];
			String station = pIdInfo[1];
			String newIdValue = pIdInfo[2];
			
			String newIdType = null;
			if(station.equals("EPT")){
				newIdType = "aMRIF";
			} else if(station.equals("MRC35113")){
				newIdType = "RCH"; 
			} else {
				newIdType = "Other";
			}
	
			if(!identityExists(PSSDIdentities,newIdType,newIdValue)){
				doc = new XmlDocMaker("args");
				doc.add("cid",subjectCid);
				doc.push("meta", new String[]{"action","add"});
				doc.push("hfi.pssd.identity",new String[]{"ns","pssd.public"});
				doc.add("id",new String[]{"type",newIdType}, newIdValue);
				doc.pop();
				doc.pop();
				XmlDoc.Element r2 = executor().execute("asset.set", doc.root());
				// 
				w.push("subject",new String[]{"cid",subjectCid});
				w.push("hfi.pssd.identity",new String[]{"ns","pssd.public"});
				w.add("id",new String[]{"type",newIdType}, newIdValue);
				w.pop();
				w.pop();
				
			}

		}
		

	}
	
	private boolean identityExists(Collection<XmlDoc.Element> identities, String type, String id) throws Throwable {
		
		if(identities==null){
			return false;
		}
		
		boolean exist = false;
		for (XmlDoc.Element identity : identities) {
			String idType = identity.value("id/@type");
			String idValue = identity.value("id");
			if(idType==null||idValue==null){
				continue;
			}
			if(idType.equals(type)&&idValue.equals(id)){
				exist = true;
				break;
			}
		}
		
		return exist;

	}

	private Collection<String[]> getPatientIds(String subjectCid) throws Throwable {

		// asset.query :where cid starts with '$subjectCid'
		// and model='om.pssd.study'
		// and mf-dicom-study has value
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("size", "infinity");
		doc.add("action", "get-meta");
		doc.add("where", "cid starts with '" + subjectCid
				+ "' and model='om.pssd.study' and mf-dicom-study has value");
		doc.add("pdist", 0);       // Force local
		XmlDoc.Element r = executor().execute("asset.query", doc.root());
		Collection<String> dicomStudyCids = r.values("asset/cid");
		if (dicomStudyCids == null) {
			return null;
		}
		Vector<String[]> patientIds = new Vector<String[]>();
		for (String dicomCid : dicomStudyCids) {
			String[] patientId = getPatientIdFromDicomStudy(dicomCid);
			if (patientId != null) {
				boolean exists = false;
				for (String[] patientId2 : patientIds) {
					if (patientId2[0].equals(patientId[0])
							&& patientId2[1].equals(patientId[1])
							&& patientId2[2].equals(patientId[2])) {
						exists = true;
						break;
					}
				}
				if (!exists) {
					patientIds.add(patientId);
				}
			}
		}
		return patientIds;

	}

	private String[] getPatientIdFromDicomStudy(String dicomStudyCid)
			throws Throwable {

		// asset.get :cid $dicomStudyCid
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("cid", dicomStudyCid);
		doc.add("pdist", 0);
		XmlDoc.Element r1 = executor().execute("asset.get", doc.root(), null,
				null);
		String institution = r1
				.value("asset/meta/mf-dicom-study/location/institution");
		String station = r1.value("asset/meta/mf-dicom-study/location/station");

		// asset.query :where cid starts with '$studyCid'
		// and model='om.pssd.dataset'
		// and type='dicom/series'
		doc = new XmlDocMaker("args");
		doc.add("size", "infinity");
		doc.add("where", "cid starts with '" + dicomStudyCid
				+ "' and model='om.pssd.dataset' and type='" + MimeTypes.DICOM_SERIES + "'");
		doc.add("pdist", 0);
		XmlDoc.Element r2 = executor().execute("asset.query", doc.root());
		Collection<String> dicomDatasets = r2.values("id");

		if(dicomDatasets==null){
			return null;
		}
		
		String patientId = null;
		// All the patient ID for the study should be the same
		// So we just use one of the series to get Patient ID
		for (String dicomCid : dicomDatasets) {
			patientId = getPatientIdFromDicomDataset(dicomCid);
			if (patientId != null) {
				break;
			}
		}
		if (patientId == null || station == null || institution == null) {
			return null;
		}
		return new String[] { institution, station, patientId };

	}

	private String getPatientIdFromDicomDataset(String dicomDatasetAssetId)
			throws Throwable {

		// dicom.metadata.get :id $dicomDatasetAssetId
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", dicomDatasetAssetId);
		doc.add("pdist", 0);
		XmlDoc.Element r = executor().execute("dicom.metadata.get", doc.root());
		return r.value("de[@tag='00100020']/value");
	}

}