package daris.client.model.user;

import arc.mf.client.xml.XmlElement;

public class RoleUser implements Comparable<RoleUser> {

	public static final String TYPE_NAME = "role";
	private String _id;
	private String _member;

	public RoleUser(XmlElement rue) {

		_id = rue.value("@id");
		_member = rue.value("@member");
	}

	public String id() {

		return _id;
	}

	public String member() {

		return _member;
	}

	@Override
	public int hashCode() {

		return _id.hashCode();
	}

	@Override
	public boolean equals(Object o) {

		if (o != null) {
			if (o instanceof RoleUser) {
				RoleUser ru = (RoleUser) o;
				return (ru.id().equals(_id) && ru.member().equals(_member));
			}
		}
		return false;
	}

	@Override
	public String toString() {

		return _member;
	}

	@Override
	public int compareTo(RoleUser o) {

		if (o == null) {
			return 1;
		}

		return String.CASE_INSENSITIVE_ORDER.compare(_member, o.member());
	}

	public String toHTML() {

		String html = "<table><thead><tr><th align=\"center\" colspan=\"2\">Role User</th></tr><thead>";
		html += "<tbody>";
		html += "<tr><td><b>id:</b></td><td>" + _id + "</td></tr>";
		html += "<tr><td><b>member:</b></td><td>" + _member + "</td></tr>";
		html += "</tbody></table>";
		return html;
	}
}
