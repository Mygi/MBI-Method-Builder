package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;
import nig.mf.pssd.plugin.util.DistributedAsset;


public class SvcMethodDescribe extends PluginService {
	private Interface _defn;

	public SvcMethodDescribe() {
		_defn = new Interface();
		Interface.Element me = new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the Method. If not given all matching Methods are returned.", 0, 1);
		me.add(new Interface.Attribute("proute", CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.", 0));
		_defn.add(me);
		//_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT,"The identity of the method.",0,1));
		_defn.add(new Interface.Element("name",StringType.DEFAULT, "The name of the method (if the id not given)", 0, 1));
		_defn.add(new Interface.Element("expand",BooleanType.DEFAULT, "Should the method be fully expanded - including all references. Defaults to false.", 0, 1));
	}

	public String name() {
		return "om.pssd.method.describe";
	}

	public String description() {
		return "Describes an identified method, or named method.  In a federation, the query is distributed if the Method id is not supplied.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		
		XmlDoc.Element id = args.element("id");
		//
		String name = args.value("name");
		boolean expand = args.booleanValue("expand", false);
		
		XmlDoc.Element r;
		XmlDocMaker dm = new XmlDocMaker("args");
		if ( id != null ) {
			
			// We don't care whether it's primary or a replica 
			DistributedAsset dID = new DistributedAsset(id);
			dm.add("cid",dID.getCiteableID());
			dm.add("pdist",0);                 // Force local on whatever server it's executed		
			
			// Get the object
			r = executor().execute(dID.getServerRouteObject(), "asset.get",dm.root());
		} else if ( name != null ) {
			String query = "xpath(pssd-method/name)='" + name + "'";
			dm.add("where",query);
			dm.add("action","get-meta");
			
			// Distributed in federation
			r = executor().execute("asset.query",dm.root());
		} else { 
			String citeableRoot = nig.mf.pssd.plugin.util.CiteableIdUtil.citeableIDRoot(executor(), null,
					nig.mf.pssd.CiteableIdUtil.METHOD_ID_ROOT_NAME);
			String query = "cid in '" + citeableRoot + "'";  
			dm.add("where",query);
			dm.add("action","get-meta");
			
			// Distributed in a federation
			r = executor().execute("asset.query",dm.root());
		}
		
		SvcMethodFind.describe(executor(),w, r,expand);
	}
	
}
