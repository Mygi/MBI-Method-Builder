package daris.model.sc;

import arc.mf.dtype.EnumerationType;

public enum MetadataOutput {
	mediaflux, none;

	public static MetadataOutput instantiate(String mo) {

		if (mo.equalsIgnoreCase("mediaflux")) {
			return mediaflux;
		} else {
			return none;
		}
	}

	public static EnumerationType<MetadataOutput> enumerationType() {

		MetadataOutput[] vs = values();
		@SuppressWarnings("unchecked")
		EnumerationType.Value<MetadataOutput>[] evs = new EnumerationType.Value[vs.length];
		for (int i = 0; i < vs.length; i++) {
			evs[i] = new EnumerationType.Value<MetadataOutput>(
					vs[i].toString(), vs[i].toString(), vs[i]);
		}
		return new EnumerationType<MetadataOutput>(evs);
	}

	public static MetadataOutput DEFAULT = MetadataOutput.none;
}
