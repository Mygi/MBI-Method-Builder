package nig.mf.plugin.pssd.services;

import nig.mf.pssd.plugin.util.DistributedAsset;
import nig.mf.pssd.plugin.util.DistributedQuery.ResultAssetType;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;


public class SvcObjectHasRemoteChildren extends PluginService {
	private Interface _defn;

	public SvcObjectHasRemoteChildren() {
		_defn = new Interface();
		Interface.Element me = new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the pssd object.", 1, 1);
		me.add(new Interface.Attribute("proute", CiteableIdType.DEFAULT,
				"In a federation, must specify the route to the peer that manages this citable ID.  If not supplied, the object is assumed to be local.", 0));
		_defn.add(me);
		_defn.add(new Interface.Element("asset-type", new EnumType(ResultAssetType.stringValues()), 
				"Specify type of child asset to find. Defaults to all.", 0, 1));
		_defn.add(new Interface.Element("pdist",IntegerType.DEFAULT,"Specifies the peer distance when looking for remote children. Defaults to infinity.  Set to 0 for local only.",0,1));
		_defn.add(new Interface.Element("ptag", StringType.DEFAULT, "Only query peers with this ptag. If none, query all peers.", 0, 1));
	}

	public String name() {
		return "om.pssd.object.has.remote.children";
	}

	public String description() {
		return "Returns whether the object has any children on remote peers or not.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}
	
		
	
	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		
		DistributedAsset dID = new DistributedAsset(args.element("id"));
		//
		String type = args.stringValue("asset-type", "all");
		ResultAssetType assetType = ResultAssetType.instantiate(type);
		String ptag = args.stringValue("ptag");
		String pdist = args.stringValue("pdist", "infinity");
		//
		w.add("remote-children", dID.hasRemoteChildren(assetType, ptag, pdist));
	}
}
