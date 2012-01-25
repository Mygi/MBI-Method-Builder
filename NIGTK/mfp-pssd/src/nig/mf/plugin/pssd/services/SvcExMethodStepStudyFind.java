package nig.mf.plugin.pssd.services;

import java.util.List;

import nig.mf.plugin.pssd.Asset;
import nig.mf.plugin.pssd.Study;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcExMethodStepStudyFind extends PluginService {

	private Interface _defn;

	public SvcExMethodStepStudyFind() {

		_defn = new Interface();
		Interface.Element me = new Interface.Element("id", CiteableIdType.DEFAULT,
				"The identity of the executing ExMethod.", 1, 1);
		me.add(new Interface.Attribute(
				"proute",
				CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.",
				0));
		_defn.add(me);

		_defn.add(new Interface.Element("step", CiteableIdType.DEFAULT, "The step within the method.", 1, 1));
	}

	public String name() {

		return "om.pssd.ex-method.step.study.find";
	}

	public String description() {

		return "Find the studies belongs to the specified step in an ex-method.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String id = args.value("id");
		String stepPath = args.value("step");

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("size", "infinity");
		dm.add("action", "get-value");
		dm.add("xpath", new String[] { "ename", "cid" }, "cid");
		dm.add("xpath", new String[] { "ename", "name" }, "meta/pssd-object/name");
		dm.add("xpath", new String[] { "ename", "type" }, "meta/pssd-study/type");
		dm.add("where", "xpath(pssd-study/method) = '" + id + "' and xpath(pssd-study/method/@step) = '" + stepPath
				+ "'");
		
		// TODO:
		// distributed query

		XmlDoc.Element r = Asset.query(executor(), dm.root());
		List<XmlDoc.Element> aes = r.elements("asset");
		if (aes != null) {
			for (XmlDoc.Element ae : aes) {
				w.push("object", new String[] { "type", Study.TYPE });
				String proute = ae.value("@proute");
				if (proute == null) {
					w.add("id", new String[] { "asset", ae.value("@id") }, id);
				} else {
					w.add("id", new String[] { "asset", ae.value("@id"), "proute", proute }, id);
				}
				String name = ae.value("name");
				if (name != null) {
					w.add("name", ae.value("name"));
				}
				w.add("type", ae.value("type"));
				w.pop();
			}
		}
		
		// TODO:
		// distributed query
	}

}