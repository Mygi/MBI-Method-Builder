package nig.mf.plugin.util;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DateUtil {

	/*
	 * Convert date to MF format dd-MMM-yyyy HH:mm:ss
	 * 
	 */
	public static String formatDate (java.util.Date date) throws Throwable {
		// Format the new date in MF date format
		SimpleDateFormat formatterOut = new SimpleDateFormat ("dd-MMM-yyyy HH:mm:ss");
		return formatterOut.format(date);
	}
	
	/**
	 * Convert date string between given  patterns
	 * 
	 * @param dateIn
	 * @param patternIn
	 * @param patternOut
	 * @return
	 * @throws Throwable
	 */
	public static String convertDateString  (String dateIn, String patternIn, String patternOut) throws Throwable  { 
		//
		SimpleDateFormat formatterIn = new SimpleDateFormat (patternIn);

		try {
			Date date = formatterIn.parse(dateIn);
			SimpleDateFormat formatterOut = new SimpleDateFormat (patternOut);
			return formatterOut.format(date);
		} catch (Throwable ex) {
			throw new RuntimeException ("The date string " + dateIn + " is not in an expected format");
		}
	}
	

}
