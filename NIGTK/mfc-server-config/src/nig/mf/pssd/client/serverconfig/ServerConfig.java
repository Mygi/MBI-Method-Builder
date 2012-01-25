package nig.mf.pssd.client.serverconfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;

import nig.io.EraserThread;
import nig.mf.client.util.ClientConnection;

import arc.mf.client.ServerClient;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;


public class ServerConfig {



	public static void main(String[] args) throws Throwable {

		// Connect to server
		ServerClient.Connection cxn = ClientConnection.createServerConnection();	

		// Authenticate
		ClientConnection.interactiveAuthenticate(cxn, "system", "manager");

		// Check we have PSSD package
		if (!packageExists(cxn, "PSSD")) {
			throw new Exception ("The PSSD package has not been installed.  You must install it first");
		}

		//
		System.out.println("\n\n Enter <CR> to retain the current/default value in []\n\n\n");

		// Server Identity
		String serverName = getSetServerIdentity (cxn);

		// JVM Data Model
		getSetProperty (cxn, "JVM Data Model (32/64 bit)", "jvm.data.model", "64");

		// JVM Max memory
		getSetProperty (cxn, "JVM Maximum Memory (MByte)", "jvm.memory.max", "1024");

		// JVM Perm Gen Max Memory
		getSetProperty (cxn, "JVM Permanent Generation Memory Size (MByte)", "jvm.memory.perm.max", "512");

		// SMTP Host
		String smtpHost = getSetProperty (cxn, "Mail SMTP host", "mail.smtp.host", null);

		// SMTP port
		getSetProperty (cxn, "Mail SMTP port", "mail.smtp.port", "25");

		// Mail from
		getSetProperty (cxn, "Mail from string when mail.send service is used", "mail.from", "mail@"+smtpHost);

		// Notifications
		getSetProperty (cxn, "Notifications from address when notifications framework used", "notification.from", "do-not-reply-"+serverName+"@"+smtpHost);

		// Default user domain
		getSetUserDomain (cxn);

		// DICOM namespace and store
		getSetDicomNameSpace (cxn);

		// DICOM proxy user domain
		String dicomDomain = getSetDicomDomain (cxn);


		// DICOM proxy users
		getSetDicomUsers (cxn, dicomDomain);

		// Add notifications
		getSetDicomNotifications (cxn);
	}


	// SUpport functions

	private static String getSetServerIdentity (ServerClient.Connection cxn) throws Throwable {
		try {
			XmlDoc.Element r = getServerIdentity (cxn);
			String name = r.value("server/name");
			String org = r.value("server/organization");
			System.out.println("***************");
			System.out.println("Server Identity");
			System.out.println("***************");
			String newName = EraserThread.readString ("Server name ["+name+"]:");
			if (newName.length()==0) newName = name;
			String newOrg = EraserThread.readString ("Server organization ["+org+"]:");
			if (newOrg.length()==0) newOrg = org;
			setServerIdentity (cxn, newName, newOrg);
			System.out.println("\n\n\n");
			return newName;
		} catch (Throwable t) {
			System.out.println(t.getMessage());
			System.out.println("\n\n\n");
			return null;
		}
	}


	private static String getSetProperty (ServerClient.Connection cxn, String label, String property, String propertyDefaultValue) throws Throwable {

		// t will be null if exception (handled)
		String  t = getServerProperty (cxn, property, propertyDefaultValue);

		try {
			int l = label.length();
			for (int i=0; i<l; i++) System.out.print("*");
			System.out.print("\n");
			System.out.println(label);
			for (int i=0; i<l; i++) System.out.print("*");
			System.out.print("\n");
			//
			String newT = null;
			if (t==null) {
				newT = EraserThread.readString (property+" []:");
			} else {
				newT = EraserThread.readString (property+" ["+t+"]:");
			}
			if (newT.length()==0) newT = t;
			//
			if (newT==null) {
				// The property did not exist, there was no default and the user entered an empty string!
				System.out.println("The property did not pre-exist, there was no default value and you entered an emtpy string");
				System.out.println("\n\n\n");
				return null;
			} else {
				setServerProperty (cxn, property, newT);
				System.out.println("\n\n\n");
				return newT;
			}
		} catch (Throwable e) {
			System.out.println(e.getMessage());
			System.out.println("\n\n\n");
			return null;
		}
	}


	private static void getSetUserDomain (ServerClient.Connection cxn) throws Throwable {

		try {
			XmlDoc.Element r = getDomains (cxn);
			System.out.println("*************************************************");
			System.out.println("Standard authentication domain to create users in");
			System.out.println("*************************************************");
			System.out.print("Existing local domains are : ");
			if (r!=null) {
				Collection<XmlDoc.Element> ds = r.elements();
				for (XmlDoc.Element d : ds) {
					String type = d.value("@type");
					if (type.equals("local")) System.out.print(d.value() + " ");
				}
				System.out.println("\n");
			}

			// We don't want a default domain (can't guess what it might be) and we
			// could be re-running this,.
			String newT = EraserThread.readString ("User domain: pre-exists is OK (<CR> on empty to finish) []:");
			if (newT.length()>0) {
				createLocalDomain (cxn, newT);
			}
			System.out.println("\n\n\n");
		} catch (Throwable t) {
			System.out.println(t.getMessage());
			System.out.println("\n\n\n");
		}
	}


	private static void getSetDicomNameSpace (ServerClient.Connection cxn) throws Throwable {


		try {
			XmlDoc.Element r = getNamespaces (cxn);
			System.out.println("***********************************************************************************");
			System.out.println("Namespace (using pre-existing) associated store to locate DICOM data-model data in.");
			System.out.println("***********************************************************************************");
			if (r!=null) {
				System.out.println("Existing namepsace/store pairs are : ");
				Collection<XmlDoc.Element> ds = r.element("namespace").elements("namespace");  // Children of the parent namespace /
				for (XmlDoc.Element d : ds) {
					String name = d.value("name");
					String store = d.value("store");
					System.out.print(name+"/"+store +" ");
				}
				System.out.println("\n");
			} else {
				System.out.print("There are no existing namepsace/store pairs.\n");
			}
			//
			r = getStores (cxn);
			if (r!=null) {
				System.out.print("Existing stores are : ");
				Collection<XmlDoc.Element> ds = r.elements();
				for (XmlDoc.Element d : ds) {
					String name = d.value("name");
					System.out.print(name+" ");
				}
				System.out.println("\n");
			} else {
				System.out.println("There are no existing stores.\n");

			}

			String ns = "dicom";
			String newNS = EraserThread.readString ("Namespace: pre-exists is OK ["+ns+"]:");
			if (newNS.length()==0) newNS = ns;
			//
			String st = "dicom";
			String newSt = EraserThread.readString ("Store: must pre-exist ["+st+"]:");
			if (newSt.length()==0) newSt = st;
			//		
			createNameSpace(cxn, newNS, "DICOM namespace", newSt);
			System.out.println("\n\n\n");
		} catch (Throwable t) {
			System.out.println(t.getMessage());
			System.out.println("\n\n\n");
		}
	}


	private static String getSetDicomDomain (ServerClient.Connection cxn) throws Throwable {

		try {
			XmlDoc.Element r = getDomains (cxn);
			System.out.println("*********************************************");
			System.out.println("Authentication domain  for proxy DICOM users.");
			System.out.println("*********************************************");
			System.out.print("Existing local domains are : ");
			if (r!=null) {
				Collection<XmlDoc.Element> ds = r.elements();
				for (XmlDoc.Element d : ds) {
					String type = d.value("@type");
					if (type.equals("local")) System.out.print(d.value() + " ");
				}
				System.out.println("\n");
			}
			String t = "dicom";
			String newT = EraserThread.readString ("DICOM proxy domain: pre-exists is OK ["+t+"]:");
			if (newT.length()==0) newT = t;
			createLocalDomain (cxn, newT);
			System.out.println("\n\n\n");
			return newT;
		} catch (Throwable t) {
			System.out.println(t.getMessage());
			System.out.println("\n\n\n");
			return null;
		}
	}


	private static void getSetDicomUsers (ServerClient.Connection cxn, String dicomDomain) throws Throwable {
		try {
			System.out.println("**************************************************");
			System.out.println("Create proxy DICOM users for calling (their) AETs.");
			System.out.println("**************************************************");

			// Generate our test user
			createLocalDicomUser (cxn, dicomDomain, "HFI-DICOM-TEST");

			// List existing users
			XmlDoc.Element r = getUsers (cxn, dicomDomain);
			System.out.print("Existing proxy DICOM users are : ");
			if (r!=null) {
				Collection<XmlDoc.Element> ds = r.elements("user");
				for (XmlDoc.Element d : ds) {
					System.out.print(d.value() + " ");
				}
				System.out.println("\n");
			}

			// Now get some new ones
			int l = 1;
			while (l>0) {
				String t = EraserThread.readString ("DICOM proxy username (<CR> on empty to finish) []:");
				l= t.length();
				if (l>0) {
					createLocalDicomUser (cxn, dicomDomain, t);
				}
			}
			System.out.println("\n\n\n");
		} catch (Throwable t) {
			System.out.println(t.getMessage());
			System.out.println("\n\n\n");
		}
	}


	private static void getSetDicomNotifications (ServerClient.Connection cxn) throws Throwable {
		try {
			System.out.println("***********************************************************");
			System.out.println("Add notifications for DICOM ingests (successes and failures");
			System.out.println("***********************************************************");

			// List existing users
			XmlDoc.Element r = getNotifications (cxn, "dicom", "ingest");
			System.out.print("Existing DICOM ingest notifications are : ");
			if (r!=null) {
				Collection<XmlDoc.Element> ds = r.elements("notification");
				if (ds!=null) {
					for (XmlDoc.Element d : ds) {
						String event = d.value("event");
						String recip = d.value("recipient");
						System.out.print(event+"/"+recip+" ");
					}
				}
			}
			System.out.println("\n");

			// Now get some new ones	
			int l = 1;
			while (l>0) {
				String t = EraserThread.readString ("Notification email address (<CR> on empty to finish) []:");
				l= t.length();
				if (l>0) {
					addEMailNotification (cxn, "dicom", "ingest", "error", t);
					addEMailNotification (cxn, "dicom", "ingest", "results", t);
				}
			}
			System.out.println("\n\n\n");
		} catch (Throwable t) {
			System.out.println(t.getMessage());
			System.out.println("\n\n\n");
		}
	}




	private static String getServerProperty (ServerClient.Connection cxn, String name, String defaultValue) throws Throwable {
		XmlStringWriter w = new XmlStringWriter();
		w.add("name", new String[] {"default", defaultValue}, name);
		try {
			XmlDoc.Element r = cxn.execute("server.property.get", w.document());
			return r.value("property");
		} catch (Throwable t) {
			// This means the default was null too and the property does not pre-exist
			return null;
		}
	}

	private static void setServerProperty (ServerClient.Connection cxn, String name, String value) throws Throwable {
		XmlStringWriter w = new XmlStringWriter();
		w.add("property", new String[] {"name", name}, value);
		cxn.execute("server.property.set", w.document());
	}

	static XmlDoc.Element getServerIdentity (ServerClient.Connection cxn) throws Throwable {
		return cxn.execute("server.identity");
	}

	private static void setServerIdentity (ServerClient.Connection cxn, String name, String organization) throws Throwable {
		XmlStringWriter w = new XmlStringWriter();
		w.add("name", name);
		w.add("organization", organization);
		cxn.execute("server.identity.set", w.document());
	}

	private static XmlDoc.Element getDomains (ServerClient.Connection cxn) throws Throwable {
		return cxn.execute("authentication.domain.list");
	}


	private static void createLocalDomain (ServerClient.Connection cxn, String name) throws Throwable {

		// Create domain
		XmlStringWriter w = new XmlStringWriter();
		w.add("domain", name);
		w.add("ifexists", "ignore");
		cxn.execute("authentication.domain.create", w.document());

		// Grant om services access to administer this domain
		w = new XmlStringWriter();
		w.add("domain", name);
		cxn.execute("om.pssd.domain.grant", w.document());
	}

	private static XmlDoc.Element getNamespaces (ServerClient.Connection cxn) throws Throwable {
		XmlStringWriter w = new XmlStringWriter();
		w.add("levels", "1");
		return cxn.execute("asset.namespace.describe", w.document());
	}


	private static XmlDoc.Element getStores (ServerClient.Connection cxn) throws Throwable {
		return cxn.execute("asset.store.list");
	}


	private static void createNameSpace (ServerClient.Connection cxn, String name, String description, String store) throws Throwable {
		XmlStringWriter w = new XmlStringWriter();
		w.add("name", store);
		XmlDoc.Element r = cxn.execute("asset.store.exists", w.document());
		if (!r.booleanValue("exists")) {
			throw new Exception ("This store does not exist - you must create stores as part of the Mediaflux installation");
		}
		//
		w = new XmlStringWriter();
		w.add("namespace", name);
		r = cxn.execute("asset.namespace.exists", w.document());
		if (r.booleanValue("exists")) return;
		//		
		w.add("description", description);
		w.add("store", store);
		cxn.execute("asset.namespace.create", w.document());
	}


	private static XmlDoc.Element getUsers (ServerClient.Connection cxn, String domain) throws Throwable {
		XmlStringWriter w = new XmlStringWriter();
		w.add("domain", domain);
		return cxn.execute("authentication.user.list", w.document());
	}

	/**
	 * Create a user in the given domain. Intended for non-humans like DICOM proxy users.
	 * 
	 * @param cxn
	 * @param domain
	 * @param user
	 * @throws Throwable
	 */
	private static void createLocalUser (ServerClient.Connection cxn, String domain, String user) throws Throwable {
		XmlStringWriter w = new XmlStringWriter();
		w.add("domain", domain);
		w.add("user", user);
		w.add("ifexists", "ignore");
		cxn.execute("authentication.user.create", w.document());
	}

	private static void createLocalDicomUser (ServerClient.Connection cxn, String domain, String user) throws Throwable {

		// Grant the PSSD DICOM role (comtains dicom-ingest)
		createLocalUser (cxn, domain, user);
		grantUserRole (cxn, domain, user, "pssd.dicom-ingest");

		// Add the nig-pssd role as well if installed
		if (packageExists(cxn, "nig-pssd")) {
			grantUserRole (cxn, domain, user, "nig.pssd.dicom-ingest");

		}
	}

	private static void grantUserRole (ServerClient.Connection cxn, String domain, String user, String role) throws Throwable {
		XmlStringWriter w = new XmlStringWriter();
		w.add("name", domain+":"+user);
		w.add("role", new String[] {"type", "role"}, role);
		w.add("type", "user");
		cxn.execute("actor.grant", w.document());
	}



	private static boolean packageExists  (ServerClient.Connection cxn, String name) throws Throwable {
		XmlStringWriter w = new XmlStringWriter();
		w.add("package", name);
		XmlDoc.Element r = cxn.execute("package.exists", w.document());
		return r.booleanValue("exists");
	}

	private static void addEMailNotification (ServerClient.Connection cxn, String objectType, String objectName, String event, String recipient) throws Throwable {
		XmlStringWriter w = new XmlStringWriter();
		w.add("recipient", recipient);
		w.add("method", "email");
		w.add("event", event);
		w.add("object", new String[] {"type", objectType}, objectName);
		XmlDoc.Element r = cxn.execute("notification.add", w.document());
		if (r!=null) return;
		//	
		cxn.execute("notification.add", w.document());
	}

	private static XmlDoc.Element getNotifications (ServerClient.Connection cxn, String objectType, String objectName) throws Throwable {
		XmlStringWriter w = new XmlStringWriter();
		w.add("object", new String[] {"type", objectType}, objectName);
		return cxn.execute("notification.describe", w.document());
	}

}
