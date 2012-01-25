package nig.mf.plugin.pssd.services;


import java.util.Collection;
import java.util.Iterator;

import nig.mf.plugin.pssd.Role;


import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.BooleanType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import arc.xml.XmlDoc.Element;

public class SvcUserCreate extends PluginService {

	private Interface _defn;

	public SvcUserCreate()  {
		_defn = new Interface();
		Interface.Element ie = new Interface.Element("authority",StringType.DEFAULT,"The authority of interest for users. Defaults to local.",0,1);
		ie.add(new Interface.Attribute("protocol", StringType.DEFAULT,
				"The protocol of the identity authority. If unspecified, defaults to federated user within the same type of repository.", 0));
		_defn.add(ie);
		_defn.add(new Interface.Element("domain", StringType.DEFAULT, "The name of the domain that the users will be created in. Defaults to 'nig'. ", 0, 1));
		_defn.add(new Interface.Element("user", StringType.DEFAULT, "The username.", 1, 1));
		//
		Interface.Element me = new Interface.Element("name", StringType.DEFAULT, "User's name", 0, Integer.MAX_VALUE);
		me.add(new Interface.Attribute("type", new EnumType(new String[] {"first", "middle", "last"}),
				"Type of name", 1));
		_defn.add(me);
		//
		_defn.add(new Interface.Element("password", StringType.DEFAULT, "The user's password.", 1, 1));
		_defn.add(new Interface.Element("email", StringType.DEFAULT, "The user's email address", 0, 1));
		_defn.add(new Interface.Element("project-creator", BooleanType.DEFAULT, "Should this user be allowed to create projects ? Defaults to false.", 0, 1));
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public Interface definition() {
		return _defn;
	}

	public String description() {
		return "Creates a standard PSSD user and assigns the basic roles: model-user, subject-creator, project-creator (optional).";
	}

	public String name() {
		return "om.pssd.user.create";
	}

	public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
		// Inputs
		XmlDoc.Element authority = args.element("authority");
		String domain = args.stringValue("domain", "nig");
		if (domain.equalsIgnoreCase("dicom"))  {
			throw new Exception ("Use service om.pssd.dicom.user.create for DICOM domain users");
		}
		String user = args.value("user");
		Collection<XmlDoc.Element> names = args.elements("name");
		String email = args.value("email");
		String pw = args.value("password");
		Boolean projectCreator = args.booleanValue("project-creator", false);

		// Create user
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("domain", domain);
		dm.add("user", user);
		dm.add("password", pw);
		dm.add("email", email);

		// Create the user
		if (authority==null) {
			if (names!=null) {
				// user.create does not take authority because it is intended for
				// local authority accounts only. 
				dm.push("meta");
				dm.push("mf-user");
				for (XmlDoc.Element name : names) {
					dm.add(name);
				}
				dm.pop();
				dm.pop();
			}
			executor().execute("user.create", dm.root());
		} else {
			// Use the 'name' element of authentication.user.create
			// We will have to pull this apart again in om.pssd.user.desctribe
			String fullName = makeName (names);	
			if (fullName!=null) dm.add("name", fullName);

			// athentication.user.create does not take element "meta" (it does
			// not make an asset); it is intended for local representations of
			// remote authorities or accounts like DICOM
			if (authority!=null) dm.add(authority);
			executor().execute("authentication.user.create", dm.root());
		}

		// Grant roles. Special case for DICOM
		dm = new XmlDocMaker("args");
		if (authority!=null) dm.add(authority);
		dm.add("domain", domain);
		dm.add("user", user);
		dm.add("role", Role.modelUserRoleName());
		dm.add("role", Role.subjectCreatorRoleName());
		if (projectCreator) dm.add("role", Role.projectCreatorRoleName());
		executor().execute("om.pssd.user.role.grant", dm.root());
	}


	private String makeName (Collection<XmlDoc.Element> names) throws Throwable {
		String first = null;
		String middle = null;
		String last = null;
		if (names!=null) {
			Iterator<XmlDoc.Element> it = names.iterator();
			XmlDoc.Element name = it.next();
			if (name.value("@type").equals("first")) {
				first = name.value();
			} else 	if (name.value("@type").equals("middle")) {
				if (middle==null) {
					middle  = name.value();
				} else {
					middle += " " + name.value();
				}
			} else if (name.value("@type").equals("last")) {
				last = name.value();
			}
		}
		String fullName = null;
		if (first!=null) fullName = first;
		if (middle!=null) fullName = fullName + " " +  middle;
		if (last!=null) fullName = fullName + " " + last;
		return fullName;
	}


}
