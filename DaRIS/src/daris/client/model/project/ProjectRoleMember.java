package daris.client.model.project;

import arc.mf.client.util.ObjectUtil;
import arc.mf.client.xml.XmlElement;
import daris.client.model.user.RoleUser;

public class ProjectRoleMember implements Comparable<ProjectRoleMember> {

	private RoleUser _member;
	private ProjectRole _role;
	private DataUse _dataUse;

	public ProjectRoleMember(XmlElement rme) {

		_member = new RoleUser(rme);
		_role = ProjectRole.parse(rme.value("@role"));
		_dataUse = DataUse.parse(rme.value("@data-use"));
	}

	public ProjectRoleMember(RoleUser roleUser, ProjectRole role,
			DataUse dataUse) {

		_member = roleUser;
		_role = role;
		_dataUse = dataUse;
	}

	public RoleUser member() {

		return _member;
	}

	public ProjectRole role() {

		return _role;
	}

	public void setRole(ProjectRole role) {

		_role = role;
	}

	public DataUse dataUse() {

		return _dataUse;
	}

	public void setDataUse(DataUse dataUse) {

		if (_role.equals(ProjectRole.PROJECT_ADMINISTRATOR)
				|| _role.equals(ProjectRole.SUBJECT_ADMINISTRATOR)) {
			_dataUse = null;
		} else {
			_dataUse = dataUse;
		}
	}

	@Override
	public String toString() {

		String s = ":role-member -id " + _member.id() + " -member "
				+ _member.member() + " -role " + _role;
		if (_dataUse != null) {
			s += " -data-use " + _dataUse;
		}
		return s;
	}

	public String toHTML() {

		String html = "<table><thead><tr><th align=\"center\" colspan=\"2\">Role Member</th></tr><thead>";
		html += "<tbody>";
		html += "<tr><td><b>id:</b></td><td>" + _member.id() + "</td></tr>";
		html += "<tr><td><b>member:</b></td><td>" + _member.member()
				+ "</td></tr>";
		html += "<tr><td><b>role:</b></td><td>" + _role + "</td></tr>";
		if (_dataUse != null) {
			html += "<tr><td><b>dataUse:</b></td><td>" + _dataUse
					+ "</td></tr>";
		}
		html += "</tbody></table>";
		return html;
	}

	@Override
	public boolean equals(Object o) {

		if (o == null) {
			return false;
		}
		if (o instanceof ProjectRoleMember) {
			ProjectRoleMember prm = (ProjectRoleMember) o;
			return _member.equals(prm.member()) && _role.equals(prm.role())
					&& ObjectUtil.equals(_dataUse, prm.dataUse());
		}
		return false;
	}

	@Override
	public int compareTo(ProjectRoleMember o) {

		// TODO Auto-generated method stub
		return 0;
	}
}
