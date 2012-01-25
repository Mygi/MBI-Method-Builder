package daris.client.util;

import com.google.gwt.regexp.shared.RegExp;

public class StringUtil {

	public static boolean isValidFileName(String fn) {

		RegExp re = RegExp.compile("^[^\\/:*?\"<>|]+$");
		return re.test(fn);
	}

	public static native String upperCaseFirst(String s) /*-{

		return s.charAt(0).toUpperCase() + s.slice(1);
	}-*/;

}
