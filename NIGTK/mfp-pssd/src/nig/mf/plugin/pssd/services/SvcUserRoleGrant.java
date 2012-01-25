package nig.mf.plugin.pssd.services;


import java.util.Collection;
import java.util.Vector;

import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.Role;
import nig.mf.plugin.pssd.user.Authority;
import nig.mf.plugin.pssd.user.UserCredential;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import arc.xml.XmlDoc.Element;

public class SvcUserRoleGrant extends PluginService {

	private Interface _defn;

	public SvcUserRoleGrant() throws Throwable {
		_defn = new Interface();
		addInterface(_defn);
	}

	public static void addInterface(Interface defn) throws Throwable {
		Interface.Element ie = new Interface.Element("authority",StringType.DEFAULT,"The authority of interest. Defaults to local.",0,1);
		ie.add(new Interface.Attribute("protocol", StringType.DEFAULT,
				"The protocol of the identity authority. If unspecified, defaults to federated user within the same type of repository.", 0));
		defn.add(ie);
		//
		defn.add(new Interface.Element("domain", StringType.DEFAULT,
				"The name of the domain that the users exist in.", 1, 1));
		defn.add(new Interface.Element("user", StringType.DEFAULT,
				"The name of the user.", 1, 1));
		//
		defn.add(new Interface.Element("role", StringType.DEFAULT, "A standard Mediaflux role.", 0, Integer.MAX_VALUE));
		defn.add(new Interface.Element("pssd-role",new EnumType(new String[] { Role.MODEL_USER_ROLE_NAME, Role.PROJECT_CREATOR_ROLE_NAME, 
				Role.SUBJECT_CREATOR_ROLE_NAME, nig.mf.plugin.pssd.dicom.Role.DICOM_INGEST}), "The generic PSSD role to grant.", 0, Integer.MAX_VALUE));		
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public Interface definition() {
		return _defn;
	}

	public String description() {
		return "Grants a standard Mediaflux role or a generic PSSD role to the specified user(s). For project-specific roles, see services om.pssd.project.members.{add,remove}";
	}

	public String name() {
		return "om.pssd.user.role.grant";
	}

	public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {

		// Get arguments and parse
		XmlDoc.Element authority = args.element("authority");
		String domain = args.value("domain");
		String user = args.value("user");
		Collection<String> roles = args.values("role");
		Collection<String> pssdRoles = args.values("pssd-role");
		if (roles==null && pssdRoles==null) {
			throw new Exception("You must supply a 'role' and/or 'pssd-role'");
		}

		// Do the work
		Boolean grant = true;
		grantRevoke(executor(), authority, domain, user, roles, pssdRoles, w, grant);
	}


	/**
	 * Grant or revoke the specified roles for all given user.
	 * 
	 * @param executor
	 * @param authority
	 * @param domain
	 * @param user
	 * @param roles  STandard MF role
	 * @param pssdRoles  Generic PSSD role
	 * @param w
	 * @param grant
	 * @throws Throwable
	 */
	public static void grantRevoke(ServiceExecutor executor, XmlDoc.Element authority, String domain, String user, Collection<String> roles,
			Collection<String> pssdRoles, XmlWriter w, Boolean grant) throws Throwable {


		// Generate list of users if user not given
		Collection<String> users = null;
		if (user!=null) {
			if (!userExists(executor, authority, domain, user)) {
				throw new Exception("User " + user + " does not exist in the given domain and authority");
			}
			users = new Vector<String>();
			users.add(user);
		} else {
			XmlDocMaker dm = new XmlDocMaker("args");
			if (authority!=null) dm.add(authority);
			dm.add("domain", domain);
			XmlDoc.Element r = executor.execute("user.list", dm.root());
			if (r==null) return;

			users = r.values("user");
			if (users==null) return;
		}

		// Iterate over users and grant role(s)
		UserCredential userCred = new UserCredential(Authority.instantiate(authority), domain, user);
		for (String u : users) {
			userCred.setUser(u);
			//
			// Mediaflux roles
			if (roles!=null) grantRevokeRoles (executor, userCred, roles, grant);

			// Generic PSSD roles
			if (pssdRoles!=null) grantRevokePSSDRoles (executor, userCred, pssdRoles, grant);

			// Output (should be enhanced)
			String a = null;
			String p = null;
			if (authority!=null) {
				a = authority.value();
				p = authority.value("@protocol");
			}
			w.add("user", new String[] {"authority",a, "protocol", p, "domain", domain}, u);
		}
	}





	// Private functions

	private  static boolean userExists(ServiceExecutor executor, XmlDoc.Element authority,
			String domain, String user) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		if (authority!=null) dm.add(authority);
		dm.add("domain", domain);
		dm.add("user", user);
		
		// User.exists won't find LDAP users
		XmlDoc.Element r = executor.execute("authentication.user.exists", dm.root());
		return r.booleanValue("exists");
	}

	/**
	 * 
	 * @param executor
	 * @param authority - does not include the protocol as these services are only intended for Mediaflux protocol
	 * @param domain
	 * @param user
	 * @param roles
	 * @param grant
	 * @throws Throwable
	 */
	private static void grantRevokeRoles (ServiceExecutor executor, UserCredential userCred, 
			Collection<String> roles, Boolean grant) throws Throwable {

		boolean create = false;
		for (String role : roles) {
			Project.grantRevoke(executor, userCred, role, create, grant);
		}
	}


	private static void grantRevokePSSDRoles (ServiceExecutor executor,  UserCredential userCred, 
			Collection<String> pssdRoles, Boolean grant) throws Throwable {
		
		boolean create = false;
		for (String role : pssdRoles) {
			if ( role.equalsIgnoreCase(Role.MODEL_USER_ROLE_NAME) ) {
				role = Role.modelUserRoleName();
			} else if ( role.equalsIgnoreCase(Role.PROJECT_CREATOR_ROLE_NAME) ) {
				role = Role.projectCreatorRoleName();
			} else if ( role.equalsIgnoreCase(Role.SUBJECT_CREATOR_ROLE_NAME) ) {
				role = Role.subjectCreatorRoleName();
			} else if (role.equalsIgnoreCase(nig.mf.plugin.pssd.dicom.Role.DICOM_INGEST)) {
				role = nig.mf.plugin.pssd.dicom.Role.dicomIngestRoleName();
			}
			//
			Project.grantRevoke(executor, userCred, role, create, grant);
		}
	}
}
