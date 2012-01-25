package daris.model.roleuser;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class RoleUsersRef extends ObjectRef<List<RoleUser>> {

	private boolean _listProjects = false;

	public RoleUsersRef(boolean listProjects) {

		_listProjects = listProjects;
	}

	public RoleUsersRef() {

		this(false);
	}

	public boolean listProjects() {

		return _listProjects;
	}

	public void setListProjects(boolean listProjects) {

		_listProjects = listProjects;
	}

	@Override
	public String idToString() {

		return "role_users";
	}

	@Override
	protected List<RoleUser> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			List<XmlElement> rues = xe.elements("role-user");
			if (rues != null) {
				Vector<RoleUser> rus = new Vector<RoleUser>(rues.size());
				for (XmlElement rue : rues) {
					rus.add(new RoleUser(rue));
				}
				return rus;
			}
		}
		return null;
	}

	@Override
	public String referentTypeName() {

		return "role_users";
	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		w.add("type", "role");
		w.add("list-projects", _listProjects);
	}

	@Override
	protected String resolveServiceName() {

		return "om.pssd.user.describe";
	}

}
