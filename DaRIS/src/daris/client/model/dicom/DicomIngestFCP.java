package daris.client.model.dicom;

import java.util.HashMap;
import java.util.Map;

import arc.mf.model.asset.task.AssetImportControls;

public class DicomIngestFCP {

	public static final String VAR_ID_CITEABLE = "nig.dicom.id.citable";
	public static final String VAR_ANONYMIZE = "anonymize";

	private String _name;

	public DicomIngestFCP(String name) {
		_name = name;
	}

	public String name() {
		return _name;
	}

	public AssetImportControls createAssetImportControls(String pid,
			boolean anonymize) {
		AssetImportControls aic = new AssetImportControls();
		aic.setProfile(_name);
		Map<String, String> vars = new HashMap<String, String>(2);
		vars.put(VAR_ID_CITEABLE, pid);
		vars.put(VAR_ANONYMIZE, Boolean.toString(anonymize));
		aic.setVariables(vars);
		return aic;
	}

	@Override
	public String toString() {
		return _name;
	}

}
