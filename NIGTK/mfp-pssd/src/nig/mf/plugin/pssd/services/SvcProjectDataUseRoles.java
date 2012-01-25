package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.xml.*;
import nig.mf.plugin.pssd.*;


public class SvcProjectDataUseRoles extends PluginService {
	
	private Interface _defn;

	public SvcProjectDataUseRoles() {
		_defn = null;
	}
	public Interface definition() {
		return _defn;
	}

	
	public String name() {
		return "om.pssd.project.data-use.roles";
	}

	public String description() {
		return "Returns the data-use role names that may be assigned to a project-team member when calling om.pssd.project.create;";
	}


	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		w.add("data-use", Project.CONSENT_SPECIFIC_ROLE_NAME);
		w.add("data-use", Project.CONSENT_EXTENDED_ROLE_NAME);
		w.add("data-use", Project.CONSENT_UNSPECIFIED_ROLE_NAME);
	}
}
