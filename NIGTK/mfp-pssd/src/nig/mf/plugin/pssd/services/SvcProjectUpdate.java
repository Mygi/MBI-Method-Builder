package nig.mf.plugin.pssd.services;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import nig.mf.plugin.pssd.ModelUser;
import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

// TODO: remove member and role-member functionality

public class SvcProjectUpdate extends PluginService {
	private Interface _defn;

	public SvcProjectUpdate() {

		_defn = new Interface();

		_defn.add(new Interface.Element("id", CiteableIdType.DEFAULT,
				"The identity of the project (managed by the local server).", 1, 1));
		_defn.add(new Interface.Element("name", StringType.DEFAULT, "The name of this project.", 0, 1));
		_defn.add(new Interface.Element("description", StringType.DEFAULT, "An arbitrary description for the project.",
				0, 1));

		Interface.Element me = new Interface.Element("method", XmlDocType.DEFAULT, "Method utilized by this project.",
				0, Integer.MAX_VALUE);
		me.add(new Interface.Attribute("action", new EnumType(new String[] { "merge", "remove", "replace", "clear" }),
				"Action to perform when updating the project methods. Defaults to 'merge'. When removing, only the id is utilized.", 0));
		me.add(new Interface.Element("id", CiteableIdType.DEFAULT, "The identity of the method.", 0, 1));
		me.add(new Interface.Element("notes", StringType.DEFAULT,
				"Arbitrary notes associated with the use of this method.", 0, 1));
		_defn.add(me);

		// Project team user members
		me = new Interface.Element("member", XmlDocType.DEFAULT, "User to become a member of this project.", 0,
				Integer.MAX_VALUE);
		me.add(new Interface.Attribute(
				"action",
				new EnumType(new String[] { "merge", "remove", "replace" }),
				"Action to perform when updating the project members. Defaults to 'replace'. With 'merge', the user is matched on authority, domain and user.  Then the 'role' and 'data-use' fields are updated as specified. You can't update who the member is. If the member is not found it is added.",
				0));
		//
		Interface.Element ie = new Interface.Element("authority", StringType.DEFAULT,
				"The authority of interest. Defaults to local.", 0, 1);
		ie.add(new Interface.Attribute(
				"protocol",
				StringType.DEFAULT,
				"The protocol of the identity authority. If unspecified, defaults to federated user within the same type of repository.",
				0));
		me.add(ie);
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
		me.add(new Interface.Attribute(
				"action",
				new EnumType(new String[] { "merge", "remove", "replace" }),
				"Action to perform when updating the project members. Defaults to 'replace'. With 'merge', the role (member) name.  Then the 'role' and 'data-use' fields are updated as specified. You can't update who the role member is. If the role member is not found it is added.",
				0));
		me.add(new Interface.Element("member", StringType.DEFAULT, "The role to become a member of the Project.", 1, 1));
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

		// Project data-use
		me = new Interface.Element(
				"data-use",
				new EnumType(new String[] { Project.CONSENT_SPECIFIC_ROLE_NAME, Project.CONSENT_EXTENDED_ROLE_NAME,
						Project.CONSENT_UNSPECIFIED_ROLE_NAME }),
				"Specifies the type of consent for the use of data for this project: 1) 'specific' means use the data only for the original specific intent, 2) 'extended' means use the data for related projects and 3) 'unspecified' means use the data for any research. Team member's data-use specifications will be silently adjusted to be consistent with this specification.",
				0, 1);
		_defn.add(me);

		//
		me = new Interface.Element("meta", XmlDocType.DEFAULT, "Optional metadata - a list of asset documents.", 0, 1);
		me.add(new Interface.Attribute("action", new EnumType(new String[] { "add", "merge", "remove", "replace" }),
				"Action to perform when updating the project meta information. Defaults to 'merge'.", 0));
		me.setIgnoreDescendants(true);
		_defn.add(me);

	}

	public String name() {

		return "om.pssd.project.update";
	}

	public String description() {

		return "Updates the meta-data for a local project.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// Set distributed citeable ID for the local Project and validate.
		String cid = args.value("id");
		DistributedAsset dID = new DistributedAsset(args.element("id"));
		String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dID);
		if (type == null) {
			throw new Exception("The asset associated with " + dID.toString() + " does not exist");
		}
		if (!type.equals(Project.TYPE)) {
			throw new Exception("Object " + dID.getCiteableID() + " [type=" + type + "] is not a " + Project.TYPE);
		}
		if (dID.isReplica()) {
			throw new Exception("The supplied Project is a replica and this service cannot modify it.");
		}

		// Creator must have project creation role..
		if (! (ModelUser.hasRole(null, executor(), Project.projectAdministratorRoleName(dID.getCiteableID())) ||
	                 ModelUser.hasRole(null, executor(), PSSDUtils.OBJECT_ADMIN_ROLE_NAME))) {
			throw new Exception ("User not authorised: requires '" + Project.projectAdministratorRoleName(dID.getCiteableID()) +
					             "' or '" + PSSDUtils.OBJECT_ADMIN_ROLE_NAME + " role");
		}

		// Update the Project asset
		updateProjectAsset(executor(), args, cid);

		/*
		 * generate system event
		 */
		// SystemEventChannel.generate(new PSSDObjectEvent(Action.destroy,
		// cid));
	}

	public static void updateProjectAsset(ServiceExecutor executor, XmlDoc.Element args, String cid) throws Throwable {

		// Update the project meta (e.g. generic doc types)
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", cid);
		if (args.value("meta/@action") != null) {
			dm.push("meta", new String[] { "action", args.value("meta/@action") });
		} else {
			// defaults to merge
			dm.push("meta", new String[] { "action", "merge" });
		}

		// Common object stuff in meta/pssd-object
		PSSDUtils.setObjectMeta(dm, Project.TYPE, args.value("name"), args.value("description"));

		// Generic meta-data in "meta/<doc type>
		PSSDUtils.setObjectOptionalMeta(dm, args.element("meta"), "om.pssd.project");

		dm.pop();

		// Do it
		executor.execute("asset.set", dm.root());

		// Now update the Method, Members, Data-use meta-data
		dm = new XmlDocMaker("args");
		dm.add("cid", cid);
		dm.add("pdist", 0); // Force local
		XmlDoc.Element r = executor.execute("asset.get", dm.root());

		// Set data-use now so we can read it from the asset later
		String currentDataUse = r.value("asset/meta/pssd-project/data-use");
		if (currentDataUse == null)
			currentDataUse = Project.CONSENT_SPECIFIC_ROLE_NAME;
		String dataUse = args.value("data-use");
		if (dataUse == null)
			dataUse = currentDataUse;

		// Prepare the update Method meta-data
		Collection<XmlDoc.Element> currentMethods = r.elements("asset/meta/pssd-project/method");
		Collection<XmlDoc.Element> updateMethods = args.elements("method");
		Collection<XmlDoc.Element> methods = updateMethods(currentMethods, updateMethods);

		// Now set the new meta-data
		dm = new XmlDocMaker("args");
		dm.add("cid", cid);
		dm.push("meta", new String[] { "action", "replace" });
		dm.push("pssd-project");
		if (methods != null) {
			for (XmlDoc.Element me : methods) {
				dm.add(me);
			}
		}
		dm.add("data-use", dataUse);
		dm.pop();
		dm.pop();

		// We only update objects on their local server
		executor.execute("asset.set", dm.root());

		// Finally update the Project roles of the user and role members
		// We want to do this last in case something else goes wrong earlier
		Collection<XmlDoc.Element> updateMembers = args.elements("member");
		Collection<XmlDoc.Element> updateRoleMembers = args.elements("role-member");
		updateMembers(executor, cid, updateMembers, updateRoleMembers, dataUse);
	}

	private static Collection<XmlDoc.Element> updateMethods(Collection<XmlDoc.Element> currentMethods,
			Collection<XmlDoc.Element> updateMethods) throws Throwable {

		if (currentMethods == null) {
			currentMethods = new Vector<XmlDoc.Element>();
		}
		if (updateMethods != null) {
			// TODO: validate
			List<XmlDoc.Element> replaceMethods = new Vector<XmlDoc.Element>();
			for (XmlDoc.Element method : updateMethods) {
				XmlDoc.Attribute aa = method.attribute("action");
				// default action: merge
				String action = "merge";
				if (aa != null) {
					action = aa.value();
					method.remove(aa);
				}
				if (action.equalsIgnoreCase("clear")) {
					return null;
				} else {
					if (method.element("id") == null) {
						throw new Exception("Missing element method/id.");
					}
					if (action.equalsIgnoreCase("remove")) {
						removeMethod(currentMethods, method);
					} else if (action.equalsIgnoreCase("merge")) {
						mergeMethod(currentMethods, method);
					} else if (action.equalsIgnoreCase("replace")) {
						replaceMethods.add(method);
					}
				}
			}
			if (!replaceMethods.isEmpty()) {
				return replaceMethods;
			}
		}
		return currentMethods;

	}

	private static Collection<XmlDoc.Element> removeMethod(Collection<XmlDoc.Element> list, XmlDoc.Element method)
			throws Throwable {

		if (list == null) {
			return null;
		}
		if (method == null) {
			return list;
		}
		XmlDoc.Element methodToRemove = null;
		for (XmlDoc.Element method2 : list) {
			if (method.value("id").equals(method2.value("id"))) {
				methodToRemove = method2;
				break;
			}
		}
		if (methodToRemove != null) {
			list.remove(methodToRemove);
		}
		return list;

	}

	private static Collection<XmlDoc.Element> mergeMethod(Collection<XmlDoc.Element> list, XmlDoc.Element method)
			throws Throwable {

		if (list == null) {
			list = new Vector<XmlDoc.Element>();
		}
		if (method == null) {
			return list;
		}
		boolean exists = false;
		XmlDoc.Element methodToReplace = null;
		for (XmlDoc.Element method2 : list) {
			if (method.value("id").equals(method2.value("id"))) {
				exists = true;
				methodToReplace = method2;
				break;
			}
		}
		if (exists) {
			if (methodToReplace != null) {
				list.remove(methodToReplace);
				list.add(method);
			}
		} else {
			list.add(method);
		}
		return list;

	}

	private static void updateMembers(ServiceExecutor executor, String cid, Collection<XmlDoc.Element> updateMembers,
			Collection<XmlDoc.Element> updateRoleMembers, String projectDataUse) throws Throwable {

		if (updateMembers == null && updateRoleMembers == null)
			return;
		SvcProjectMembersReplace.checkOneAdmin(updateMembers, updateRoleMembers);
		SvcProjectMembersReplace.replace(executor, true, projectDataUse, updateMembers, updateRoleMembers, cid, null);
	}
}
