package nig.mf.plugin.pssd.services;

import nig.mf.pssd.plugin.util.DistributedAsset;
import nig.mf.pssd.plugin.util.DistributedAssetUtil;
import nig.mf.pssd.plugin.util.DistributedQuery;
import nig.mf.pssd.plugin.util.DistributedQuery.ResultAssetType;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcObjectExists extends PluginService {
	

	private Interface _defn;
	

	public SvcObjectExists() {
		_defn = new Interface();
		//
		Interface.Element me = new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the PSSD object.", 1, 1);
		me.add(new Interface.Attribute("proute", CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.", 0));
		_defn.add(me);
		//
		me = new Interface.Element("asset-type",
				new EnumType(new String[] {"primary", "replica", "all"}), 
				"Specify type of asset to check exists. Defaults to all.", 0, 1);
		_defn.add(me);
		//
		_defn.add(new Interface.Element("pdist",IntegerType.DEFAULT,"Specifies the peer distance for a distributed query. Defaults to all servers in a federated session.  Set to 0 for local only or infinity for all peers (regardless of whether session is federated or not).",0,1));
		_defn.add(new Interface.Element("include-children", BooleanType.DEFAULT, "If true, will also check if the CID has any children that have assets. Defaults to false.", 0, 1));
		_defn.add(new Interface.Element("pssd-only", BooleanType.DEFAULT, "If true (default), will ensure that any found object is a PSSD object. Otherwise any object with thye given CID will be potentially found.", 0, 1));
	}

	public String name() {
		return "om.pssd.object.exists";
	}

	public String description() {
		return "Finds whether any assets (to which the user has access) are associated with the given CID.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}
	
	
	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
				
		DistributedAsset dID = new DistributedAsset(args.element("id"));
		String pdist = args.value("pdist");
		Boolean includeChildren = args.booleanValue("include-children", false);
		String assetType = args.stringValue("asset-type", "all");
		Boolean pssdOnly = args.booleanValue("pssd-only", true);
		
		// Do it.
		ResultAssetType tp = DistributedQuery.ResultAssetType.instantiate(assetType);
        DistributedAssetUtil.assetExists (executor(), dID.getServerRoute(), pdist, dID.getCiteableID(), tp, includeChildren, pssdOnly, w);
 	}
	


	
}
