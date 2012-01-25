package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.*;
import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

public class SvcRSubjectAdminRemove extends PluginService {
	
	private Interface _defn;

	public SvcRSubjectAdminRemove() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT,"The identity of the r-subject.",1,1));
		_defn.add(new Interface.Element("project",CiteableIdType.DEFAULT,"The project that has implicit access. If specified, all subject administrators in the project will be given administration rights for this subject.",0,1));
	}

	public String name() {
		return "om.pssd.r-subject.admin.remove";
	}

	public String description() {
		return "Revoke an administrator for the subject.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public static final Object LOCK = new Object();
	
	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		// By definition the CID is for a local object. We don't care if
		// asset exists or primary/replica
		String id = args.value("id");
		String project = args.value("project");
		revokeAccessToProject(executor(),id,project);
	}
	
	public static void revokeAccessToProject(ServiceExecutor executor,String id,String project) throws Throwable {
		if ( project == null ) {
			return;
		}
		
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("type","role");
		dm.add("name",Project.subjectAdministratorRoleName(project));
		dm.add("role",new String[] { "type", "role" },RSubject.administratorRoleName(id));
		
		executor.execute("actor.revoke",dm.root());
	}
}
