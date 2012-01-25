package nig.mf.plugin.pssd.services;

import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

public class SvcObjectIsReplica extends PluginService {
	private Interface _defn;

	public SvcObjectIsReplica() {
		_defn = new Interface();
		Interface.Element me = new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the pssd object.", 1, 1);
		me.add(new Interface.Attribute("proute", CiteableIdType.DEFAULT,
				"In a federation, must specify the route to the peer that manages this citable ID.  If not supplied, the object is assumed to be local.", 0));
		_defn.add(me);
	}

	public String name() {
		return "om.pssd.object.is.replica";
	}

	public String description() {
		return "Returns whether the object is a replica or not.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}
	
		
	
	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		
		// Get object ID
		DistributedAsset dID = new DistributedAsset(args.element("id"));
		w.add("replica", dID.isReplica());
	}
}
