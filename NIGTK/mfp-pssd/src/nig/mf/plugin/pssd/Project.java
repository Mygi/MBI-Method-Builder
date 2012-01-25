package nig.mf.plugin.pssd;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import nig.mf.plugin.pssd.user.UserCredential;
import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

public class Project {
	public static final String TYPE = "project";
	public static final String MODEL = "om.pssd.project";

	public static final String ADMINISTRATOR_ROLE_NAME = "project-administrator";
	public static final String SUBJECT_ADMINISTRATOR_ROLE_NAME = "subject-administrator";
	public static final String MEMBER_ROLE_NAME = "member";
	public static final String GUEST_ROLE_NAME = "guest";
	//
	public static final String SPECIFIC_ADMINISTRATOR_ROLE_NAME_ROOT = "pssd.project.admin";
	public static final String SPECIFIC_SUBJECT_ADMINISTRATOR_ROLE_NAME_ROOT = "pssd.project.subject.admin";
	public static final String SPECIFIC_MEMBER_ROLE_NAME_ROOT = "pssd.project.member";
	public static final String SPECIFIC_GUEST_ROLE_NAME_ROOT = "pssd.project.guest";

	public static final int DEPTH = 0;

	// Define the types of ethical consent for use and reuse of human data
	// These are used to specify 1) the overall project data-use
	// characteristics,
	// 2) Subject-specific data-use characteristics (e.g. to over-ride 1)
	// and 3) How a user wants to use project data
	public static final String CONSENT_SPECIFIC_ROLE_NAME = "specific"; // Original
																		// project
																		// concept
																		// use
	public static final String CONSENT_EXTENDED_ROLE_NAME = "extended"; // Use
																		// for
																		// original
																		// and
																		// related
																		// work
	public static final String CONSENT_UNSPECIFIED_ROLE_NAME = "unspecified"; // Use
																				// for
																				// any
																				// research

	// SPecify type of project member of interest
	public static final int MEMBER_TYPE_USER = 0; // Project member is a user
	public static final int MEMBER_TYPE_ROLE = 1; // Project member is a role
	public static final int MEMBER_TYPE_ALL = 2; // All member types

	/**
	 * Trivial container class to hold a generic project-specific role and the
	 * cid which the specific role has been split into. specific project role =
	 * generic project role + cid
	 * 
	 * @author nebk
	 * 
	 */
	public static class ProjectCIDAndRole {
		private String _id;
		private String _role;

		/**
		 * 
		 * @param id
		 *            The citable id of the project role
		 * @param genericProjectRole
		 *            The generic project role (project-administrator,
		 *            subject-administrator, member, guest)
		 */
		public ProjectCIDAndRole(String id, String genericProjectRole) {

			_id = id;
			_role = genericProjectRole;
		}

		public String projectId() {

			return _id;
		}

		public String role() {

			return _role;
		}
	}

	/**
	 * Returns true if the distributed CID is for a Project object on the local
	 * server
	 */
	public static boolean isObjectProject(ServiceExecutor executor, DistributedAsset dCID) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", dCID.getCiteableID());
		XmlDoc.Element r = executor.execute(dCID.getServerRouteObject(), "om.pssd.object.type", dm.root());
		String type = r.value("type");
		if (type.equals(TYPE))
			return true;
		return false;
	}

	/**
	 * Create all of the roles we need for this Project
	 * 
	 * @param cid
	 *            CID of Project
	 * @throws Throwable
	 */
	static public void createProjectRoles(ServiceExecutor executor, String cid) throws Throwable {

		// Create the 4 team-member roles
		PSSDUtils.createRole(executor, Project.projectAdministratorRoleName(cid));
		PSSDUtils.createRole(executor, Project.subjectAdministratorRoleName(cid));
		PSSDUtils.createRole(executor, Project.memberRoleName(cid));
		PSSDUtils.createRole(executor, Project.guestRoleName(cid));

		// Grant the sub-roles to the project administrator.
		PSSDUtils.grantRoleToRole(executor, Project.projectAdministratorRoleName(cid),
				Project.subjectAdministratorRoleName(cid));
		PSSDUtils.grantRoleToRole(executor, Project.projectAdministratorRoleName(cid), Project.memberRoleName(cid));
		PSSDUtils.grantRoleToRole(executor, Project.projectAdministratorRoleName(cid), Project.guestRoleName(cid));

		// Grant the sub-roles to the subject-administrator.
		PSSDUtils.grantRoleToRole(executor, Project.subjectAdministratorRoleName(cid), Project.memberRoleName(cid));
		PSSDUtils.grantRoleToRole(executor, Project.subjectAdministratorRoleName(cid), Project.guestRoleName(cid));

		// Grant the ability to see R-Subjects (from other projects)
		// for searching to administrators.
		//
		// TODO - only allow for methods that use R-Subjects...
		//
		PSSDUtils.grantRoleToRole(executor, Project.subjectAdministratorRoleName(cid),
				PSSDUtils.SUBJECT_GUEST_ROLE_NAME);

		// Grant the sub-roles to the member.
		PSSDUtils.grantRoleToRole(executor, Project.memberRoleName(cid), Project.guestRoleName(cid));

		// Create the project-specific roles that specify whether subject data
		// can be used for 'specific', 'extended' or 'unspecified' use
		// We don't actually need the 'specific' role (it's implicit) but
		// create it and assign it for completeness
		PSSDUtils.createRole(executor, Project.specificUseRoleName(cid));
		PSSDUtils.createRole(executor, Project.extendedUseRoleName(cid));
		PSSDUtils.createRole(executor, Project.unspecifiedUseRoleName(cid));
	}

	/**
	 * Destroy all roles associated with the specified Project on the local
	 * server. You should only do this if the Project assets have been destroyed
	 * but this is not checked here. There is no check that the CID is
	 * associated with a Project object (as the assets should already be
	 * destroyed). It is the callers responsibility to make these checks
	 * 
	 * @param executor
	 * @param cid
	 *            Project CID
	 * @throws Throwable
	 */

	public static void destroyRoles(ServiceExecutor executor, String cid) throws Throwable {

		// Team roles
		PSSDUtils.destroyRole(executor, guestRoleName(cid));
		PSSDUtils.destroyRole(executor, memberRoleName(cid));
		PSSDUtils.destroyRole(executor, subjectAdministratorRoleName(cid));
		PSSDUtils.destroyRole(executor, projectAdministratorRoleName(cid));

		// Data use roles
		PSSDUtils.destroyRole(executor, specificUseRoleName(cid));
		PSSDUtils.destroyRole(executor, extendedUseRoleName(cid));
		PSSDUtils.destroyRole(executor, unspecifiedUseRoleName(cid));

	}

	// Role names which will be granted to users specifying
	// their project team role
	public static String projectAdministratorRoleName(String cid) {

		return SPECIFIC_ADMINISTRATOR_ROLE_NAME_ROOT + "." + cid;
	}

	public static String subjectAdministratorRoleName(String cid) {

		return SPECIFIC_SUBJECT_ADMINISTRATOR_ROLE_NAME_ROOT + "." + cid;
	}

	public static String memberRoleName(String cid) {

		return SPECIFIC_MEMBER_ROLE_NAME_ROOT + "." + cid;
	}

	public static String guestRoleName(String cid) {

		return SPECIFIC_GUEST_ROLE_NAME_ROOT + "." + cid;
	}

	/**
	 * Converts generic name and project CID into project-specific role name
	 * 
	 * @param projectRole
	 *            Should be one of the generic project or data-use roles
	 * @param id
	 * @return
	 * @throws Throwable
	 */
	public static String setSpecificRoleName(String projectRole, String id) throws Throwable {

		if (projectRole.equalsIgnoreCase(ADMINISTRATOR_ROLE_NAME)) {
			return projectAdministratorRoleName(id);
		} else if (projectRole.equalsIgnoreCase(SUBJECT_ADMINISTRATOR_ROLE_NAME)) {
			return subjectAdministratorRoleName(id);
		} else if (projectRole.equalsIgnoreCase(MEMBER_ROLE_NAME)) {
			return memberRoleName(id);
		} else if (projectRole.equalsIgnoreCase(GUEST_ROLE_NAME)) {
			return guestRoleName(id);
		} else if (projectRole.equalsIgnoreCase(CONSENT_SPECIFIC_ROLE_NAME)) {
			return specificUseRoleName(id);
		} else if (projectRole.equalsIgnoreCase(CONSENT_EXTENDED_ROLE_NAME)) {
			return extendedUseRoleName(id);
		} else if (projectRole.equalsIgnoreCase(CONSENT_UNSPECIFIED_ROLE_NAME)) {
			return unspecifiedUseRoleName(id);
		} else {
			throw new Exception(projectRole + " is not a known Project role");
		}
	}

	// These roles specify how users are allowed to use subject data
	// in a project. A project may be set for 'extended' use, but
	// a specific subject may over-ride that with 'specific'. These roles
	// are relevant to this over-ride process only.
	/**
	 * User wants to use subject data only for the original intended use
	 */
	public static String specificUseRoleName(String cid) {

		return "pssd.project.subject.use.specific." + cid;
	}

	/**
	 * User wants to use subject data for the original intended use and related
	 * projects
	 * 
	 * @param cid
	 * @return
	 */
	public static String extendedUseRoleName(String cid) {

		return "pssd.project.subject.use.extended." + cid;
	}

	/**
	 * User wants to use subject data for any research project
	 * 
	 * @param cid
	 * @return
	 */
	public static String unspecifiedUseRoleName(String cid) {

		return "pssd.project.subject.use.unspecified." + cid;
	}

	/**
	 * Get Project data-use specification from Project asset.
	 * 
	 * @param executor
	 * @param dPID
	 *            Distributed citeable id for the Project
	 * @return data-use role; may be null
	 * @throws Throwable
	 */
	public static String getProjectDataUse(ServiceExecutor executor, DistributedAsset dPID) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", dPID.getCiteableID());
		XmlDoc.Element r = executor.execute(dPID.getServerRouteObject(), "om.pssd.object.describe", dm.root());
		return r.value("object/data-use"); // May be null
	}

	/**
	 * Is this role an admin (Project or subject) role ?
	 * 
	 * @param teamRole
	 * @return
	 */
	public static boolean isAdmin(String teamRole) {

		return teamRole.equals(ADMINISTRATOR_ROLE_NAME) || teamRole.equals(SUBJECT_ADMINISTRATOR_ROLE_NAME);
	}

	/**
	 * Get the name of the specified project
	 * 
	 * @param executor
	 * @param id
	 * @return
	 * @throws Throwable
	 */
	public static String getProjectName(ServiceExecutor executor, String id) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		XmlDoc.Element r = executor.execute("om.pssd.object.describe", dm.root());
		return r.value("object/name");
	}

	/**
	 * Grant specific project role to user-members. Handles member and data-use
	 * roles
	 * 
	 * @param executor
	 * @param id
	 *            CID of project
	 * @param authority
	 * @param domain
	 * @param user
	 *            The project-team user-member
	 * @param role
	 *            The generic Project-specific role name drawn from the list of
	 *            member (project-administrator, etc) and data-use (specific,
	 *            etc) roles
	 * @param create
	 *            Create role if does not exist if granting
	 * @throws Throwable
	 */
	public static void grantProjectRole(ServiceExecutor executor, String id, UserCredential userCred, String role,
			boolean create) throws Throwable {

		grantRevokeProjectRole(executor, id, userCred, role, create, true);
	}

	/**
	 * Grant specific project role to role-members. Handles member and data-use
	 * roles.
	 * 
	 * @param executor
	 * @param id
	 *            CID of project
	 * @param roleMember
	 *            The project-team role-member
	 * @param role
	 *            The generic Project role name drawn from the list of member
	 *            and data-use roles
	 * @param create
	 *            Create role if does not exist
	 * @throws Throwable
	 */
	public static void grantProjectRole(ServiceExecutor executor, String id, String roleMember, String role,
			boolean create) throws Throwable {

		grantRevokeProjectRole(executor, id, roleMember, role, create, true);
	}

	/**
	 * Revoke specific project role to user-members. Handles member and data-use
	 * roles
	 * 
	 * @param executor
	 * @param id
	 *            CID of project
	 * @param authority
	 * @param domain
	 * @param user
	 *            The project-team user-member
	 * @param role
	 *            The generic Project-specific role name drawn from the list of
	 *            member (project-administrator, etc) and data-use (specific,
	 *            etc) roles
	 * @throws Throwable
	 */
	public static void revokeProjectRole(ServiceExecutor executor, String id, UserCredential userCred, String role)
			throws Throwable {

		boolean grant = false;
		grantRevokeProjectRole(executor, id, userCred, role, false, grant);
	}

	/**
	 * Revoke specific project role to role-members. Handles member and data-use
	 * roles.
	 * 
	 * @param executor
	 * @param id
	 *            CID of project
	 * @param memberRole
	 *            The project-team role-member
	 * @param role
	 *            The generic Project role name drawn from the list of member
	 *            and data-use roles
	 * @param create
	 *            Create role if does not exist
	 * @throws Throwable
	 */
	public static void revokeProjectRole(ServiceExecutor executor, String id, String memberRole, String role)
			throws Throwable {

		boolean grant = false;
		grantRevokeProjectRole(executor, id, memberRole, role, false, grant);
	}

	/**
	 * Returns the Project members; direct users and (optionally de-referenced)
	 * role members for a given Project which hold the given Project role.
	 * 
	 * @param proute
	 *            Server route to cid
	 * @param cid
	 *            the project CID
	 * @param memberRole
	 *            the desired team role. Must be one of null (all members),
	 *            ADMINISTRATOR_ROLE_NAME, SUBJECT_ADMINISTRATOR_ROLE_NAME,
	 *            MEMBER_ROLE_NAME, and GUEST_ROLE_NAME
	 * @param explicit
	 *            if true, the member is added if they explicitly hold the given
	 *            role. If false, the member is added if it holds the role
	 *            implicitly by hierarchy. E.g. if
	 *            SUBJECT_ADMINISTRATOR_ROLE_NAME is specified but the member is
	 *            a project administrator, then through the hierarchical nature
	 *            of the Project roles they hold SUBJECT_ADMINISTRATOR_ROLE_NAME
	 * @param memberType
	 *            0 means user members, 1 means role members and 2 means all
	 *            member types
	 * @param deReference
	 *            If true, de-reference role members to actual users
	 * @param showDetail
	 *            If true adds extra detail on user members (email/names)
	 * @return : Collection<XmlDoc.Element> The structure is the same as that
	 *         from the "member" element of om.pssd.object.describe on a Project
	 *         This includes their actual project role.
	 * @throws Throwable
	 */
	public static Collection<XmlDoc.Element> membersWithProjectRole(ServiceExecutor executor, String proute,
			String cid, String memberRole, boolean explicit, int memberType, boolean deReference, Boolean showDetail)
			throws Throwable {

		if (cid == null)
			return null;
		Vector<XmlDoc.Element> res = new Vector<XmlDoc.Element>();

		// Get the user members, their actual project roles and data-use role
		if (memberType == MEMBER_TYPE_USER || memberType == MEMBER_TYPE_ALL) {
			Vector<XmlDoc.Element> t = userMembers(executor, proute, cid, memberRole, explicit, showDetail);
			if (t != null && t.size() > 0)
				res.addAll(t);
		}

		// Get the role-members, their actual project roles and data-use roles.
		// De-reference to users if requested
		if (memberType == MEMBER_TYPE_ROLE || memberType == MEMBER_TYPE_ALL) {

			// Get the role members
			Collection<XmlDoc.Element> t = roleMembers(executor, proute, cid, memberRole, explicit);

			// Iterate
			if (t != null && t.size() > 0) {
				if (deReference) {
					for (XmlDoc.Element el : t) {
						String roleMember = el.value("@member"); // The role
																	// member
																	// (i.e. the
																	// role that
																	// is a
																	// project
																	// member)
						String projectRole = el.value("@role"); // The project
																// role
																// (admin,member,guest)
																// that this
																// role holds

						// Add the new users to the Collection and dereference
						// to users if desired (e.g. to send emails)
						Collection<XmlDoc.Element> tt = dereferenceUsersFromRole(executor, roleMember, projectRole);
						if (tt != null && tt.size() > 0)
							res.addAll(tt);
					}
				} else {
					res.addAll(t);
				}
			}
		}

		// We now have a list of user and/or role members. Compact this into a
		// unique list
		// TODO: when we de-reference role-members, we may end up with users
		// multiple times
		// for the same project with different roles. Compact this into just the
		// top-level roles.
		return makeUnique(res);
	}

	/**
	 * Dereference a project's role member into a collection of users that hold
	 * that role
	 * 
	 * @param executor
	 * @param roleMember
	 *            The role member element to dereference
	 * @param projectRole
	 *            the project role that this role member holds. Can be null if
	 *            you are not interested in it.
	 * @return : Collection<XmlDoc.Element> The structure is the same as that
	 *         from the "member" element of om.pssd.object.describe on a Project
	 *         Returns null if no dereferenced users found
	 * @throws Throwable
	 */
	public static Vector<XmlDoc.Element> dereferenceUsersFromRole(ServiceExecutor executor, String roleMember,
			String projectRole) throws Throwable {

		// Results in here
		Vector<XmlDoc.Element> res = new Vector<XmlDoc.Element>();

		// Find the users that have this role-member role
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("role", new String[] { "type", "role" }, roleMember);
		XmlDoc.Element r = executor.execute("user.describe", dm.root());

		Collection<XmlDoc.Element> users = r.elements("user");
		if (users == null)
			return null;

		// Iterate over these actors.
		for (XmlDoc.Element user : users) {
			// Describe user in the same structure as
			// om.pssd.project.members.list lists user members
			XmlDocMaker el = new XmlDocMaker("member", new String[] { "id", user.value("@id"), "authority",
					user.value("@authority"), "protocol", user.value("@protocol"), "domain", user.value("@domain"),
					"user", user.value("@user"), "role", projectRole });
			res.add(el.root());
		}
		//
		if (res.size() == 0) {
			return null;
		} else {
			return res;
		}
	}

	/**
	 * Grant or revoke a user's role
	 * 
	 * @param executor
	 * @param authority
	 *            authority with attribute protocol
	 * @param domain
	 * @param user
	 * @param role
	 * @param create
	 * @param grant
	 * @throws Throwable
	 */
	public static void grantRevoke(ServiceExecutor executor, UserCredential userCred, String role, boolean create,
			Boolean grant) throws Throwable {

		if (role == null)
			return;

		// Create role if desired. Ignores if exists
		if (create && grant)
			PSSDUtils.createRole(executor, role);

		// Grant the role. WIll fail if does not exist.
		XmlDocMaker dm = new XmlDocMaker("args");
		if (userCred.hasAuthority()) {
			dm.add(userCred.authority().toXmlElement());
		}
		dm.add("domain", userCred.domain());
		dm.add("user", userCred.user());
		dm.add("role", new String[] { "type", "role" }, role);

		// The user. services are intended for users in the local repository
		// These users are either in the local authority, in proxy
		// representations
		// of external authorities, or in remote authorities such as LDAP
		if (grant) {
			executor.execute("user.grant", dm.root());
		} else {
			executor.execute("user.revoke", dm.root());
		}
	}

	public static void grantRevoke(ServiceExecutor executor, String roleMember, String role, boolean create,
			Boolean grant) throws Throwable {

		// Create role if desired. Ignores if exists
		if (create && grant)
			PSSDUtils.createRole(executor, role);

		// Grant the role. WIll fail if does not exist.
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("name", roleMember);
		dm.add("type", "role");
		dm.add("role", new String[] { "type", "role" }, role);

		if (grant) {
			executor.execute("actor.grant", dm.root());
		} else {
			executor.execute("actor.revoke", dm.root());
		}
	}

	// Private functions
	private static Collection<XmlDoc.Element> makeUnique(Collection<XmlDoc.Element> in) throws Throwable {

		if (in == null)
			return null;
		if (in.size() == 0)
			return in;
		//
		Vector<XmlDoc.Element> out = new Vector<XmlDoc.Element>();
		HashMap<String, Boolean> have = new HashMap<String, Boolean>();
		for (XmlDoc.Element el : in) {
			String t = el.toString(); // Use the whole element as the key.
			if (!have.containsKey(t)) {
				out.add(el);
				have.put(t, true);
			}
		}
		//
		return out;
	}

	/**
	 * Grant/revoke specific project role to user-members. Handles member and
	 * data-use roles
	 * 
	 * @param executor
	 * @param id
	 *            CID of project
	 * @param authority
	 * @param domain
	 * @param user
	 *            The project-team user-member
	 * @param role
	 *            The generic Project-specific role name drawn from the list of
	 *            member (project-administrator, etc) and data-use (specific,
	 *            etc) roles
	 * @param create
	 *            Create role if does not exist if granting
	 * @throws Throwable
	 */
	private static void grantRevokeProjectRole(ServiceExecutor executor, String id, UserCredential userCred,
			String role, boolean create, boolean grant) throws Throwable {

		if (role == null)
			return;

		// Convert generic role name to project-specific role
		String role2 = Project.setSpecificRoleName(role, id);

		// Create role if desired and grant the role
		grantRevoke(executor, userCred, role2, create, grant);
	}

	/**
	 * Grant/revoke specific project role to role-members. Handles member and
	 * data-use roles. This function is used by other classes so it's public
	 * 
	 * @param executor
	 * @param id
	 *            CID of project
	 * @param roleMember
	 *            The project-team role-member
	 * @param role
	 *            The generic Project role name drawn from the list of member
	 *            and data-use roles
	 * @param create
	 *            Create role if does not exist
	 * @throws Throwable
	 */
	private static void grantRevokeProjectRole(ServiceExecutor executor, String id, String roleMember, String role,
			boolean create, Boolean grant) throws Throwable {

		if (role == null)
			return;

		// Convert generic role name to project-specific role
		String role2 = Project.setSpecificRoleName(role, id);

		// Create role if desired. Ignores if exists
		if (create && grant)
			PSSDUtils.createRole(executor, role2);

		// Create role if desired and grant the role
		grantRevoke(executor, roleMember, role2, create, grant);
	}

	/**
	 * Get user members who hold the given project role
	 * 
	 * @param executor
	 * @param proute
	 *            to the server of interest
	 * @param cid
	 * @param projectRole
	 * @param explicit
	 * @return null if none
	 * @throws Throwable
	 */
	private static Vector<XmlDoc.Element> userMembers(ServiceExecutor executor, String proute, String cid,
			String genericProjectRole, boolean explicit, boolean showDetail) throws Throwable {

		if (cid == null)
			return null;
		ServerRoute route = new ServerRoute(proute);

		// Get the project-specific role from the generic
		String specificProjectRole = setSpecificRoleName(genericProjectRole, cid);

		// Output record
		Vector<XmlDoc.Element> membersOut = new Vector<XmlDoc.Element>();

		// Find out who has this role (an implicit search)
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("role", new String[] { "type", "role" }, specificProjectRole);
		dm.add("permissions", "true");
		XmlDoc.Element r = executor.execute(route, "user.describe", dm.root());
		if (r == null)
			return null;
		//
		Collection<XmlDoc.Element> users = r.elements("user");
		if (users == null)
			return null;

		for (XmlDoc.Element user : users) {

			// Find the user's direct project role, if any
			// We use the local implementation rather than that of ProjectMember
			// for performance
			// reasons (saves another call to user.describe)
			// String actualGenericProjectRole = pM.directProjectRole (executor,
			// route, cid);
			String actualGenericProjectRole = directUserProjectRole(user, cid, false);

			boolean keep = false;
			XmlDoc.Element el2 = null;
			UserCredential cred = null;
			if (actualGenericProjectRole != null) {

				// Generate user credential
				cred = new UserCredential(user.value("@authority"), user.value("@protocol"), user.value("@domain"),
						user.value("@user"));

				// Prepare output
				el2 = new XmlDoc.Element("member");
				addUser(el2, user.value("@id"), cred);
				if (explicit) {
					if (actualGenericProjectRole.equals(genericProjectRole)) {
						el2.add(new XmlDoc.Attribute("role", genericProjectRole));
						keep = true;
					}
				} else {
					el2.add(new XmlDoc.Attribute("role", actualGenericProjectRole));
					keep = true;
				}
			}

			// Add data-use as well if wanted
			if (keep) {
				// We use the local implementation rather than that of
				// ProjectMember for performance
				// reasons (saves another call to user.describe)
				// String actualGenericDataUseRole = pM.directDataUseRole
				// (executor, route, cid);
				String actualGenericDataUseRole = directUserProjectRole(user, cid, true);
				if (actualGenericDataUseRole != null)
					el2.add(new XmlDoc.Attribute("data-use", actualGenericDataUseRole));
				if (showDetail)
					addUserDetail(executor, cred, el2); // cred guarenteed
														// non-null
				membersOut.add(el2);
			}
		}
		//
		return membersOut;
	}

	private static void addUser(XmlDoc.Element el, String id, UserCredential cred) throws Throwable {

		el.add(new XmlDoc.Attribute("id", id));
		if (cred.hasAuthority()) {
			el.add(new XmlDoc.Attribute("authority", cred.authorityName()));
			String p = cred.authorityProtocol();
			if (p != null)
				el.add(new XmlDoc.Attribute("protocol", p));
		}
		el.add(new XmlDoc.Attribute("domain", cred.domain()));
		el.add(new XmlDoc.Attribute("user", cred.user()));
	}

	/**
	 * Get role members who hold the given project role
	 * 
	 * @param executor
	 * @param proute
	 *            to the server of interest
	 * @param cid
	 * @param projectRole
	 * @param explicit
	 * @param userMember
	 *            true for user members and false for role members
	 * @return
	 * @throws Throwable
	 */
	private static Vector<XmlDoc.Element> roleMembers(ServiceExecutor executor, String proute, String cid,
			String genericProjectRole, boolean explicit) throws Throwable {

		if (cid == null)
			return null;
		ServerRoute route = new ServerRoute(proute);

		// Get the project-specific role from the generic
		String specificProjectRole = setSpecificRoleName(genericProjectRole, cid);

		// Output record
		Vector<XmlDoc.Element> membersOut = new Vector<XmlDoc.Element>();

		// FInd the roles that are allowed to be project team members
		XmlDocMaker dm = new XmlDocMaker("args");
		XmlDoc.Element r = executor.execute(route, "om.pssd.role-member-registry.list", dm.root());
		if (r == null)
			return null;

		// Iterate
		Collection<XmlDoc.Element> roleMembers = r.elements("role");
		if (roleMembers != null) {
			for (XmlDoc.Element el : roleMembers) {
				String roleMember = el.value();
				String roleMemberId = el.value("@id");
				ProjectMember pM = new ProjectMember(roleMember);

				// Does this role member implicitly contain the project role we
				// want
				Boolean keep = ModelUser.hasRole(route, executor, roleMember, specificProjectRole);

				String actualGenericProjectRole = null;
				// TODO: optimization (like for users), would be to describe the
				// role-member once
				// locally and re-implement the ProjectMember functions
				// (directProjectRole & directDataUseRole)
				// locally using that information passed in. They are both doing
				// a describe at the moment.
				if (keep) {
					// Find the role-members's direct project role, if any
					actualGenericProjectRole = pM.directProjectRole(executor, route, cid);
					if (actualGenericProjectRole != null) {
						if (explicit) {
							if (actualGenericProjectRole.equals(genericProjectRole))
								keep = true;
						} else {
							keep = true;
						}
					}
				}

				// Add data-use as well
				if (keep) {
					String actualGenericDataUseRole = pM.directDataUseRole(executor, route, cid);
					XmlDocMaker dm2 = new XmlDocMaker("role-member", new String[] { "id", roleMemberId, "member",
							roleMember, "role", actualGenericProjectRole, "data-use", actualGenericDataUseRole });
					membersOut.add(dm2.root());
				}
			}
		}

		return membersOut;
	}

	private static void addUserDetail(ServiceExecutor executor, UserCredential cred, XmlDoc.Element el)
			throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		if (cred.authority() != null) {
			dm.add(cred.authority().toXmlElement());
		}
		dm.add("domain", cred.domain());
		dm.add("user", cred.user());
		XmlDoc.Element r = executor.execute("om.pssd.user.describe", dm.root());
		if (r == null)
			return;

		// Add name elements
		Collection<XmlDoc.Element> names = r.elements("user/name");
		if (names != null) {
			for (XmlDoc.Element t : names) {
				if (t.name().equals("name"))
					el.add(t);
			}
		}

		// Add email
		Collection<XmlDoc.Element> emails = r.elements("user/email");
		if (emails != null) {
			for (XmlDoc.Element t : emails) {
				if (t.name().equals("email"))
					el.add(t);
			}
		}
	}

	/**
	 * Reimplement locally rather than use ProjectMember.directProjectRole for
	 * performance reasons
	 * 
	 * @param executor
	 * @param route
	 * @param cid
	 * @return
	 * @throws Throwable
	 */
	private static String directUserProjectRole(XmlDoc.Element user, String cid, boolean dataUse) throws Throwable {

		// Iterate through the member's roles and see if any of them are a
		// project role for this cid
		// There is no way to wild-card these kinds of queries e.g. find roles
		// with *.<cid> so you have to look through
		// them all. Only finds the first one.
		// TODO: detect multiples and throw exception
		Collection<String> values = user.values("role[@type='role']");
		if (values == null)
			return null;
		for (String role : values) {
			String actualGenericRole = null;
			if (dataUse) {
				actualGenericRole = ProjectMember.holdsThisDataUseRole(role, cid);
			} else {
				actualGenericRole = ProjectMember.holdsThisProjectRole(role, cid);
			}
			if (actualGenericRole != null)
				return actualGenericRole;
		}
		return null;
	}
}
