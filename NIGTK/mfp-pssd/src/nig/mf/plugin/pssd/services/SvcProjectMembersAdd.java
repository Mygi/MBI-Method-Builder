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

public class SvcProjectMembersAdd extends PluginService {
	private Interface _defn;

	public SvcProjectMembersAdd() {

		_defn = new Interface();

		_defn.add(new Interface.Element("id", CiteableIdType.DEFAULT,
				"The identity of the project (managed by the local server).",
				1, 1));

		// Project team user members
		Interface.Element me = new Interface.Element("member", XmlDocType.DEFAULT,
				"User to become a member of this project.", 0, Integer.MAX_VALUE);
		//
		Interface.Element ie = new Interface.Element("authority",StringType.DEFAULT,"The authority of interest. Defaults to local.",0,1);
		ie.add(new Interface.Attribute("protocol", StringType.DEFAULT,
				"The protocol of the identity authority. If unspecified, defaults to federated user within the same type of repository.", 0));
		me.add(ie);
		//
		me.add(new Interface.Element("domain", StringType.DEFAULT, "The domain name of the member.", 1, 1));
		me.add(new Interface.Element("user", StringType.DEFAULT, "The user name within the domain.", 1, 1));
		me.add(new Interface.Element("role", new EnumType(new String[] { Project.ADMINISTRATOR_ROLE_NAME,
						Project.SUBJECT_ADMINISTRATOR_ROLE_NAME,
						Project.MEMBER_ROLE_NAME, Project.GUEST_ROLE_NAME }),
						"The project role bestowed on the user member. Note: to add/update a member, role must be specified.",
						1, 1));
		me.add(new Interface.Element("data-use", new EnumType(new String[] { Project.CONSENT_SPECIFIC_ROLE_NAME,
						Project.CONSENT_EXTENDED_ROLE_NAME,
						Project.CONSENT_UNSPECIFIED_ROLE_NAME }),
						"Specify how this member (only if role is 'member' or 'guest') will use data from this project",
						0, 1));
		_defn.add(me);

		// Project team role-members
		me = new Interface.Element("role-member", XmlDocType.DEFAULT,
				"Role to become a member of this project. Potential role members must be a member of the role-member registry; see om.pssd.role-member-registry.list", 0,
				1);
		me.add(new Interface.Element("member", StringType.DEFAULT, "The role to become a member of the Project.", 1, 1));
		me.add(new Interface.Element("role", new EnumType(new String[] { Project.ADMINISTRATOR_ROLE_NAME,
						Project.SUBJECT_ADMINISTRATOR_ROLE_NAME,
						Project.MEMBER_ROLE_NAME, Project.GUEST_ROLE_NAME }),
						"The project role bestowed on the role member. Note: to add/update a role-member, role must be specified.",
						1, 1));
		me.add(new Interface.Element("data-use", new EnumType(new String[] { Project.CONSENT_SPECIFIC_ROLE_NAME,
						Project.CONSENT_EXTENDED_ROLE_NAME,
						Project.CONSENT_UNSPECIFIED_ROLE_NAME }),
						"Specify how this member (only if role is 'member' or 'guest') will use data from this project",
						0, 1));
		//
		_defn.add(me);

	}

	public String name() {
		return "om.pssd.project.members.add";
	}

	public String description() {
		return "Adds the specified  team members to the given Project.";
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
		if (members==null && roleMembers==null) {
			throw new Exception ("You must specify the member and/or role-member elements");
		}

		// Set distributed citeable ID for the local Project and validate.
		DistributedAsset dID = new DistributedAsset(args.element("id"));
		String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dID);
		if (type == null) {
			throw new Exception("There is no asset associated with citable ID " + dID.toString());
		}
		if (!type.equals(Project.TYPE)) {
			throw new Exception("Object " + dID.getCiteableID() + " [type="
					+ type + "] is not a " + Project.TYPE);
		}

		// Executing user must have specific project admin or overall admin role to run this service
		String cid = dID.getCiteableID();
		if (!(ModelUser.checkHasRoleNoThrow(null, executor(), Project.projectAdministratorRoleName(cid)) ||
				ModelUser.checkHasRoleNoThrow(null, executor(), PSSDUtils.OBJECT_ADMIN_ROLE_NAME))) {
			throw new Exception ("Caller must hold specific project administrator role for this project or the global administrator role.");
		}

		// Get existing Project data-use role
		String projectDataUse = Project.getProjectDataUse(executor(), dID);

		// Add the members
		add (executor(), projectDataUse, members, roleMembers, dID.getCiteableID(), w);
	}



	// Private functions
	private  void add (ServiceExecutor executor, String projectDataUse, Collection<XmlDoc.Element> members,
			Collection<XmlDoc.Element> roleMembers, String cid, XmlWriter w) throws Throwable {

		// Find how may project admins there are in the Project. We must leave one at least behind
		ServerRoute route = null;

		// Iterate through specified  user members and revoke roles
		if (members!=null) {
			for (XmlDoc.Element member : members) {

				// Get the user
				UserCredential userCred = new UserCredential(member.value("authority"), member.value("authority/@protocol"), member.value("domain"),  member.value("user"));
				String projectRole = member.value("role");
				//
				ProjectMember pM = new ProjectMember (userCred);

				// Find directly held project role
				String currentProjectRole = pM.directProjectRole(executor, route, cid);

				// If the user already is a team member they should be removed before being re-added. Leave this to the caller.
				if (currentProjectRole!=null) {
					throw new Exception ("The user member " + userCred.toString() + " already exists. You must remove them first with service om.pssd.project.members.remove");
				}

				// Grant new role
				Project.grantProjectRole(executor, cid, userCred, projectRole, false);

				// Make sure data-use role is consistent with Project usage
				ProjectMember.setValidProjectMemberDataUse(projectDataUse, member);

				// Grant data-use role
				String dataUseRole = member.value("data-use");
				Project.grantProjectRole(executor, cid, userCred, dataUseRole, false);
			}
		}

		// Iterate through specified role members and revoke roles
		if (roleMembers!=null) {
			for (XmlDoc.Element member : roleMembers) {

				// Get role member
				XmlDoc.Element roleMemberEl = member.element("member");
				SvcRoleMemberRegAdd.addRoleID(executor, roleMemberEl);
				String roleMember = roleMemberEl.value();
				String projectRole = member.value("role");
				ProjectMember pM = new ProjectMember (roleMember);
				

				// Make sure the specified role is in the role-member registry. Silently drop if not
				// as we don't want to throw an exception in the middle of replacing the whole team
				if (AssetRegistry.isInRegistry(executor, SvcRoleMemberRegAdd.REGISTRY_ASSET_NAME, SvcRoleMemberRegAdd.DOCTYPE, roleMemberEl)) {					
					
					// Find directly held project role
					String currentProjectRole = pM.directProjectRole(executor, route, cid);

					// If the role-member already is a team member they should be removed before being re-added. Leave this to the caller.
					if (currentProjectRole!=null) {
						throw new Exception ("The role member  " + roleMember + " already exists. You must remove them first with service om.pssd.project.members.remove");
					}

					// Grant new project role		
					Project.grantProjectRole(executor, cid, roleMember, projectRole, false);

					// Make sure data-use role is consistent with Project usage
					ProjectMember.setValidProjectMemberDataUse(projectDataUse, member);

					// Grant new data-use role
					String dataUse = member.value("data-use");
					Project.grantProjectRole(executor, cid, roleMember, dataUse, false);

					//
					w.add(member);
				}
			}
		}
	}
}
