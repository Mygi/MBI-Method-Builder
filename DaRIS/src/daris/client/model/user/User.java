package daris.client.model.user;

import arc.mf.client.util.ObjectUtil;
import arc.mf.client.xml.XmlElement;

public class User implements Comparable<User> {

	public static final String TYPE_NAME = "user";

	private String _id;
	private String _authority;
	private String _protocol;
	private String _domain;
	private String _user;
	private String _name;
	private String _email;

	protected User(String id, String authority, String protocol, String domain,
			String user, String name, String email) {

		_id = id;
		_authority = authority;
		_protocol = protocol;
		_domain = domain;
		_user = user;
		_name = name;
		_email = email;
	}

	public User(XmlElement ue) {

		_id = ue.value("@id");
		_authority = ue.value("@authority");
		_protocol = ue.value("@protocol");
		_domain = ue.value("@domain");
		_user = ue.value("@user");
		_email = ue.value("email");
		_name = ue.value("name");
	}

	public String id() {

		return _id;
	}

	public String name() {

		return _name;
	}

	public String authority() {

		return _authority;
	}

	public String protocol() {

		return _protocol;
	}

	public String domain() {

		return _domain;
	}

	public String user() {

		return _user;
	}

	public String email() {

		return _email;
	}

	@Override
	public boolean equals(Object o) {

		if (o != null) {
			if (o instanceof User) {
				User u = (User) o;
				if (u.id().equals(_id) && u.domain().equals(_domain)
						&& u.user().equals(_user)
						&& ObjectUtil.equals(u.authority(), _authority)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int hashCode() {

		return _id.hashCode();
	}

	@Override
	public String toString() {

		return (_authority == null ? "" : _authority + ":") + _domain + ":"
				+ _user;
	}

	public String toHTML() {

		String html = "<table><thead><tr><th align=\"center\" colspan=\"2\">User</th></tr><thead>";
		html += "<tbody>";
		html += "<tr><td><b>id:</b></td><td>" + _id + "</td></tr>";
		if (_authority != null) {
			html += "<tr><td><b>authority:</b></td><td>" + _authority
					+ "</td></tr>";
		}
		if (_protocol != null) {
			html += "<tr><td><b>protocol:</b></td><td>" + _protocol
					+ "</td></tr>";
		}
		html += "<tr><td><b>domain:</b></td><td>" + _domain + "</td></tr>";
		html += "<tr><td><b>user:</b></td><td>" + _user + "</td></tr>";
		if (_name != null) {
			html += "<tr><td><b>name:</b></td><td>" + _name + "</td></tr>";
		}
		if (_email != null) {
			html += "<tr><td><b>email:</b></td><td>" + _email + "</td></tr>";
		}
		html += "</tbody></table>";
		return html;
	}

	@Override
	public int compareTo(User o) {

		if (o == null) {
			return 1;
		}
		if (!ObjectUtil.equals(_authority, o.authority())) {
			if (_authority != null) {
				return 1;
			} else {
				return -1;
			}
		}
		int r = String.CASE_INSENSITIVE_ORDER.compare(_domain, o.domain());
		if (r != 0) {
			return r;
		}
		r = String.CASE_INSENSITIVE_ORDER.compare(_user, o.user());
		return r;
	}
}
