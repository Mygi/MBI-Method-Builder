package daris.model.user;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class UsersRef extends ObjectRef<List<User>> {
	
	private boolean _listProjects = false;

	public UsersRef(boolean listProjects) {

		_listProjects = listProjects;

	}

	public UsersRef() {

		this(false);
	}	

	public void setListProjects(boolean listProjects) {

		_listProjects = listProjects;

	}

	public boolean listProjects() {

		return _listProjects;

	}

	@Override
	protected List<User> instantiate(XmlElement xe) throws Throwable {

		List<XmlElement> ues = xe.elements("user");
		if (ues != null) {
			Vector<User> us = new Vector<User>(ues.size());
			for (XmlElement ue : ues) {
				us.add(new User(ue));
			}
			return us;
		}
		return null;

	}

	@Override
	public String referentTypeName() {

		return "users";

	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		w.add("type", "user");
		w.add("list-projects", _listProjects);
	}

	@Override
	protected String resolveServiceName() {

		return "om.pssd.user.describe";
	}

	@Override
	public String idToString() {

		return "users";

	}

}
