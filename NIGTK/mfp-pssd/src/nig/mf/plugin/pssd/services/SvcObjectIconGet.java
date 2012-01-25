package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

public class SvcObjectIconGet extends PluginService {
	private Interface _defn;

	public SvcObjectIconGet() {
		_defn = new Interface();
		Interface.Element me = new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the PSSD object.", 1, 1);
		me.add(new Interface.Attribute("proute", CiteableIdType.DEFAULT,
				"In a federation, must specify the route to the peer that manages this citable ID.  If not supplied, the object is assumed to be local.", 0));
		_defn.add(me);
		_defn.add(new Interface.Element("size",IntegerType.POSITIVE_ONE,"The size of the bounding box for the icon.",1,1));
	}

	public String name() {
		return "om.pssd.object.icon.get";
	}

	public String description() {
		return "Returns an icon for the specified object.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}
	
	public int minNumberOfOutputs() {
		return 1;
	}
	
	public int maxNumberOfOutputs() {
		return 1;
	}
	
	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		String id = args.value("id");
		String proute = args.value("id/@proute");
		int size = args.intValue("size");
		
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid",id);
		dm.add("size",size);
		
		executor().execute(new ServerRoute(proute), "asset.icon.get", dm.root(), null, out);
	}
}
