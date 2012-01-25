package daris.model.project.messages;

import java.util.List;

import daris.model.project.ProjectMember;
import daris.model.project.ProjectRef;
import daris.model.project.ProjectRoleMember;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class ProjectMembersReplace extends ObjectMessage<Boolean> {

	private List<ProjectMember> _members;
	private List<ProjectRoleMember> _roleMembers;
	private String _id;

	public ProjectMembersReplace(String id, List<ProjectMember> members,
			List<ProjectRoleMember> roleMembers) {

		_id = id;
		_members = members;
		_roleMembers = roleMembers;
	}

	public ProjectMembersReplace(ProjectRef p) {

		this(p.id(), p.members(), p.roleMembers());
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("id", _id);
		if (_members != null) {
			if (!_members.isEmpty()) {
				for (ProjectMember member : _members) {
					w.push("member");
					if (member.user().authority() != null) {
						if (member.user().protocol() != null) {
							w.add("authority", new String[] { "protocol",
									member.user().protocol() }, member.user()
									.authority());
						} else {
							w.add("authority", member.user().authority());
						}
					}
					w.add("domain", member.user().domain());
					w.add("user", member.user().user());
					w.add("role", member.role());
					if (member.dataUse() != null) {
						w.add("data-use", member.dataUse());
					}
					w.pop();
				}
			}
		}
		if (_roleMembers != null) {
			if (!_roleMembers.isEmpty()) {
				for (ProjectRoleMember roleMember : _roleMembers) {
					w.push("role-member");
					w.add("member", roleMember.roleUser().member());
					w.add("role", roleMember.role());
					if (roleMember.dataUse() != null) {
						w.add("data-use", roleMember.dataUse());
					}
					w.pop();
				}
			}
		}
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.project.members.replace";
	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {

		return true;
	}

	@Override
	protected String objectTypeName() {

		return "project.member.list";
	}

	@Override
	protected String idToString() {

		return _id;
	}

}
