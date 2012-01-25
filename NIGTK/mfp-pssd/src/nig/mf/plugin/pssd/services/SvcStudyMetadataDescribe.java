package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.Study;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcStudyMetadataDescribe extends PluginService {

	private Interface _defn;

	public SvcStudyMetadataDescribe() {
		_defn = new Interface();
		//
		Interface.Element me = new Interface.Element("pid",CiteableIdType.DEFAULT, "The identity of the parent ExMethod.", 1, 1);
		me.add(new Interface.Attribute("proute", CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.", 0));
		_defn.add(me);
		_defn.add(new Element("step", CiteableIdType.DEFAULT, "The citeable id of the step within the method. You may want to run om.pssd.ex-method.study.step.find service first to find the available steps.", 1, 1));
	}

	public String name() {
		return "om.pssd.study.metadata.describe";
	}

	public String description() {
		return "Describes the metadata that can be associated with a Study.  In a federation, fetches the meta-data from the server that manages the Project.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// Set ID and validate
		DistributedAsset dEID = new DistributedAsset(args.element("pid"));
		String eid = dEID.getCiteableID();
		String eproute = dEID.getServerRoute();
		//
		String step = args.value("step");

		// Domain metadata from om.pssd.type.metadata.describe.  We fetch this from the server
		// that manages the parent Project.  In theviewing context this can be primary or replica
		Boolean readOnly = true;
		DistributedAsset dProject = dEID.getParentProject(readOnly);
		if (dProject==null) {
			throw new Exception ("Cannot find Project parent of the given ExMethod");
		}
		//
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("type", Study.TYPE);
		XmlDoc.Element r1 = executor().execute(dProject.getServerRouteObject(), "om.pssd.type.metadata.describe", dm.root());
		Collection<XmlDoc.Element> mes = r1.elements("metadata");
		if (mes != null) {
			w.push("meta");
			for (XmlDoc.Element me : mes) {
				w.add(me);
			}
			w.pop();
		}

		
		// Method metadata from om.pssd.ex-method.step.describe.  This has to come from the ExMethod itself.
		// Now we are splitting the meta-data fetch over 2 servers. :-(
		dm = new XmlDocMaker("args");
		if (eproute!=null) {
			dm.add("id", new String[] {"proute", eproute}, eid);
		} else {
			dm.add("id", eid);
		}
		dm.add("step",step);
		XmlDoc.Element r2 = executor().execute("om.pssd.ex-method.step.describe", dm.root());
		Collection<XmlDoc.Element> metadatas = r2.elements("ex-method/step/study/metadata");
		if (metadatas != null) {
			w.push("method");
			for (XmlDoc.Element me : metadatas) {
				w.add(me);
			}
			w.pop();
		}
		
	}
}
