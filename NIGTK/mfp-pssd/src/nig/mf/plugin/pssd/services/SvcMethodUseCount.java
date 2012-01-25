package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;
import nig.mf.plugin.pssd.method.*;
import nig.mf.pssd.plugin.util.DistributedAsset;

public class SvcMethodUseCount extends PluginService {
	private Interface _defn;

	public SvcMethodUseCount() {
		_defn = new Interface();
		Interface.Element me = new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the Method.", 1, 1);
		_defn.add(new Interface.Element("pdist",IntegerType.DEFAULT,"Specifies the peer distance for a distributed query. Defaults to infinity in a federated session.  Set to 0 for local only or infinity for all peers (regardless of whether session is federated or not).",0,1));
		_defn.add(me);
	}

	public String name() {
		return "om.pssd.method.use.count";
	}

	public String description() {
		return "Discovers the number of ExMethods and Methods that use this Method.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		String pdist = args.value("pdist");

		// Validate
		DistributedAsset dID = new DistributedAsset(args.element("id"));
		String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dID);
		if (type==null) {
			throw new Exception("The asset associated with " + dID.toString() + " does not exist");
		}
		if ( !type.equals(Method.TYPE) ) {
			throw new Exception("Object " + dID.getCiteableID() + " [type=" + type + "] is not a " + Method.TYPE);
		}		
		//
		w.add("ex-methods",Method.methodUseCount(executor(), true, dID, pdist, "infinity"));
		w.add("methods",Method.methodUseCount(executor(), false, dID, pdist, "infinity"));
	}
	
}
