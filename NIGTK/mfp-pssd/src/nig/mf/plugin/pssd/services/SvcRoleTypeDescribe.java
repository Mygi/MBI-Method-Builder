package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.xml.*;
import nig.mf.plugin.pssd.*;


public class SvcRoleTypeDescribe extends PluginService {
	private Interface _defn;

	public SvcRoleTypeDescribe() {
		_defn = null;
	}

	public String name() {
		return "om.pssd.role.type.describe";
	}

	public String description() {
		return "Returns the types of (generic) roles that can be granted to a user.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		describe(w,Role.MODEL_USER_ROLE_NAME,"This role allows indicates the holder is a user of the model. Required to appear in the the list of users returned by the service om.pssd.user.describe");
		describe(w,Role.PROJECT_CREATOR_ROLE_NAME,"This role allows the holder to create new projects.");
		describe(w,Role.SUBJECT_CREATOR_ROLE_NAME,"This role allows the holder to create new cross-project subjects.");
	}
	
	private static void describe(XmlWriter w,String role,String description) throws Throwable {
		w.push("role");
		w.add("name",role);
		w.add("description",description);
		w.pop();
	}
}
