package nig.mf.plugin.pssd.services;

import java.util.List;
import java.util.Vector;

import nig.mf.plugin.pssd.method.Step;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcExMethodStepList extends PluginService {

	private Interface _defn;

	public SvcExMethodStepList() {

		_defn = new Interface();
		Interface.Element me = new Interface.Element("id", CiteableIdType.DEFAULT,
				"The identity of the executing ExMethod.", 1, 1);
		me.add(new Interface.Attribute(
				"proute",
				CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.",
				0));
		_defn.add(me);
	}

	public String name() {

		return "om.pssd.ex-method.step.list";
	}

	public String description() {

		return "List the steps hierarchically.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String id = args.stringValue("id");
		String proute = args.stringValue("id/@proute");

		XmlDocMaker dm = new XmlDocMaker("args");
		if (proute == null) {
			dm.add("id", id);
		} else {
			dm.add("id", new String[] { "proute", proute }, id);
		}

		XmlDoc.Element r = executor().execute("om.pssd.object.describe", dm.root());
		List<XmlDoc.Element> ses = r.elements("object/method/step");
		if (ses == null) {
			return;
		}
		for (XmlDoc.Element se : ses) {
			listStep(se, null, w);
		}
	}

	private void listStep(XmlDoc.Element se, String parentStepPath, XmlWriter w) throws Throwable {

		String id = se.value("@id");
		String name = se.value("name");
		String path = parentStepPath == null ? id : parentStepPath + "." + id;
		Step.Type type = Step.Type.stepTypeFor(se);
		List<String> attributes = new Vector<String>();
		addAttribute("type", type.toString(), attributes);
		addAttribute("path", path, attributes);
		addAttribute("id", id, attributes);
		addAttribute("name", name, attributes);
		String[] as;
		switch (type) {
		case subject:
		case study:
			as = new String[attributes.size()];
			w.add("step", attributes.toArray(as));
			break;
		case method:
			String methodId = se.value("method/id");
			addAttribute("method-id", methodId, attributes);
			String methodName = se.value("method/name");
			addAttribute("method-name", methodName, attributes);
			as = new String[attributes.size()];
			w.push("step", attributes.toArray(as));
			List<XmlDoc.Element> cses = se.elements("method/step");
			if (cses != null) {
				for (XmlDoc.Element cse : cses) {
					listStep(cse, path, w);
				}
			}
			w.pop();
			break;
		case branch:
			String branchType = se.value("branch/@type");
			addAttribute("branch-type", branchType, attributes);
			as = new String[attributes.size()];
			w.push("step", attributes.toArray(as));
			List<XmlDoc.Element> mes = se.elements("branch/method");
			if (mes != null) {
				for (int i = 0; i < mes.size(); i++) {
					XmlDoc.Element me = mes.get(i);
					String mid = me.value("id");
					String mname = me.value("name");
					String mpath = path + "." + ((int) (i + 1));
					w.push("method", new String[] { "id", mid, "name", mname, "path", mpath });
					List<XmlDoc.Element> cses2 = me.elements("step");
					if (cses2 != null) {
						for (XmlDoc.Element cse : cses2) {
							listStep(cse, mpath, w);
						}
					}
					w.pop();
				}
			}
			w.pop();
			break;
		default:
			throw new Exception("Invalid method step: " + se.toString());
		}

	}

	private void addAttribute(String attrName, String attrValue, List<String> attributes) {

		if (attrValue != null) {
			attributes.add(attrName);
			attributes.add(attrValue);
		}
	}

}