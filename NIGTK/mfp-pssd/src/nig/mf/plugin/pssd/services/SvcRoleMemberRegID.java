package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.xml.*;
import nig.mf.plugin.util.AssetRegistry;


public class SvcRoleMemberRegID extends PluginService {
	private Interface _defn;

	public SvcRoleMemberRegID() {
		_defn = new Interface();

	}

	public String name() {
		return "om.pssd.role-member-registry.id";
	}

	public String description() {
		return "Find the asset ID of the local registry asset that stores the local roles that are available for use as a role-member when creating local projects.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {

		// Find the Registry. Return if none yet.
		String id = AssetRegistry.findRegistry(executor(), SvcRoleMemberRegAdd.REGISTRY_ASSET_NAME);
		
		if (id==null) return;
		w.add("id", id);
		
	}
		
}
