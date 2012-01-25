package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

import nig.mf.plugin.pssd.*;
import nig.mf.plugin.pssd.user.UserCredential;
import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.plugin.util.AssetRegistry;
import nig.mf.pssd.plugin.util.DistributedAsset;

import java.util.*;

public class SvcProjectMembersReplace extends PluginService {
	private Interface _defn;

	public SvcProjectMembersReplace() {

		_defn = new Interface();

		_defn.add(new Interface.Element("id", CiteableIdType.DEFAULT,
				"The identity of the project (managed by the local server).", 1, 1));

		// Project team user members
		Interface.Element me = new Interface.Element("member", XmlDocType.DEFAULT,
				"User to become a member of this project.", 0, Integer.MAX_VALUE);
		//
		Interface.Element ie = new Interface.Element("authority", StringType.DEFAULT,
				"The authority of interest. Defaults to local.", 0, 1);
		ie.add(new Interface.Attribute(
				"protocol",
				StringType.DEFAULT,
				"The protocol of the identity authority. If unspecified, defaults to federated user within the same type of repository.",
				0));
		me.add(ie);
		//
		me.add(new Interface.Element("domain", StringType.DEFAULT, "The domain name of the member.", 1, 1));
		me.add(new Interface.Element("user", StringType.DEFAULT, "The user name within the domain.", 1, 1));
		me.add(new Interface.Element("role", new EnumType(new String[] { Project.ADMINISTRATOR_ROLE_NAME,
				Project.SUBJECT_ADMINISTRATOR_ROLE_NAME, Project.MEMBER_ROLE_NAME, Project.GUEST_ROLE_NAME }),
				"The project role bestowed on the user member. Note: to add/update a member, role must be specified.",
				1, 1));
		me.add(new Interface.Element("data-use", new EnumType(new String[] { Project.CONSENT_SPECIFIC_ROLE_NAME,
				Project.CONSENT_EXTENDED_ROLE_NAME, Project.CONSENT_UNSPECIFIED_ROLE_NAME }),
				"Specify how this member (only if role is 'member' or 'guest') will use data from this project", 0, 1));
		_defn.add(me);

		// Project team role-members
		me = new Interface.Element("role-member", XmlDocType.DEFAULT, "Role to become a member of this project.", 0,
				Integer.MAX_VALUE);
		me.add(new Interface.Element(
				"member",
				StringType.DEFAULT,
				"The role to become a member of the Project. Potential role members must be a member of the role-member registry; see om.pssd.role-member-registry.list",
				1, 1));
		me.add(new Interface.Element(
				"role",
				new EnumType(new String[] { Project.ADMINISTRATOR_ROLE_NAME, Project.SUBJECT_ADMINISTRATOR_ROLE_NAME,
						Project.MEMBER_ROLE_NAME, Project.GUEST_ROLE_NAME }),
				"The project role bestowed on the role member. Note: to add/update a role-member, role must be specified.",
				1, 1));
		me.add(new Interface.Element("data-use", new EnumType(new String[] { Project.CONSENT_SPECIFIC_ROLE_NAME,
				Project.CONSENT_EXTENDED_ROLE_NAME, Project.CONSENT_UNSPECIFIED_ROLE_NAME }),
				"Specify how this member (only if role is 'member' or 'guest') will use data from this project", 0, 1));
		_defn.add(me);
		//
		_defn.add(new Interface.Element("ignore-system", BooleanType.DEFAULT,
				"Ignore SYSTEM domain members (specified and extant) ?  Defaults to true.", 0, 1));

	}

	public String name() {

		return "om.pssd.project.members.replace";
	}

	public String description() {

		return "Replaces the project team members (all of those pre-existing) with those specified.  By default, all users from the SYSTEM domain are ignored; you can neither add nor revoke their roles.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// CHeck
		Collection<XmlDoc.Element> members = args.elements("member");
		Collection<XmlDoc.Element> roleMembers = args.elements("role-member");
		if (members == null && roleMembers == null) {
			throw new Exception("You must specify the member and/or role-member elements");
		}

		// Make sure there is one project admin the list
		checkOneAdmin(members, roleMembers);

		// Set distributed citeable ID for the local Project and validate.
		DistributedAsset dID = new DistributedAsset(args.element("id"));
		String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dID);
		if (type == null) {
			throw new Exception("The asset associated with " + dID.toString() + " does not exist");
		}
		if (!type.equals(Project.TYPE)) {
			throw new Exception("Object " + dID.getCiteableID() + " [type=" + type + "] is not a " + Project.TYPE);
		}

		// Executing user must have specific project admin or overall admin role
		// to run this service
		String cid = dID.getCiteableID();
		if (!(ModelUser.checkHasRoleNoThrow(null, executor(), Project.projectAdministratorRoleName(cid)) || ModelUser
				.checkHasRoleNoThrow(null, executor(), PSSDUtils.OBJECT_ADMIN_ROLE_NAME))) {
			throw new Exception(
					"Caller must hold specific project administrator role for this project or the global administrator role.");
		}

		// Get existing Project data-use role
		String projectDataUse = Project.getProjectDataUse(executor(), dID);

		// Replace the members
		Boolean ignoreSystem = args.booleanValue("ignore-system", true);
		replace(executor(), ignoreSystem, projectDataUse, members, roleMembers, dID.getCiteableID(), w);
	}

	public static void checkOneAdmin(Collection<XmlDoc.Element> members, Collection<XmlDoc.Element> roleMembers)
			throws Throwable {

		int nAdmin = 0;
		//
		if (members != null) {
			for (XmlDoc.Element member : members) {
				String role = member.value("role");
				if (role.equals(Project.ADMINISTRATOR_ROLE_NAME))
					nAdmin++;
			}
		}
		//
		if (nAdmin == 0 && roleMembers != null) {
			for (XmlDoc.Element member : roleMembers) {
				String role = member.value("role");
				if (role.equals(Project.ADMINISTRATOR_ROLE_NAME))
					nAdmin++;
			}
		}
		//
		if (nAdmin == 0) {
			throw new Exception("The list of project team members must contain at least one project administrator");
		}
	}

	public static void replace(ServiceExecutor executor, Boolean ignoreSystem, String projectDataUse,
			Collection<XmlDoc.Element> members, Collection<XmlDoc.Element> roleMembers, String cid, XmlWriter w)
			throws Throwable {

		// Get existing list
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", cid);
		XmlDoc.Element currentMembers = executor.execute("om.pssd.project.members.list", dm.root());

		// Revoke all roles for this Project for the existing team members
		// (user- and role members)
		XmlDoc.Element memberFailedOn = revokeAllMembers(executor, currentMembers, cid, ignoreSystem);
		if (memberFailedOn != null) {
			throw new Exception("Failed to revoke the role of member '" + memberFailedOn
					+ "'.  Leaving roles unchanged and exiting");
		}

		// Iterate through new user members and set new team-member roles
		if (members != null) {
			for (XmlDoc.Element member : members) {

				// Fetch new
				try {
					String domain = member.value("domain");
					if (!ignoreSystem || (ignoreSystem && !domain.equalsIgnoreCase("system"))) {

						UserCredential userCred = new UserCredential(member.value("authority"), member.value("authority/@protocol"), domain,
								member.value("user"));
						String projectRole = member.value("role");

						// Grant new project role
						Project.grantProjectRole(executor, cid, userCred, projectRole, false);

						// Make sure data-use role is consistent with Project
						// usage
						ProjectMember.setValidProjectMemberDataUse(projectDataUse, member);

						// Grant new data-use role
						String dataUse = member.value("data-use");
						Project.grantProjectRole(executor, cid, userCred, dataUse, false);
					}
				} catch (Throwable e) {
					// We really don't want to bomb out in the middle of
					// replacing members
					if (w != null) {
						member.add(new XmlDoc.Attribute("status", "failed to grant"));
						w.add(member);
					}
				}
			}
		}

		// Iterate through new role members and set new team-member roles
		if (roleMembers != null) {
			for (XmlDoc.Element member : roleMembers) {

				// Fetch new
				try {
					XmlDoc.Element roleMemberEl = member.element("member");
					String roleMember = member.value("member");
					String projectRole = member.value("role");

					// Make sure the specified role is in the role-member
					// registry.
					if (AssetRegistry.isInRegistry(executor, SvcRoleMemberRegAdd.REGISTRY_ASSET_NAME,
							SvcRoleMemberRegAdd.DOCTYPE, roleMemberEl)) {

						// Grant new project role
						Project.grantProjectRole(executor, cid, roleMember, projectRole, false);

						// Make sure data-use role is consistent with Project
						// usage
						ProjectMember.setValidProjectMemberDataUse(projectDataUse, member);

						// Grant new data-use role
						String dataUse = member.value("data-use");
						Project.grantProjectRole(executor, cid, roleMember, dataUse, false);
					} else {
						member.add(new XmlDoc.Attribute("status", "not in role-member registry"));
						w.add(member);
					}
				} catch (Throwable e) {
					// We really don't want to bomb out in the middle of
					// replacing members
					if (w != null) {
						member.add(new XmlDoc.Attribute("status", "failed to grant"));
						w.add(member);
					}
				}
			}
		}
	}

	/**
	 * Revoke the project-specific roles for all members (user and role) of this
	 * project
	 * 
	 * @param executor
	 * @param members
	 * @param cid
	 * @throws Throwable
	 */
	private static XmlDoc.Element revokeAllMembers(ServiceExecutor executor, XmlDoc.Element members, String cid,
			Boolean ignoreSystem) throws Throwable {

		if (members == null)
			return null;

		// Keep track of the successful ones
		ArrayList<XmlDoc.Element> doneMembers = new ArrayList<XmlDoc.Element>();
		XmlDoc.Element memberFailedOn = null;

		// Iterate through the user- and role-members and revoke their
		// project-specific role
		Collection<XmlDoc.Element> members2 = members.elements();
		if (members2 == null)
			return null;
		//
		boolean create = false;
		boolean grant = false;
		for (XmlDoc.Element member : members2) {
			try {
				String name = member.name();
				//
				String genericProjectRole = member.value("@role");
				String specificProjectRole = Project.setSpecificRoleName(genericProjectRole, cid);
				//
				String genericDataUseRole = member.value("@data-use"); // Will
																		// be
																		// null
																		// for
																		// admin
																		// project
																		// roles
				String specificDataUseRole = null;
				if (genericDataUseRole != null)
					specificDataUseRole = Project.setSpecificRoleName(genericDataUseRole, cid);

				if (name.equals("member")) {
					String domain = member.value("@domain");
					if (!ignoreSystem || (ignoreSystem && !domain.equalsIgnoreCase("system"))) {

						
						UserCredential userCred = new UserCredential(member.value("@authority"),
								member.value("@protocol"), domain, member.value("@user"));
						//
						Project.grantRevoke(executor, userCred, specificProjectRole, create, grant);
						if (specificDataUseRole != null)
							Project.grantRevoke(executor, userCred, specificProjectRole, create, grant);
					}
				} else if (name.equals("role-member")) {
					String roleMember = member.value("@member");
					Project.grantRevoke(executor, roleMember, specificProjectRole, create, grant);
					if (specificDataUseRole != null)
						Project.grantRevoke(executor, roleMember, specificDataUseRole, create, grant);
				}
				doneMembers.add(member);
			} catch (Throwable e) {
				// If we get an exception, what we need to do is stop; regrant
				// the original roles and exit the whole process
				memberFailedOn = member;
				break;
			}
		}

		// We had a failure. Re-grant the roles as they were and then we bug
		// out.
		if (memberFailedOn != null) {
			grant = true;
			for (XmlDoc.Element member : doneMembers) {
				String name = member.name();
				//
				String genericProjectRole = member.value("@role");
				String specificProjectRole = Project.setSpecificRoleName(genericProjectRole, cid);
				//
				String genericDataUseRole = member.value("@data-use"); // Will
																		// be
																		// null
																		// for
																		// admin
																		// project
																		// roles
				String specificDataUseRole = null;
				if (genericDataUseRole != null)
					specificDataUseRole = Project.setSpecificRoleName(genericDataUseRole, cid);

				if (name.equals("member")) {
					
					
					UserCredential userCred = new UserCredential(member.value("@authority"),
							member.value("@protocol"), member.value("@domain"),
							member.value("@user"));

					//
					Project.grantRevoke(executor, userCred, specificProjectRole, create, grant);
					if (specificDataUseRole != null)
						Project.grantRevoke(executor, userCred, specificProjectRole, create, grant);
				} else if (name.equals("role-member")) {
					String roleMember = member.value("@member");
					Project.grantRevoke(executor, roleMember, specificProjectRole, create, grant);
					if (specificDataUseRole != null)
						Project.grantRevoke(executor, roleMember, specificDataUseRole, create, grant);
				}
			}
			return memberFailedOn;
		}
		return null;
	}
}
