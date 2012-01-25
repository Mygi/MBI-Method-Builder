package nig.mf.plugin.pssd.util;

import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.user.Authority;
import nig.mf.plugin.pssd.user.UserCredential;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import arc.mf.plugin.PluginThread;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.*;

/**
 * Class to enable message to be sent to project members when new data arrived
 * for their project. A variety of ways to select users for email are provided:
 * 
 * 1) Via Project role. Users (user members and de-referenced role-members) who
 * hold the given role can be emailed. The role can be set either explicitly or
 * implicitly (hierarchically). 2) User- and role members to email can also be
 * set directly. 3) email addresses (whether in Project team or not) can also be
 * directly supplied 4) The pssd-notifications meta-data, if set on a Project
 * can also be used to create the list of users to email.
 * 
 * The class accumulates users as they are added via the various categories and
 * all users then receive an email. Degenerate addresses are filtered out.
 * 
 * @author nebk
 * 
 */
public class MailHandler {
	private String _message = null;
	private String _subject = null;
	private String _cid = null;
	private Collection<XmlDoc.Element> _users = null;
	private Vector<String> _emails = null;
	private ServiceExecutor _thread = null;

	/**
	 * Constructor
	 * 
	 * @param cid
	 *            Project CID
	 * @param subject
	 *            : mail message subject
	 * @param message
	 *            mail message body
	 */
	public MailHandler(String cid, String subject, String message) throws Throwable {

		_cid = cid;
		if (subject == null) {
			throw new Exception("You must supply a Subject");
		}
		_subject = subject;
		_message = message;
		_thread = PluginThread.serviceExecutor();
		reset();
	}

	/**
	 * Reset the internals to empty lists of users and emails.
	 */
	public void reset() {

		_users = new Vector<XmlDoc.Element>();
		_emails = new Vector<String>();
	}

	/**
	 * Add a project role to the list of roles to send an email to. Both user
	 * members and de-referenced (to users) role members are handled.
	 * 
	 * @param projectRole
	 *            : the desired project member role to send the message to. Must
	 *            be one of Project.ADMINISTRATOR_ROLE_NAME,
	 *            Project.SUBJECT_ADMINISTRATOR_ROLE_NAME,
	 *            Project.MEMBER_ROLE_NAME, and Project.GUEST_ROLE_NAME
	 * @param isExclusive
	 *            Ihe member role is held exclusively or inclusively (e.g.
	 *            giving member would also send an email to all admins).
	 */
	public void addProjectRole(String projectRole, boolean isExclusive) throws Throwable {

		if (projectRole == null)
			return;

		// Get users that have this project role (includes user members and
		// dereferenced role-members)
		int memberType = 2;
		String proute = null; // LOcal projects only
		boolean showDetail = false;
		boolean deRef = true;
		Collection<XmlDoc.Element> members = Project.membersWithProjectRole(_thread, proute, _cid, projectRole,
				isExclusive, memberType, deRef, showDetail);
		if (members == null)
			return;
		_users.addAll(members);
	}

	/**
	 * Add a specific user to send email to.
	 * 
	 * @param authority
	 *            null for local authority
	 * @param protocol
	 *            for authority
	 * @param domain
	 *            Must be supplied
	 * @param user
	 *            Must be supplied
	 * @throws Throwable
	 */
	public void addUserMember(String authority, String protocol, String domain, String user) throws Throwable {

		if (domain != null && user != null) {

			// We use a flat structure of attributes because this is what
			// Project.membersWithProjectRole
			// returns. The authority is coded as two attributes 'authority' and
			// 'protocol'
			// rather than an XmlElement with protocol attribute
			XmlDoc.Element el = new XmlDoc.Element("member");
			if (authority != null)
				el.add(new XmlDoc.Attribute("authority", authority));
			if (protocol != null)
				el.add(new XmlDoc.Attribute("protocol", protocol));
			el.add(new XmlDoc.Attribute("domain", domain));
			el.add(new XmlDoc.Attribute("user", user));
			_users.add(el);
		}
	}

	/**
	 * Add the users that the given role-member de-references to. Send these
	 * users email
	 * 
	 * @param roleMember
	 * @throws Throwable
	 */
	public void addRoleMember(String roleMember) throws Throwable {

		// Find all of the users that this role dereferences to
		Collection<XmlDoc.Element> users = Project.dereferenceUsersFromRole(_thread, roleMember, null);
		if (users != null)
			_users.addAll(users);
	}

	/**
	 * Adds the users to email who have been specified in the standard
	 * pssd-notification meta-data if it has been set on the Project
	 * 
	 * @param forDataUpload
	 *            If true, looks at the 'data-upload' part of pssd-notification.
	 *            If false, nothing is currently implemented. Probably need to
	 *            turn this into an enum.
	 * @throws Throwable
	 */
	public void addNotificationUsers(boolean forDataUpload) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", _cid);
		XmlDoc.Element r = _thread.execute("om.pssd.object.describe", dm.root());
		XmlDoc.Element notif = r.element("object/meta/pssd-notification");
		if (notif == null)
			return;

		if (forDataUpload) {
			XmlDoc.Element du = notif.element("data-upload");
			if (du == null)
				return;

			// Project roles
			Collection<XmlDoc.Element> roles = du.elements("project-role");
			if (roles != null) {
				for (XmlDoc.Element role : roles) {
					if (role != null) {
						addProjectRole(role.value(), role.booleanValue("@explicit"));
					}
				}
			}

			// Project user members
			Collection<XmlDoc.Element> members = du.elements("member");
			if (members != null) {
				for (XmlDoc.Element userMember : members) {
					addUserMember(userMember.value("authority"), userMember.value("protocol"),
							userMember.value("domain"), userMember.value("user"));
				}
			}

			// Project role members
			Collection<XmlDoc.Element> roleMembers = du.elements("role-member");
			if (roleMembers != null) {
				for (XmlDoc.Element el : roleMembers) {
					String roleMember = el.value();
					addRoleMember(roleMember);
				}
			}

			// Email addresses
			Collection<XmlDoc.Element> emails = du.elements("email");
			if (emails != null) {
				for (XmlDoc.Element el : emails) {
					String email = el.value();
					addEMailMember(email);
				}
			}
		}
	}

	/**
	 * Add a user directly via their email address.
	 * 
	 * @param email
	 */
	public void addEMailMember(String email) {

		_emails.add(email);
	}

	/**
	 * Send the message
	 * 
	 * @param executor
	 * @throws Throwable
	 */
	public void sendMessage(boolean async) throws Throwable {

		if (_cid == null)
			return;

		// Make a hashmap so we can keep track of which users we have
		// already sent a message to.
		HashMap<String, Boolean> sent = new HashMap<String, Boolean>();

		// Iterate over users
		if (_users != null) {
			for (XmlDoc.Element el : _users) {
				String authority = el.value("@authority");
				String protocol = el.value("@protocol");
				String user = el.value("@user");
				String domain = el.value("@domain");
				//
				String email = getUserEMail(_thread, authority, protocol, domain, user);
				if (email != null) {
					if (!sent.containsKey(email)) {
						sendMessage(_thread, email, _subject, _message, async);
						sent.put(email, true);
					}
				}
			}
		}

		// Iterate over email users
		if (_emails != null) {
			for (String email : _emails) {
				if (email != null) {
					if (!sent.containsKey(email)) {
						sendMessage(_thread, email, _subject, _message, async);
						sent.put(email, true);
					}
				}
			}
		}

	}

	/**
	 * Useful utility. Send mail to all implcitly held subject admins
	 * 
	 * @param executor
	 * @param projectId
	 * @param subject
	 * @param msg
	 * @throws Throwable
	 */
	public static void sendAdminMessage(ServiceExecutor executor, String projectId, String subject, String msg)
			throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", projectId);
		dm.add("subject", subject);
		dm.add("message", msg);
		dm.add("project-role", new String[] { "explicit", "false" }, "subject-administrator");
		dm.add("async", true);
		executor.execute("om.pssd.project.mail.send", dm.root());
	}

	// Private functions

	private String getUserEMail(ServiceExecutor executor, String authority, String protocol, String domain, String user)
			throws Throwable {

		// Get user details and email
		String email = null;
		if (domain != null && user != null) {
			XmlDocMaker dm = new XmlDocMaker("args");
			if (authority != null) {
				dm.add(Authority.toXmlElement(authority, protocol));
			}
			dm.add("domain", domain);
			dm.add("user", user);
			XmlDoc.Element r = executor.execute("user.describe", dm.root());

			// See if we can find email address; newer accounts put it in
			// higher-level structure
			email = r.value("user/e-mail");
			if (email == null)
				email = r.value("user/asset/meta/mf-user/email");
		}
		return email;
	}

	private void sendMessage(ServiceExecutor executor, String email, String subject, String msg, boolean async)
			throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("to", email);
		dm.add("subject", subject); // Required by mail.send
		if (msg != null)
			dm.add("body", msg);
		dm.add("async", async);
		System.out.println("sending email to " + email + " with subject " + subject);
		executor.execute("mail.send", dm.root());
	}
}
