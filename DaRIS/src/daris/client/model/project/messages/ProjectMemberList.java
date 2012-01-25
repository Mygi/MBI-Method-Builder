package daris.client.model.project.messages;

import java.util.List;
import java.util.Vector;

import daris.client.model.project.Project;
import daris.client.model.project.ProjectMember;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class ProjectMemberList extends ObjectMessage<List<ProjectMember>> {

	private String _id;
	private String _proute;

	public ProjectMemberList(String id, String proute) {

		_id = id;
		_proute = proute;
	}

	public ProjectMemberList(Project o) {

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
	protected List<ProjectMember> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			List<XmlElement> mes = xe.elements("member");
			if (mes != null) {
				List<ProjectMember> ms = new Vector<ProjectMember>(mes.size());
				for (XmlElement me : mes) {
					ms.add(new ProjectMember(me));
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

		return "list of project members";
	}

	@Override
	protected String idToString() {

		return _id;
	}

}
