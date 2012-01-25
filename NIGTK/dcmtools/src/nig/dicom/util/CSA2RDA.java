package nig.dicom.util;

import java.io.File;

import nig.dicom.siemens.CSAFileUtils;

public class CSA2RDA {

	/**
	 * The main class.
	 * 
	 * @param args
	 * @throws Throwable
	 */
	public static void main(String[] args) throws Throwable {

		String csaFileName = null;
		String rdaFileName = null;
		if (args.length == 1) {
			csaFileName = args[0];
			if (csaFileName.endsWith(".dcm")) {
				rdaFileName = csaFileName.substring(0, csaFileName.length() - 4) + ".rda";
			} else {
				rdaFileName = csaFileName + ".rda";
			}
		} else if (args.length == 2) {
			csaFileName = args[0];
			rdaFileName = args[1];
		} else {
			printUsage();
			System.exit(1);
		}

		CSAFileUtils.convertToSiemensRDA(new File(csaFileName), new File(rdaFileName));
	}

	/**
	 * Prints the usage.
	 */
	private static void printUsage() {

		System.out.println("Usage:");
		System.out.println("\t CSA2RDA    <CSA_DICOM_File>    [Siemens_RDA_File]");

	}

}
