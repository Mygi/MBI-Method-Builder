package daris.client.model.sc;

import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class DeliveryDestinationListRef extends ObjectRef<List<DeliveryDestination>> {

	public DeliveryDestinationListRef() {

	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

	}

	@Override
	protected String resolveServiceName() {

		return "om.pssd.shoppingcart.destination.list";
	}

	@Override
	protected List<DeliveryDestination> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			List<XmlElement> ddes = xe.elements("destination");
			if (ddes != null) {
				return DeliveryDestination.instantiate(ddes);
			}
		}
		return null;
	}

	@Override
	public String referentTypeName() {

		return "destinations";
	}

	@Override
	public String idToString() {

		return null;
	}

}
