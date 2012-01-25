package nig.iio.dicom;

import arc.mf.plugin.dicom.DataElementTag;




public class DicomElements {
	
	public static final String DICOM_MODALITY_DICTIONARY = "pssd.dicom.modality";

	
	public static final DataElementTag REFERRING_PHYSICIANS_NAME = new DataElementTag(0x0008,0x0090);
	public static final DataElementTag MODALITY = new DataElementTag(0x0008,0x0060);
	public static final DataElementTag STATION_NAME = new DataElementTag(0x0008,0x1010);

	public static final DataElementTag PATIENT_NAME = new DataElementTag(0x0010,0x0010);
	public static final DataElementTag PATIENT_ID = new DataElementTag(0x0010,0x0020);
	public static final DataElementTag PATIENT_BIRTH_DATE = new DataElementTag(0x0010,0x0030);
	public static final DataElementTag PATIENT_BIRTH_TIME = new DataElementTag(0x0010,0x0032);
	public static final DataElementTag PATIENT_SEX = new DataElementTag(0x0010,0x0040);
	public static final DataElementTag PATIENT_AGE = new DataElementTag(0x0010,0x1010);
	public static final DataElementTag PATIENT_SIZE = new DataElementTag(0x0010,0x1020);
	public static final DataElementTag PATIENT_WEIGHT = new DataElementTag(0x0010,0x1030);
	
	public static final DataElementTag IMAGE_ORIENTATION_PATIENT = new DataElementTag(0x0020, 0x0037);
	public static final DataElementTag IMAGE_POSITION_PATIENT = new DataElementTag(0x0020, 0x0032);

	public static final DataElementTag PROTOCOL_NAME = new DataElementTag(0x0018,0x1030);

	public static final DataElementTag STUDY_UID = new DataElementTag(0x0020,0x000D);
	public static final DataElementTag STUDY_ID = new DataElementTag(0x0020,0x0010);
	public static final DataElementTag STUDY_DESCRIPTION = new DataElementTag(0x0008,0x1030);
	public static final DataElementTag STUDY_DATE = new DataElementTag(0x0008,0x0020);
	public static final DataElementTag STUDY_TIME = new DataElementTag(0x0008,0x0030);
	public static final DataElementTag SERIES_UID = new DataElementTag(0x0020,0x000E);
	public static final DataElementTag SERIES_NUMBER = new DataElementTag(0x0020,0x0011);
	public static final DataElementTag SERIES_DESCRIPTION = new DataElementTag(0x0008,0x103E);
	public static final DataElementTag SERIES_DATE = new DataElementTag(0x0008,0x0021);
	public static final DataElementTag SERIES_TIME = new DataElementTag(0x0008,0x0031);
	public static final DataElementTag ACQUISITION_NB = new DataElementTag(0x0020,0x0012);
	public static final DataElementTag INSTANCE_NB = new DataElementTag(0x0020,0x0013);
	public static final DataElementTag IMAGE_COMMENT = new DataElementTag(0x0020,0x4000);
	
	public static final DataElementTag EQUIPMENT_MANUFACTURER = new DataElementTag(0x0008,0x0070);
	public static final DataElementTag EQUIPMENT_MODEL = new DataElementTag(0x0008,0x1090);
	public static final DataElementTag INSTITUTION_NAME = new DataElementTag(0x0008,0x0080);
	
	public static final DataElementTag MAGNETIC_FIELD_STRENGTH = new DataElementTag(0x0018,0x0087);
	public static final DataElementTag SLICE_SPACING = new DataElementTag(0x0018,0x0088);


}
