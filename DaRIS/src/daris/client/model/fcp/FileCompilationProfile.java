package daris.client.model.fcp;

import java.util.HashMap;
import java.util.Map;

import arc.mf.model.asset.task.AssetImportControls;

public class FileCompilationProfile {

	public static final String TYPE_NAME = "fcp";

	private String _assetId;
	private String _name;
	private String _description;

	private Map<String, String> _variables;

	protected FileCompilationProfile(String assetId, String name,
			String description) {
		_assetId = assetId;
		_name = name;
		_description = description;
	}

	public String id() {
		return _assetId;
	}

	public String name() {
		return _name;
	}

	public String description() {
		return _description;
	}

	public Map<String, String> variables() {
		return _variables;
	}

	protected void setVariable(String name, String value) {
		if (_variables == null) {
			_variables = new HashMap<String, String>();
		}
		_variables.put(name, value);
	}

	protected String variableValue(String name) {
		if (_variables == null) {
			return null;
		}
		return _variables.get(name);
	}

	@Override
	public String toString() {
		return _name;
	}

	public AssetImportControls assetImportControls() {
		AssetImportControls aic = new AssetImportControls();
		aic.setProfile(name());
		aic.setVariables(variables());
		return aic;
	}

	public static FileCompilationProfile create(String assetId, String name,
			String description) {
		if (name.equals(DicomIngestFCP.FCP_NAME)) {
			return new DicomIngestFCP(assetId, name, description);
		} else if (name.equals(ObjectAttachFCP.FCP_NAME)) {
			return new ObjectAttachFCP(assetId, name, description);
		} else if (name.equals(DerivationDataSetCreateFCP.FCP_NAME)) {
			return new DerivationDataSetCreateFCP(assetId, name, description);
		} else if (name.equals(PrimaryDataSetCreateFCP.FCP_NAME)) {
			return new PrimaryDataSetCreateFCP(assetId, name, description);
		} else {
			return new FileCompilationProfile(assetId, name, description);
		}
	}

}
