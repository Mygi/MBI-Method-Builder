package daris.model.project;

import java.util.List;
import java.util.Vector;

import daris.model.user.User;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class ProjectMembersRef extends ObjectRef<List<ProjectMember>> {

	private String _proute;
	private String _id;
	private boolean _dereference;
	private boolean _detail;
	private boolean _ignoreSystemDomain;

	public ProjectMembersRef(String id, String proute, boolean dereference, boolean detail, boolean ignoreSystemDomain) {

		assert id != null;
		_proute = proute;
		_id = id;
		_dereference = dereference;
		_detail = detail;
		_ignoreSystemDomain = ignoreSystemDomain;
	}

	public ProjectMembersRef(ProjectRef p, boolean forEdit) {

		this(p.id(), p.proute(), false, true, forEdit ? true : false);
	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		w.add("dereference", _dereference);
		if (_proute != null) {
			w.add("id", new String[] { "proute", _proute }, _id);
		} else {
			w.add("id", _id);
		}
		if (_detail) {
			w.add("detail", _detail);
		}
	}

	@Override
	protected String resolveServiceName() {

		return "om.pssd.project.members.list";
	}

	@Override
	protected List<ProjectMember> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			List<XmlElement> mes = xe.elements("member");
			if (mes != null) {
				List<ProjectMember> ms = new Vector<ProjectMember>();
				for (XmlElement me : mes) {
					if (_ignoreSystemDomain) {
						String domain = me.value("@domain");
						if (domain != null) {
							if (domain.equals("system")) {
								continue;
							}
						}
					}
					User user = new User(me);
					ProjectMember m = new ProjectMember(user, me.value("@role"), me.value("@data-use"));
					ms.add(m);
				}
				if (!ms.isEmpty()) {
					return ms;
				}
			}
		}
		return null;
	}

	@Override
	public String referentTypeName() {

		return "project.member.list";
	}

	@Override
	public String idToString() {

		return _id;
	}

}
