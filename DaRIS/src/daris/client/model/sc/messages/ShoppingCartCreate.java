package daris.client.model.sc.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.sc.Archive;
import daris.client.model.sc.DeliveryDestination;
import daris.client.model.sc.MetadataOutput;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartRef;

public class ShoppingCartCreate extends ObjectMessage<ShoppingCartRef> {

	private String _name;
	private Archive _archive;
	private DeliveryDestination _destination;
	private MetadataOutput _metadataOutput;

	public ShoppingCartCreate(String name, DeliveryDestination destination, Archive archive,
			MetadataOutput metadataOutput) {

		_name = name;
		_destination = destination;
		_archive = archive;
		_metadataOutput = metadataOutput;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		if (_name != null) {
			w.add("name", _name);
		}
		if (_destination != null) {
			w.add("destination", _destination.name());
		}
		if (_archive != null) {
			w.push("archive");
			w.add("type", _archive.type().toString());
			if (_archive.hasParameters()) {
				for (Archive.Parameter parameter : _archive.parameters()) {
					w.add("parameter", new String[] { "name", parameter.name() }, parameter.value().toString());
				}
			}
			w.pop();
		}
		if (_metadataOutput != null) {
			w.add("metadata-output", _metadataOutput.toString());
		}
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.shoppingcart.create";
	}

	@Override
	protected ShoppingCartRef instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			long scid = xe.longValue("scid");
			return new ShoppingCartRef(scid);
		}
		return null;
	}

	@Override
	protected String objectTypeName() {

		return ShoppingCart.TYPE_NAME;
	}

	@Override
	protected String idToString() {

		return null;
	}

}
