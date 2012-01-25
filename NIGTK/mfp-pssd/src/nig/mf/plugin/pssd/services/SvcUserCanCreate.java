package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.*;

import arc.mf.plugin.*;
import arc.xml.*;
import arc.mf.plugin.dtype.*;

public class SvcUserCanCreate extends PluginService {
	
	public static final String SERVICE_NAME = "om.pssd.user.cancreate";
	
	private Interface _defn;

	public SvcUserCanCreate() {
		_defn = new Interface();
		_defn.add(new Interface.Element("object",StringType.DEFAULT,"The object for creation: one of 'project', 'subject', 'study'",1,1));
		//
		_defn.add(new Interface.Element("pid",CiteableIdType.DEFAULT, "The citeable ID of the parent project if object='subject' or 'study'", 0, 1));
	}

	public String name() {
		return SERVICE_NAME;
	}

	public String description() {
		return "Returns whether the current user, based on its roles, is allowed to create specific PSSD objects on the local server.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public int executeMode() {
		return EXECUTE_DISTRIBUTED_ALL;
	}
	
	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		// Parse arguments
		String pssdObject = args.stringValue("object");

		// We don't need the proute/actual project because roles are just specified according
		// to the Project CID.  If we ever change to going back to the parent Project then
		// we would need proute as well.
		String pid = args.stringValue("pid");
		//
		String role=null;
		if (pssdObject.equals("project")) { 
			role = Role.projectCreatorRoleName();
		} else if (pssdObject.equals("subject")) {
			if (pid == null) {
				throw new Exception ("Parent project must be given");
			}
			role = Project.subjectAdministratorRoleName(pid);
		} else if (pssdObject.equals("study")) {
			if (pid == null) {
				throw new Exception ("Parent project must be given");
			}
			role = Project.memberRoleName(pid);
		} else {
			throw new Exception ("Object must be 'project', 'subject' or 'study'");
		}
		
		// See if user has the desired role
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("role",new String[] { "type", "role" },role);			
		XmlDoc.Element r = executor().execute("actor.self.have",dm.root());
		Boolean canCreate = r.booleanValue("role");
		// 
		w.add("create", canCreate);
	}	
}
