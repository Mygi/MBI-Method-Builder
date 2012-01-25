package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

import nig.mf.plugin.pssd.*;
import nig.mf.plugin.pssd.util.*;
import nig.mf.pssd.plugin.util.DistributedAsset;

public class SvcSubjectUpdate extends PluginService {
	private Interface _defn;

	public SvcSubjectUpdate() {
		_defn = new Interface();	
		_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the Subject (managed by the local server). ", 1, 1));
		
		SvcSubjectCreate.addInterfaceDefn(_defn);
		_defn.add(new Interface.Element("action", new EnumType(new String[] {"add", "merge", "replace"}), "Action to apply to meta-data (defaults to replace).", 0, 1));
	}

	public String name() {
		return "om.pssd.subject.update";
	}

	public String description() {
		return "Updates the meta-data attached to a  local Subject object.   ";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		
		
		// Set distributed citeable ID for the local SUbject
		DistributedAsset dSID = new DistributedAsset(args.element("id"));
		
		// Check a few things...
		String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dSID);
		if (type==null) {
			throw new Exception("The asset associated with " + dSID.toString() + " does not exist");
		}
		if ( !type.equals(Subject.TYPE) ) {
			throw new Exception("Object " + dSID.getCiteableID() + " [type=" + type + "] is not a " + Subject.TYPE);
		}
		if (dSID.isReplica()) {
			throw new Exception ("The supplied Subject is a replica and this service cannot modify it.");
		}
 		
		// Find primary parent Project 
		Boolean readOnly = false;
		DistributedAsset dPID = dSID.getParentProject(readOnly);
		if (dPID==null) {
			throw new Exception ("Cannot find primary Project parent of the given Subject");
		}

		// Modifier must have project subject administrator role locally
		Boolean isAdmin = (ModelUser.hasRole(null, executor(), Project.subjectAdministratorRoleName(dPID.getCiteableID())) ||
				           ModelUser.hasRole(null, executor(), PSSDUtils.OBJECT_ADMIN_ROLE_NAME));
		if (!isAdmin) {
			throw new Exception ("User not authorised: requires '" + Project.subjectAdministratorRoleName(dPID.getCiteableID()) +
					             "' or '" + PSSDUtils.OBJECT_ADMIN_ROLE_NAME + " role");
		}
		
		boolean fillIn = false;
		boolean encryptOnCreate = false;
		String action = args.stringValue("action", "replace");
		SvcSubjectCreate.createOrUpdateSubjectAsset(executor(), args, dSID.getCiteableID(), dPID, -1, fillIn, 
				encryptOnCreate, action);
	}
	
}
