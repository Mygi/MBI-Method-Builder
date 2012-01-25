package nig.mf.plugin.pssd.services;

import java.util.Vector;

import arc.mf.plugin.PluginService;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcFCPList extends PluginService {

	public static final String DICOM_INGEST_FCP = "pssd-dicom-ingest.fcp";

	private Interface _defn;

	public SvcFCPList() {
	}

	public String name() {

		return "om.pssd.fcp.list";
	}

	public String description() {

		return "List the file compilation profiles for importing local file/directory.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("action", "get-meta");
		dm.add("where", "type='application/arc-fcp'");
		XmlDoc.Element r = executor().execute("asset.query", dm.root());
		Vector<XmlDoc.Element> aes = r.elements("asset");
		if (aes != null) {
			for (XmlDoc.Element ae : aes) {
				String id = ae.value("@id");
				String name = ae.value("name");
				String description = ae.value("description");
				if (name != null) {
					w.push("fcp");
					w.add("id", id);
					w.add("name", name);
					if (description != null) {
						w.add("description", description);
					}
					w.pop();
				}
			}
		}
	}

}