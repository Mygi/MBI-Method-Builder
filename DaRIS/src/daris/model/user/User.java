package daris.model.user;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arc.mf.client.util.ObjectUtil;
import arc.mf.client.xml.XmlElement;

public class User {

	private String _authority;

	private String _domain;

	private String _protocol;

	private String _user;

	private String _id;

	private String _firstName;

	private String _middleName;

	private String _lastName;

	private String _email;

	private List<String> _roles;

	private Map<String, String> _projectRoles;

	public User(String authority, String domain, String protocol, String user,
			String id, String firstName, String middleName, String lastName,
			List<String> roles, Map<String, String> projectRoles) {

		_authority = authority;
		_domain = domain;
		_protocol = protocol;
		_user = user;
		_id = id;
		_firstName = firstName;
		_middleName = middleName;
		_lastName = lastName;
		_roles = roles;
		_projectRoles = projectRoles;

	}

	public User(XmlElement ue) {

		_id = ue.value("@id");
		_authority = ue.value("@authority");
		_domain = ue.value("@domain");
		_protocol = ue.value("@protocol");
		if(_protocol!=null){
			System.out.println(_protocol);
		}
		_user = ue.value("@user");
		_email = ue.value("email");

		// TODO: validate the xpath below:
		_firstName = ue.value("name[@type='first']");
		_middleName = ue.value("name[@type='middle']");
		_lastName = ue.value("name[@type='last']");

		_roles = ue.values("role");

		List<XmlElement> projects = ue.elements("project");
		if (projects != null) {
			_projectRoles = new HashMap<String, String>(projects.size());
			for (int i = 0; i < projects.size(); i++) {
				XmlElement pe = projects.get(i);
				_projectRoles.put(pe.value(), pe.value("@role"));
			}
		}

	}

	public String authority() {

		return _authority;

	}

	public String domain() {

		return _domain;

	}

	public String protocol() {

		return _protocol;
	}

	public String email() {

		return _email;

	}

	public String firstName() {

		return _firstName;
	}

	public String id() {

		return _id;

	}

	public String lastName() {

		return _lastName;

	}

	public String middleName() {

		return _middleName;

	}

	public String projectRole(String projectId) {

		if (_projectRoles == null) {
			return null;
		}
		return _projectRoles.get(projectId);

	}

	public Collection<String> projects() {

		if (_projectRoles == null) {
			return null;
		}
		return _projectRoles.keySet();

	}

	public List<String> roles() {

		return _roles;

	}

	public String user() {

		return _user;

	}

	public boolean equals(Object o) {

		if (o != null) {
			if (o instanceof User) {
				if (_id.equals(((User) o).id())
						&& ObjectUtil
								.equals(_authority, ((User) o).authority())
						&& _domain.equals(((User) o).domain())
						&& _user.equals(((User) o).user())) {
					return true;
				}
			}
		}
		return false;

	}

	public int hashCode() {

		return _id.hashCode();
	}

	public String toString() {

		String s = _authority == null ? "" : (_authority + ":");
		s += _domain + ":";
		s += _user;
		return s;
	}

	public Map<String, String> projectRoles() {

		return _projectRoles;

	}

	protected void setAuthority(String _authority) {

		this._authority = _authority;
	}

	protected void setDomain(String _domain) {

		this._domain = _domain;
	}

	protected void setUser(String _user) {

		this._user = _user;
	}

	protected void setId(String _id) {

		this._id = _id;
	}

	protected void setFirstName(String _firstName) {

		this._firstName = _firstName;
	}

	protected void setMiddleName(String _middleName) {

		this._middleName = _middleName;
	}

	protected void setLastName(String _lastName) {

		this._lastName = _lastName;
	}

	protected void setEmail(String _email) {

		this._email = _email;
	}

	protected void setRoles(List<String> _roles) {

		this._roles = _roles;
	}

	protected void setProjectRoles(Map<String, String> _projectRoles) {

		this._projectRoles = _projectRoles;
	}

}
