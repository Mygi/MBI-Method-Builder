package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

public class SvcObjectType extends PluginService {
	private Interface _defn;

	public SvcObjectType() {
		_defn = new Interface();
		Interface.Element me = new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the PSSD object.", 1, 1);
		me.add(new Interface.Attribute("proute", CiteableIdType.DEFAULT,
				"In a federation, must specify the route to the peer that manages this citable ID.  If not supplied, the object is assumed to be local.", 0));
		_defn.add(me);
	}

	public String name() {
		return "om.pssd.object.type";
	}

	public String description() {
		return "Returns the PSSD object type. If the object exists, but is not a PSSD object, returns 'unknown'.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}
	
	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		String id = args.value("id");
		String proute = args.value("id/@proute");
		
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid",id);
		dm.add("pdist",0);                 // Force local on whatever server it's executed		

		XmlDoc.Element r = executor().execute(new ServerRoute(proute), "asset.get",dm.root());
		String type = r.value("asset/meta/pssd-object/type");
		String proute2 = r.value("asset/@proute");         // Expanded
		if ( type == null ) {
			type = "unknown";
		}
				
		w.add("type",new String[] { "proute", proute2, "id", id },type);
	}
}
