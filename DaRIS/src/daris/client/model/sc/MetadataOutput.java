package daris.client.model.sc;

public enum MetadataOutput {
	none, mediaflux;

	public static MetadataOutput instantiate(String metadataOutput) {
		if (metadataOutput != null) {
			if (metadataOutput.equalsIgnoreCase(none.toString())) {
				return none;
			} else if (metadataOutput.equalsIgnoreCase(none.toString())) {
				return mediaflux;
			}
		}
		return none;
	}

	public static String[] stringValues() {
		MetadataOutput[] values = values();
		String[] stringValues = new String[values.length];
		for (int i = 0; i < values.length; i++) {
			stringValues[i] = values[i].toString();
		}
		return stringValues;
	}
}
