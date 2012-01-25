package nig.mf.plugin.pssd;

public class PSSDObjectEventFilterFactory implements arc.event.FilterFactory {

	public static PSSDObjectEventFilterFactory INSTANCE = new PSSDObjectEventFilterFactory();

	private PSSDObjectEventFilterFactory() {

	}

	@Override
	public arc.event.Filter create(String id, boolean descend) {

		return new PSSDObjectEvent.Filter(id);
	}

}
