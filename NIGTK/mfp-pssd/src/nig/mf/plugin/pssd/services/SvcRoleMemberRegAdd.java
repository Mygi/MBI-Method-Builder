package nig.mf.plugin.pssd.services;


import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.plugin.util.AssetRegistry;

public class SvcRoleMemberRegAdd extends PluginService {

	
	public static final String DOCTYPE = "pssd-role-member-registry";
	public static final String REGISTRY_ASSET_NAME = "pssd-role-member-registry";

	private Interface _defn;

	public SvcRoleMemberRegAdd() {
		_defn = new Interface();
		_defn.add(new Interface.Element("role", StringType.DEFAULT, "The role to register", 1, 1));

	}

	public String name() {
		return "om.pssd.role-member-registry.add";
	}

	public String description() {
		return "Registers a local role as being available for use as a role-member when creating local Projects.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {

		// Add the actor ID on
		XmlDoc.Element role = args.element("role");
		PSSDUtils.checkRoleExists(executor(), role.value(), true);
		addRoleID (executor(), role);

		// See if the singleton asset already exists; else create.
		String id = AssetRegistry.findAndCreateRegistry(executor(), REGISTRY_ASSET_NAME);
		
		// Add the new role and add it's actor ID as well
		boolean added = AssetRegistry.addItem(executor(), id, role, DOCTYPE);
		if (added) w.add("id", id);
	}
	
	public static void addRoleID (ServiceExecutor executor, XmlDoc.Element role) throws Throwable {
		
		String roleName = role.value();
		
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("name", roleName);
		dm.add("type", "role");
		dm.add("levels", 1);
		XmlDoc.Element r = executor.execute("actor.describe", dm.root());
		if (r==null) return;
		//
		role.add(new XmlDoc.Attribute("id", r.value("actor/@id")));
	}
}
