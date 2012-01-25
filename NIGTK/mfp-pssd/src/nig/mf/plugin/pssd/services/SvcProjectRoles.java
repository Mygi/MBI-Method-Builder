package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.xml.*;


import nig.mf.plugin.pssd.*;


public class SvcProjectRoles extends PluginService {
	
	private Interface _defn;

	public SvcProjectRoles() {
		_defn = null;
	}
	public Interface definition() {
		return _defn;
	}

	
	public String name() {
		return "om.pssd.project.roles";
	}

	public String description() {
		return "Returns the project role names that may to be supplied for a member when calling om.pssd.project.create;";
	}


	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		w.add("role", Project.ADMINISTRATOR_ROLE_NAME);
		w.add("role", Project.SUBJECT_ADMINISTRATOR_ROLE_NAME);
		w.add("role", Project.MEMBER_ROLE_NAME);
		w.add("role", Project.GUEST_ROLE_NAME);
	}
}
