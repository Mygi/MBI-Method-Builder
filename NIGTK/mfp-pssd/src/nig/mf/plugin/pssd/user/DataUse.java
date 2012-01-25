package nig.mf.plugin.pssd.user;

public enum DataUse {

	specific, extended, unspecified;
	public static DataUse instantiate(String dataUse) {

		if (dataUse != null) {
			if (dataUse.equalsIgnoreCase(specific.toString())) {
				return specific;
			} else if (dataUse.equalsIgnoreCase(extended.toString())) {
				return extended;
			} else if (dataUse.equalsIgnoreCase(unspecified.toString())) {
				return unspecified;
			}
		}
		return null;
	}

}
