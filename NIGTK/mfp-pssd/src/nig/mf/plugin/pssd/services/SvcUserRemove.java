package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.ModelUser;
import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.ProjectMember;
import nig.mf.plugin.pssd.user.Authority;
import nig.mf.plugin.pssd.user.UserCredential;
import nig.mf.plugin.pssd.util.PSSDUtils;

import java.util.Collection;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import arc.xml.XmlDoc.Element;

public class SvcUserRemove extends PluginService {

	private Interface _defn;

	public SvcUserRemove() throws Throwable {
		_defn = new Interface();
		Interface.Element ie = new Interface.Element("authority",StringType.DEFAULT,"The authority of interest. Defaults to local.",0,1);
		ie.add(new Interface.Attribute("protocol", StringType.DEFAULT,
				"The protocol of the identity authority. If unspecified, defaults to federated user within the same type of repository.", 0));
		_defn.add(ie);
		_defn.add(new Interface.Element("domain", StringType.DEFAULT, "The name of the domain.", 1, 1));
		_defn.add(new Interface.Element("user", StringType.DEFAULT, "The name of the user.", 1, 1));
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public Interface definition() {
		return _defn;
	}

	public String description() {
		return "Revokes a local user's direct and indirect access to all local PSSD projects.";
	}

	public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
		XmlDoc.Element authority = args.element("authority");
		String domain = args.value("domain");
		String user = args.value("user");

		// Executing user must have overall admin role to run this service
		if (!ModelUser.checkHasRoleNoThrow(null, executor(), PSSDUtils.OBJECT_ADMIN_ROLE_NAME)) {
			throw new Exception ("Caller must hold the global administrator role.");
		}

		// See if user exists
		if (!userExists(executor(), authority, domain, user)) {
			throw new Exception ("The specified user does not exist");
		}
				
		// Do it
		removeProjectMember(executor(), authority, domain, user, w);
	}

	public String name() {
		return "om.pssd.user.remove";
	}


	public static void removeProjectMember(ServiceExecutor executor, XmlDoc.Element authority, String domain, String user, XmlWriter w) throws Throwable {
		
		
		// Find the local Projects that this user is a member of
		UserCredential cred = new UserCredential (Authority.instantiate(authority), domain, user);
		ProjectMember pM = new ProjectMember(cred);
		Collection<Project.ProjectCIDAndRole> projects = pM.projectsAccessed (executor);
		if (projects==null) return;
		
		// Iterate through projects and remove user's roles for each project
		// Note that if a user holds a role through inheritance, rather than directly,
		// this removal will have no effect on that user.   The inherited role
		// would have to be removed.
		for (Project.ProjectCIDAndRole project : projects) {
				String projectCid = project.projectId();
				removeProjectMember(executor, authority, domain, user, projectCid, w);
		}
		
		// Remove any role-member roles that they may hold
		XmlDoc.Element r = executor.execute("om.pssd.role-member-registry.list");
		if (r!=null) {		
			// Just revoke the user's access to all role-members. There won't be many and if the user doesn't hold it it's fine
			Collection<String> roleMembers = r.values("role");			
			SvcUserRoleGrant.grantRevoke(executor, authority, domain, user, roleMembers, null, w, false);
		}
	}


	// Private functions
	
	/**
	 * Remove a user from one project
	 */
	private static void removeProjectMember(ServiceExecutor executor, XmlDoc.Element authority,
			String domain, String user, String projectCid, XmlWriter w) throws Throwable {

		// Prepare
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", projectCid);
		doc.push("member");
		if (authority!=null) doc.add(authority);
		doc.add("domain", domain);
		doc.add("user", user);
		doc.pop();
		
		// Do it
		XmlDoc.Element r = executor.execute("om.pssd.project.members.remove", doc.root());
		if (r!=null) w.add(r);
	}


	
	private static boolean userExists(ServiceExecutor executor, XmlDoc.Element authority,
			String domain, String user) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		if (authority!=null) dm.add(authority);
		dm.add("domain", domain);
		dm.add("user", user);
		XmlDoc.Element r = executor.execute("user.exists", dm.root());
		return r.booleanValue("exists");
	}

}
