package nig.mf.pssd.client.bruker;

import java.util.Collection;
import java.util.Vector;

import arc.mf.client.ServerClient;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;

// Simple wrapper class for PSSD Study object. A Study may have both
// Bruker and DICOM DataSet children. 

public class Study extends BaseObject {

	public static final String OBJECT_TYPE = "study";

	private String _studyUID;
	private String _studyID;
	private boolean _hasBruker;    // Does the Study hold Bruker meta-data can have DICOM too)
	private boolean _hasDicom;     // Does the Study hold DICOM meta-data (can have Bruker too)

	protected Study(String id, String name, String description, String studyUID, String studyID, boolean isBruker,
			boolean isDicom) {
		super(id, name, description);
		_studyUID = studyUID;
		_studyID = studyID;
		_hasBruker = isBruker;
		_hasDicom = isDicom;
	}

	public String studyUID() {
		return _studyUID;
	}

	public String studyID() {
		return _studyID;
	}

	public boolean hasBruker() {
		return _hasBruker;
	}

	public boolean hasDicom() {
		return _hasDicom;
	}

	/**
	 * Create a Bruker study.
	 * 
	 * @param cxn
	 * @param pid  Parent ExMethod
	 * @param name
	 * @param description
	 * @param studyUID
	 * @param studyID
	 * @return
	 * @throws Throwable
	 */
	public static Study create(ServerClient.Connection cxn, String pid, String name, String description,
			String studyUID, String studyID, String domain, String user) throws Throwable {

		// Find the method step
		XmlStringWriter w = new XmlStringWriter();
		w.add("id", pid);
		w.add("type", PSSDUtil.BRUKER_STUDY_TYPE);
		XmlDoc.Element r = cxn.execute("om.pssd.ex-method.study.step.find", w.document());
		String methodStep = r.value("ex-method/step[0]");
		if (methodStep == null) {
			throw new Exception("The ex-method " + pid + " does not have a study of type/modality: "
					+ PSSDUtil.BRUKER_STUDY_TYPE);
		}
		// Create a study with bruker doc attached
		w = new XmlStringWriter();
		w.add("pid", pid);
		w.add("step", methodStep);
		w.add("type", PSSDUtil.BRUKER_STUDY_TYPE);
		if (name != null) {
			// study name is specified
			w.add("name", name);
		} else {
			// study name is not specified, "MR" is used as name.
			w.add("name", PSSDUtil.BRUKER_STUDY_TYPE);
		}
		if (description != null) {
			w.add("description", description);
		}
		w.push("meta");
		w.push("hfi-bruker-study", new String[] { "ns", PSSDUtil.BRUKER_META_NAMESPACE });
		w.add("id", studyID);
		w.add("uid", studyUID);
		//
		w.push("ingest");
		w.add("date", "now");
		w.add("domain", domain);
		w.add("user", user);
		w.pop();
		//
		w.pop();
		w.pop();
		r = cxn.execute("om.pssd.study.create", w.document());
		String id = r.value("id");
		return new Study(id, name, description, studyUID, studyID, true, false);

	}

	/**
	 * Create study without knowing the ex-method id (pid), assuming there is only one ex-method within the subject and
	 * use it.
	 * 
	 * @param cxn
	 * @param subjectCID
	 * @param name
	 * @param description
	 * @param studyUID
	 * @param studyID
	 * @return
	 * @throws Throwable
	 */
	public static Study createFromSubjectCID(ServerClient.Connection cxn, String subjectCID, String name,
			String description, String studyUID, String studyID, String domain, String user) throws Throwable {
		Vector<ExMethod> exms = Subject.getExMethods(cxn, subjectCID);
		ExMethod exm = null;
		if (exms != null) {
			if (exms.size() > 0) {
				exm = exms.get(0);
			}
		}
		if (exm == null) {
			throw new Exception("Could not find a ExMethod in Subject: " + subjectCID);
		}
		return create(cxn, exm.id(), name, description, studyUID, studyID, domain, user);
	}

	/**
	 * Update an existing study (could be DICOM or Bruker) with specified Bruker meta.
	 * 
	 * @param cxn
	 * @param id
	 * @param name
	 * @param description
	 * @param studyUID
	 * @param studyID
	 * @return
	 * @throws Throwable
	 */
	public static Study update(ServerClient.Connection cxn, String id, String name, String description,
			String studyUID, String studyID, String domain, String user) throws Throwable {

		// Update with Bruker meta-data
		// This should be enhanced to only update if not pre-existing else we will end
		// up with multiples (e.g. upload fid and image)
		XmlStringWriter w = new XmlStringWriter();
		w.add("id", id);
		w.add("type", PSSDUtil.BRUKER_STUDY_TYPE);
		if (name != null) {
			w.add("name", name);
		}
		if (description != null) {
			w.add("description", description);
		}
		w.push("meta");
		w.push("hfi-bruker-study", new String[] { "ns", PSSDUtil.BRUKER_META_NAMESPACE });
		w.add("id", studyID);
		w.add("uid", studyUID);
		//
		w.push("ingest");
		w.add("date", "now");
		w.add("domain", domain);
		w.add("user", user);
		w.pop();
		//
		w.pop();
		w.pop();
		cxn.execute("om.pssd.study.update", w.document());

		// Check if the study is also a dicom study.
		w = new XmlStringWriter();
		w.add("id", id);
		XmlDoc.Element r = cxn.execute("om.pssd.object.describe", w.document());
		boolean hasDicomStudy = false;
		if (r.element("object/meta/mf-dicom-study") != null) {
			hasDicomStudy = true;
		}
		return new Study(id, name, description, studyUID, studyID, true, hasDicomStudy);

	}

	/**
	 * Find a study (Bruker or DICOM) by Study UID.
	 * 
	 * @param cxn
	 * @param subjectCID The CID of the Subject we are uploading the data to.
	 * @param exMethodCID The CID of the ExMethod we are uploading data to. May be null if unknown. IN this
	 *                     case, a Study will be auto-created under the first ExMethod.
	 * @param studyUID
	 * @return the citeable id of the study
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	public static Study find(ServerClient.Connection cxn, String subjectCID, String exMethodCID, String studyUID) throws Throwable {

		// Restrict the query to the desired tree as well as we can
		String cidQuery = null;
		if (exMethodCID!=null) {
			cidQuery = "cid starts with '" + exMethodCID + "'";
		} else {
			cidQuery = "cid starts with '" + subjectCID + "'";
		}
		
		//
		XmlStringWriter w = new XmlStringWriter();
		w.add("where", cidQuery + " and model = 'om.pssd.study' and " + "( xpath(mf-dicom-study/uid) = '" + studyUID
				+ "' or xpath(hfi-bruker-study/uid) = '" + studyUID + "' )");
		w.add("action", "get-meta");
		XmlDoc.Element r = cxn.execute("asset.query", w.document());
		Collection<XmlDoc.Element> assets = r.elements("asset");
		if (assets == null) {
			return null;
		}
		if (assets.size() == 0) {
			return null;
		}
		if (assets.size() != 1) {
			String errMsg = "Found multiple PSSD studies with the same studyUID " + studyUID
					+ ". They could be duplicate studies." + " Please ask administrator to resolve this problem.";
			PSSDUtil.logError(cxn, errMsg);
			throw new Exception(errMsg);
		}

		// Get meta from existing asset
		String id = r.value("asset/cid");
		String name = r.value("asset/meta/pssd-object/name");
		String description = r.value("asset/meta/pssd-object/description");
		
		boolean hasDicom =  r.element("asset/meta/mf-dicom-study") != null;
		boolean hasBruker = r.element("asset/meta/hfi-bruker-study") != null;
		
		// Validate studyUID if both DICOM and Bruker meta attached
		if (hasDicom && hasBruker) {
			String uidDicom = r.value("asset/meta/mf-dicom-study/uid");
			String uidBruker = r.value("asset/meta/hfi-bruker-study/uid");
			if (!uidDicom.equals(uidBruker)) {
				String errMsg = "hfi-bruker-study/uid is not equal to mf-dicom-study/uid on PSSD study asset(cid=" + id
				+ ").";
				PSSDUtil.logError(cxn, errMsg);
				throw new Exception(errMsg);
			}
		}
		
		// Get studyID element.  This code is suspect (nebk) as there is no guarentee
		// that the IDs are the same.
		String studyID = r.value("asset/meta/hfi-bruker-study/id");
		if (studyID == null) {
			studyID = r.value("asset/meta/mf-dicom-study/id");
		}
		return new Study(id, name, description, studyUID, studyID, hasBruker, hasDicom);

	}

	/**
	 * Check if the study exists.
	 * 
	 * @param cxn
	 * @param id
	 *            the citeable id of the study.
	 * @return
	 * @throws Throwable
	 */
	public static boolean exists(ServerClient.Connection cxn, String id) throws Throwable {

		return exists(cxn, id, Study.OBJECT_TYPE);

	}

}
