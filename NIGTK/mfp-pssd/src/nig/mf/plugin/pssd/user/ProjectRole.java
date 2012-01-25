package nig.mf.plugin.pssd.user;

public class ProjectRole {

	public static final String PROJECT_ADMINISTRATOR_ROLE_PREFIX = "pssd.project.admin";
	public static final String SUBJECT_ADMINISTRATOR_ROLE_PREFIX = "pssd.project.subject.admin";
	public static final String MEMBER_ROLE_PREFIX = "pssd.project.member";
	public static final String GUEST_ROLE_PREFIX = "pssd.project.guest";

	public static enum Type {
		project_administrator, subject_administrator, member, guest;

		@Override
		public String toString() {

			return super.toString().replace('_', '-');
		}

		public String prefix() {

			switch (this) {
			case project_administrator:
				return PROJECT_ADMINISTRATOR_ROLE_PREFIX;
			case subject_administrator:
				return SUBJECT_ADMINISTRATOR_ROLE_PREFIX;
			case member:
				return MEMBER_ROLE_PREFIX;
			case guest:
				return GUEST_ROLE_PREFIX;
			}
			return null;
		}

		public boolean isAdmin() {

			return this == project_administrator || this == subject_administrator;
		}
	}

	private Type _type;
	private String _cid;

	public ProjectRole(Type type, String cid) {

		_type = type;
		_cid = cid;
	}

	@Override
	public String toString() {

		return name();
	}

	public String cid() {

		return _cid;
	}

	public Type type() {

		return _type;
	}

	public String name() {

		return _type.prefix() + "." + _cid;
	}

	public static String prefix(Type type) {

		return type.prefix();
	}

	public static ProjectRole projectAdministratorRoleOf(String cid) {

		return new ProjectRole(Type.project_administrator, cid);
	}

	public static String projectAdministratorRoleNameOf(String cid) {

		return PROJECT_ADMINISTRATOR_ROLE_PREFIX + '.' + cid;
	}

	public static ProjectRole subjectAdministratorRoleOf(String cid) {

		return new ProjectRole(Type.subject_administrator, cid);
	}

	public static String subjectAdministratorRoleNameOf(String cid) {

		return SUBJECT_ADMINISTRATOR_ROLE_PREFIX + '.' + cid;
	}

	public static ProjectRole memberRoleOf(String cid) {

		return new ProjectRole(Type.member, cid);
	}

	public static String memberRoleNameOf(String cid) {

		return MEMBER_ROLE_PREFIX + '.' + cid;
	}

	public static ProjectRole guestRoleOf(String cid) {

		return new ProjectRole(Type.guest, cid);
	}

	public static String guestRoleNameOf(String cid) {

		return GUEST_ROLE_PREFIX + '.' + cid;
	}

	public static ProjectRole parse(String role) throws Throwable {

		if (role.startsWith(PROJECT_ADMINISTRATOR_ROLE_PREFIX)) {
			return new ProjectRole(Type.project_administrator, role.substring(PROJECT_ADMINISTRATOR_ROLE_PREFIX
					.length() + 1));
		} else if (role.startsWith(SUBJECT_ADMINISTRATOR_ROLE_PREFIX)) {
			return new ProjectRole(Type.subject_administrator, role.substring(SUBJECT_ADMINISTRATOR_ROLE_PREFIX
					.length() + 1));
		} else if (role.startsWith(MEMBER_ROLE_PREFIX)) {
			return new ProjectRole(Type.member, role.substring(MEMBER_ROLE_PREFIX.length() + 1));
		} else if (role.startsWith(GUEST_ROLE_PREFIX)) {
			return new ProjectRole(Type.guest, role.substring(GUEST_ROLE_PREFIX.length() + 1));
		}
		throw new Exception("Invalid project role: " + role);
	}
}
