package nig.mf.plugin.pssd.services;


import java.util.Collection;

import arc.mf.plugin.*;
import arc.xml.*;

public class SvcUserRoleRevoke extends PluginService {
	private Interface _defn;

	public SvcUserRoleRevoke() throws Throwable {
		_defn = new Interface();
		SvcUserRoleGrant.addInterface(_defn);      // Re-use interface
	}

	public String name() {
		return "om.pssd.user.role.revoke";
	}

	public String description() {
		return "Revokes a standard Mediaflux role or a generic PSSD role to the specified user(s). For project-specific roles, see services om.pssd.project.members.{add,remove}";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		// Get arguments and parse
		XmlDoc.Element authority = args.element("authority");
		String domain = args.value("domain");
		String user = args.value("user");
		Collection<String> roles = args.values("role");
		Collection<String> pssdRoles = args.values("pssd-role");
		if (roles==null && pssdRoles==null) {
			throw new Exception("You must supply a 'role' and/or 'pssd-role'.");
		}

		// Do the work
		Boolean grant = false;
		SvcUserRoleGrant.grantRevoke(executor(), authority, domain, user, roles, pssdRoles, w, grant);
	}
}
