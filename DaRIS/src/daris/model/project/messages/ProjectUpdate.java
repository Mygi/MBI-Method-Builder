package daris.model.project.messages;

import arc.mf.client.xml.XmlWriter;
import daris.model.method.MethodRef;
import daris.model.object.messages.ObjectUpdate;
import daris.model.project.Project;
import daris.model.project.ProjectMember;
import daris.model.project.ProjectRef;
import daris.model.project.ProjectRoleMember;

public class ProjectUpdate extends ObjectUpdate {

	public ProjectUpdate(ProjectRef ref) {

		super(ref);

	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.project.update";

	}

	@Override
	protected String objectTypeName() {

		return Project.TYPE_NAME;

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
				if (m != null) {
					w.push("method", new String[] { "action", "replace" });
					w.add("id", m.id());
					w.pop();
				}
			}
		} else {
			w.add("method", new String[] { "action", "clear" });
		}
		if (p.members() != null) {
			for (ProjectMember pm : p.members()) {
				w.push("member");
				if (pm.user().authority() != null) {
					if (pm.user().protocol() != null) {
						w.add("authority", new String[] { "protocol", pm.user().protocol() }, pm.user().authority());
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

}
