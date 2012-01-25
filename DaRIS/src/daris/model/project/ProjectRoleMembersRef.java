package daris.model.project;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;
import daris.model.roleuser.RoleUser;

public class ProjectRoleMembersRef extends ObjectRef<List<ProjectRoleMember>> {

	private String _proute;
	private String _id;
	private boolean _dereference;

	public ProjectRoleMembersRef(String id, String proute, boolean dereference) {

		_proute = proute;
		_id = id;
		_dereference = dereference;
	}

	public ProjectRoleMembersRef(ProjectRef p, boolean dereference) {

		this(p.id(), p.proute(), dereference);
	}

	public ProjectRoleMembersRef(ProjectRef p) {

		this(p, false);
	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		w.add("dereference", _dereference);
		if (_proute != null) {
			w.add("id", new String[] { "proute", _proute }, _id);
		} else {
			w.add("id", _id);
		}
	}

	@Override
	protected String resolveServiceName() {

		return "om.pssd.project.members.list";
	}

	@Override
	protected List<ProjectRoleMember> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			List<XmlElement> rmes = xe.elements("role-member");
			if (rmes != null) {
				List<ProjectRoleMember> rms = new Vector<ProjectRoleMember>(rmes.size());
				for (XmlElement rme : rmes) {
					RoleUser ru = new RoleUser(rme.value("@id"), rme.value("@member"));
					ProjectRoleMember rm = new ProjectRoleMember(ru, rme.value("@role"), rme.value("@data-use"));
					rms.add(rm);
				}
				if (!rms.isEmpty()) {
					return rms;
				}
			}
		}
		return null;
	}

	@Override
	public String referentTypeName() {

		return "project.role-member.list";
	}

	@Override
	public String idToString() {

		return _id;
	}

}