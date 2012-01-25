package daris.client.model.project.messages;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.project.Project;
import daris.client.model.project.ProjectMember;

public class ProjectMemberAdd extends ObjectMessage<Boolean> {

	private String _id;
	private List<ProjectMember> _members;

	public ProjectMemberAdd(String id, List<ProjectMember> members) {

		_id = id;
		_members = members;
	}

	public ProjectMemberAdd(String id, ProjectMember member) {

		_id = id;
		_members = new Vector<ProjectMember>(1);
		_members.add(member);
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("id", _id);
		assert _members != null;
		assert !_members.isEmpty();
		for (ProjectMember _member : _members) {
			w.push("member");
			if (_member.user().authority() != null) {
				if (_member.user().protocol() != null) {
					w.add("authority", new String[] { "protocol",
							_member.user().protocol() }, _member.user()
							.authority());
				} else {
					w.add("authority", _member.user().authority());
				}
			}
			w.add("domain", _member.user().domain());
			w.add("user", _member.user());
			w.add("role", _member.role());
			if (_member.dataUse() != null) {
				w.add("data-use", _member.dataUse());
			}
			w.pop();
		}
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.project.members.add";
	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {

		return true;
	}

	@Override
	protected String objectTypeName() {

		return Project.TYPE_NAME;
	}

	@Override
	protected String idToString() {

		return _id;
	}

}
