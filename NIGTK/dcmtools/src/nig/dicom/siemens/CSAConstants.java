package nig.dicom.siemens;

import com.pixelmed.dicom.AttributeTag;

public class CSAConstants {

	/*
	 * Siemens Private Tags for CSA NON IMAGAE
	 */
	public static final AttributeTag TAG_CSAImageHeaderInfo = new AttributeTag(
			0x0029, 0x1110);
	public static final AttributeTag TAG_CSASeriesHeaderInfo = new AttributeTag(
			0x0029, 0x1120);
	public static final AttributeTag TAG_CSADataType = new AttributeTag(
			0x7fe1, 0x0010);
	public static final AttributeTag TAG_CSAData = new AttributeTag(0x7fe1,
			0x1010);
	
	/*
	 * Siemens SOP Class UID for CSA NON IMAGE
	 */
	public static final String UID_CSANonImageStorage = "1.3.12.2.1107.5.9.1";

}
