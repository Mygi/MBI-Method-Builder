package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

import nig.mf.plugin.pssd.*;
import nig.mf.plugin.pssd.user.UserCredential;
import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.pssd.plugin.util.DistributedAsset;

import java.util.*;

public class SvcProjectMembersRemove extends PluginService {
	private Interface _defn;

	public SvcProjectMembersRemove() {

		_defn = new Interface();

		_defn.add(new Interface.Element("id", CiteableIdType.DEFAULT,
				"The identity of the project (managed by the local server).",
				1, 1));

		// Project team user members
		Interface.Element me = new Interface.Element("member", XmlDocType.DEFAULT,
				"User member to remove from this Project.", 0,
				Integer.MAX_VALUE);
		//
		Interface.Element ie = new Interface.Element("authority",StringType.DEFAULT,"The authority of interest. Defaults to local.",0,1);
		ie.add(new Interface.Attribute("protocol", StringType.DEFAULT,
				"The protocol of the identity authority. If unspecified, defaults to federated user within the same type of repository.", 0));
		me.add(ie);
		//
		me.add(new Interface.Element("domain", StringType.DEFAULT,
				"The domain name of the member.", 1, 1));
		me.add(new Interface.Element("user", StringType.DEFAULT,
				"The user name within the domain.", 1, 1));
		_defn.add(me);

		// Project team role-members. Although I could drop the upper level, leave it
		// like this for consistency with all other interfaces.
		me = new Interface.Element("role-member", XmlDocType.DEFAULT,
				"The role-member to remove from this Project.", 0,
				Integer.MAX_VALUE);
		me.add(new Interface.Element("member", StringType.DEFAULT,
				"The role member.", 1, 1));
		_defn.add(me);

	}

	public String name() {
		return "om.pssd.project.members.remove";
	}

	public String description() {
		return "Removes the specified  team members from the given Project.";
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

		// Remove the members
		remove (executor(), members, roleMembers, dID.getCiteableID(), w);
	}



	
	private  void remove (ServiceExecutor executor, Collection<XmlDoc.Element> members,
			Collection<XmlDoc.Element> roleMembers, String cid, XmlWriter w) throws Throwable {
		
		// FInd how may project admins there are in the Project. We must leave one at least behind
		String proute = null;
		ServerRoute route = null;
		int memberType = 2;   // User members and role members
		boolean deRef = false;
		boolean explicit = true;
		boolean showDetail = false;
		Collection<XmlDoc.Element> admins =  Project.membersWithProjectRole(executor, proute, cid,
				     Project.ADMINISTRATOR_ROLE_NAME, explicit, memberType, deRef, showDetail);
		int nAdmin = admins.size();

		// Iterate through specified  user members and revoke roles
		if (members!=null) {
			for (XmlDoc.Element member : members) {

				// Get the user
				UserCredential userCred = new UserCredential(member.value("authority"), member.value("authority/@protocol"), member.value("domain"), member.value("user"));
				ProjectMember pM = new ProjectMember (userCred);
				
				// Find directly held project role
				String projectRole = pM.directProjectRole(executor, route, cid);
				
				// Ignore if not already a project member
				if (projectRole!=null) {

					// Exception if last admin left
					checkAdmins (nAdmin, projectRole, userCred.user(), cid);

					// Revoke
					if (projectRole!=null) {
						Project.revokeProjectRole(executor, cid, userCred, projectRole);
						if (projectRole.equals(Project.ADMINISTRATOR_ROLE_NAME)) nAdmin--;
					}

					// Find current data-use role
					String dataUseRole = pM.directDataUseRole(executor, route, cid);
					if (dataUseRole!=null) {
						Project.revokeProjectRole(executor, cid, userCred, dataUseRole);
					}
					//
					w.add(member);
				}
			}
		}
		
		// Iterate through specified role members and revoke roles
		if (roleMembers!=null) {
			for (XmlDoc.Element member : roleMembers) {

				// Get the role member
				String roleMember = member.value("member");
				ProjectMember pM = new ProjectMember (roleMember);
				
				// Find directly held project role
				String projectRole = pM.directProjectRole(executor, route, cid);
				
				// Ignore if not already a project member
				if (projectRole!=null) {

					// Exception if last admin left
					checkAdmins (nAdmin, projectRole, roleMember, cid);

					// Revoke
					if (projectRole!=null) {
						Project.revokeProjectRole(executor, cid, roleMember, projectRole);
						if (projectRole.equals(Project.ADMINISTRATOR_ROLE_NAME)) nAdmin--;
					}

					// Find current data-use role
					String dataUseRole = pM.directDataUseRole(executor, route, cid);
					if (dataUseRole!=null) {
						Project.revokeProjectRole(executor, cid, roleMember, dataUseRole);
					}
					//
					w.add(member);
				}
			}
		}



	}

	private void checkAdmins (Integer nProjectAdmin, String role, String member, String projectCid) throws Throwable {
		if (nProjectAdmin==1 && role.equals(Project.ADMINISTRATOR_ROLE_NAME)) {
			throw new Exception ("Member '" + member + "' is the last Project Admin for project '" + projectCid + "'. Cannot remove.");
		}
	}

}
