package nig.dicom.util;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import nig.io.FileUtils;
import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TransferSyntax;

//import com.pixelmed.dicom.DicomFileUtilities;

public class DicomModify {

	public static boolean backup = false;
	public static String Default_AETitle = "DCMTOOLS";

	public static void main(String[] args) throws Throwable {

		if (args.length < 2) {
			printUsage();
			System.exit(1);
		}

		int start = 0;
		if (args[0].equals("-b") || args[0].equals("--backup")) {
			if (args.length < 3) {
				printUsage();
				System.exit(1);
			}
			backup = true;
			start++;
		}
		String av = args[start];
		if ((av.startsWith("\"") || av.startsWith("'"))
				&& !(av.endsWith("\"") || av.endsWith("'"))
				&& args.length > start + 1) {
			String av2 = args[start + 1];
			if (!(av2.startsWith("\"") || av2.startsWith("'"))
					&& (av2.endsWith("\"") || av2.endsWith("'"))) {
				av += " " + av2;
				start++;
			}
		}
		AttributeTag aTag = parseAttributeTag(av);
		String aValue = parseAttributeValue(av);
		for (int i = start + 1; i < args.length; i++) {
			File f = new File(args[i]);
			if (!f.exists()) {
				System.err.println("Error: " + f.getAbsolutePath()
						+ " does not exist.");
				continue;
			}
			if (!DicomFileCheck.isDicomFile(f)) {
				System.err.println("Error: " + args[i]
						+ " is not a valid DICOM file.");
				continue;
			}

			if (backup) {
				// Make a backup.
				File bf = new File(args[i] + ".bak");
				FileUtils.copyFile(f, bf);
			}

			editFile(f, aTag, aValue);
		}

	}

	/**
	 * Edit the file with the specified tag and value
	 * 
	 * @param f
	 * @param aTag
	 * @param aValue
	 * @throws Throwable
	 */
	public static void editFile(File f, AttributeTag aTag, String aValue)
			throws Throwable {

		// System.out.print("Modifying " + f.getPath() + "... ");
		AttributeList list = new AttributeList();
		list.read(f);

		Attribute mediaStorageSOPClassUIDAttr = list
				.get(TagFromName.MediaStorageSOPClassUID);
		String mediaStorageSOPClassUID = null;
		if (mediaStorageSOPClassUIDAttr != null) {
			mediaStorageSOPClassUID = mediaStorageSOPClassUIDAttr
					.getSingleStringValueOrNull();
		}
		Attribute mediaStorageSOPInstanceUIDAttr = list
				.get(TagFromName.MediaStorageSOPInstanceUID);
		String mediaStorageSOPInstanceUID = null;
		if (mediaStorageSOPInstanceUIDAttr != null) {
			mediaStorageSOPInstanceUID = mediaStorageSOPInstanceUIDAttr
					.getSingleStringValueOrNull();
		}
		// String implementationClassUID =
		// list.get(TagFromName.ImplementationClassUID).getSingleStringValueOrNull();
		// String implementationVersionName =
		// list.get(TagFromName.ImplementationVersionName).getSingleStringValueOrNull();

		/*
		 * Cleanup
		 */
		list.removeGroupLengthAttributes();
		list.removeMetaInformationHeaderAttributes();
		list.remove(TagFromName.DataSetTrailingPadding);

		if (mediaStorageSOPClassUID != null
				&& mediaStorageSOPInstanceUID != null) {
			FileMetaInformation.addFileMetaInformation(list,
					mediaStorageSOPClassUID, mediaStorageSOPInstanceUID,
					TransferSyntax.ExplicitVRLittleEndian, Default_AETitle);
		} else {
			FileMetaInformation.addFileMetaInformation(list,
					TransferSyntax.ExplicitVRLittleEndian, Default_AETitle);
		}

		Attribute attr = list.get(aTag);
		if (attr != null) {
			attr.setValue(aValue);
		} else {
			list.putNewAttribute(aTag).addValue(aValue);
		}
		list.write(new FileOutputStream(f),
				TransferSyntax.ExplicitVRLittleEndian, true, true);
		//System.out.println("done");
	}

	public static void printUsage() {

		System.out.println("Usage:");
		System.out
				.println("\t DCMODIFY  [options]  <tag=\"value\">  <dicom-files>\n");
		System.out.println("\t Options:");
		System.out
				.println("\t          -b|--backup                       create a backup file with extension .bak\n");
//		System.out
//				.println("\t          -o|--output <output-directory>    save the output into the specified directory.\n");
		System.out.println("Examples:");
		System.out
				.println("\t DCMODIFY    \"(0010,0010)=Smith^John\"  test.dcm");
		System.out.println("\t DCMODIFY    \"(0010,0010)=Smith^Tom\"  ./*.dcm");
		System.out.println("\t DCMODIFY    \"(0010,0020)=12345\"  ./*.dcm");
		System.out
				.println("\t DCMODIFY -b \"(0010,0010)=Smith^John\"  test.dcm");
		System.out.println("\t DCMODIFY -b \"(0010,0010)=Smith^Tom\"  ./*.dcm");
		System.out
				.println("\t DCMODIFY --backup  \"(0010,0020)=12345\"  ./*.dcm");

		// printTagNames();

	}

	public static void printTagNames() {

		System.out.println("Tags and Tag Names:");
		System.out.println("\t [Tag] \t\t [Tag Name]");

		Field[] fields = AttributeTag.class.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			try {
				AttributeTag tag = (AttributeTag) fields[i].get(null);
				String tagName = fields[i].getName();
				System.out.println("\t" + tag.toString() + "\t\t" + tagName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private static String parseAttributeValue(String arg) throws Throwable {

		String aValue = null;
		String[] parts = arg.split("=");
		aValue = parts[1];
		while (aValue.startsWith("\"") || aValue.startsWith("'")) {
			aValue = aValue.substring(1, aValue.length());
		}
		while (aValue.endsWith("\"") || aValue.endsWith("'")) {
			aValue = aValue.substring(0, aValue.length() - 1);
		}
		return aValue;

	}

	private static AttributeTag parseAttributeTag(String arg) throws Throwable {

		AttributeTag aTag = null;
		String[] parts = arg.split("=");
		String tag = parts[0];
		while (tag.startsWith("\"") || tag.startsWith("'")
				|| tag.startsWith("(")) {
			tag = tag.substring(1);
		}
		while (tag.endsWith("\"") || tag.endsWith("'") || tag.endsWith(")")) {
			tag = tag.substring(0, tag.length() - 1);
		}
		String[] parts2 = tag.split(",");
		String hGroup = parts2[0];
		int group = Integer.parseInt(hGroup, 16);
		String hElement = parts2[1];
		int element = Integer.parseInt(hElement, 16);
		aTag = new AttributeTag(group, element);
		return aTag;

	}

}
