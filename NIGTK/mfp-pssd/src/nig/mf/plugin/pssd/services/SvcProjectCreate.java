package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.ModelUser;
import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.ProjectMember;
import nig.mf.plugin.pssd.Role;
import nig.mf.plugin.pssd.method.Method;
import nig.mf.plugin.pssd.user.UserCredential;
import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.pssd.plugin.util.DistributedAsset;
import nig.mf.pssd.plugin.util.DistributedAssetUtil;
import nig.mf.pssd.plugin.util.DistributedQuery.ResultAssetType;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcProjectCreate extends PluginService {
	private Interface _defn;

	public SvcProjectCreate() {
		_defn = new Interface();

		_defn.add(new Interface.Element("project-number", IntegerType.POSITIVE_ONE,
				"Specifies the project number for the identifier. If specified, then there cannot be any other asset/object with this identity assigned. Used for importing projects from another system.",
				0, 1));
		_defn.add(new Element("fillin", BooleanType.DEFAULT, "If the project-number is not given, fill in the Project allocator space (re-use allocated CIDs with no assets), otherwise create the next available CID at the end of the CID pool. Defaults to true.", 0, 1));
		addInterfaceDefn(_defn);
	}

	public static void addInterfaceDefn(Interface defn) {
		defn.add(new Interface.Element("namespace", StringType.DEFAULT,
						"The namespace in which to create this project. Defaults to 'pssd'.", 0, 1));
		defn.add(new Interface.Element("name", StringType.DEFAULT,
				"The name of this project.", 0, 1));
		defn.add(new Interface.Element("description", StringType.DEFAULT,
				"An arbitrary description for the project.", 0, 1));

		// NB: "method", "member", "role-member"and "data-use" must all match
		// elements in the pssd-project document type.
		// Method meta-data
		Interface.Element me = new Interface.Element("method", XmlDocType.DEFAULT, 
				"Method utilized by this project.  In a federation must be managed by the local server.", 0, Integer.MAX_VALUE);
		me.add(new Interface.Element("id", CiteableIdType.DEFAULT,
				"The identity of the method (must be local to this server).", 1, 1));
		me.add(new Interface.Element("notes", StringType.DEFAULT,
				"Arbitrary notes associated with the use of this method.", 0, 1));
		defn.add(me);

		// Project team user member
		me = new Interface.Element("member", XmlDocType.DEFAULT,
				"User to become a member of this project. In a federation must be local to this server.", 0,
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
		me.add(new Interface.Element("role", new EnumType(new String[] {
				Project.ADMINISTRATOR_ROLE_NAME,
				Project.SUBJECT_ADMINISTRATOR_ROLE_NAME,
				Project.MEMBER_ROLE_NAME, Project.GUEST_ROLE_NAME }),
				"The project-team role bestowed on the user member.", 1, 1));
		me.add(new Interface.Element("data-use",
				new EnumType(new String[] {Project.CONSENT_SPECIFIC_ROLE_NAME,
						Project.CONSENT_EXTENDED_ROLE_NAME,
						Project.CONSENT_UNSPECIFIED_ROLE_NAME }),
						"Specify how this member (only if role is 'member' or 'guest') will use data from this project (defaults to 'specific')",
						0, 1));
		defn.add(me);

		// Project team role member
		me = new Interface.Element("role-member", XmlDocType.DEFAULT,
				"Role to become a member of this project. In a federation must be local to this server.", 0,
				Integer.MAX_VALUE);
		me.add(new Interface.Element("member", StringType.DEFAULT,
				"The role to become a member of the Project.", 0, 1));
		me.add(new Interface.Element("role", new EnumType(new String[] {
				Project.ADMINISTRATOR_ROLE_NAME,
				Project.SUBJECT_ADMINISTRATOR_ROLE_NAME,
				Project.MEMBER_ROLE_NAME, Project.GUEST_ROLE_NAME }),
				"The project-team role bestowed on the role member.", 1, 1));
		me.add(new Interface.Element("data-use",
				new EnumType(new String[] {
						Project.CONSENT_SPECIFIC_ROLE_NAME,
						Project.CONSENT_EXTENDED_ROLE_NAME,
						Project.CONSENT_UNSPECIFIED_ROLE_NAME }),
						"Specify how this member (only if role is 'member' or 'guest') will use data from this project (defaults to 'specific')",
						0, 1));
		defn.add(me);

		// Project data-use
		me = new Interface.Element(
				"data-use",
				new EnumType(new String[] { Project.CONSENT_SPECIFIC_ROLE_NAME,
						Project.CONSENT_EXTENDED_ROLE_NAME,
						Project.CONSENT_UNSPECIFIED_ROLE_NAME }),
				"Specifies the type of consent for the use of data for this project: 1) 'specific' means use the data only for the original specific intent, 2) 'extended' means use the data for related projects and 3) 'unspecified' means use the data for any research",
				1, 1);
		defn.add(me);

		//
		me = new Interface.Element("meta", XmlDocType.DEFAULT,
				"Optional metadata - a list of asset documents.", 0, 1);
		me.setIgnoreDescendants(true);

		defn.add(me);
	}

	public String name() {
		return "om.pssd.project.create";
	}

	public String description() {
		return "Creates a PSSD Project on the local server.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {
		
		// Projects are always created on the local server
		String proute = null;
		String projectRoot = nig.mf.pssd.plugin.util.CiteableIdUtil.projectIDRoot(executor(), proute);
		DistributedAsset dProjectRoot = new DistributedAsset(proute, projectRoot);
		
		// Creator must have project creation role..
		ModelUser.checkHasRole(new ServerRoute(proute), executor(), Role.projectCreatorRoleName());

		// Validate inputs before we start creating roles and assets...
		// Exception if no good	
		checkMembersExist(args);		
		checkMethodsLocal(executor(), args);

		// If the user does not give project-number,  we may want to fill in 
		// any holes in the allocator space for Projects as sometimes we use 
		// large numbers for 'service' activities. 
        boolean fillIn = args.booleanValue("fillin", true);
		long projectNumber = args.longValue("project-number", -1);
			
		// Generate CID, filling in allocator space if desired
		String cid = nig.mf.pssd.plugin.util.CiteableIdUtil.generateCiteableID(executor(), dProjectRoot, projectNumber, fillIn);

		// Create the team-member and the data re-use (consent) roles locally
		Project.createProjectRoles(executor(), cid);

		// Create the project and grant the Project team their Project roles
		try {
			createProjectAsset(executor(), args, cid);
		} catch (Throwable t) {

			// Cleanup project and roles and rethrow
			cleanUp(executor(), cid);
			throw t;
		}

		// Grant access to 'self' to the project we just created. It is debatable
		// whether this should be done as the creator may not be part of the project
		// team (e.g. system:manager operating on behalf of someone else). So I
		// might remove this in favour of only the members specified in the "member" list
		grantProjectAdminRole(cid);

		w.add("id", cid);
		
//		SystemEventChannel.generate(new PSSDObjectEvent(Action.create, cid));
	}

	/**
	 * Checks all of the specified project members already exist.
	 * 
	 * @param args
	 * @throws Throwable
	 */
	private void checkMembersExist(XmlDoc.Element args) throws Throwable {

		// Check user members first
		{
			Collection<XmlDoc.Element> members = args.elements("member");

			if (members != null) {
				for (XmlDoc.Element me : members) {

					// Authority will be absent for local accounts and present
					// for accounts from other authorities.  In the PSSD implementation
					// the local server will require accounts from other authorities
					// so we can always check locally.
					XmlDoc.Element authority = me.element("authority");
					String domain = me.value("domain");
					String user = me.value("user");

					XmlDocMaker dm = new XmlDocMaker("args");
					if (authority!=null) dm.add(authority);
					dm.add("domain", domain);
					dm.add("user", user);

					// user.exists won't find LDAP users
					XmlDoc.Element r = executor().execute("authentication.user.exists", dm.root());
					if (!r.booleanValue("exists")) {
						throw new Exception(
								"Cannot add project member: the domain ("
										+ domain + ") and/or user (" + user
										+ ") does not exist.");
					}
				}
			}
		}

		// Now check the role members
		{
			Collection<XmlDoc.Element> members = args.elements("role-member");

			if (members != null) {
				for (XmlDoc.Element me : members) {
					String member = me.value("member");
					XmlDocMaker dm = new XmlDocMaker("args");
					dm.add("role", member);
					XmlDoc.Element r = executor().execute(
							"authorization.role.exists", dm.root());
					if (!r.booleanValue("exists")) {
						throw new Exception(
								"Cannot add project member-role: the member-role ("
										+ member + ") does not exist.");
					}
				}
			}
		}
	}

	
	/**
	 * Checks that the specified Methods are managed (i.e. they are primary) by the local (executing)
	 * server (where we are creating the Project).
	 * 
	 * @param args
	 * @throws Throwable
	 */
	private void checkMethodsLocal (ServiceExecutor executor, XmlDoc.Element args) throws Throwable {

		Collection<XmlDoc.Element> methods = args.elements("method");
		if (methods != null) {
			for (XmlDoc.Element me : methods) {
				String id = me.value("id");
				Method.isMethodLocal(executor, id);           // Exception if not
			}
		}
	}

	public static void createProjectAsset(ServiceExecutor executor, XmlDoc.Element args, 
			String cid) throws Throwable {
		String ns = args.stringValue("namespace", "pssd");

		XmlDocMaker dm = new XmlDocMaker("args");
		if (cid!=null) dm.add("cid", cid);

		dm.add("namespace", new String[] { "create", "true" }, ns);
		dm.add("model", Project.MODEL);

		dm.push("meta");

		// Set the standard PSSD object meta-data (matches Doc Type
		// "pssd-object")
		PSSDUtils.setObjectMeta(dm, Project.TYPE, args.value("name"), args.value("description"));

		// Set the generic meta-data
		PSSDUtils.setObjectOptionalMeta(dm, args.element("meta"), "om.pssd.project");

		// Get the project 'data-use' element that has been set. We use this to
		// check the validity of the member 'data-use' values
		String projectDataUse = args.value("data-use"); // Required element

		// Fetch the 'method' meta-data from the user
		Collection<XmlDoc.Element> methods = args.elements("method");
		
		// Fetch the member meta-data from the user
		Collection<XmlDoc.Element> members = args.elements("member");
		Collection<XmlDoc.Element> roleMembers = args.elements("role-member");

		if (methods != null || members != null || roleMembers != null) {
			dm.push("pssd-project");

			// Prepare method meta-data for Project
			if (methods != null) {
				for (XmlDoc.Element me : methods) {
					// Check Method exists
					String id = me.value("id");
					String proute = null;
					String pdist = "0";
					Boolean pssdOnly = true;
					if (!DistributedAssetUtil.assetExists(executor, proute, pdist, id, ResultAssetType.primary, false, pssdOnly, null)) {
						throw new Exception ("The Method with cid '" + id + "' does not exist");
					}
					dm.add(me);
				}
			}
			
			
			// Grant project roles to user members
			if (members != null) {
				for (XmlDoc.Element me : members) {
					UserCredential userCred = new UserCredential(me.value("authority"), me.value("authority/@protocol"),  me.value("domain"), me.value("user"));
					
					// Set defaults or over-rides for team-member's 'data-use'.
					ProjectMember.setValidProjectMemberDataUse(projectDataUse, me);

					// Grant the hierarchical team role to the user-member
					Project.grantProjectRole(executor, cid, userCred, me.value("role"), false);   // We want an error if the role has not been created

					// Grant data-use role to non  admin user-members
					// The "data-use" field will be null for admins.
					Project.grantProjectRole(executor, cid, userCred, me.value("data-use"), false);
				}
			}

			// Grant project roles to role members
			if (roleMembers != null) {
				for (XmlDoc.Element me : roleMembers) {

					// Set defaults or over-rides for team-member's 'data-use'.
					ProjectMember.setValidProjectMemberDataUse(projectDataUse, me);

					// Grant the hierarchical team role to the role-member
					Project.grantProjectRole(executor, cid,
							me.value("member"), me.value("role"), false);

					// Grant data-use role to non p-admin role-members
					Project.grantProjectRole(executor, cid,
							me.value("member"), me.value("data-use"), false);
				}
			}

			if (projectDataUse != null) {
				dm.add("data-use", projectDataUse);
			}

			dm.pop();
		}
		dm.pop();

		// Add ACLs to the Project roles
		PSSDUtils.addProjectACLs(dm, cid);

		// Finally create the Project
		executor.execute("asset.create", dm.root());
	}

	private void grantProjectAdminRole(String cid) throws Throwable {
		PSSDUtils.grantRoleToSelf(executor(), Project.projectAdministratorRoleName(cid));
	}

	/**
	 * Cleanup after failure
	 * 
	 * @param executor
	 * @param cid
	 * @throws Throwable
	 */
	private void cleanUp(ServiceExecutor executor, String cid) throws Throwable {

		// Destroy the asset if it exists. This service will destroy any
		// project associated roles too. Else just destroy the roles.
		// There is no need to revoke roles as well

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", cid);
		XmlDoc.Element r = executor.execute("om.pssd.object.exists", dm.root());
		if (r.booleanValue("exists")) {
			r = executor.execute("om.pssd.object.destroy", dm.root());
		} else {
			Project.destroyRoles(executor, cid);
		}
	}

}
