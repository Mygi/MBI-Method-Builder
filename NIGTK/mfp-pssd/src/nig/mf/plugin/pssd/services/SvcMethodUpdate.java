package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.method.Method;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.*;
import arc.mf.plugin.PluginService.Inputs;
import arc.mf.plugin.PluginService.Outputs;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

public class SvcMethodUpdate extends PluginService {
	private Interface _defn;

	public SvcMethodUpdate() throws Throwable {
		_defn = new Interface();
		_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT,"The identity of the method. If not given, a new Method is created",0,1));
		SvcMethodCreate.addInterface(_defn);
		_defn.add(new Interface.Element("replace", BooleanType.DEFAULT, "Replace (default) or merge the meta-data", 0, 1));
	}

	public String name() {
		return "om.pssd.method.update";
	}

	public String description() {
		return "Updates a research method. You can replace (overwrite) or merge the meta-data in this process.  Limitations:  you can add new steps (to the end of the list) but not edit existing ones";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		updateMethod (executor(), args, in, out, w);
	}

	public static void updateMethod (ServiceExecutor executor, XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// Set distributed citeable ID for the Method.  It is local by definition
		String id = null;
		DistributedAsset dID = new DistributedAsset(args.element("id"));
		if (dID.getCiteableID()!=null) {
			id = dID.getCiteableID();


			// Check a few things...
			String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor, dID);
			if (type==null) {
				throw new Exception("The asset associated with " + dID.toString() + " does not exist");
			}
			if (!type.equals(Method.TYPE)) {
				throw new Exception("Object " + dID.getCiteableID() + " [type=" + type + "] is not a " + Method.TYPE);
			}
			if (dID.isReplica()) {
				throw new Exception ("The supplied Method is a replica and this service cannot modify it.");
			}
		}

		// Update or create
		Boolean replace = args.booleanValue("replace", true);
		if (id==null) replace = null;

		SvcMethodCreate.execute(executor, id, args, w, replace);
	}
}
