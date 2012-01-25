package daris.client.model.project;

import java.util.List;
import java.util.Vector;

import arc.mf.dtype.EnumerationType;

public enum DataUse {

	SPECIFIC, EXTENDED, UNSPECIFIED;
	@Override
	public String toString() {

		return super.toString().toLowerCase();
	}

	public static EnumerationType<DataUse> asEnumerationType() {

		List<EnumerationType.Value<DataUse>> evs = new Vector<EnumerationType.Value<DataUse>>(
				values().length);
		for (int i = 0; i < values().length; i++) {
			evs.add(new EnumerationType.Value<DataUse>(values()[i].toString(),
					values()[i].toString(), values()[i]));
		}
		return new EnumerationType<DataUse>(evs);
	}

	public static DataUse parse(String dataUse) {

		if (dataUse != null) {
			if (dataUse.equalsIgnoreCase(SPECIFIC.toString())) {
				return SPECIFIC;
			}
			if (dataUse.equalsIgnoreCase(EXTENDED.toString())) {
				return EXTENDED;
			}
			if (dataUse.equalsIgnoreCase(UNSPECIFIED.toString())) {
				return UNSPECIFIED;
			}
		}
		return null;
	}
}
