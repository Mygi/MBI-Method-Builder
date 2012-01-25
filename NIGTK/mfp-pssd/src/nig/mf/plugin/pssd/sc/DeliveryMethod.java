package nig.mf.plugin.pssd.sc;

public enum DeliveryMethod {

	download, deposit;
	public static DeliveryMethod instantiate(String deliveryMethod) {
		if (deliveryMethod != null) {
			if (deliveryMethod.equalsIgnoreCase(deposit.toString())) {
				return deposit;
			}
		}
		return download;
	}
}
