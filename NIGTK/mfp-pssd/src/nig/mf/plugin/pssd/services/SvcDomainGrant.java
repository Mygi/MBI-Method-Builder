package nig.mf.plugin.pssd.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import arc.xml.XmlDoc.Element;

public class SvcDomainGrant extends PluginService {

	private Interface _defn;

	public SvcDomainGrant() throws Throwable {
		_defn = new Interface();
		addInterface(_defn);
	}

	public static void addInterface(Interface defn) throws Throwable {
		Interface.Element ie = new Interface.Element("authority",StringType.DEFAULT,"The authority of interest. Defaults to local.",0,1);
		ie.add(new Interface.Attribute("protocol", StringType.DEFAULT,
				"The protocol of the identity authority. If unspecified, defaults to federated user within the same type of repository.", 0));
		defn.add(ie);
		//
		defn.add(new Interface.Element("domain", StringType.DEFAULT,
				"The name of the domain that the users exist in.", 1, 1));
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public Interface definition() {
		return _defn;
	}

	public String description() {
		return "Grants specific services the permission to administer the specified domain.  This is required to allow standard users the ability to grant/revoke project roles. Returns whether domain exists or not.";
	}

	public String name() {
		return "om.pssd.domain.grant";
	}

	public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {

		// Get arguments and parse
		XmlDoc.Element authority = args.element("authority");
		String domain = args.value("domain");	
		
		// CHeck exists
		XmlDocMaker dm = new XmlDocMaker("args");
	    if (authority != null) {
	    	String p = authority.value("@protocol");
	    	if (p!=null && p.equalsIgnoreCase("ldap")) {
	    		// For LDAP  just use unique domain
	    	} else {
	    		dm.add(authority);
	    	}
	    }
		dm.add("domain", domain);
		XmlDoc.Element  r = executor().execute("authentication.domain.exists", dm.root());
		if (!r.booleanValue("exists")) {
			w.add("exists", false);
			return;
		}
		w.add("exists", true);
		
		// These services need to mess with roles
		boolean grant = true;
		grantRevoke (executor(), "om.pssd.project.members.replace", authority, domain, grant);
		grantRevoke (executor(), "om.pssd.project.members.remove", authority, domain, grant);
		grantRevoke (executor(), "om.pssd.project.members.add", authority, domain, grant);
		grantRevoke (executor(), "om.pssd.project.create", authority, domain, grant);
		grantRevoke (executor(), "om.pssd.project.update", authority, domain, grant);
	}


	// Private functions
	public static void grantRevoke (ServiceExecutor executor, String service, XmlDoc.Element authority, String domain, boolean grant) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("name", service);
		dm.add("type", "plugin:service");
		dm.push("perm");
		dm.add("access", "ADMINISTER");
		//
	    String name = domain;
	    String a = null;
	    String p = null;
	    if (authority != null) {
	    	a = authority.value();
	    	p = authority.value("@protocol");
	    	if (p==null || (p!=null && !p.equalsIgnoreCase("ldap"))) {
	    		// FOr LDAP just use unique domain
	    		name = a + ":" + domain;
	    	}
	    }
		
		dm.add("resource", new String[] {"type", "authentication:domain"}, name);
		if (grant) {
			executor.execute("actor.grant", dm.root());
		} else {
			executor.execute("actor.revoke", dm.root());

		}
	}
}
