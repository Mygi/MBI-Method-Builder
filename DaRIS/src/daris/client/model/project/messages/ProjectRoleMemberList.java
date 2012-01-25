package daris.client.model.project.messages;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.project.Project;
import daris.client.model.project.ProjectRoleMember;

public class ProjectRoleMemberList extends
		ObjectMessage<List<ProjectRoleMember>> {

	private String _id;
	private String _proute;

	public ProjectRoleMemberList(String id, String proute) {

		_id = id;
		_proute = proute;
	}

	public ProjectRoleMemberList(Project o) {

		this(o.id(), o.proute());
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		if (_proute != null) {
			w.add("id", new String[] { "proute", _proute }, _id);
		} else {
			w.add("id", _id);
		}

	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.project.members.list";
	}

	@Override
	protected List<ProjectRoleMember> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			List<XmlElement> rmes = xe.elements("role-member");
			if (rmes != null) {
				List<ProjectRoleMember> ms = new Vector<ProjectRoleMember>(rmes.size());
				for (XmlElement me : rmes) {
					ms.add(new ProjectRoleMember(me));
				}
				if (!ms.isEmpty()) {
					return ms;
				}
			}
		}
		return null;
	}

	@Override
	protected String objectTypeName() {

		return "list of project role-members";
	}

	@Override
	protected String idToString() {

		return _id;
	}

}
