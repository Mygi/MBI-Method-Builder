package nig.dicom.util;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DicomDictionary;
import com.pixelmed.dicom.DicomInputStream;
import java.io.File;
import java.util.Iterator;

public class DicomDump {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {

		if (args.length != 1) {
			System.err.println("Error: Invalid number of arguments.");
			printUsage();
			System.exit(1);
		}
		
		File dcmFile = new File(args[0]);
		if(!dcmFile.exists()){
			System.err.println("Error: " + args[0] + "does not exist.");
			System.exit(1);
		}

		try {
			DicomInputStream dis = new DicomInputStream(dcmFile);
			AttributeList list = new AttributeList();
			list.read(dis);
			DicomDictionary dict = new DicomDictionary();			
			for(Iterator<AttributeTag> it = list.keySet().iterator();it.hasNext();){
				AttributeTag tag = it.next();
				Attribute attr = list.get(tag);
				String tagString = tag.toString();
				String tagName = dict.getNameFromTag(tag);
				if(tagName==null){
					tagName="";
				}
				System.out.println(String.format("%15s  VR=<%2s> VL=<0x%08X> %-36s\t <%s>", tagString, attr.getVRAsString(), attr.getVL(), tagName, attr.getDelimitedStringValuesOrEmptyString()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	static void printUsage() {

		System.out.println("Usage:");
		System.out.println("\t DCMDUMP    <dicom-file>");

	}

}
