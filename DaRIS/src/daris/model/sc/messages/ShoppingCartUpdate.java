package daris.model.sc.messages;

import java.util.Collection;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.model.sc.Destination;
import daris.model.sc.Layout;
import daris.model.sc.MetadataOutput;
import daris.model.sc.ShoppingCart;
import daris.model.sc.ShoppingCartRef;
import daris.model.sc.archive.Archive;
import daris.model.transcode.Transcode;

public class ShoppingCartUpdate extends ObjectMessage<Boolean> {

	private String _id;

	private String _name;

	private Destination _destination;

	private Layout _layout;

	private Archive _archive;

	private MetadataOutput _metadataOutput;

	private Collection<Transcode> _transcodes;

	public ShoppingCartUpdate(ShoppingCartRef cart) {

		this(cart.id(), cart.name(), cart.destination(), cart.layout(), cart
				.archive(), cart.metadataOutput(), cart.transcodes());
	}

	public ShoppingCartUpdate(ShoppingCart cart) {

		this(cart.id(), cart.name(), cart.destination(), cart.layout(), cart
				.archive(), cart.metadataOutput(), cart.transcodes());
	}

	public ShoppingCartUpdate(String id, String name, Destination destination,
			Layout layout, Archive archive, MetadataOutput mo,
			Collection<Transcode> transcodes) {

		_id = id;
		_name = name;
		_destination = destination;
		_layout = layout;
		_archive = archive;
		_metadataOutput = mo;
		_transcodes = transcodes;
	}

	public void setName(String name) {

		_name = name;
	}

	public Destination destination() {

		return _destination;
	}

	public void setDestination(Destination destination) {

		_destination = destination;
	}

	public Layout layout() {

		return _layout;
	}

	public void setLayout(Layout layout) {

		_layout = layout;
	}

	public Archive archive() {

		return _archive;
	}

	public void setArchive(Archive archive) {

		_archive = archive;
	}

	public MetadataOutput metadataOutput() {

		return _metadataOutput;
	}

	public void setMetadataOutput(MetadataOutput mo) {

		_metadataOutput = mo;
	}

	public Collection<Transcode> transcodes() {

		return _transcodes;
	}

	public void setTranscodes(Collection<Transcode> transcodes) {

		_transcodes = transcodes;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("id", _id);

		if (_name != null) {
			w.add("name", _name);
		}

		if (_destination != null) {
			w.add("destination", _destination.name());
		}

		if (_layout != null) {
			w.add("layout", _layout.name());
		}

		if (_archive != null) {
			w.push("archive",
					new String[] { "type", _archive.type().toString() });
			if (_archive.parameters() != null) {
				for (Archive.Parameter p : _archive.parameters()) {
					w.add("parameter", new String[] { "name", p.name() },
							p.value());
				}
			}
			w.pop();
		}

		if (_metadataOutput != null) {
			w.add("metadata-output", _metadataOutput.toString());
		}

		if (_transcodes != null) {
			for (Transcode t : _transcodes) {
				w.push("transcode");
				w.add("from", t.from());
				w.add("to", t.to());
				w.pop();
			}
		}

	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.shoppingcart.update";
	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {

		return true;
	}

	@Override
	protected String objectTypeName() {

		return "shopping-cart";
	}

	@Override
	protected String idToString() {

		return _id;
	}
}
