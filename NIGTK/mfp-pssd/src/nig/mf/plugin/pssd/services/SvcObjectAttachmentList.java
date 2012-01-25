package nig.mf.plugin.pssd.services;

import java.util.Collection;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcObjectAttachmentList extends PluginService {
	private Interface _defn;

	public SvcObjectAttachmentList() {
		_defn = new Interface();
		Interface.Element me = new Interface.Element("id",
				CiteableIdType.DEFAULT, "The identity of the PSSD object.", 1,
				1);
		_defn.add(me);

	}

	public String name() {
		return "om.pssd.object.attachment.list";
	}

	public String description() {
		return "List all attachments of the specified object.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {

		String id = args.value("id");
		String where = "related to{attached-to} ( cid = '" + id + "' )";
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where", where);
		dm.add("action", "get-meta");
		dm.add("size", "infinity");
		XmlDoc.Element r = executor().execute("asset.query", dm.root());
		Collection<XmlDoc.Element> aes = r.elements("asset");
		if (aes != null) {
			for (XmlDoc.Element ae : aes) {
				String proute = ae.value("@proute");
				if (proute != null) {
					w.push("attachment", new String[] { "proute", proute, "id",
							ae.value("@id") });
				} else {
					w.push("attachment", new String[] { "id", ae.value("@id") });
				}
				w.add("name", ae.value("name"));
				String desc = ae.value("description");
				if (desc != null) {
					w.add("description", desc);
				}
				w.add(ae.element("content/type"), true);
				w.add(ae.element("content/size"), true);
				w.pop();
			}
		}
	}
}