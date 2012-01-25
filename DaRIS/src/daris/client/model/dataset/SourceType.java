package daris.client.model.dataset;

public enum SourceType {
	primary, derivation;
	public static SourceType parse(String sourceType) {

		if (sourceType != null) {
			if (sourceType.equalsIgnoreCase(primary.toString())) {
				return primary;
			}
			if (sourceType.equalsIgnoreCase(derivation.toString())) {
				return derivation;
			}
		}
		return null;
	}
}
