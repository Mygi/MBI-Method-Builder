package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.xml.*;
import nig.mf.plugin.pssd.*;

import java.util.*;

public class SvcRoleList extends PluginService {
	private Interface _defn;

	public SvcRoleList() {
		_defn = null;
	}

	public String name() {
		return "om.pssd.role.list";
	}

	public String description() {
		return "Returns the list of (generic) roles held by the caller.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("role",new String[] { "type", "role" },Role.projectCreatorRoleName());
		dm.add("role",new String[] { "type", "role" },Role.subjectCreatorRoleName());

		XmlDoc.Element r = executor().execute("actor.self.have",dm.root());
		Collection<XmlDoc.Element> res = r.elements("role");
		if ( res != null ) {
			for (XmlDoc.Element re : res) {
				
				String name = re.value("@name");
				
				String rname = null;
				
				if ( name.equalsIgnoreCase(Role.projectCreatorRoleName()) ) {
					rname = Role.PROJECT_CREATOR_ROLE_NAME;
				} else if ( name.equalsIgnoreCase(Role.subjectCreatorRoleName()) ) {
					rname = Role.SUBJECT_CREATOR_ROLE_NAME;
				}
				
				if ( rname != null && re.booleanValue() ) {
					w.add("role",rname);
				}
			}
		}
		
	}
}
