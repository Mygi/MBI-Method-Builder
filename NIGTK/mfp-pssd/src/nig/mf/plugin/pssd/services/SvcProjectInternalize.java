package nig.mf.plugin.pssd.services;

import nig.mf.pssd.plugin.util.PSSDUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;


public class SvcProjectInternalize extends PluginService {

	private Interface _defn;

	public SvcProjectInternalize() {

		_defn = new Interface();
		_defn.add(new Element("cid", CiteableIdType.DEFAULT,
				"Citeable id of the project.", 1, 1));
		_defn.add(new Element("method", new EnumType(new String[] { "copy", "move" }),
						"The method of acquiring the content. 'copy' takes a copy, leaving the orgininal. 'move' will move the content (if an accessible file) into the file-system data store (if there is one). Defaults to 'copy'.",
						0, 1));
	}

	public String name() {
		return "om.pssd.project.internalize";
	}

	public String description() {
		return "Internalize (copy external content to local) all the local assets belonging to this local PSSD project.";
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
		
		// Exception if Project has remote children.  It would probably be fine
		// to allow just the local assets to be internalised. Relax if desired.
		if (PSSDUtil.hasRemoteChildren(executor(), cid)) {
			throw new Exception("This project has children on remote peers. Cannot proceed.");
		}
		
		String method = args.value("method");
		if(method==null){
			method = "copy";
		}

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("cid", cid);
		doc.add("pdist", 0);       // Force local
		XmlDoc.Element r = executor().execute("asset.get", doc.root(), null,
				null);

		if (r.value("asset/meta/pssd-object/type") == null) {
			throw new Exception("asset(cid=" + cid + ") is not a PSSD object.");
		}
		if (!r.value("asset/meta/pssd-object/type").equals("project")) {
			throw new Exception("asset(cid=" + cid + ") is not a PSSD project.");
		}

		doc = new XmlDocMaker("args");
		doc.add("where", "cid starts with '" + cid
				+ "' and content is external");
		doc.add("size", "infinity");
		doc.add("action", "pipe");
		doc.push("service", new String[]{"name","asset.internalize"});
		doc.add("method",method);
		doc.pop();
		doc.add("pdist", 0);             // Force local
		executor().execute("asset.query", doc.root());
	}
}