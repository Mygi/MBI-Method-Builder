package daris.client.model.sc.messages;

import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.sc.Archive;
import daris.client.model.sc.DeliveryDestination;
import daris.client.model.sc.MetadataOutput;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartRef;
import daris.client.model.transcode.Transcode;

public class ShoppingCartOrder extends ObjectMessage<Boolean> {

	private long _scid;
	private String _name;
	private DeliveryDestination _destination;
	private Archive _archive;
	private MetadataOutput _metadataOutput;
	private List<Transcode> _transcodes;

	public ShoppingCartOrder(long scid, String name, DeliveryDestination destination, Archive archive,
			MetadataOutput metadataOutput, List<Transcode> transcodes) {

		_scid = scid;
		_name = name;
		_destination = destination;
		_archive = archive;
		_metadataOutput = metadataOutput;
		_transcodes = transcodes;
	}

	public ShoppingCartOrder(long scid) {

		this(scid, null, null, null, null, null);
	}

	public ShoppingCartOrder(ShoppingCart cart) {

		this(cart.scid(), cart.name(), cart.destination(), cart.archive(), cart.medatadataOutput(), cart.transcodes());
	}

	public ShoppingCartOrder(ShoppingCartRef cart) {

		if (cart.referent() != null) {
			_scid = cart.referent().scid();
			_name = cart.referent().name();
			_destination = cart.referent().destination();
			_archive = cart.referent().archive();
			_metadataOutput = cart.referent().medatadataOutput();
			_transcodes = cart.referent().transcodes();
		} else {
			_scid = cart.scid();
			_name = null;
			_destination = null;
			_archive = null;
			_metadataOutput = null;
			_transcodes = null;
		}
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("scid", _scid);
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
		if (_transcodes != null) {
			for (Transcode transcode : _transcodes) {
				w.push("transcode");
				w.add("from", transcode.from());
				w.add("to", transcode.to());
				w.pop();
			}
		}
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.shoppingcart.order";
	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			return true;
		}
		return null;
	}

	@Override
	protected String objectTypeName() {

		return ShoppingCart.TYPE_NAME;
	}

	@Override
	protected String idToString() {

		return Long.toString(_scid);
	}

}
