package daris.client.model.sc.messages;

import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.sc.DeliveryDestination;

public class ShoppingCartDestinationList extends
		ObjectMessage<List<DeliveryDestination>> {

	public ShoppingCartDestinationList() {

	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

	}

	@Override
	protected String messageServiceName() {
		return "om.pssd.shoppingcart.destination.list";
	}

	@Override
	protected List<DeliveryDestination> instantiate(XmlElement xe)
			throws Throwable {
		if (xe != null) {
			List<XmlElement> des = xe.elements("destination");
			if (des != null) {
				return DeliveryDestination.instantiate(des);
			}
		}
		return null;
	}

	@Override
	protected String objectTypeName() {
		return null;
	}

	@Override
	protected String idToString() {
		return null;
	}

}
