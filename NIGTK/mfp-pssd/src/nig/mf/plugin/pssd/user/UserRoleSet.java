package nig.mf.plugin.pssd.user;

import java.util.Collection;
import java.util.List;

public class UserRoleSet {

	private Collection<String> _roles;

	public UserRoleSet(Collection<String> roles) {

		_roles = roles;
	}

	public boolean hasRole(String role) {

		if (_roles == null) {
			return false;
		}
		return _roles.contains(role);
	}

	public Collection<String> roles() {

		return _roles;
	}
}
