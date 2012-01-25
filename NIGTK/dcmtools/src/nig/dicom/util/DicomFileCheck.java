package nig.dicom.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomFileUtilities;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.TagFromName;

public class DicomFileCheck {

	public static boolean isDicomFile(File f) {
		if (DicomFileUtilities.isDicomOrAcrNemaFile(f)) {
			return true;
		}
		try {
			DicomInputStream in = new DicomInputStream(f);
			AttributeList al = new AttributeList();
			al.read(f);
			String sopClass = Attribute.getSingleStringValueOrNull(al, TagFromName.MediaStorageSOPClassUID);
			String sopInstance = Attribute.getSingleStringValueOrNull(al, TagFromName.MediaStorageSOPInstanceUID);
			String transferSyntax = Attribute.getSingleStringValueOrNull(al, TagFromName.TransferSyntaxUID);
			if (sopClass == null || sopInstance == null || transferSyntax == null) {
				sopClass = Attribute.getSingleStringValueOrNull(al, TagFromName.SOPClassUID);
				sopInstance = Attribute.getSingleStringValueOrNull(al, TagFromName.SOPInstanceUID);
				transferSyntax = in.getTransferSyntaxInUse().getUID();
			}
			in.close();
			if (sopClass == null || sopInstance == null || transferSyntax == null) {
				return false;
			} else {
				return true;
			}
		} catch (Throwable t) {
			return false;
		}
	}

	public static boolean isDicomFile(String f) {
		return isDicomFile(new File(f));
	}

	public static boolean hasFileMetaInformation(File f) {
		try {
			DicomInputStream in = new DicomInputStream(f);
			AttributeList al = new AttributeList();
			al.readOnlyMetaInformationHeader(in);
			String mediaStorageSOPClassUID = Attribute.getSingleStringValueOrNull(al,
					TagFromName.MediaStorageSOPClassUID);
			String mediaStorageSOPInstanceUID = Attribute.getSingleStringValueOrNull(al,
					TagFromName.MediaStorageSOPInstanceUID);
			String transferSyntaxUID = Attribute.getSingleStringValueOrNull(al, TagFromName.TransferSyntaxUID);
			in.close();
			if (mediaStorageSOPClassUID != null && mediaStorageSOPInstanceUID != null && transferSyntaxUID != null) {
				return true;
			} else {
				return false;
			}
		} catch (Throwable t) {
			return false;
		}
	}

	public static boolean hasPreamble(File f) {

		byte[] b = new byte[132];
		DataInputStream in;
		try {
			in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
			in.readFully(b);
			in.close();
			if (new String(b, 128, 4).equals("DICM")) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}

	}

	public static void main(String[] args) {

		if (args.length != 1) {
			printUsage();
			System.exit(7);
		}
		File f = new File(args[0]);
		boolean isDicomFile = isDicomFile(f);
		boolean hasPreamble = hasPreamble(f);
		boolean hasFileMeta = hasFileMetaInformation(f);
		int bIsDicom = isDicomFile ? 0 : 1;
		int bHasPreamble = hasPreamble ? 0 : 2;
		int bHasFileMeta = hasFileMeta ? 0 : 4;
		int exitCode = bIsDicom + bHasPreamble + bHasFileMeta;
		System.out.println("isDicomFile = " + isDicomFile);
		System.out.println("hasPreamble = " + hasPreamble);
		System.out.println("hasFileMeta = " + hasFileMeta);
		System.exit(exitCode);

	}

	private static void printUsage() {

		System.out.println("Usage:");
		System.out.println("\t DCMCHK    <dicom-file>");

	}

}
