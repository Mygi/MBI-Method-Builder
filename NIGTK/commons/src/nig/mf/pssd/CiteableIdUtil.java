package nig.mf.pssd;

import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

public class CiteableIdUtil {

	// CIteable ID named roots
	public static final String PROJECT_ID_ROOT_NAME = "pssd.project";
	public static final String RSUBJECT_ID_ROOT_NAME = "pssd.r-subject";
	public static final String METHOD_ID_ROOT_NAME = "pssd.method";
	public static final String TRANSFORM_ID_ROOT_NAME = "pssd.transform";

	/**
	 * Depth of the project id root. e.g. 1.5
	 */
	public static final int PROJECT_ID_ROOT_DEPTH = 2;

	/**
	 * Depth of the project id. e.g. 1.5.1
	 */
	public static final int PROJECT_ID_DEPTH = 3;

	/**
	 * Depth of the subject id. e.g. 1.5.1.1
	 */
	public static final int SUBJECT_ID_DEPTH = 4;
	/**
	 * Depth of the ex-method id. e.g. 1.5.1.1.1
	 */
	public static final int EX_METHOD_ID_DEPTH = 5;
	/**
	 * Depth of the study id. e.g. 1.5.1.1.1.1
	 */
	public static final int STUDY_ID_DEPTH = 6;
	/**
	 * Depth of the dataset id. e.g. 1.5.1.1.1.1.1
	 */
	public static final int DATASET_ID_DEPTH = 7;
	/**
	 * Depth of the data-object id. e.g. 1.5.1.1.1.1.1.1
	 */
	public static final int DATA_OBJECT_ID_DEPTH = 8;

	/**
	 * Returns the server's root citeable identifier.
	 * 
	 * @param executor
	 * @param proute
	 *            Route to remote server. If null use local
	 * @return
	 * @throws Throwable
	 */
	public static String citeableIDRoot(ServiceExecutor executor, String proute) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		//
		XmlDoc.Element r = null;
		if (proute == null) {
			r = executor.execute("citeable.root.get", dm.root());
		} else {
			r = executor.execute(new ServerRoute(proute), "citeable.root.get", dm.root());
		}
		return r.value("cid");
	}

	/**
	 * Check if the string is a citeable id.
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isCiteableId(String s) {

		if (s == null) {
			return false;
		}
		
		return s.matches("^\\d+(\\d*.)*\\d+$");

	}

	/**
	 * Returns the project id.
	 * 
	 * @param id
	 * @return
	 */
	public static String getProjectId(String id) {

		int depth = getIdDepth(id);
		if (depth < PROJECT_ID_DEPTH || depth > DATA_OBJECT_ID_DEPTH) {
			return null;
		}
		int diff = depth - PROJECT_ID_DEPTH;
		return getParentId(id, diff);

	}

	/**
	 * Returns the parent id.
	 * 
	 * @param id
	 * @return
	 */
	public static String getParentId(String id) {

		int idx = id.lastIndexOf('.');
		if (idx == -1) {
			return null;
		}
		return id.substring(0, idx);

	}

	/**
	 * Returns the parent/ancester id by specifying the levels.
	 * 
	 * @param id
	 * @param levels
	 * @return
	 */

	public static String getParentId(String id, int levels) {

		for (int i = 0; i < levels; i++) {
			id = getParentId(id);
		}
		return id;

	}

	/**
	 * Depth of the given identifier. This is the number of dots.
	 * 
	 * @param id
	 * @return
	 */
	public static int getIdDepth(String id) {

		if (id == null || id.length() == 0) {
			return 0;
		}
		int depth = 1;
		int idx = id.indexOf('.');
		while (idx != -1) {
			depth++;
			idx = id.indexOf('.', idx + 1);
		}
		return depth;

	}

	/**
	 * Return the last section of the specified citeable id. e.g. the last
	 * section of 1.2.3 is 3
	 * 
	 * @param id
	 * @return
	 */
	public static String getLastSection(String id) {

		int idx = id.lastIndexOf('.');
		if (idx == -1) {
			return null;
		}
		return id.substring(idx + 1);

	}

	/**
	 * Replace left sections of the citeable id with the specified string.
	 * 
	 * @param id
	 * @param leftSections
	 * @return
	 */
	public static String replaceLeftSections(String id, String leftSections) {

		if (getIdDepth(id) < getIdDepth(leftSections)) {
			return null;
		}
		if (getIdDepth(id) == getIdDepth(leftSections)) {
			return leftSections;
		}
		String[] parts1 = id.split("\\.");
		String[] parts2 = leftSections.split("\\.");

		for (int i = 0; i < parts2.length; i++) {
			parts1[i] = parts2[i];
		}
		String newCid = parts1[0];
		for (int j = 1; j < parts1.length; j++) {
			newCid = newCid + "." + parts1[j];
		}
		return newCid;

	}

	public static boolean isProjectId(String id) {

		if (!isCiteableId(id)) {
			return false;
		}
		if (getIdDepth(id) == PROJECT_ID_DEPTH) {
			return true;
		}
		return false;

	}

	public static boolean isSubjectId(String id) {

		if (!isCiteableId(id)) {
			return false;
		}
		if (getIdDepth(id) == SUBJECT_ID_DEPTH) {
			return true;
		}
		return false;

	}

	public static boolean isExMethodId(String id) {

		if (!isCiteableId(id)) {
			return false;
		}
		if (getIdDepth(id) == EX_METHOD_ID_DEPTH) {
			return true;
		}
		return false;

	}

	public static boolean isStudyId(String id) {

		if (!isCiteableId(id)) {
			return false;
		}
		if (getIdDepth(id) == STUDY_ID_DEPTH) {
			return true;
		}
		return false;

	}

	public static boolean isDataSetId(String id) {

		if (!isCiteableId(id)) {
			return false;
		}
		if (getIdDepth(id) == DATASET_ID_DEPTH) {
			return true;
		}
		return false;

	}

	public static boolean isDataObjectId(String id) {

		if (!isCiteableId(id)) {
			return false;
		}
		if (getIdDepth(id) == DATA_OBJECT_ID_DEPTH) {
			return true;
		}
		return false;

	}

	public static boolean isProjectIdSegment(String s) {

		if (!isCiteableId(s)) {
			return false;
		}
		if (getIdDepth(s) == PROJECT_ID_DEPTH - PROJECT_ID_ROOT_DEPTH) {
			return true;
		} else {
			return false;
		}

	}

	public static boolean isSubjectIdSegment(String s) {

		if (!isCiteableId(s)) {
			return false;
		}
		if (getIdDepth(s) == SUBJECT_ID_DEPTH - PROJECT_ID_ROOT_DEPTH) {
			return true;
		} else {
			return false;
		}

	}

	public static boolean isExMethodIdSegment(String s) {

		if (!isCiteableId(s)) {
			return false;
		}
		if (getIdDepth(s) == EX_METHOD_ID_DEPTH - PROJECT_ID_ROOT_DEPTH) {
			return true;
		} else {
			return false;
		}

	}

	public static boolean isStudyIdSegment(String s) {

		if (!isCiteableId(s)) {
			return false;
		}
		if (getIdDepth(s) == STUDY_ID_DEPTH - PROJECT_ID_ROOT_DEPTH) {
			return true;
		} else {
			return false;
		}

	}

	public static boolean isDataSetIdSegment(String s) {

		if (!isCiteableId(s)) {
			return false;
		}
		if (getIdDepth(s) == DATASET_ID_DEPTH - PROJECT_ID_ROOT_DEPTH) {
			return true;
		} else {
			return false;
		}

	}

	public static boolean isDataObjectIdSegment(String s) {

		if (!isCiteableId(s)) {
			return false;
		}
		if (getIdDepth(s) == DATA_OBJECT_ID_DEPTH - PROJECT_ID_ROOT_DEPTH) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Returns the given string with leading non-digits removed. Will remove all
	 * prefix that are not numbers.
	 * 
	 * For instance,
	 * 
	 * AB9.1.2 -> 9.1.2 AB.8.2 -> 8.2
	 * 
	 * @param sid
	 * @return
	 */
	public static String removeLeadingNonDigits(String sid) {

		if (sid == null) {
			return null;
		}

		// Run through until we find a number..
		for (int i = 0; i < sid.length(); i++) {
			char ch = sid.charAt(i);
			if (Character.isDigit(ch)) {
				return sid.substring(i);
			}
		}

		return null;
	}

	/**
	 * Find out if a string is numeric. Useful to find out if a String is a CID
	 * 
	 * @param s
	 * @return true or false
	 * @throws Throwable
	 */
	public static boolean isNumeric(String s) throws Throwable {

		return s.matches("\\d+");
	}

}
