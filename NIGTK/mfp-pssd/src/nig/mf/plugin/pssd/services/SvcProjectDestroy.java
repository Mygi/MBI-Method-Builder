package nig.mf.plugin.pssd.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import java.util.Collection;

import nig.mf.plugin.util.AssetUtil;
import nig.mf.pssd.plugin.util.PSSDUtil;

public class SvcProjectDestroy extends PluginService {

	private Interface _defn;

	public SvcProjectDestroy() {

		_defn = new Interface();
		_defn.add(new Element("id", AssetType.DEFAULT,
				"Asset ID of the project.", 0, 1));
		_defn.add(new Element("cid", CiteableIdType.DEFAULT,
				"Citeable ID of the project.", 0, 1));
		_defn.add(new Element("destroy-r-subject", BooleanType.DEFAULT,
				"Set to true to destroy any rSubjects that are related to the specified Project but not related to other Projects. Defaults to false (don't destroy any rSubjects)",
				0, 1));
		_defn.add(new Element("destroyCID", BooleanType.DEFAULT,
				"Destroy the CID as as well as the asset (defaults to false). If the CID is not destroyed, it can be reused",
				0, 1));
	}

	public String name() {
		return "om.pssd.project.destroy";
	}

	public String description() {
		return "Destroy a local PSSD project (primary or replica) and optionally related local RSubjects (primary or replica) if they are unused in other Projects.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
	throws Throwable {

		String cid = args.value("cid");
		String id = args.value("id");
		if (cid == null && id == null) {
			throw new Exception("No 'id' or 'cid' argument specified");
		}
		if (cid != null && id != null) {
			throw new Exception("either 'id' or 'cid' required but not both.");
		}

		boolean destroyRSubject = args.booleanValue("destroy-r-subject", false);	
		boolean destroyCID = args.booleanValue("destroyCID", false);

		// Validate
		if (cid==null) cid = AssetUtil.getCid(executor(), id);
		PSSDUtil.isValidProject(executor(), cid, true);	

		// Find R-Subjects on local server related to this Project if needed
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", cid);
		XmlDoc.Element r0 = null;
		if (destroyRSubject) r0 = executor().execute("om.pssd.project.r-subject.find", dm.root());

		// Destroy this PSSD project and all that it contains, its roles and dangling ACLs
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", cid);
		doc.add("destroyCID", destroyCID);
		doc.add("destroy", true);
		executor().execute("om.pssd.object.destroy", doc.root());
		
		/*
		 * generate system event
		 */
//		SystemEventChannel.generate(new PSSDObjectEvent(Action.destroy, cid));

		// Destroy related R-Subjects if possible
		if (r0!=null) {
			Collection<String> rCIDS = r0.values("object/id");

			if (destroyRSubject == true && rCIDS != null) {
				for (String rSubjectCid : rCIDS) {
					doc = new XmlDocMaker("args");
					doc.add("id", rSubjectCid);
					doc.add("destroyCID", destroyCID);

					try {

						// Local destroy only
						// Will throw an exception if RSubject has related Subjects
						executor().execute("om.pssd.object.destroy", doc.root());
					} catch (Exception ex) {
						// continue to next one
					}
				}
			}
		}
	}
}
