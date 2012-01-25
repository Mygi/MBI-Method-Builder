package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.Project;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcUserCanEdit extends PluginService {

	public static final String SERVICE_NAME = "om.pssd.user.canedit";

	private Interface _defn;

	public SvcUserCanEdit() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id", CiteableIdType.DEFAULT, "The citeable ID of the local object of interest.", 1, 1));
	}

	public String name() {
		return SERVICE_NAME;
	}

	public String description() {
		return "Returns whether the user, based on its roles, is allowed to edit specific PSSD objects on the local server.";
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

		String id = args.value("id");
		String projectId = nig.mf.pssd.plugin.util.CiteableIdUtil.getProjectCID(id);
		String systemAdminRole = "system-administrator";
//		String projectCreatorRole = Role.projectCreatorRoleName();
//		String subjectCreatorRole = Role.subjectCreatorRoleName();
		String projectAdminRole = Project.projectAdministratorRoleName(projectId);
		String subjectAdminRole = Project.subjectAdministratorRoleName(projectId);
		String projectMemberRole = Project.memberRoleName(projectId);
		boolean canEdit = false;
		if (nig.mf.pssd.plugin.util.CiteableIdUtil.isProjectId(id)) {
			if (hasRole(executor(), systemAdminRole)) {
				canEdit = true;
			} else if (hasRole(executor(), projectAdminRole)) {
				canEdit = true;
			}
		} else if (nig.mf.pssd.plugin.util.CiteableIdUtil.isSubjectId(id) || 
				nig.mf.pssd.plugin.util.CiteableIdUtil.isExMethodId(id)) {
			if (hasRole(executor(), systemAdminRole)) {
				canEdit = true;
			} else if (hasRole(executor(), projectAdminRole)) {
				canEdit = true;
			} else if (hasRole(executor(), subjectAdminRole)) {
				canEdit = true;
			}
		} else if (nig.mf.pssd.plugin.util.CiteableIdUtil.isStudyId(id) || 
				nig.mf.pssd.plugin.util.CiteableIdUtil.isDataSetId(id)) {
			if (hasRole(executor(), systemAdminRole)) {
				canEdit = true;
			} else if (hasRole(executor(), projectAdminRole)) {
				canEdit = true;
			} else if (hasRole(executor(), subjectAdminRole)) {
				canEdit = true;
			} else if (hasRole(executor(), projectMemberRole)) {
				canEdit = true;
			}
		}
		w.add("edit", canEdit);
	}

	private boolean hasRole(ServiceExecutor executor, String role)
			throws Throwable {
		boolean has = false;
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("role", new String[] { "type", "role" }, role);
		XmlDoc.Element r = executor().execute("actor.self.have", dm.root());
		has = r.booleanValue("role");
		return has;
	}
}
