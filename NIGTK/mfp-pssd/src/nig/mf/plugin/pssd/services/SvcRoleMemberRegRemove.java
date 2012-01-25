package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.plugin.util.AssetRegistry;



public class SvcRoleMemberRegRemove extends PluginService {

	private Interface _defn;

	public SvcRoleMemberRegRemove() {
		_defn = new Interface();
		_defn.add(new Interface.Element("role", StringType.DEFAULT, "The role to remove", 1, 1));

	}

	public String name() {
		return "om.pssd.role-member-registry.remove";
	}

	public String description() {
		return "Removes a local role from being available for use as a role-member when creating local projects.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
	throws Throwable {

		// Add the role ID on
		XmlDoc.Element role = args.element("role");
		PSSDUtils.checkRoleExists(executor(), role.value(), true);
		SvcRoleMemberRegAdd.addRoleID(executor(), role);
		
		// Find the registry asset. If does not exist yet return silently.
		String id = AssetRegistry.findRegistry(executor(), SvcRoleMemberRegAdd.REGISTRY_ASSET_NAME);
		if (id==null) return;

		// Remove the role 
		boolean removed = AssetRegistry.removeItem(executor(), id, role, SvcRoleMemberRegAdd.DOCTYPE);
		if (removed) w.add("id", id);
	}
}
