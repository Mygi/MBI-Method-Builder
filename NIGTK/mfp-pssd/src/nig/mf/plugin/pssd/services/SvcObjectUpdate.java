package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.util.*;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

public class SvcObjectUpdate extends PluginService {
	private Interface _defn;

	public SvcObjectUpdate() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT,"The identity of the object.",1,1));
		_defn.add(new Interface.Element("name",StringType.DEFAULT, "The name of this object.", 1, 1));
		_defn.add(new Interface.Element("description",StringType.DEFAULT, "An arbitrary description for the object.", 0, 1));
	}

	public String name() {
		return "om.pssd.object.update";
	}

	public String description() {
		return "Updates the name and description for an object on the local server.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// Set distributed citeable ID for the local object. and validate
		DistributedAsset dID = new DistributedAsset(args.element("id"));
		
		// Check type
		String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dID);
		if (type==null) {
			throw new Exception("The asset associated with " + dID.toString() + " does not exist");
		}
		if (dID.isReplica()) {
			throw new Exception ("The supplied object is a replica and this service cannot modify it.");
		}
	
		// Update
		String name = args.value("name");
		String description = args.value("description");
		//
		XmlDocMaker dm = new XmlDocMaker("args");	
		dm.add("cid",dID.getCiteableID());		
		dm.push("meta",new String[] { "action", "replace" });
		PSSDUtils.setObjectMeta(dm, type, name, description);
		dm.pop();
		
		// Update locally
		executor().execute("asset.set",dm.root());
	}
	
}
