package daris.model.project;

import arc.mf.client.util.ObjectUtil;
import daris.model.user.User;

public class ProjectMember {

	private User _user;

	private String _roleType;

	private String _dataUse;

	public ProjectMember(User user, String roleType, String dataUse) {

		_user = user;
		_roleType = roleType;
		_dataUse = dataUse;

	}

	public User user() {

		return _user;
	}

	public String role() {

		return _roleType;
	}

	public String dataUse() {

		return _dataUse;
	}

	public boolean equals(Object o) {

		if (o != null) {
			if (o instanceof ProjectMember) {
				if (_user.equals(((ProjectMember) o).user()) && _roleType.equals(((ProjectMember) o).role())
						&& ObjectUtil.equals(_dataUse, ((ProjectMember) o).dataUse())) {
					return true;
				}
			}
		}
		return false;
	}

	public void setDataUse(String dataUse) {

		_dataUse = dataUse;
	}

	public void setRole(String role) {

		_roleType = role;
	}

}
