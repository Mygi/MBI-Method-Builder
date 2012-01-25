package nig.mf.plugin.pssd.dicom;

import nig.iio.dicom.StudyMetadata;

/**
 * Some helper functions for DICOM meta-data
 * 
 * @author nebk
 *
 */
public class DICOMMetaUtil {

	private static final String BRUKER_SCANNER = "Bruker";
	private static final String SIEMENS_SCANNER = "Siemens";


	/**
	 * Are the data from a  Bruker scanner ?
	 * 
	 * @return
	 */
	public static boolean isBrukerScanner (StudyMetadata sm) {
		return scannerManufacturer(sm).equals(BRUKER_SCANNER);
	}
	
	
	/**
	 * Who was the manufacturer of the MR scanner ?
	 * Returns a standardized string for "Bruker" or "Siemens" 
	 * and just the DICOM element (0x0008,0x0070) otherwise
	 * 
	 * @param sm
	 * @return
	 */		
	private static String scannerManufacturer (StudyMetadata sm) {
		String manufacturer = sm.manufacturer().toUpperCase();
		if (manufacturer.contains("BRUKER")) {
			return  BRUKER_SCANNER;
		} else if (manufacturer.contains("SIEMENS")) {
			return SIEMENS_SCANNER;
		} else {
			return sm.manufacturer();
		}
	}
}
