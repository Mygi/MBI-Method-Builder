package daris.client.model.fcp;

public class PrimaryDataSetCreateFCP extends FileCompilationProfile {

	public static final String FCP_NAME = "pssd-dataset-primary.fcp";
	public static final String VAR_PID = "pid";
	public static final String VAR_NAME = "name";

	protected PrimaryDataSetCreateFCP(String assetId, String name,
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

	public void setDataSetName(String name) {
		setVariable(VAR_NAME, name);
	}

	public String dataSetName() {
		return variableValue(VAR_NAME);
	}
}