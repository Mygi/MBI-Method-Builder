package daris.client.model.project;

import java.util.List;
import java.util.Vector;

import arc.mf.dtype.EnumerationType;

public enum ProjectRole {
	PROJECT_ADMINISTRATOR, SUBJECT_ADMINISTRATOR, MEMBER, GUEST;
	@Override
	public String toString() {

		return super.toString().toLowerCase().replace('_', '-');
	}

	public static EnumerationType<ProjectRole> asEnumerationType() {

		List<EnumerationType.Value<ProjectRole>> evs = new Vector<EnumerationType.Value<ProjectRole>>(
				values().length);
		for (int i = 0; i < values().length; i++) {
			evs.add(new EnumerationType.Value<ProjectRole>(values()[i]
					.toString(), values()[i].toString(), values()[i]));
		}
		return new EnumerationType<ProjectRole>(evs);
	}

	public static ProjectRole parse(String role) {

		if (role != null) {
			if (role.equalsIgnoreCase(PROJECT_ADMINISTRATOR.toString())) {
				return PROJECT_ADMINISTRATOR;
			}
			if (role.equalsIgnoreCase(SUBJECT_ADMINISTRATOR.toString())) {
				return SUBJECT_ADMINISTRATOR;
			}
			if (role.equalsIgnoreCase(MEMBER.toString())) {
				return MEMBER;
			}
			if (role.equalsIgnoreCase(GUEST.toString())) {
				return GUEST;
			}
		}
		return null;
	}
}
