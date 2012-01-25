package daris.model.roleuser;

import arc.mf.client.xml.XmlElement;

public class RoleUser {

	private String _member;

	private String _id;

	public RoleUser(XmlElement xe) {

		this(xe.value("@id"), xe.value("@member"));
	}

	public RoleUser(String id, String member) {

		_id = id;
		_member = member;
	}

	public String member() {

		return _member;
	}

	public String id() {

		return _id;
	}

	public boolean equals(Object o) {

		if (o != null) {
			if (o instanceof RoleUser) {
				if (_id.equals(((RoleUser) o).id())
						&& _member.equals(((RoleUser) o).member())) {
					return true;
				}
			}
		}
		return false;
	}

	public int hashCode() {

		return _id.hashCode();
	}

}
