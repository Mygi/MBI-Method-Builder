package nig.mf.plugin.pssd.user;


public class ProjectDataUseRole {

	public static final String ROLE_PREFIX = "pssd.project.subject.use";

	public static final String SPECIFIC_ROLE_PREFIX = ROLE_PREFIX + '.' + DataUse.specific;
	public static final String EXTENDED_ROLE_PREFIX = ROLE_PREFIX + '.' + DataUse.extended;
	public static final String UNSPECIFIED_ROLE_PREFIX = ROLE_PREFIX + '.' + DataUse.unspecified;

	private DataUse _dataUse;
	private String _cid;

	public ProjectDataUseRole(DataUse dataUse, String cid) {

		_dataUse = dataUse;
		_cid = cid;
	}

	public DataUse dataUse() {

		return _dataUse;
	}

	public String cid() {

		return _cid;
	}

	@Override
	public String toString() {

		return name();
	}

	public String name() {

		return ROLE_PREFIX + "." + _dataUse.toString() + "." + _cid;
	}

	public static ProjectDataUseRole parse(String role) throws Throwable {

		if (role.startsWith(SPECIFIC_ROLE_PREFIX)) {
			return new ProjectDataUseRole(DataUse.specific, role.substring(SPECIFIC_ROLE_PREFIX.length() + 1));
		} else if (role.startsWith(EXTENDED_ROLE_PREFIX)) {
			return new ProjectDataUseRole(DataUse.extended, role.substring(EXTENDED_ROLE_PREFIX.length() + 1));
		} else if (role.startsWith(UNSPECIFIED_ROLE_PREFIX)) {
			return new ProjectDataUseRole(DataUse.unspecified, role.substring(UNSPECIFIED_ROLE_PREFIX.length() + 1));
		}
		throw new Exception("Invalid project data-use role: " + role);
	}
}
