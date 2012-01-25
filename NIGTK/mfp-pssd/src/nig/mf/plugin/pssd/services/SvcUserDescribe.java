package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.*;
import nig.mf.plugin.pssd.user.Authority;
import nig.mf.plugin.pssd.user.UserCredential;

import java.util.Collection;

import arc.mf.plugin.*;
import arc.xml.*;
import arc.mf.plugin.dtype.*;

public class SvcUserDescribe extends PluginService {


	private Interface _defn;

	// TODO: The interface needs to be rationalized so that specific role members as well as
	// user members can be described. It will list all role members at present if type=role
	// and ignore what is specified in the user elements...

	public SvcUserDescribe() {
		_defn = new Interface();
		Interface.Element ie = new Interface.Element("authority",StringType.DEFAULT,"The authority of interest for users. Defaults to all.",0,1);
		ie.add(new Interface.Attribute("protocol", StringType.DEFAULT,
				"The protocol of the identity authority. If unspecified, defaults to federated user within the same type of repository.", 0));
		_defn.add(ie);
		//
		_defn.add(new Interface.Element("domain",StringType.DEFAULT,"The authentication domain for users. Defaults to all.",0,1));
		_defn.add(new Interface.Element("user",StringType.DEFAULT,"The user in the authentication domain to list. Defaults to all",0,1));
		_defn.add(new Interface.Element("ignore-system", BooleanType.DEFAULT, "Ignore SYSTEM domain users ?  Defaults to true.", 0, 1));
		_defn.add(new Interface.Element("type",StringType.DEFAULT,"The type of user, 'user' (default) or 'role' (i.e. roles that may be project members) to list",0,1));
		_defn.add(new Interface.Element("list-projects",BooleanType.DEFAULT,"List the (local only) projects to which the user(s) have access?",0,1));
	}

	public String name() {
		return "om.pssd.user.describe";
	}

	public String description() {
		return "Returns information on local users that have been registered as users of the object model.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	/*
	public int executeMode() {
		return EXECUTE_DISTRIBUTED_ALL;
	}
	 */



	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		// Parse arguments
		boolean projects = args.booleanValue("list-projects",false);
		String userType = args.value("type");
		if (userType == null) userType = "user";
		if ( !(userType.equals("user") || userType.equals("role")) ) {
			throw new Exception ("type must be 'user' or 'role'");
		}
		

		// User		
		Boolean ignoreSystem = args.booleanValue("ignore-system", true);
		XmlDoc.Element authority = args.element("authority");
		String domain = args.value("domain");
		String user = args.value("user");
		if (user != null) {
			if (domain == null) {
				throw new Exception("Both user and domain must be given if user is given");
			}
		}

		//Handle role-members; domain is irrelevant
		if (userType.equals("role")) {

			// Find the roles that have been registered for use as project role-members			
			XmlDoc.Element r = executor().execute("om.pssd.role-member-registry.list");

			// Now list them
			Collection<XmlDoc.Element> roleUsers = r.elements("role");
			if (roleUsers != null) {
				for (XmlDoc.Element roleUser : roleUsers) {
					String role = roleUser.value();
					describeRoleMember(executor(),w,role,projects);
				}
			}
		} else if (userType.equals("user")) {
			
			XmlDocMaker dm = new XmlDocMaker("args");
			if (authority!=null) dm.add(authority);
			if (domain!=null) dm.add("domain", domain);
			if (user!=null) dm.add("user", user);
			dm.add("size", "infinity");
			dm.add("role",new String[] { "type", "role" },ModelUser.modelUserRoleName());
			XmlDoc.Element r = executor().execute("user.describe", dm.root());
			
			// Describe in the way we want
			Collection<XmlDoc.Element> users = r.elements("user");
			if ( users != null ) {

				// Iterate over users and add detail as required
				for (XmlDoc.Element u : users) { 
					describe(executor(),w,u,projects,ignoreSystem);
				}
			}

		}	
	}

	private void describe(ServiceExecutor executor,XmlWriter w,XmlDoc.Element ue,boolean projects, boolean ignoreSystem) throws Throwable {
		
		// Format
		Authority authority = new Authority( ue.stringValue("@authority"), ue.stringValue("@protocol"));
		String domain = ue.value("@domain");
		if (domain.equalsIgnoreCase("system") && ignoreSystem) return;
		//
		String user   = ue.value("@user");
		String id = ue.value("@id");	
		w.push("user",new String[] { "id", id, "authority", authority.name(), "protocol", authority.protocol(), "domain", domain, "user", user});


		// Add generic roles
		ServerRoute route = null;
		{
			
			if ( ModelUser.hasRole(route, executor,  authority, domain, user, Role.projectCreatorRoleName()) ) {
				w.add("role",Role.PROJECT_CREATOR_ROLE_NAME);
			}

			if ( ModelUser.hasRole(route, executor,  authority, domain,user,Role.subjectCreatorRoleName()) ) {
				w.add("role",Role.SUBJECT_CREATOR_ROLE_NAME);
			}
		}

		// email may be in top-level (user/e-mail) or in mf-user/email
		String email = ue.stringValue("e-mail");
		if (email==null) email = ue.stringValue("asset/meta/mf-user/email");
		if (email!=null) w.add("email", email);      // Standardize on email

		// Name may be in two places also.
		// user/name or mf-user/name (multiples)
		// Settle on an attribute style presentation
		addName (ue.value("name"), ue.elements("asset/meta/mf-user/name"), w);

		// Add local projects accessed by this user
		if ( projects ) {
			UserCredential cred = new UserCredential (authority, domain, user);
			ProjectMember pM = new ProjectMember(cred);
			Collection<Project.ProjectCIDAndRole> ps = pM.projectsAccessed (executor);
			if ( ps != null ) {
				for (Project.ProjectCIDAndRole pur : ps) {
					w.add("project",new String[] { "role", pur.role() },pur.projectId());
				}
			}
		}
		w.pop();
	}


	private  void describeRoleMember (ServiceExecutor executor, XmlWriter w, String roleMember, boolean projects) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("type", "role");
		dm.add("name", roleMember);
		XmlDoc.Element r = executor.execute("actor.describe", dm.root());

		// "id" and "member" consistent with om.pssd.object.describe usage on a Project object
		if (r != null) {
			w.push("role-user",new String[] { "id", r.value("actor/@id"), "member", roleMember});
		}	

		// Add local projects
		if ( projects ) {
			ProjectMember pM = new ProjectMember(roleMember);
			Collection<Project.ProjectCIDAndRole> ps = pM.projectsAccessed (executor);
			if ( ps != null ) {
				for (Project.ProjectCIDAndRole pur : ps) {
					w.add("project",new String[] { "role", pur.role() },pur.projectId());
				}
			}
		}

		//
		w.pop();
	}
	
	
	/**
	 * Compact various name possibilities into one structure
	 * 
	 * @param name
	 * @param names
	 * @param w
	 * @throws Throwable
	 */
	private void addName (String name, Collection<XmlDoc.Element> names, XmlWriter w) throws Throwable {
		
		// Use the names elements in preference to name
		if (names!=null) {
			for (XmlDoc.Element n : names) {
				w.add(n);
			}
		} else if (name!=null) {
			// Split into components with heuristic
			String[] t = name.split(" ");
			int n = t.length;
			if (n==1) {
				w.add("name", new String[]{"type", "first"}, t[0]);
			} else if (n==2) {
				w.add("name", new String[]{"type", "first"}, t[0]);
				w.add("name", new String[]{"type", "last"}, t[1]);
			} else if (n>2) {
				w.add("name", new String[]{"type", "first"}, t[0]);
				for (int i=1; i<(n-1); i++) {
					w.add("name", new String[]{"type", "middle"}, t[i]);
				}
				w.add("name", new String[]{"type", "last"}, t[n-1]);			
			}
		}	
	}
}
