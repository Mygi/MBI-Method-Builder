package daris.client.model.fcp;


public class DicomIngestFCP extends FileCompilationProfile {
	public static final String FCP_NAME = "pssd-dicom-ingest.fcp";
	public static final String VAR_ANONYMIZE = "anonymize";
	public static final String VAR_ID_CITABLE = "nig.dicom.id.citable";

	protected DicomIngestFCP(String assetId, String name, String description) {
		super(assetId, name, description);
		setVariable(VAR_ANONYMIZE, Boolean.toString(true));
		setVariable(VAR_ID_CITABLE, null);
	}

	public void setAnonymize(boolean anonymize) {
		setVariable(VAR_ANONYMIZE, Boolean.toString(anonymize));
	}

	public boolean anonymize() {
		String v = variableValue(VAR_ANONYMIZE);
		if (v != null) {
			return Boolean.parseBoolean(v);
		}
		return false;
	}

	public void setIdCitable(String idCitable) {
		setVariable(VAR_ID_CITABLE, idCitable);
	}

	public String idCitable() {
		return variableValue(VAR_ID_CITABLE);
	}
}
