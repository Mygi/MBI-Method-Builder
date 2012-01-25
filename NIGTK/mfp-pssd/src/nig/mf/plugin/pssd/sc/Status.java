package nig.mf.plugin.pssd.sc;

public enum Status {
	editable, await_processing, assigned, processing, data_ready, fulfilled, rejected, error, withdrawn, aborted;
	@Override
	public String toString() {

		return super.toString().replace('_', ' ');
	}

	public static Status instantiate(String status) throws Throwable {

		assert status != null;
		if (status.equalsIgnoreCase(editable.toString())) {
			return editable;
		}
		if (status.equalsIgnoreCase(await_processing.toString())) {
			return await_processing;
		}
		if (status.equalsIgnoreCase(assigned.toString())) {
			return assigned;
		}
		if (status.equalsIgnoreCase(processing.toString())) {
			return processing;
		}
		if (status.equalsIgnoreCase(data_ready.toString())) {
			return data_ready;
		}
		if (status.equalsIgnoreCase(fulfilled.toString())) {
			return fulfilled;
		}
		if (status.equalsIgnoreCase(rejected.toString())) {
			return rejected;
		}
		if (status.equalsIgnoreCase(error.toString())) {
			return error;
		}
		if (status.equalsIgnoreCase(withdrawn.toString())) {
			return withdrawn;
		}
		if (status.equalsIgnoreCase(aborted.toString())) {
			return aborted;
		}
		throw new Exception("Invalid status: " + status);
	}

	public static String[] stringValues() {

		Status[] values = values();
		String[] stringValues = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			stringValues[i] = values[i].toString();
		}
		return stringValues;
	}
}
