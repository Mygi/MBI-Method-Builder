package nig.mf.plugin.pssd.ni;

import java.util.Collection;

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

public class SvcStudyImport extends PluginService {
	private Interface _defn;

	public SvcStudyImport() {

		_defn = new Interface();

		_defn.add(new Element("id", AssetType.DEFAULT,
				"The asset ID of the DICOM object model Study asset(source dataset) to be imported.", 1, 1));

		_defn.add(new Element("project", CiteableIdType.DEFAULT,
				"The citeable id of the PSSD project.", 1, 1));
		_defn.add(new Element("method", CiteableIdType.DEFAULT,
						"The citeable ID of the method. You need to specify this argument if there are multiple methods for this project.",
						0, 1));
		_defn.add(new Element("step", CiteableIdType.DEFAULT,
						"The step within the method that resulted in this study.", 0, 1));
		/* RSubjects are now deprecated in our system. SO remove these from the interface and deactivate internally
		_defn.add(new Element("no-r-subject",BooleanType.DEFAULT,
						"If it is set to true it will not create an R-Subject asset, populating the Subject meta-data instead (for humans use Subject/private for Identity). Otherwise an R-SUbject will be created. Defaults to false. If it is set to true it conflicts with r-subject and force-r-subject-creation arguments.",
						0, 1));
		_defn.add(new Element("r-subject",CiteableIdType.DEFAULT,
						"The citeable ID of the PSSD R-Subject. If specified, it will ignore the identity information in the input data, and use the specified R-Subject. Otherwise it tries to find a pre-existing R-Subject matching the Identitiy informaition.  This argument conficts with force-r-subject-creation argument.",
						0, 1));
		_defn.add(new Element("force-r-subject-creation",BooleanType.DEFAULT,
						"If true it will create a new R-Subject using the information from the input data without searching for a pre-existing R-Subject.  Defaults to false. If it is set to true it conflicts with r-subject argument.",
						0, 1));
         */
		try {
			DictionaryEnumType eType = new DictionaryEnumType(
					"pssd.study.types");
			_defn.add(new Element("type", eType,
							"The type of the study. You can run 'dictionary.entries.list :dictionary pssd.study.types' to list the available types. If not specified, defaults to MR.",
							0, 1));
		} catch (Throwable e) {
			e.printStackTrace();
		}

		try {
			DictionaryEnumType eType = new DictionaryEnumType(
					"nig.pssd.animal.species");
			_defn.add(new Element("species", eType,
							"The species of the subject. You can run 'dictionary.entries.list :dictionary nig.pssd.animal.species' to list the available species. If not specified. Defaults to human. If no-r-subject is specified, this argument is required.",
							0, 1));
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		_defn.add(new Element("internalize",
						new EnumType(new String[] { "none", "copy", "move" }),
						"'none' turns off content internalization. 'copy' takes a copy, leaving the orgininal. 'move' will move the content (if an accessible file) into the file-system data store (if there is one). Defaults to 'none'.",
						0, 1));

		_defn.add(new Element("destroy-old-assets", BooleanType.DEFAULT,
						"If it is set to true it will internalize the contents (implies 'internalize=move' argument to be true) and it will destroy the old study & series assets when migrating is finished. Defaults to false.",
						0, 1));

	

	}

	public String name() {
		return "nig.pssd.study.import";
	}

	public String description() {
		//return "Import a DICOM study and series into a PSSD project. Creates PSSD Subject, R-Subject (if required) and Ex-Method objects automatically.";
		// RSubjects now deprecated from our system
		return "Import a local DICOM study and series into a local PSSD project. Creates PSSD Subject and Ex-Method objects automatically.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String id = args.value("id");
		String pssdProjectCid = args.value("project");
		if (PSSDUtil.isReplica(executor(), pssdProjectCid)) {
			throw new Exception ("The given Project is a replica. Cannot import data into it");
		}

		String pssdProjectMethodCid = args.value("method");
		if (pssdProjectMethodCid == null) {
			pssdProjectMethodCid = getProjectMethod(pssdProjectCid);
		}
		String namespace = getProjectNamespace(pssdProjectCid);
		String step = args.value("step");
		
		// RSubjects now deprecated from our system
		String pssdRSubjectCid = null;
		//String pssdRSubjectCid = args.value("r-subject");

		String type = args.value("type");
		String species = args.value("species");
		String name = null;
		String description = null;
		//
		String internalize = args.value("internalize");
		if (internalize == null) internalize = "none";

		boolean noRSubject = true;    // RSubjects now deprecated in our system
		/*
		boolean noRSubject = false;
		if (args.value("no-r-subject") != null) {
			if (args.value("no-r-subject").equals("true")) {
				noRSubject = true;
			}
			if (args.value("no-r-subject").equals("false")) {
				noRSubject = false;
			}
		}
		*/

		boolean forceRSubjectCreation = false;
		/*
		if (args.value("force-r-subject-creation") != null) {
			if (args.value("force-r-subject-creation").equals("true")) {
				forceRSubjectCreation = true;
			}
			if (args.value("force-r-subject-creation").equals("false")) {
				forceRSubjectCreation = false;
			}
		}
		*/

		/*
		if (noRSubject && (forceRSubjectCreation || pssdRSubjectCid != null)) {
			throw new Exception(
					"Arguments conflict. no-rsubject arguments conflicts with force-r-subject-creation and r-subject arguments.");
		}
		*/

		/*
		if (forceRSubjectCreation && pssdRSubjectCid != null) {
			throw new Exception(
					"Arguments conflict. You cannot set force-r-subject-creation argument to true and specify r-subject argument at the same time.");
		}
		*/
		
		if (noRSubject && species == null){
			throw new Exception("species argument is required when no-r-subject argument is set to true.");
		}

		boolean destroyOldAssets = false;
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

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", id);
		doc.add("pdist", 0);               // Force local
		XmlDoc.Element r1 = executor().execute("asset.get", doc.root());

		String assetType = r1.value("asset/type");
		if (!assetType.equals("dicom/study")) {
			throw new Exception("Asset (id=" + id
					+ ") is not a valid dicom/study asset.");
		}

		String fromNamespace = r1.value("asset/namespace");
		doc = new XmlDocMaker("args");
		doc.add("namespace", fromNamespace);
		XmlDoc.Element r1a = executor().execute("asset.namespace.describe", doc.root());
		if (r1a.value("namespace/store").equals("db")) {
			throw new Exception(
					"Contents of dicom study(id="
							+ id
							+ ")'s series assets are in database. It is not supported by this service. You can move it into namespace which uses file system.");
		}

		String dicomStudyUID = r1.value("asset/meta/mf-dicom-study/uid");

		String dicomPatientAssetId = r1
				.value("asset/related[@type='had-by']/to");
		if (dicomPatientAssetId == null) {
			throw new Exception(
					"No dicom patient asset relates to study asset(id=" + id
							+ ")");
		}

		// Now create the Subject, R-Subject & Ex-Method or the Subject & Ex-Method
		String[] cids = new String[2];
		if (!noRSubject) {			
			if (pssdRSubjectCid == null) {
				pssdRSubjectCid = createPSSDRSubject(dicomPatientAssetId,
						pssdProjectCid, pssdProjectMethodCid, namespace,
						forceRSubjectCreation);
				w.add("R-Subject", pssdRSubjectCid);
			} else {
				doc = new XmlDocMaker("args");
				doc.add("cid", pssdRSubjectCid);
				XmlDoc.Element r1b = executor().execute("asset.get", doc.root());
				if (r1b.value("asset/model") == null) {
					throw new Exception("The specified r-subject(cid="
							+ pssdRSubjectCid + ") is not a valid PSSD asset.");
				}
				if (!r1b.value("asset/model").equals("om.pssd.r-subject")) {
					throw new Exception("The specified r-subject(cid="
							+ pssdRSubjectCid
							+ ") is not a valid PSSD r-subject asset.");
				}
			}
			cids = createPSSDSubject(pssdProjectCid,
					pssdProjectMethodCid, pssdRSubjectCid, dicomPatientAssetId, id, species);
		} else {
			cids = createPSSDSubject(pssdProjectCid,
					pssdProjectMethodCid, null, dicomPatientAssetId, id, species);
		}
		//
		String pssdSubjectMid = cids[0];
		String pssdSubjectCid = cids[1];
		w.add("Subject", pssdSubjectCid);
		w.add("Ex-Method", pssdSubjectMid);

	
		// Retrofit
		doc = new XmlDocMaker("args");
		doc.add("id", id);
		doc.add("pid", pssdSubjectMid);
		doc.add("step", step);                    // Only suitable for simple methods with one step
		doc.add("type", type);
	    doc.add("internalize", internalize);
	    doc.add("destroy-old-assets", destroyOldAssets);
	    doc.add("validate-subject-identity", false);
		XmlDoc.Element r2 = executor().execute("nig.pssd.study.retrofit", doc.root());
		String pssdStudyCid = r2.value("cid");
		w.add("Study", pssdStudyCid);
	
	}





	
	String getProjectMethod(String pssdProjectCid) throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("cid", pssdProjectCid);
		doc.add("pdist", 0);         // Force local
		XmlDoc.Element r1 = executor().execute("asset.get", doc.root(), null,
				null);
		String method = r1.value("asset/meta/pssd-project/method/id");
		if (method == null) {
			throw new Exception("No method found in PSSD project asset(cid="
					+ pssdProjectCid + ");");
		}
		return method;

	}

	String getProjectNamespace(String pssdProjectCid) throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("cid", pssdProjectCid);
		doc.add("pdist", 0);         // Force local
		XmlDoc.Element r1 = executor().execute("asset.get", doc.root(), null,
				null);
		String namespace = r1.value("asset/namespace");
		return namespace;

	}

	String createPSSDRSubject(String dicomPatientAssetId,
			String pssdProjectCid, String pssdProjectMethodCid,
			String namespace, boolean forceRSubjectCreation) throws Throwable {

		// Fetch subject meta-data from the DICOM header
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", dicomPatientAssetId);
		doc.add("pdist", 0);         // Force local
		XmlDoc.Element r1 = executor().execute("asset.get", doc.root());
		String firstName = r1
				.value("asset/meta/mf-dicom-patient/name[@type='first']");
		String lastName = r1
				.value("asset/meta/mf-dicom-patient/name[@type='last']");

		String pssdRSubjectAssetId = null;
		String pssdRSubjectCid = null;

		// See if we find a pre-existing RSubject.
		// RSubjects are now deprecated and not encrypted.
		if (!forceRSubjectCreation) {
			doc = new XmlDocMaker("args");
			String queryString = "model='om.pssd.r-subject' ";
			if (firstName != null) {

				queryString += "and xpath(hfi.pssd.human.identity/first) as string = '"
						+ firstName + "' ";

			}
			if (lastName != null) {

				queryString += "and xpath(hfi.pssd.human.identity/last) as string = '"
						+ lastName + "' ";
			}
			doc.add("where", queryString);
			doc.add("action", "get-meta");
			doc.add("pdist", 0);         // Force local
			XmlDoc.Element r2 = executor().execute("asset.query", doc.root());
			if (r2.elements("asset") != null) {
				if (r2.elements("asset").size() > 1) {
					String errMsg = "";
					Collection<String> cids = r2.values("asset/cid");
					if(cids!=null){
						for (String cid : cids) {
							errMsg = errMsg + "(r-subject: cid=" + cid + "), ";
						}
					}
					throw new Exception("More than one r-subjects," + errMsg + "are found. You can specified one of them use r-subject argument.");
				}
				pssdRSubjectAssetId = r2.value("asset/@id");
				pssdRSubjectCid = r2.value("asset/cid");
			}
		}
		
		if(pssdRSubjectCid==null) {
			doc = new XmlDocMaker("args");
			doc.push("administration");
			doc.add("project", pssdProjectCid);
			doc.pop();
			doc.add("namespace", namespace);
			doc.add("method", pssdProjectMethodCid);
			doc.push("identity");
			doc.push("hfi.pssd.human.identity");
			doc.add("first", firstName);
			doc.add("last", lastName);
			doc.pop();
			doc.pop();
			doc.push("private");
			doc.add(r1.element("asset/meta/mf-dicom-patient"), false);
			doc.pop();
			XmlDoc.Element r4 = executor().execute("om.pssd.r-subject.create",doc.root());
			pssdRSubjectCid = r4.value("id");
		}
		
		return pssdRSubjectCid;

	}

	String[] createPSSDSubject(String pssdProjectCid,
			String pssdProjectMethodCid, String pssdRSubjectCid,
			String dicomPatientAssetId, String dicomStudyAssetId, String species) throws Throwable {

		// Get subject details from the DICOM header
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", dicomPatientAssetId);
		XmlDoc.Element r0 = executor().execute("asset.get", doc.root());
		String sex = r0.value("asset/meta/mf-dicom-patient/sex");
		String dob = r0.value("asset/meta/mf-dicom-patient/dob");
		String id = r0.value("asset/meta/mf-dicom-patient/id");
		String firstName = r0.value("asset/meta/mf-dicom-patient/name[@type='first']");
        String lastName = r0.value("asset/meta/mf-dicom-patient/name[@type='last']");
	
		doc.add("id", dicomStudyAssetId);
		XmlDoc.Element r0a = executor().execute("asset.get", doc.root());
		// "Brain Research Institute" : "MEDPC"
		// "Children's MRI Centre @ RCH" : "MRC35113"
		// "Howard Florey Institute" : "EPT"
		String institution = r0a.value("asset/meta/mf-dicom-study/location/institution");
		String station = r0a.value("asset/meta/mf-dicom-study/location/station");
		
		String pssdSubjectAssetId = null;
		String pssdExMethodCid = null;
		String pssdSubjectCid = null;
		String[] cids = new String[2];
		cids[0] = null;
		cids[1] = null;
		
		// If r-subject is specified or found
		// RSubject are now deprecated from our system and this variable will be null
		if(pssdRSubjectCid!=null){
			doc = new XmlDocMaker("args");
			doc.add("where", "cid starts with '" + pssdProjectCid
					+ "' and xpath(pssd-subject/r-subject)='" + pssdRSubjectCid
					+ "' and model='om.pssd.subject'");
			doc.add("pdist", 0);         // Force local
			XmlDoc.Element r1 = executor().execute("asset.query", doc.root());
			pssdSubjectAssetId = r1.value("id");
			
			// If the subject has been already created.
			if (pssdSubjectAssetId != null) {
				doc = new XmlDocMaker("args");
				doc.add("id", pssdSubjectAssetId);
				doc.add("pdist", 0);         // Force local
				XmlDoc.Element r1a = executor().execute("asset.get", doc.root());
				pssdSubjectCid = r1a.value("asset/cid");
				cids[1] = pssdSubjectCid;

				doc = new XmlDocMaker("args");
				doc.add("id", pssdSubjectCid);
				doc.add("pdist", 0);         // Force local
				XmlDoc.Element r1b = executor().execute("om.pssd.collection.members", doc.root());
				pssdExMethodCid = r1b.value("object/id");

				if (pssdExMethodCid != null) {
					Collection<XmlDoc.Element> es = r1b.elements("object");
					if (es.size() > 1) {
						boolean found = false;
						for (XmlDoc.Element e : es) {

							if (pssdProjectMethodCid.equals(e.value("object/method/id"))) {
								found = true;
								pssdExMethodCid = e.value("object/id");
								cids[0] = pssdExMethodCid;
								return cids;
							}
						}
						if (!found) {
							throw new Exception(
									"pssdSubjectCid:"
											+ pssdSubjectCid
											+ " Found the subject, but cannot find the Ex-Method matching the project Method: "
											+ pssdProjectMethodCid);
						}
					} else {
						cids[0] = pssdExMethodCid;
					}
				} else {
					throw new Exception("No ExMethod found.");
				}
			}
		}

		doc = new XmlDocMaker("args");
		doc.add("pid", pssdProjectCid);
		doc.add("method", pssdProjectMethodCid);
		if(pssdRSubjectCid!=null){
			doc.add("r-subject", pssdRSubjectCid);
		}
		doc.push("public");
		doc.push("hfi.pssd.subject");
		doc.add("type", "animal");
		doc.add("control", "false");
		doc.pop();
		if (sex != null || dob != null) {
			doc.push("hfi.pssd.animal.subject");
			if (dob != null) {
				doc.add("birthDate", dob);
			}
			if (sex != null) {
				doc.add("gender", sex);
			}
			if (species != null) {
				doc.add("species", species);
			}
			doc.pop();
		}
		if (id != null && station != null) {
			String itype = null;
			if(station.equals("EPT")){
				itype = "aMRIF";
			} else if(station.equals("MRC35113")){
				itype = "RCH"; 
			} else {
				itype = "Other";
			}
			doc.push("hfi.pssd.identity");
			doc.add("id",new String[]{"type",itype}, id);
			doc.pop();
		}
		doc.pop();
		// For Humans, if no R-Subject store Identity in Subject/private
		if (pssdRSubjectCid==null && species.equals("human")) {
			if (firstName != null && lastName != null) {
				doc.push("private");
				doc.push("hfi.pssd.human.identity");
				doc.add("first", firstName);
				doc.add("last", lastName);
				doc.pop();
				doc.pop();
			}
		}
		XmlDoc.Element r2 = executor().execute("om.pssd.subject.create", doc.root());
		pssdSubjectCid = r2.value("id");
		pssdExMethodCid = r2.value("id/@mid");

		cids[0] = pssdExMethodCid;
		cids[1] = pssdSubjectCid;
		return cids;
	}

	
}