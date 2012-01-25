package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.Project;
import arc.mf.plugin.PluginService;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcProjectMetadataDescribe extends PluginService {

	private Interface _defn;

	public SvcProjectMetadataDescribe() {
		_defn = new Interface();
	}

	public String name() {
		return "om.pssd.project.metadata.describe";
	}

	public String description() {
		return "Describes the metadata that can be associated with the creation of a local Project object.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("type", Project.TYPE);
		
		// Fetch from the local server. 
		XmlDoc.Element r = executor().execute("om.pssd.type.metadata.describe", dm.root());
		Collection<XmlDoc.Element> mes = r.elements("metadata");
		if (mes != null) {
			w.push("meta");
			for (XmlDoc.Element me : mes) {
				w.add(me);
			}
			w.pop();
		}
	}
}
