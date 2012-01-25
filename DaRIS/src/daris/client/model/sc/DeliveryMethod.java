package daris.client.model.sc;

import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.EnumerationType.Value;

public enum DeliveryMethod {

	download, deposit;
	public static DeliveryMethod instantiate(String method) {

		if (method != null) {
			if (method.equalsIgnoreCase(deposit.toString())) {
				return deposit;
			}
			if (method.equalsIgnoreCase(download.toString())) {
				return download;
			}
		}
		return null;
	}

	public static EnumerationType<DeliveryMethod> asEnumerationType() {

		DeliveryMethod[] values = values();
		@SuppressWarnings("unchecked")
		Value<DeliveryMethod>[] vs = new Value[values.length];
		for (int i = 0; i < values.length; i++) {
			vs[i] = new Value<DeliveryMethod>(values[i].toString(), values[i].toString(), values[i]);
		}
		return new EnumerationType<DeliveryMethod>(vs);
	}
}
