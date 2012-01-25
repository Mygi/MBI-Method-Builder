package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;
import nig.mf.plugin.pssd.ModelUser;
import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.method.*;
import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.pssd.plugin.util.CiteableIdUtil;
import nig.mf.pssd.plugin.util.DistributedAsset;



public class SvcExMethodUpdate extends PluginService {
	private Interface _defn;

	public SvcExMethodUpdate() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT,"The identity of the ExMethod (managed by the local server).",1,1));
		_defn.add(new Interface.Element("name",StringType.DEFAULT, "The name of this object.", 1, 1));
		_defn.add(new Interface.Element("description",StringType.DEFAULT, "An arbitrary description for the object.", 0, 1));
		_defn.add(new Interface.Element("state",new EnumType(new String[] { "incomplete", "waiting", "complete", "abandoned" }),"The workflow status of the method.",1,1));
	}

	public String name() {
		return "om.pssd.ex-method.update";
	}

	public String description() {
		return "Updates the named, description and status of a locally managed ExMethod object.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// Set distributed citeable ID for the local ExMethod.  The object is local by definition
		DistributedAsset dID = new DistributedAsset(args.element("id"));
		
		// Check a few things...
		String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dID);
		if (type==null) {
			throw new Exception("The asset associated with " + dID.toString() + " does not exist");
		}
		if ( !type.equals(ExMethod.TYPE) ) {
			throw new Exception("Object " + dID.getCiteableID() + " [type=" + type + "] is not a " + ExMethod.TYPE);
		}
		if (dID.isReplica()) {
			throw new Exception ("The supplied ExMethod is a replica and this service cannot modify it.");
		}
		
		// What authorisation to we require to update the ExMethod ?  End users need to update state
		// We shoul at least check they are part of the project.
		String pID = CiteableIdUtil.getProjectCID(dID.getCiteableID());
		if (! (ModelUser.hasRole(null, executor(), Project.guestRoleName(pID)) ||
	                 ModelUser.hasRole(null, executor(), PSSDUtils.OBJECT_ADMIN_ROLE_NAME))) {
			throw new Exception ("User not authorised: requires '" + Project.guestRoleName(pID) +
					             "' or '" + PSSDUtils.OBJECT_ADMIN_ROLE_NAME + " role");
		}

		
		String name = args.value("name");
		String description = args.value("description");
		String state = args.value("state");
		
		XmlDocMaker dm = new XmlDocMaker("args");
		
		dm.add("cid", dID.getCiteableID());
		
		dm.push("meta",new String[] { "action", "replace" });
		PSSDUtils.setObjectMeta(dm, ExMethod.TYPE, name, description);
		dm.pop();
		
		// This metadata must merge, otherwise we will override 
		// the other essential information..
		dm.push("meta");
		dm.push("pssd-ex-method");
		dm.add("state",state);
		dm.pop();
		dm.pop();
		
		// Update the local object
		executor().execute("asset.set",dm.root());
	}
	
}
