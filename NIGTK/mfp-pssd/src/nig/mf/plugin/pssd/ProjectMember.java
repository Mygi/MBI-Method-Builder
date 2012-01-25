package nig.mf.plugin.pssd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import nig.mf.plugin.pssd.Project.ProjectCIDAndRole;
import nig.mf.plugin.pssd.user.UserCredential;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

/**
 * Container class to hold a user or role member. Used in interfacing. ALso some
 * useful member-focussed utility functions
 * 
 * @author nebk
 * 
 */
public class ProjectMember {
	private UserCredential _userMember = null;
	private String _roleMember = null;

	public ProjectMember(UserCredential userMember) {

		_userMember = userMember;
	}

	public ProjectMember(String roleMember) {

		_roleMember = roleMember;
	}

	public UserCredential userMember() {

		return _userMember;
	}

	public String roleMember() {

		return _roleMember;
	}

	public Boolean isUser() {

		return _userMember != null;
	}

	/**
	 * Return the projects accessed by the member. Projects may be accessed via
	 * inherited roles.
	 * 
	 * Return includes generic project role held by member
	 * 
	 * @param executor
	 * @return
	 * @throws Throwable
	 */
	public Collection<ProjectCIDAndRole> projectsAccessed(ServiceExecutor executor) throws Throwable {

		// Describe the member to infinity and beyond...
		ServerRoute route = null;
		XmlDoc.Element r = describe(executor, route, "infinity");

		// Hash map to track which roles we have found already
		// key = <specific project role> e.g. pssd.project.admin.1.5.1
		// value = <generic project role>:<cid> e.g. project-administrator:1.5.1
		HashMap<String, String> map = new HashMap<String, String>();

		// Recursively find all unique roles held by this member
		findProjects(r, map);

		// Iterate through map and populate container
		Collection<ProjectCIDAndRole> projects = new ArrayList<ProjectCIDAndRole>();
		Collection<String> values = map.values();
		if (values != null && values.size() > 0) {
			for (String value : values) {
				String[] t = value.split(":");
				ProjectCIDAndRole pcr = new ProjectCIDAndRole(t[1], t[0]);
				projects.add(pcr);
			}
		}

		return projects;
	}

	/**
	 * Set team-member's default 'data-use'. We force the value for *-admin
	 * roles to null. For other roles, we default to specific and over-ride with
	 * the Project value if required.
	 * 
	 * The team-member's data-use is only ever used in the context of Subject
	 * over-ride of the Project-wide specification
	 * 
	 * @param projectDataUse
	 *            The project-wide data use value
	 * @param me
	 *            The 'member' XmlDoc.Element for this project-team member which
	 *            gets updated
	 * @throws Throwable
	 */
	public static void setValidProjectMemberDataUse(String projectDataUse, XmlDoc.Element me) throws Throwable {

		// Default project data-use is specific
		if (projectDataUse == null)
			projectDataUse = Project.CONSENT_SPECIFIC_ROLE_NAME;

		// Find existing data-use value
		String dataUse = me.value("data-use");

		// Remove the data-use element if it exists so we can replace its value
		XmlDoc.Element me2 = me.element("data-use");
		if (me2 != null)
			me.removeInstance(me2);
		//
		String teamRole = me.value("role");
		if (teamRole.equals(Project.ADMINISTRATOR_ROLE_NAME)
				|| teamRole.equals(Project.SUBJECT_ADMINISTRATOR_ROLE_NAME)) {

			// The project-admin can do anything in the project, and by
			// definition the subject-admin can see all subjects, and so
			// the data-use specification is irrelevant. We *never*
			// set the data-use role for a *-admin (or look at it)
			dataUse = null;
		} else {

			// By default, other team members get Specific data-use
			if (dataUse == null) {
				dataUse = Project.CONSENT_SPECIFIC_ROLE_NAME;
			} else {
				dataUse = compareDataUse(projectDataUse, dataUse);
			}

			// Add new element
			XmlDoc.Element el = new XmlDoc.Element("data-use", dataUse);
			me.add(el);
		}

	}

	/**
	 * Set valid subject 'data-use'element; only used in the context of Subject
	 * over-ride of the Project-wide specification
	 * 
	 * @param projectDataUse
	 *            If null defaults to 'specific'
	 * @param dataUse
	 *            Must be specified, null case not sensibly handled.
	 * @return The valid dataUse string to use
	 */
	public static String setValidSubjectDataUse(String projectDataUse, String dataUse) throws Throwable {

		if (projectDataUse == null)
			projectDataUse = Project.CONSENT_SPECIFIC_ROLE_NAME;
		if (dataUse == null)
			return null;
		return compareDataUse(projectDataUse, dataUse);
	}

	/**
	 * Find the members direct (i.e. not held through inheritance) project role,
	 * if any, for this cid
	 * 
	 * @param executor
	 * @param cid
	 * @return The generic (admin/member/guest) project role of interest. Null
	 *         if none.
	 * @throws Throwable
	 */
	public String directProjectRole(ServiceExecutor executor, ServerRoute route, String cid) throws Throwable {

		// Describe member to level 1
		XmlDoc.Element r = describe(executor, route, "1");
		if (r == null)
			return null;

		// Iterate through the member's roles and see if any of them are a
		// project role for this cid
		// There is no way to wild-card these kinds of queries e.g. find roles
		// with *.<cid> so you have to look through
		// them all. Only finds the first one.
		// TODO: detect multiples and throw exception
		Collection<String> values = r.values("actor/role");
		if (values == null)
			return null;
		//
		for (String role : values) {
			String actualGenericProjectRole = holdsThisProjectRole(role, cid);
			if (actualGenericProjectRole != null)
				return actualGenericProjectRole;
		}
		return null;
	}

	/**
	 * Find the members direct (i.e. not held through inheritance) data-use
	 * role, if any.
	 * 
	 * @param executor
	 *            @ param cid
	 * @return The generic (specific/extended/unsepcified) data-use role. Null
	 *         if none.
	 * @throws Throwable
	 */
	public String directDataUseRole(ServiceExecutor executor, ServerRoute route, String cid) throws Throwable {

		// Describe to level 1
		XmlDoc.Element r = describe(executor, route, "1");
		if (r == null)
			return null;

		// Iterate through the actors roles and see if any of them are a project
		// role
		// Only finds the first one.
		// TODO: detect multiples and throw exception
		Collection<String> values = r.values("actor/role");
		if (values == null)
			return null;
		//
		for (String role : values) {
			String actualGenericDataUseRole = holdsThisDataUseRole(role, cid);
			if (actualGenericDataUseRole != null)
				return actualGenericDataUseRole;
		}
		return null;
	}

	/**
	 * See if the given specific project member role matches a given generic
	 * project member role and cid
	 * 
	 * @param role
	 *            The specific role of interest
	 * @param cid
	 * @return The generic project role if found
	 * @throws Throwable
	 */
	public static String holdsThisProjectRole(String role, String cid) throws Throwable {

		String specificRole = Project.setSpecificRoleName(Project.ADMINISTRATOR_ROLE_NAME, cid);
		if (role.equals(specificRole))
			return Project.ADMINISTRATOR_ROLE_NAME;
		//
		specificRole = Project.setSpecificRoleName(Project.SUBJECT_ADMINISTRATOR_ROLE_NAME, cid);
		if (role.equals(specificRole))
			return Project.SUBJECT_ADMINISTRATOR_ROLE_NAME;
		//
		specificRole = Project.setSpecificRoleName(Project.MEMBER_ROLE_NAME, cid);
		if (role.equals(specificRole))
			return Project.MEMBER_ROLE_NAME;
		//
		specificRole = Project.setSpecificRoleName(Project.GUEST_ROLE_NAME, cid);
		if (role.equals(specificRole))
			return Project.GUEST_ROLE_NAME;
		//
		return null;
	}

	/**
	 * See if the given role matches a specific project data-use role
	 * 
	 * @param role
	 *            The specific role of interest
	 * @param cid
	 * @return The generic project role if found
	 * @throws Throwable
	 */
	public static String holdsThisDataUseRole(String role, String cid) throws Throwable {

		String specificRole = Project.setSpecificRoleName(Project.CONSENT_SPECIFIC_ROLE_NAME, cid);
		if (role.equals(specificRole))
			return Project.CONSENT_SPECIFIC_ROLE_NAME;
		//
		specificRole = Project.setSpecificRoleName(Project.CONSENT_EXTENDED_ROLE_NAME, cid);
		if (role.equals(specificRole))
			return Project.CONSENT_EXTENDED_ROLE_NAME;
		//
		specificRole = Project.setSpecificRoleName(Project.CONSENT_UNSPECIFIED_ROLE_NAME, cid);
		if (role.equals(specificRole))
			return Project.CONSENT_UNSPECIFIED_ROLE_NAME;
		//
		return null;
	}

	// Private functions

	private void findProjects(XmlDoc.Element r, HashMap<String, String> map) throws Throwable {

		Collection<XmlDoc.Element> children = r.elements();
		if (children == null)
			return;
		//
		for (XmlDoc.Element e : children) {
			String name = e.name();
			if (name.equals("role")) {
				String type = e.value("@type");
				String role = e.value();
				ProjectRole pr = new ProjectRole(role);
				//
				if (type.equals("role") && pr.want()) {
					if (map.containsKey(role)) {
						// We already have this role
					} else {
						if (hasHigher(map, pr)) {
							// If the map has a higher role for this project, we
							// don't want this one
						} else {
							String lowerRole = hasLower(map, pr);
							if (lowerRole != null) {

								// If the map has a lower role, replace with
								// this one
								map.remove(lowerRole);
								String value = pr.genericRole() + ":" + pr.cid();
								map.put(role, value);
							} else {
								// We have neither higher nor lower, so add this
								// one
								String value = pr.genericRole() + ":" + pr.cid();
								map.put(role, value);
							}
						}
					}
				}
			}

			// Having dealt with the actor, deal with its children
			findProjects(e, map);
		}

	}

	/**
	 * Determine if he hash map contains a 'higher' role already than the one we
	 * are examining
	 * 
	 * @param map
	 * @param pr
	 * @return
	 */
	private boolean hasHigher(HashMap<String, String> map, ProjectRole pr) {

		// pssd.project.admin.<cid>
		// 5 13 19
		// pssd.project.subject.admin.<cid>
		// pssd.project.member.<cid>
		// pssd.project.guest.<cid>

		if (pr.genericRole().equals(Project.ADMINISTRATOR_ROLE_NAME)) {
			return false;
		} else if (pr.genericRole().equals(Project.SUBJECT_ADMINISTRATOR_ROLE_NAME)) {
			String higherRole = Project.SPECIFIC_ADMINISTRATOR_ROLE_NAME_ROOT + "." + pr.cid();
			if (map.containsKey(higherRole))
				return true;
		} else if (pr.genericRole().equals(Project.MEMBER_ROLE_NAME)) {
			String higherRole = Project.SPECIFIC_ADMINISTRATOR_ROLE_NAME_ROOT + "." + pr.cid();
			if (map.containsKey(higherRole))
				return true;
			//
			higherRole = Project.SPECIFIC_SUBJECT_ADMINISTRATOR_ROLE_NAME_ROOT + "." + pr.cid();
			if (map.containsKey(higherRole))
				return true;
		} else if (pr.genericRole().equals(Project.GUEST_ROLE_NAME)) {
			//
			String higherRole = Project.SPECIFIC_ADMINISTRATOR_ROLE_NAME_ROOT + "." + pr.cid();
			if (map.containsKey(higherRole))
				return true;
			//
			higherRole = Project.SPECIFIC_SUBJECT_ADMINISTRATOR_ROLE_NAME_ROOT + "." + pr.cid();
			if (map.containsKey(higherRole))
				return true;
			//
			higherRole = Project.SPECIFIC_MEMBER_ROLE_NAME_ROOT + "." + pr.cid();
			if (map.containsKey(higherRole))
				return true;
		}
		return false;
	}

	private String hasLower(HashMap<String, String> map, ProjectRole pr) {

		// pssd.project.admin.<cid>
		// 5 13 19
		// pssd.project.subject.admin.<cid>
		// pssd.project.member.<cid>
		// pssd.project.guest.<cid>

		if (pr.genericRole().equals(Project.ADMINISTRATOR_ROLE_NAME)) {
			String lowerRole = Project.SPECIFIC_SUBJECT_ADMINISTRATOR_ROLE_NAME_ROOT + "." + pr.cid();
			if (map.containsKey(lowerRole))
				return lowerRole;
			//
			lowerRole = Project.SPECIFIC_MEMBER_ROLE_NAME_ROOT + "." + pr.cid();
			if (map.containsKey(lowerRole))
				return lowerRole;
			//
			lowerRole = Project.SPECIFIC_GUEST_ROLE_NAME_ROOT + "." + pr.cid();
			if (map.containsKey(lowerRole))
				return lowerRole;
		} else if (pr.genericRole().equals(Project.SUBJECT_ADMINISTRATOR_ROLE_NAME)) {
			String lowerRole = Project.SPECIFIC_MEMBER_ROLE_NAME_ROOT + "." + pr.cid();
			if (map.containsKey(lowerRole))
				return lowerRole;
			//
			lowerRole = Project.SPECIFIC_GUEST_ROLE_NAME_ROOT + "." + pr.cid();
			if (map.containsKey(lowerRole))
				return lowerRole;
		} else if (pr.genericRole().equals(Project.MEMBER_ROLE_NAME)) {
			String lowerRole = Project.SPECIFIC_GUEST_ROLE_NAME_ROOT + "." + pr.cid();
			if (map.containsKey(lowerRole))
				return lowerRole;
		} else if (pr.genericRole().equals(Project.GUEST_ROLE_NAME)) {
			// We can't sink lower than this :-(
			return null;
		}
		return null;
	}

	/**
	 * This helper class takes a specific project role and splits it into a
	 * generic role and cid
	 * 
	 * @author nebk
	 * 
	 */
	private class ProjectRole {
		private String _specific = null; // Specific role
		private ProjectCIDAndRole _cidAndRole = null; // Generic role and cid

		public ProjectRole(String specificRole) {

			split(specificRole);
		}

		public String genericRole() {

			return _cidAndRole.role();
		}

		public String cid() {

			return _cidAndRole.projectId();
		}

		public String specificRole() {

			return _specific;
		}

		public boolean want() {

			return _specific != null && _cidAndRole != null;
		}

		private void split(String specificRole) {

			// pssd.project.admin.<cid>
			// 5 13 19
			// pssd.project.subject.admin.<cid>
			// pssd.project.member.<cid>
			// pssd.project.guest.<cid>

			_specific = specificRole;
			int len = specificRole.length();
			if (specificRole.startsWith("pssd.project.admin.")) {
				_cidAndRole = new ProjectCIDAndRole(specificRole.substring(19, len), Project.ADMINISTRATOR_ROLE_NAME);
			} else if (specificRole.startsWith("pssd.project.subject.admin.")) {
				_cidAndRole = new ProjectCIDAndRole(specificRole.substring(27, len),
						Project.SUBJECT_ADMINISTRATOR_ROLE_NAME);
			} else if (specificRole.startsWith("pssd.project.member.")) {
				_cidAndRole = new ProjectCIDAndRole(specificRole.substring(20, len), Project.MEMBER_ROLE_NAME);
			} else if (specificRole.startsWith("pssd.project.guest.")) {
				_cidAndRole = new ProjectCIDAndRole(specificRole.substring(19, len), Project.GUEST_ROLE_NAME);
			} else {
				_specific = null;
				_cidAndRole = null;
			}
		}
	}

	private static String compareDataUse(String projectDataUse, String dataUse) {

		if (projectDataUse.equals(Project.CONSENT_SPECIFIC_ROLE_NAME)) {
			if (dataUse.equals(Project.CONSENT_EXTENDED_ROLE_NAME)
					|| dataUse.equals(Project.CONSENT_UNSPECIFIED_ROLE_NAME))
				dataUse = projectDataUse;
		} else if (projectDataUse.equals(Project.CONSENT_EXTENDED_ROLE_NAME)) {
			if (dataUse.equals(Project.CONSENT_UNSPECIFIED_ROLE_NAME))
				dataUse = projectDataUse;
		} else if (projectDataUse.equals(Project.CONSENT_UNSPECIFIED_ROLE_NAME)) {
			// Nothing to do
		}
		return dataUse;
	}

	/**
	 * Describe the member to specified level showing all permissions
	 * 
	 * @param executor
	 * @param route
	 * @return Note that if you use levels infinity, actor.describe returns a
	 *         different structure
	 * @throws Throwable
	 */
	private XmlDoc.Element describe(ServiceExecutor executor, ServerRoute route, String levels) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		if (isUser()) {
			if (_userMember.hasAuthority()) {
				dm.add(_userMember.authority().toXmlElement());
			}
			dm.add("domain", _userMember.domain());
			dm.add("user", _userMember.user());
			dm.add("permissions", new String[] { "levels", levels }, "true");

			// The top level returned will be "user"
			XmlDoc.Element r = executor.execute(route, "user.describe", dm.root());
			if (r == null)
				return null;

			// Rewrite the top-level as actor as the pain of having different
			// top-levels is too much upstream
			r.renameElement("user", "actor");
			return r;
		} else {
			dm.add("name", _roleMember);
			dm.add("type", "role");
			dm.add("levels", levels);
			// The top level returned will be "actor"
			return executor.execute(route, "actor.describe", dm.root());
		}
	}
}
