package nig.dicom.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomFileUtilities;
import com.pixelmed.dicom.TagFromName;

public class DicomFileOrganize {

	public static void main(String[] args) {

		if (args.length != 2) {
			printUsage();
			System.exit(1);
		}
		File srcDir = new File(args[0]);
		File dstDir = new File(args[1]);
		if (!srcDir.exists()) {
			System.err.println("Error: source dicom directory " + srcDir.getAbsolutePath() + "does not exist");
			System.exit(0);
		}

		organizeDicomDirectory(srcDir, dstDir);
	}

	public static void printUsage() {

		System.out.println("Usage:");
		System.out.println("\t DCMORGANIZE    <SrcDicomDirectory>    <DstDicomDirectory>");

	}

	public static void organizeDicomDirectory(File dcmDir, File dstDirRoot) {

		File[] entries = dcmDir.listFiles();
		for (File entry : entries) {
			if (entry.isDirectory()) {
				organizeDicomDirectory(entry, dstDirRoot);
			} else {
				if (entry.isFile()) {
					organizeDicomFile(entry, dstDirRoot);
				}
			}
		}

	}

	public static void organizeDicomFile(File dcmFile, File dstDirRoot) {

		if (!DicomFileUtilities.isDicomOrAcrNemaFile(dcmFile)) {
			return;
		}

		AttributeList list = new AttributeList();

		try {
			list.read(dcmFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (DicomException e) {
			e.printStackTrace();
			return;
		}

		String patientDirName = selectPatientDirName(list);
		String studyDirName = selectStudyDirName(list);
		String seriesDirName = selectSeriesDirName(list);
		String seriesInstanceUID = getSeriesInstanceUID(list);
		String instanceNumber = getInstanceNumber(list);

		String toFilename = instanceNumber;
		if (toFilename != null) {
			try {
				toFilename = String.format("%08d", Integer.parseInt(toFilename));
			} catch (Throwable t) {
				t.printStackTrace();
			}
		} else {
			toFilename = dcmFile.getName();
		}
		if (!toFilename.endsWith(".dcm")) {
			toFilename = toFilename + ".dcm";
		}

		String dstDirPath = dstDirRoot.getAbsolutePath() + "/" + patientDirName + "/" + studyDirName + "/"
				+ seriesDirName;

		File dstDir = new File(dstDirPath);
		dstDir.mkdirs();

		File toFile = new File(dstDir, toFilename);
		/*
		 * To Test (start)
		 */
		if (toFile.exists()) {
			if (DicomFileUtilities.isDicomOrAcrNemaFile(toFile)) {
				AttributeList list2 = new AttributeList();
				try {
					list2.read(toFile.getAbsolutePath());
				} catch (IOException e) {
					e.printStackTrace();
					return;
				} catch (DicomException e) {
					e.printStackTrace();
					return;
				}
				String seriesInstanceUID2 = getSeriesInstanceUID(list2);
				String instanceNumber2 = getInstanceNumber(list2);
				if (seriesInstanceUID.equals(seriesInstanceUID2) && instanceNumber.equals(instanceNumber2)) {
					System.err.println("Warning: " + toFile.getAbsolutePath() + " already exists. Skipped.");
					return;
				}
			}
		}
		while (toFile.exists()) {
			char firstLetter = toFilename.charAt(0);
			if (firstLetter == 'z' || (firstLetter >= '0' && firstLetter <= '9')) {
				toFilename = 'a' + toFilename;
				toFile = new File(dstDir, toFilename);
				continue;
			}
			if (firstLetter >= 'a' && firstLetter < 'z') {
				firstLetter++;
				toFilename = firstLetter + toFilename.substring(1);
				toFile = new File(dstDir, toFilename);
				continue;
			}
		}
		/*
		 * To Test (end)
		 */

		System.out.print("Copying " + dcmFile.getAbsolutePath() + " to " + toFile.getAbsolutePath() + "... ");
		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(dcmFile));
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(toFile));

			byte[] buffer = new byte[4096];
			int length;
			while ((length = bis.read(buffer)) > 0) {
				bos.write(buffer, 0, length);
			}
			bis.close();
			bos.close();
			System.out.println("done");
		} catch (Throwable t) {
			t.printStackTrace();
		}

	}

	private static String selectPatientDirName(AttributeList list) {

		String patientName = getPatientName(list);
		if (patientName != null) {
			patientName = formatStringForFilename(patientName);
		}
		String patientID = getPatientID(list);
		if (patientID != null) {
			patientID = formatStringForFilename(patientID);
		}
		String patientDirName = "";

		if (patientID != null && patientName != null) {
			patientDirName = patientID + "-" + patientName;
		} else if (patientName != null) {
			patientDirName = patientName;
		} else if (patientID != null) {
			patientDirName = patientID;
		} else {
			patientDirName = "unknown_patient";
		}

		return patientDirName;

	}

	private static String selectStudyDirName(AttributeList list) {

		String studyID = getStudyID(list);
		if (studyID != null) {
			studyID = formatStringForFilename(studyID);
		}
		String studyDescription = getStudyDescription(list);
		if (studyDescription != null) {
			studyDescription = formatStringForFilename(studyDescription);
		}
		String studyInstanceUID = getStudyInstanceUID(list);
		if (studyInstanceUID != null) {
			studyInstanceUID = formatStringForFilename(studyInstanceUID);
		}
		String studyDirName = "";

		if (studyID != null && studyDescription != null) {
			studyDirName = studyID + "-" + studyDescription;
		} else if (studyID != null) {
			studyDirName = studyID;
		} else if (studyDescription != null) {
			studyDirName = studyDescription;
		} else if (studyInstanceUID != null) {
			studyDirName = studyInstanceUID;
		} else {
			studyDirName = "unknown_study";
		}

		return studyDirName;

	}

	private static String selectSeriesDirName(AttributeList list) {

		String protocolName = getProtocolName(list);
		if (protocolName != null) {
			protocolName = formatStringForFilename(protocolName);
		}
		String seriesDescription = getSeriesDescription(list);
		if (seriesDescription != null) {
			seriesDescription = formatStringForFilename(seriesDescription);
		}
		String scanningSequence = getScanningSequence(list);
		if (scanningSequence != null) {
			scanningSequence = formatStringForFilename(scanningSequence);
		}
		String seriesNumber = getSeriesNumber(list);
		if (seriesNumber != null) {
			seriesNumber = formatStringForFilename(seriesNumber);
		}
		String seriesInstanceUID = getSeriesInstanceUID(list);
		if (seriesInstanceUID != null) {
			seriesInstanceUID = formatStringForFilename(seriesInstanceUID);
		}
		String seriesDirName = "";

		if (seriesNumber != null && protocolName != null) {
			seriesDirName = seriesNumber + "-" + protocolName;
		} else if (seriesNumber != null && seriesDescription != null) {
			seriesDirName = seriesNumber + "-" + seriesDescription;
		} else if (seriesNumber != null && scanningSequence != null) {
			seriesDirName = seriesNumber + "-" + scanningSequence;
		} else if (protocolName != null) {
			seriesDirName = protocolName;
		} else if (seriesDescription != null) {
			seriesDirName = seriesDescription;
		} else if (seriesNumber != null) {
			seriesDirName = seriesNumber;
		} else if (scanningSequence != null) {
			seriesDirName = scanningSequence;
		} else if (seriesInstanceUID != null) {
			seriesDirName = seriesInstanceUID;
		} else {
			seriesDirName = "unknown_series";
		}
		return seriesDirName;

	}

	private static String formatStringForFilename(String str) {

		String newStr = str.trim();
		newStr = newStr.replaceAll("[ ?<>|^:/\\*\\\\]", "_");
		newStr = newStr.replaceAll("[\'\"]", "");

		return newStr;

	}

	private static String getPatientName(AttributeList list) {

		Attribute patientNameAttr = list.get(TagFromName.PatientName);
		String patientName = null;
		if (patientNameAttr != null) {
			patientName = patientNameAttr.getDelimitedStringValuesOrNull();
		}
		return patientName;

	}

	private static String getPatientID(AttributeList list) {

		Attribute patientIDAttr = list.get(TagFromName.PatientID);
		String patientID = null;
		if (patientIDAttr != null) {
			patientID = patientIDAttr.getDelimitedStringValuesOrNull();
		}
		return patientID;

	}

	private static String getStudyID(AttributeList list) {

		Attribute studyIDAttr = list.get(TagFromName.StudyID);
		String studyID = null;
		if (studyIDAttr != null) {
			studyID = studyIDAttr.getDelimitedStringValuesOrNull();
		}
		return studyID;

	}

	private static String getStudyDescription(AttributeList list) {

		Attribute studyDescriptionAttr = list.get(TagFromName.StudyDescription);
		String studyDescription = null;
		if (studyDescriptionAttr != null) {
			studyDescription = studyDescriptionAttr.getDelimitedStringValuesOrNull();
		}
		return studyDescription;

	}

	private static String getStudyInstanceUID(AttributeList list) {

		Attribute studyInstanceUIDAttr = list.get(TagFromName.StudyInstanceUID);
		String studyInstanceUID = null;
		if (studyInstanceUIDAttr != null) {
			studyInstanceUID = studyInstanceUIDAttr.getDelimitedStringValuesOrNull();
		}
		return studyInstanceUID;

	}

	private static String getProtocolName(AttributeList list) {

		Attribute protocolNameAttr = list.get(TagFromName.ProtocolName);
		String protocolName = null;
		if (protocolNameAttr != null) {
			protocolName = protocolNameAttr.getDelimitedStringValuesOrNull();
		}
		return protocolName;

	}

	private static String getSeriesDescription(AttributeList list) {

		Attribute seriesDescriptionAttr = list.get(TagFromName.SeriesDescription);
		String seriesDescription = null;
		if (seriesDescriptionAttr != null) {
			seriesDescription = seriesDescriptionAttr.getDelimitedStringValuesOrNull();
		}
		return seriesDescription;

	}

	private static String getScanningSequence(AttributeList list) {

		Attribute scanningSequenceAttr = list.get(TagFromName.ScanningSequence);
		String scanningSequence = null;
		if (scanningSequenceAttr != null) {
			scanningSequence = scanningSequenceAttr.getDelimitedStringValuesOrNull();
		}
		return scanningSequence;

	}

	private static String getSeriesInstanceUID(AttributeList list) {

		Attribute seriesInstanceUIDAttr = list.get(TagFromName.SeriesInstanceUID);
		String seriesInstanceUID = null;
		if (seriesInstanceUIDAttr != null) {
			seriesInstanceUID = seriesInstanceUIDAttr.getDelimitedStringValuesOrNull();
		}
		return seriesInstanceUID;

	}

	private static String getSeriesNumber(AttributeList list) {

		Attribute seriesNumberAttr = list.get(TagFromName.SeriesNumber);
		String seriesNumber = null;
		if (seriesNumberAttr != null) {
			seriesNumber = seriesNumberAttr.getDelimitedStringValuesOrNull();
		}
		return seriesNumber;

	}

	private static String getInstanceNumber(AttributeList list) {

		Attribute instanceNumberAttr = list.get(TagFromName.InstanceNumber);
		String instanceNumber = null;
		if (instanceNumberAttr != null) {
			instanceNumber = instanceNumberAttr.getDelimitedStringValuesOrNull();
		}
		return instanceNumber;

	}

	// private static String getSOPInstanceUID(AttributeList list) {
	//
	// Attribute SOPInstanceUIDAttr = list.get(TagFromName.SOPInstanceUID);
	// String SOPInstanceUID = null;
	// if (SOPInstanceUIDAttr != null) {
	// SOPInstanceUID = SOPInstanceUIDAttr
	// .getDelimitedStringValuesOrNull();
	// }
	// return SOPInstanceUID;
	//
	// }

}
