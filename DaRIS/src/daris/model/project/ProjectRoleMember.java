package daris.model.project;

import daris.model.roleuser.RoleUser;

public class ProjectRoleMember {
	private RoleUser _roleUser;

	private String _roleType;

	private String _dataUse;

	public ProjectRoleMember(RoleUser roleUser, String roleType, String dataUse) {

		_roleUser = roleUser;
		_roleType = roleType;
		_dataUse = dataUse;

	}

	public RoleUser roleUser() {

		return _roleUser;
	}

	public String role() {

		return _roleType;
	}

	public void setRole(String role) {

		_roleType = role;
	}

	public String dataUse() {

		return _dataUse;
	}

	public void setDataUse(String dataUse) {

		_dataUse = dataUse;
	}

	public boolean equals(Object o) {

		if (o instanceof ProjectRoleMember) {
			if (_roleUser.equals(((ProjectRoleMember) o).roleUser())
					&& _roleType.equals(((ProjectRoleMember) o).role())) {
				return true;
			}
		}
		return false;
	}
}
