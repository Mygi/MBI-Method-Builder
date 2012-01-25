package nig.mf.plugin.pssd;


public class Role {
	
	/**
	 * User has access to the model.
	 */
	public static final String MODEL_USER_ROLE_NAME      = "model-user";
	
	/**
	 * User can create projects.
	 */
	public static final String PROJECT_CREATOR_ROLE_NAME = "project-creator";
	
	/**
	 * User can create r-subjects.
	 */
	public static final String SUBJECT_CREATOR_ROLE_NAME = "subject-creator";
	
	
	public static String modelUserRoleName() {
		return "pssd.model.user";
	}
	
	public static String projectCreatorRoleName() {
		return "pssd.project.create";
	}
	
	public static String subjectCreatorRoleName() {
		return "pssd.subject.create";
	}
	

}
