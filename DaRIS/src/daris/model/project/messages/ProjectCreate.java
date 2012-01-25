package daris.model.project.messages;

import arc.mf.client.xml.XmlWriter;
import daris.model.method.MethodRef;
import daris.model.object.messages.ObjectCreate;
import daris.model.project.Project;
import daris.model.project.ProjectMember;
import daris.model.project.ProjectRef;
import daris.model.project.ProjectRoleMember;
import daris.model.repository.RepositoryRootRef;

public class ProjectCreate extends ObjectCreate {

	public ProjectCreate(RepositoryRootRef parent, ProjectRef object) {

		super(parent, object);

	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.project.create";

	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		super.messageServiceArgs(w);

		ProjectRef p = (ProjectRef) object();
		if (p.dataUse() != null) {
			w.add("data-use", p.dataUse());
		}
		if (p.methods() != null) {
			for (MethodRef m : p.methods()) {
				w.push("method");
				w.add("id", m.id());
				w.pop();
			}
		}
		if (p.members() != null) {
			for (ProjectMember pm : p.members()) {
				w.push("member");
				if (pm.user().authority() != null) {
					if (pm.user().protocol() != null) {
						w.add("authority", new String[] { "protocol",
								pm.user().protocol() }, pm.user().authority());
					} else {
						w.add("authority", pm.user().authority());
					}
				}
				w.add("domain", pm.user().domain());
				w.add("user", pm.user().user());
				w.add("role", pm.role());
				if (pm.dataUse() != null) {
					w.add("data-use", pm.dataUse());
				}
				w.pop();
			}
		}
		
		if (p.roleMembers() != null) {
			for (ProjectRoleMember prm : p.roleMembers()) {
				w.push("role-member");
				w.add("member", prm.roleUser().member());
				w.add("role", prm.role());
				if (prm.dataUse() != null) {
					w.add("data-use", prm.dataUse());
				}
				w.pop();
			}
		}
		if (p.metaToSave() != null) {
			w.add(p.metaToSave(), true);
		}
	}

	@Override
	protected String objectTypeName() {

		return Project.TYPE_NAME;

	}

}
