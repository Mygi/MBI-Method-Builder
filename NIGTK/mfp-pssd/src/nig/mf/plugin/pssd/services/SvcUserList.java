package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.*;


import java.util.Collection;

import arc.mf.plugin.*;
import arc.xml.*;
import arc.mf.plugin.dtype.*;

public class SvcUserList extends PluginService {

	//public static final String SERVICE_NAME = "om.pssd.user.list";

	private Interface _defn;

	public SvcUserList() {
		_defn = new Interface();
		Interface.Element ie = new Interface.Element("authority",StringType.DEFAULT,"The authority of interest for users. Defaults to all.",0,1);
		ie.add(new Interface.Attribute("protocol", StringType.DEFAULT,
				"The protocol of the identity authority. If unspecified, defaults to federated user within the same type of repository.", 0));
		_defn.add(ie);
		_defn.add(new Interface.Element("domain",StringType.DEFAULT,"The authentication domain for users. Defaults to all.",0,1));
		_defn.add(new Interface.Element("ignore-system", BooleanType.DEFAULT, "Ignore SYSTEM domain users ?  Defaults to true.", 0, 1));
	}

	public String name() {
		return "om.pssd.user.list";
	}

	public String description() {
		return "Returns a list of local users that have been registered as users of the PSSD object model. Includes external authorities.";
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
		XmlDoc.Element authority = args.element("authority");
		String domain = args.stringValue("domain");
		Boolean ignoreSystem = args.booleanValue("ignore-system", true);


		// Find the users that have the PSSD model role.  This will work well for LDAP
		// as it will only return the LDAP users who have the role (efficiently)
		XmlDocMaker dm = new XmlDocMaker("args");
		if (authority!=null) dm.add(authority);
		if (domain!=null) dm.add("domain", domain);
		dm.add("role", new String[]{"type", "role"}, Role.modelUserRoleName());
		XmlDoc.Element r = executor().execute("user.describe", dm.root());
		if (r==null) return;

		// FInd just the users
		Collection<XmlDoc.Element> users = r.elements("user");
		if (users==null) return;

		// Now we have to iterate through and select the users we want by domain and authority
		for (XmlDoc.Element user : users) {
			String d = user.value("@domain");
			if (!ignoreSystem || (ignoreSystem && !d.equalsIgnoreCase("system"))) {
				w.add("user", new String[] {"id", user.value("@id"), "authority", user.value("@authority"), "@protocol", user.value("protocol"), 
						"domain", user.value("@domain")}, user.value("@user"));
			}
		}
	}
}
