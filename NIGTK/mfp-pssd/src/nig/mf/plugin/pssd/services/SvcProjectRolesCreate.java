package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

import nig.mf.plugin.pssd.*;

public class SvcProjectRolesCreate extends PluginService {
	private Interface _defn;

	public SvcProjectRolesCreate() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT, "The citable ID of the project.", 1, 1));
	}

	public String name() {
		return "om.pssd.project.roles.create";
	}

	public String description() {
		return "Creates all of the roles locally for a project.  Normally this happens when a project is created via om.pssd.project.create. However, when importing projects from other servers via an archive, the roles need to be created separately.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {

		// Creator must have project creation role..
		ModelUser.checkHasRole(null, executor(), Role.projectCreatorRoleName());

		// Get id
		String id = args.value("id");

		// Just check the depth; we don't check it's a project as it may not exist
		if (!nig.mf.pssd.plugin.util.CiteableIdUtil.isProjectId(id)) {
			throw new Exception ("Given cid has the wrong depth for a Project");
		}
		
		// Create roles
		Project.createProjectRoles(executor(), id);		
	}

}
