package daris.client.model.fcp;

public class ObjectAttachFCP extends FileCompilationProfile {

	public static final String FCP_NAME = "pssd-object-attach.fcp";
	public static final String VAR_ID = "id";
	public static final String VAR_NAME = "name";
	public static final String VAR_DESCRIPTION = "description";

	protected ObjectAttachFCP(String assetId, String name, String description) {
		super(assetId, name, description);
		setVariable(VAR_ID, null);
		setVariable(VAR_NAME, null);
		setVariable(VAR_DESCRIPTION, null);
	}

	public void setObjectId(String id) {
		setVariable(VAR_ID, id);
	}

	public String objectId() {
		return variableValue(VAR_ID);
	}

	public void setAttachmentName(String name) {
		setVariable(VAR_NAME, name);
	}

	public String attachmentName() {
		return variableValue(VAR_NAME);
	}

	public void setAttachmentDescription(String description) {
		setVariable(VAR_DESCRIPTION, description);
	}

	public String attachmentDescription() {
		return variableValue(VAR_DESCRIPTION);
	}

}
