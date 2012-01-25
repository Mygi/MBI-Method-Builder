package nig.mf.plugin.pssd.dicom;


public class Role {
	/**
	 * DICOM ingest role for DICOM users
	 *
	 * @return
	 */
	public static final String DICOM_INGEST = "dicom-ingest";
	
	public static String dicomIngestRoleName() {
		return "pssd.dicom-ingest";
	}
	
}
