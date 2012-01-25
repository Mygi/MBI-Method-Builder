package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

import nig.mf.plugin.pssd.*;


public class SvcModelTypesList extends PluginService {
	private Interface _defn;

	public SvcModelTypesList() {
		_defn = new Interface();
		Interface.Element me = new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the PSSD object. If not supplied, returns root level object types.", 0, 1);
		me.add(new Interface.Attribute("proute", CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.", 0));
		_defn.add(me);
	}

	public String name() {
		return "om.pssd.model.types.list";
	}

	public String description() {
		return "Returns the types of objects that are created as members of the specified object according to the PSSD object model.";
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
		
		String[] types = null;
		if ( id == null ) {
			types = Model.memberTypesFor(null);
		} else {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("cid",id);
			dm.add("pdist",0);                 // Force local on whatever server it's executed		

			XmlDoc.Element r = executor().execute(new ServerRoute(proute), "asset.get",dm.root());

			String type = r.value("asset/meta/pssd-object/type");
			if ( type != null ) {
				types = Model.memberTypesFor(type);
			}
		}
		
		if ( types != null ) {
			for ( int i=0; i < types.length; i++ ) {
				w.add("type",types[i]);
			}
		}
		
	}
}
