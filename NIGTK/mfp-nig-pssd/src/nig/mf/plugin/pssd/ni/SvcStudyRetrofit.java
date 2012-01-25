package nig.mf.plugin.pssd.ni;

import java.util.Collection;

import nig.mf.MimeTypes;
import nig.mf.pssd.plugin.util.CiteableIdUtil;
import nig.mf.pssd.plugin.util.PSSDUtil;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.DictionaryEnumType;
import arc.mf.plugin.dtype.BooleanType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcStudyRetrofit extends PluginService {
	private Interface _defn;

	public SvcStudyRetrofit() {

		_defn = new Interface();

		_defn.add(new Element("id", AssetType.DEFAULT,
				"The asset ID of the local DICOM object model Study asset to be retrofitted.", 1, 1));
		_defn.add(new Element("pid", CiteableIdType.DEFAULT,
						"The citeable ID of the local, parent PSSD ExMethod or Study. If an ExMethod, a new Study is created. If a Study, that Study is used and the Series are converted to DataSets under that Study.",
						1, 1));
		_defn.add(new Element("step", CiteableIdType.DEFAULT,
				"The step within the method that resulted in this study.", 0, 1));

		try {
			DictionaryEnumType eType = new DictionaryEnumType("pssd.study.types");
			_defn.add(new Element("type", eType,
					"The type of the study. If not specified, then method must be specified.", 0, 1));
		} catch (Throwable e) {
			e.printStackTrace();
		}

		_defn.add(new Element("internalize",
						new EnumType(new String[] { "none", "copy", "move" }),
						"'none' turns off content internalization. 'copy' takes a copy, leaving the orgininal. 'move' will move the content (if an accessible file) into the file-system data store (if there is one). Defaults to 'move'.",
						0, 1));

		_defn.add(new Element("destroy-old-assets", BooleanType.DEFAULT,
						"If true internalize the contents (implies 'internalize=move' argument to be true) and it will destroy the old study & series assets when migrating is finished. Defaults to true.",
						0, 1));

		_defn.add(new Element("validate-subject-identity", BooleanType.DEFAULT,
						"If true compare the DICOM Patient name with the PSSD Subject/private or R-Subject/Identity  Human identity. Defaults to false.",
						0, 1));

	}

	public String name() {
		return "nig.pssd.study.retrofit";
	}

	public String description() {
		return "Retrofit DICOM object model Study asset to pre-existing PSSD ExMethod or Study. Creates Study if  needed.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// Args
		String id = args.value("id");
		String mid = args.value("pid");
		if (PSSDUtil.isReplica(executor(), mid)) {
			throw new Exception ("The given object '" + mid + "' is a replica. Cannot import data into it.");
		}
		String type = args.value("type");
		String step = args.value("step");
		String internalize = args.value("internalize");

		// Some parsing
		String name = null;
		String description = null;
		boolean validateSubjectIdentity = args.booleanValue("validate-subject-identity", false);

		if (internalize == null) {
			internalize = "move";
		}
		boolean destroyOldAssets = true;
		if (args.value("destroy-old-assets") != null) {
			if (args.value("destroy-old-assets").equals("true")) {
				destroyOldAssets = true;
			}
			if (args.value("destroy-old-assets").equals("false")) {
				destroyOldAssets = false;
			}
		}
		if (destroyOldAssets == true) {
			internalize = "move";
		}
		if (destroyOldAssets == true && internalize.equals("none")) {
			throw new Exception("destroy-old-assets implies internalize=move.");
		}

		// What kind of Object is the parent ?
		Boolean isExMethod = true;
		try {
			PSSDUtil.isValidExMethod(executor(), mid, true);
		} catch (Throwable t) {
			isExMethod = false;
		}
		if (!isExMethod) {
			try {
				PSSDUtil.isValidStudy(executor(), mid, true);
			} catch (Throwable t) {
				throw new Exception("Parent object is neither ExMethod nor Study");
			}
		}

		// Get the parent Subject CID
		String pssdSubjectCid = null;
		String pssdExMethodCid = null;
		String pssdStudyCid = null;
		if (isExMethod) {
			pssdExMethodCid = mid;
			pssdSubjectCid = CiteableIdUtil.getParentId(mid);
		} else {
			pssdStudyCid = mid;
			pssdExMethodCid = CiteableIdUtil.getParentId(mid);
			pssdSubjectCid = CiteableIdUtil.getParentId(pssdExMethodCid);
		}

		// Validate input STudy type
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", id);
		doc.add("pdist", 0);       // FOrce local
		XmlDoc.Element r1 = executor().execute("asset.get", doc.root());

		String assetType = r1.value("asset/type");
		if (!assetType.equals("dicom/study")) {
			throw new Exception("Asset (id=" + id + ") is not a valid dicom/study asset.");
		}

		// Validate subject identity and retrieve Patient asset ID
		String pid = findPatientAssetID(id);
		if (validateSubjectIdentity) {
			if (doValidateSubjectIdentity(pid, id, pssdExMethodCid) == false) {
				throw new Exception("Fails subject identity validation.");
			}
		}

		// Update subject hfi.pssd.identity first
		updatePSSDSubjectIdentity(id, pssdSubjectCid);

		String cid = r1.value("asset/cid");
		// Create Study if needed
		if (pssdStudyCid == null) {
			if (type == null) type = "Magnetic Resonance Imaging";
			name = r1.value("asset/meta/mf-dicom-study/id"); // There is nothing else to use
			description = r1.value("asset/meta/mf-dicom-study/description");

			// om.pssd.study.create
			doc = new XmlDocMaker("args");
			if (name != null) doc.add("name", name);
			if (description != null) doc.add("description", description);
			doc.add("pid", mid);
			doc.add("type", type);
			if (step != null) doc.add("step", step);
			// Create
			XmlDoc.Element r2 = executor().execute("om.pssd.study.create", doc.root());
			pssdStudyCid = r2.value("id");
		} 
		
		// Now copy mf-dicom-study over from DICOM study to PSSD Study
		doc = new XmlDocMaker("args");
		doc.add("id", pssdStudyCid);
		doc.push("meta", new String[] {"action", "merge"});
		if (r1.element("asset/meta/mf-dicom-study") != null) {
			XmlDoc.Element xeMFDicomStudy = r1.element("asset/meta/mf-dicom-study");
			xeMFDicomStudy.add(new XmlDoc.Attribute("ns", "dicom"));
			doc.add(xeMFDicomStudy);
		}
		doc.push("mf-note");
		String note = "Retroftitted from " + id;
		if (cid != null) {
			note += "," + cid;
		}
		doc.add("note", note);
		doc.pop();
		doc.pop();
		//
		XmlDoc.Element r2 = executor().execute("om.pssd.study.update", doc.root());

		// Now migrate the Series into DataSets of the Study
		w.add("cid", pssdStudyCid);
		Collection seriesAssetIds = r1.values("asset/related[@type='contains']/to");
		if (seriesAssetIds != null) {
			migrateSeriesAll(seriesAssetIds, pssdStudyCid);
		}

		// Internalize Assets
		if (!internalize.equals("none")) {
			internalizeAssets(pssdStudyCid, internalize);
		}

		// Destroy old Assets
		if (destroyOldAssets == true) {
			destroyOldStudy(id);
			destroyAsset(pid);
		}

	}

	private void migrateSeriesAll(Collection<String> seriesAssetIds, String pssdStudyCid) throws Throwable {

		for (String seriesAssetId : seriesAssetIds) {
			String[] dataSetCIDs = migrateSeries(seriesAssetId, pssdStudyCid);

			// Ensure names of DataSets are correctly formed from DICOM "protocol_description".
			// Also ensure that the DataSets description is filled in.
			// These services are consistent with the behaviour of the DICOM PSSD server
			XmlDocMaker doc = null;
			XmlDoc.Element r = null;
			if (dataSetCIDs[0] != null) {
				doc = new XmlDocMaker("args");
				doc.add("cid", dataSetCIDs[0]);
				doc.add("overwrite", true);
				r = executor().execute("nig.pssd.dataset.name.grab", doc.root());
				r = executor().execute("nig.pssd.dataset.description.grab", doc.root());
			}
			if (dataSetCIDs[1] != null) {
				doc = new XmlDocMaker("args");
				doc.add("cid", dataSetCIDs[1]);
				doc.add("overwrite", true);
				r = executor().execute("nig.pssd.dataset.name.grab", doc.root());
				r = executor().execute("nig.pssd.dataset.description.grab", doc.root());
			}
		}

	}

	private String[] migrateSeries(String seriesAssetId, String pssdStudyCid) throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", seriesAssetId);
		doc.add("pdist", 0);       // FOrce local
		XmlDoc.Element r1 = executor().execute("asset.get", doc.root());
		String seriesCid = r1.value("asset/cid");

		// Check whether the content type is supported.
		String seriesAssetType = r1.value("asset/type");
		if (!(seriesAssetType.equals(MimeTypes.DICOM_SERIES) || seriesAssetType.equals(MimeTypes.BRUKER_SERIES))) {
			throw new Exception("The content type of asset(id=" + seriesAssetId + ") is not supported.");
		}

		// Check whether the content is in DB or File System.
		String seriesAssetContentUrl = r1.value("asset/content/url");
		String seriesAssetContentType = r1.value("asset/content/type");
		String name = r1.value("asset/meta/mf-dicom-series/protocol");

		if (seriesAssetContentUrl == null && r1.element("asset/content") != null) {
			throw new Exception(
					"Content of asset(id="
							+ seriesAssetId
							+ ") is in database. It is not supported by this service. You can move it into namespace which uses file system.");
		}

		String pssdPrimaryDatasetCid = null;
		String pssdPrimaryDatasetVid = null;
		String pssdDerivationDatasetCid = null;

		String proxySeriesAssetId = r1.value("asset/related[@type='proxy']/to");
		if (proxySeriesAssetId != null) {
			// migrate proxy asset as primary dataset.
			pssdPrimaryDatasetCid = migrateProxySeries(proxySeriesAssetId, seriesAssetId, pssdStudyCid);
			doc = new XmlDocMaker("args");
			doc.add("cid", pssdPrimaryDatasetCid);
			doc.add("pdist", 0);       // FOrce local
			XmlDoc.Element r2 = executor().execute("asset.get", doc.root(), null, null);
			pssdPrimaryDatasetVid = r2.value("asset/@vid");
		}

		// migrate series asset as derivation dataset.
		doc = new XmlDocMaker("args");
		if (name == null) {
			name = "Migrated_from_" + seriesAssetId;
		}
		doc.add("name", name);
		doc.add("pid", pssdStudyCid);
		if (pssdPrimaryDatasetCid != null && pssdPrimaryDatasetVid != null) {
			doc.add("input", new String[] { "vid", pssdPrimaryDatasetVid }, pssdPrimaryDatasetCid);
		}
		doc.add("type", seriesAssetType);
		
		// Copy over the mf-dicom-series meta-data
		doc.push("meta");
		if (seriesAssetType.equals(MimeTypes.DICOM_SERIES)) {
			XmlDoc.Element xeMFDicomSeries = r1.element("asset/meta/mf-dicom-series");
			xeMFDicomSeries.add(new XmlDoc.Attribute("ns", "dicom"));
			doc.add(xeMFDicomSeries);
		}

		doc.push("mf-note");
		String note = "Retrofitted from " + seriesAssetId;
		if (seriesCid != null) {
			note += "," + seriesCid;
		}
		doc.add("note", note);
		doc.pop();

		doc.pop();

		if (pssdPrimaryDatasetCid != null) {
			String methodCid = getMethodCid(pssdStudyCid);
			doc.push("transform");
			doc.add("id", methodCid);
			doc.pop();
		}
		XmlDoc.Element r3 = executor().execute("om.pssd.dataset.derivation.create", doc.root());
		pssdDerivationDatasetCid = r3.value("id");

		if (seriesAssetContentUrl != null) {
			setAssetContentUrlAndType(pssdDerivationDatasetCid, seriesAssetContentUrl, seriesAssetContentType);
		}

		//
		String[] cids = new String[2];
		cids[0] = pssdDerivationDatasetCid;
		cids[1] = pssdPrimaryDatasetCid;
		return cids;

	}

	private String migrateProxySeries(String proxySeriesAssetId, String seriesAssetId, String pssdStudyCid)
			throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", seriesAssetId);
		XmlDoc.Element r1 = executor().execute("asset.get", doc.root());
		String name = r1.value("asset/meta/mf-dicom-series/protocol");

		doc = new XmlDocMaker("args");
		doc.add("id", proxySeriesAssetId);
		doc.add("pdist", 0);       // FOrce local
		XmlDoc.Element r2 = executor().execute("asset.get", doc.root());
		String proxySeriesCid = r2.value("asset/cid");
		String proxySeriesAssetContentUrl = r2.value("asset/content/url");
		String proxySeriesAssetContentType = r2.value("asset/content/type");
		String proxySeriesAssetType = r2.value("asset/type");

		if (proxySeriesAssetContentUrl == null && r2.element("asset/content") != null) {
			throw new Exception(
					"Content of asset(id="
							+ proxySeriesAssetId
							+ ") is in database. It is not supported by this service. You can move it into namespace which uses file system.");
		}

		doc = new XmlDocMaker("args");
		if (name == null) {
			name = "Migrated_from_" + proxySeriesAssetId;
		}
		doc.add("name", name);
		doc.add("pid", pssdStudyCid);
		doc.add("type", proxySeriesAssetType);
		doc.push("meta");
		if (proxySeriesAssetType.equals(MimeTypes.DICOM_SERIES)) {
			XmlDoc.Element xeMFDicomSeries = r1.element("asset/meta/mf-dicom-series");
			xeMFDicomSeries.add(new XmlDoc.Attribute("ns", "dicom"));
			doc.add(xeMFDicomSeries);
		}
		doc.push("mf-note");
		String note = "M_FROM " + proxySeriesAssetId;
		if (proxySeriesCid != null) {
			note += "," + proxySeriesCid;
		}
		doc.add("note", note);
		doc.pop();
		doc.pop();

		doc.push("subject");
		doc.add("state", 1);
		doc.pop();

		XmlDoc.Element r3 = executor().execute("om.pssd.dataset.primary.create", doc.root());
		String pssdPrimaryDatasetCid = r3.value("id");

		if (proxySeriesAssetContentUrl != null) {
			setAssetContentUrlAndType(pssdPrimaryDatasetCid, proxySeriesAssetContentUrl, proxySeriesAssetContentType);
		}

		return pssdPrimaryDatasetCid;

	}

	private void setAssetContentUrlAndType(String cid, String contentUrl, String contentType) throws Throwable {

		// asset.set :cid $cid :url -by reference $url
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("cid", cid);
		doc.add("url", new String[] { "by", "reference" }, contentUrl);
		doc.add("ctype", contentType);
		XmlDoc.Element r = executor().execute("asset.set", doc.root());

	}

	private String getMethodCid(String pssdStudyCid) throws Throwable {

		String pssdExMethodCid = getParentCid(pssdStudyCid);
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("cid", pssdExMethodCid);
		doc.add("pdist", 0);       // FOrce local
		XmlDoc.Element r = executor().execute("asset.get", doc.root());
		String methodCid = r.value("asset/meta/pssd-ex-method/method");

		return methodCid;

	}

	private String getParentCid(String cid) {

		String pid = null;
		if (cid.indexOf(".") > 0) {
			String[] parts = cid.split("\\.");
			pid = parts[0];
			for (int i = 1; i < parts.length - 1; i++) {
				pid = pid + "." + parts[i];
			}
		}
		return pid;

	}

	private void internalizeAssets(String cid, String method) throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("cid", cid);
		doc.add("pdist", 0);       // FOrce local
		XmlDoc.Element r = executor().execute("asset.get", doc.root());
		if (r.value("asset/meta/pssd-object/type") == null) {
			throw new Exception("asset(cid=" + cid + ") is not a PSSD object.");
		}

		doc = new XmlDocMaker("args");
		doc.add("where", "cid starts with '" + cid + "' and content is external");
		doc.add("size", "infinity");
		doc.add("action", "pipe");
		doc.push("service", new String[] { "name", "asset.internalize" });
		doc.add("method", method);
		doc.pop();
		doc.add("pdist", 0);       // FOrce local
		XmlDoc.Element r1 = executor().execute("asset.query", doc.root());

	}

	void destroyOldStudy(String OldStudyAssetId) throws Throwable {

		// Find the patient asset that goes with this Study

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", OldStudyAssetId);
		doc.add("pdist", 0);       // FOrce local
		XmlDoc.Element r = executor().execute("asset.get", doc.root());
		Collection<String> PSSSeriesAssetIds = r.values("asset/related[@type='contains']/to");
		if (PSSSeriesAssetIds != null) {
			for (String seriesAssetId : PSSSeriesAssetIds) {
				destroySeries(seriesAssetId);
			}
		}

		destroyAsset(OldStudyAssetId);

	}

	void destroySeries(String seriesAssetId) throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", seriesAssetId);
		doc.add("pdist", 0);       // FOrce local
		XmlDoc.Element r = executor().execute("asset.get", doc.root());
		Collection<String> proxyAssetIds = r.values("asset/related[@type='proxy']/to");
		if (proxyAssetIds != null) {
			for (String proxyAssetId : proxyAssetIds) {
				destroyAsset(proxyAssetId);
			}
		}

		destroyAsset(seriesAssetId);

	}

	void destroyAsset(String assetId) throws Throwable {

		if (assetId == null)
			return;
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", assetId);
		doc.add("imc", "true");
		XmlDoc.Element r = executor().execute("asset.destroy", doc.root());

	}

	boolean doValidateSubjectIdentity(String dicomPatientAssetId, String dicomStudyAssetId, String pssdExMethodCid)
			throws Throwable {

		if (dicomPatientAssetId == null)
			return true;

		// Identity from Dicom Patient Asset
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", dicomPatientAssetId);
		doc.add("pdist", 0);       // FOrce local
		XmlDoc.Element r2 = executor().execute("asset.get", doc.root());
		String firstNameA = r2.value("asset/meta/mf-dicom-patient/name[@type='first']");
		String lastNameA = r2.value("asset/meta/mf-dicom-patient/name[@type='last']");
		if (firstNameA != null) {
			firstNameA = firstNameA.trim().toLowerCase();
		}
		if (lastNameA != null) {
			lastNameA = lastNameA.trim().toLowerCase();
		}

		// Fetch the Human Identity from Subject or RSubject
		String pssdSubjectCid = getParentCid(pssdExMethodCid);
		doc = new XmlDocMaker("args");
		doc.add("id", pssdSubjectCid);
		XmlDoc.Element r3 = executor().execute("om.pssd.object.describe", doc.root());  // Decrypts any encryption
		String pssdRSubjectCid = r3.value("object/r-subject");
		//
		String firstNameB = null;
		String lastNameB = null;
		if (pssdRSubjectCid == null) {
			
			// Fetch the human identity

			firstNameB = r3.value("object/private/hfi.pssd.human.identity/first");
			lastNameB = r3.value("object/private/hfi.pssd.human.identity/last");
		} else {
			
			doc = new XmlDocMaker("args");
			doc.add("id", pssdRSubjectCid);
			XmlDoc.Element r4 = executor().execute("om.pssd.object.descibe", doc.root());
			firstNameB = r4.value("object/identity/hfi.pssd.human.identity/first");
			lastNameB = r4.value("object/identity/hfi.pssd.human.identity/last");
		}
		//
		if (firstNameB != null) {
			firstNameB = firstNameB.trim().toLowerCase();
		}
		if (lastNameB != null) {
			lastNameB = lastNameB.trim().toLowerCase();
		}
		if (firstNameA != null && firstNameB != null && !firstNameA.equals(firstNameB)) {
			return false;
		}
		if (lastNameA != null && lastNameB != null && !lastNameA.equals(lastNameB)) {
			return false;
		}
		return true;

	}

	String findPatientAssetID(String dicomStudyAssetId) throws Throwable {
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", dicomStudyAssetId);
		doc.add("pdist", 0);       // FOrce local
		XmlDoc.Element r1 = executor().execute("asset.get", doc.root());

		return r1.value("asset/related[@type='had-by']/to");
	}

	void updatePSSDSubjectIdentity(String dicomStudyAssetId, String PSSDSubjectCid) throws Throwable {

		// Identity from Dicom Patient Asset
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", dicomStudyAssetId);
		doc.add("pdist", 0);       // FOrce local
		XmlDoc.Element r1 = executor().execute("asset.get", doc.root());
		// "Brain Research Institute" : "MEDPC"
		// "Children's MRI Centre @ RCH" : "MRC35113"
		// "Howard Florey Institute" : "EPT"
		String institution = r1.value("asset/meta/mf-dicom-study/location/institution");
		String station = r1.value("asset/meta/mf-dicom-study/location/station");

		String dicomPatientAssetId = r1.value("asset/related[@type='had-by']/to");
		if (dicomPatientAssetId == null) return;

		doc = new XmlDocMaker("args");
		doc.add("id", dicomPatientAssetId);
		doc.add("pdist", 0);       // FOrce local
		XmlDoc.Element r2 = executor().execute("asset.get", doc.root());
		String patientId = r2.value("asset/meta/mf-dicom-patient/id");

		if (patientId == null) return;
		if (institution == null || station == null) return;

		String itype = null;
		if (station.equals("EPT")) {
			itype = "aMRIF";
		} else if (station.equals("MRC35113")) {
			itype = "RCH";
		} else {
			itype = "Other";
		}

		// Identity from PSSD SUbject
		doc = new XmlDocMaker("args");
		doc.add("id", PSSDSubjectCid);
		XmlDoc.Element r3 = executor().execute("om.pssd.object.describe", doc.root());

		String PSSDIdentityId = r3.value("object/public/hfi.pssd.identity/id[@type='" + itype + "']");
		if (PSSDIdentityId != null && PSSDIdentityId.equals(patientId)) return;

		// Add new identity
		doc = new XmlDocMaker("args");
		doc.add("id", PSSDSubjectCid);
		doc.push("public");

		doc.push("hfi.pssd.identity");
		doc.add("id", new String[] { "type", itype }, patientId);
		doc.pop();

		doc.pop();
		XmlDoc.Element r4 = executor().execute("om.pssd.subject.update", doc.root());

	}

	String[] getDicomPatientName(String dicomPatientAssetId) throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", dicomPatientAssetId);
		doc.add("pdist", 0);       // FOrce local
		XmlDoc.Element r2 = executor().execute("asset.get", doc.root());
		String firstName = r2.value("asset/meta/mf-dicom-patient/name[@type='first']");
		String lastName = r2.value("asset/meta/mf-dicom-patient/name[@type='last']");

		String[] name = null;
		if (firstName != null && lastName != null) {
			name = new String[2];
			name[0] = firstName;
			name[1] = lastName;
		}
		if (name == null) {
			throw new Exception("Patient name is null or incomplete.");
		}

		return name;

	}

}