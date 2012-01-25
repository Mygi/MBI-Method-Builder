package daris.client.model.fcp;

public class DerivationDataSetCreateFCP extends FileCompilationProfile {

	public static final String FCP_NAME = "pssd-dataset-derivation.fcp";
	public static final String VAR_PID = "pid";

	protected DerivationDataSetCreateFCP(String assetId, String name,
			String description) {
		super(assetId, name, description);
		setVariable(VAR_PID, null);
	}

	public void setPid(String pid) {
		setVariable(VAR_PID, pid);
	}

	public String pid() {
		return variableValue(VAR_PID);
	}

}