package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.util.MailHandler;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.mf.plugin.dtype.BooleanType;

import arc.xml.XmlDoc;
import arc.xml.XmlWriter;


/**
 * Send email to local Project team members via various mechanisms.
 * 
 * @author nebk
 *
 */
public class SvcProjectMailSend extends PluginService {
	private Interface _defn;

	public SvcProjectMailSend() {

		_defn = new Interface();
		//
		_defn.add(new Element("id", CiteableIdType.DEFAULT, "The  citable ID of the Project.",1,1));
		//
		_defn.add(new Element("subject", StringType.DEFAULT, "The subject of the message.",1,1));
		_defn.add(new Element("message", StringType.DEFAULT, "The body of the message.",1,1));
		//
		Interface.Element me = new Interface.Element("member", XmlDocType.DEFAULT,
				"User member to send email to.", 0, Integer.MAX_VALUE);
		//
		Interface.Element ie = new Interface.Element("authority",StringType.DEFAULT,"The authority of interest for users. Defaults to all.",0,1);
		ie.add(new Interface.Attribute("protocol", StringType.DEFAULT,
				"The protocol of the identity authority. If unspecified, defaults to federated user within the same type of repository.", 0));
		me.add(ie);
		//
		me.add(new Interface.Element("domain", StringType.DEFAULT,
				"The domain name of the member.", 0, 1));
		me.add(new Interface.Element("user", StringType.DEFAULT,
				"The user name within the domain.", 0, 1));
		_defn.add(me);
		//
		me = new Interface.Element("role-member", StringType.DEFAULT,
				"Role-member to send email to.", 0, Integer.MAX_VALUE);
		_defn.add(me);
		//
		me = new Interface.Element("project-role", new EnumType(new String[] {
				Project.ADMINISTRATOR_ROLE_NAME,
				Project.SUBJECT_ADMINISTRATOR_ROLE_NAME,
				Project.MEMBER_ROLE_NAME, Project.GUEST_ROLE_NAME }),
				"Send email to project members (user or role) that hold this Project role.", 0, Integer.MAX_VALUE);
		me.add(new Interface.Attribute("explicit", BooleanType.DEFAULT,
				"If true (default), the user must hold the role explicitly. If false, the user may hold the role implicitly (e.g. admin holds member)", 0));
		_defn.add(me);	
		//
		me = new Interface.Element("email", StringType.DEFAULT,
				"Direct email address to send messaeg to (does not have to be project team member).", 0, Integer.MAX_VALUE);
		_defn.add(me);
		//
		me = new Interface.Element("use-notification", BooleanType.DEFAULT,
				"Send email to users according to the pssd-notification meta-data attached to the Project.", 0, 1);
		me.add(new Interface.Attribute("category", new EnumType(new String[] {"data-upload"}),
				"Select the category in pssd-notification to select which users are emailed.", 0));
		_defn.add(me);	
		//
		me = new Interface.Element("async", BooleanType.DEFAULT, "Send message asynchronously (default true).", 0, 1);
		_defn.add(me);

	}

	public String name() {
		return "om.pssd.project.mail.send";
	}

	public String description() {
		return "Send a message to project team members via a variety of mechanisms: 1) by direct user specification and 2) by project role.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		
		String id = args.value("id");
		String subject = args.value("subject");
		String message = args.value("message");
		MailHandler mh = new MailHandler(id, subject, message);

		// See if we have any members to write to based on the given project role
		sendToProjectRoles (executor(), mh, args);
		
		// See if we have any direct user members to write to
		sendToUserMembers (executor(), mh, args);
		
		// See if we have any direct role members to write to
		sendToRoleMembers (executor(), mh, args);
				
		// See if we have any direct email addresses to write to
		sendToEMailMembers (executor(), mh, args);

		// See if we have any Project-specific meta-data specifying who to write to and for what purpose
		sendToNotifications(executor(), mh, args);	

		// Send message
		boolean async = args.booleanValue("async", true);
		mh.sendMessage(async);
	}
	
	
	// Private functions
	
	private void sendToProjectRoles (ServiceExecutor executor, MailHandler mh, XmlDoc.Element args) throws Throwable {
		Collection<XmlDoc.Element> members = args.elements("project-role");
		if (members != null) {
			for (XmlDoc.Element me : members) {
			    mh.addProjectRole(me.value(), me.booleanValue("@explicit", true));
			}
		}
}

	private void sendToUserMembers (ServiceExecutor executor, MailHandler mh, XmlDoc.Element args) throws Throwable {

		Collection<XmlDoc.Element> members = args.elements("member");

		if (members != null) {
			for (XmlDoc.Element me : members) {

				// Add individual users
				XmlDoc.Element authority = me.element("authority");
				String domain = me.value("domain");
				String user = me.value("user");
				if (user!=null && domain!=null) {
					mh.addUserMember(authority.value(), authority.value("@protocol"), domain, user);
				}
			}
		}
	}
	
	private void sendToRoleMembers (ServiceExecutor executor, MailHandler mh, XmlDoc.Element args) throws Throwable {

		
		Collection<String> members = args.values("role-member");
		if (members != null) {
			for (String roleMember : members) {
				mh.addRoleMember(roleMember);
			}
		}
	}

	private void sendToEMailMembers (ServiceExecutor executor, MailHandler mh, XmlDoc.Element args) throws Throwable {

		
		Collection<String> emails = args.values("email");
		if (emails != null) {
			for (String email : emails) {
				mh.addEMailMember(email);
			}
		}
	}
	
	private void sendToNotifications (ServiceExecutor executor, MailHandler mh, XmlDoc.Element args) throws Throwable {
		Boolean useNotification = args.booleanValue("use-notification", false);
		
		if (useNotification) {
			String category = args.stringValue("use-notification/@category", "data-upload");   

			// Add users in desired notification category
			if (category.equals("data-upload")) {
				mh.addNotificationUsers(true);
			} else {
				// Nothing yet
			}
		}
	}
}