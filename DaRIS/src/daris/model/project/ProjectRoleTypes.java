package daris.model.project;

import java.util.List;
import java.util.Vector;

public class ProjectRoleTypes {

	public static final String PROJECT_ADMINISTRATOR = "project-administrator";
	public static final String SUBJECT_ADMINISTRATOR = "subject-administrator";
	public static final String MEMBER = "member";
	public static final String GUEST = "guest";

	public static List<String> asList() {
		List<String> list = new Vector<String>();
		list.add(PROJECT_ADMINISTRATOR);
		list.add(SUBJECT_ADMINISTRATOR);
		list.add(MEMBER);
		list.add(GUEST);
		return list;
	}

}
