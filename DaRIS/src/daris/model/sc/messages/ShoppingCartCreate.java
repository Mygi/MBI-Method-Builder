package daris.model.sc.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.model.sc.Destination;
import daris.model.sc.Layout;
import daris.model.sc.MetadataOutput;
import daris.model.sc.ShoppingCartRef;
import daris.model.sc.archive.Archive;

public class ShoppingCartCreate extends ObjectMessage<String> {

	private String _name;

	private Destination _destination;

	private Layout _layout;

	private Archive _archive;

	private MetadataOutput _metadataOutput;

	public ShoppingCartCreate(String name, Destination destination,
			Layout layout, Archive archive, MetadataOutput mo) {

		_name = name;
		_destination = destination;
		_layout = layout;
		_archive = archive;
		_metadataOutput = mo;
	}

	public ShoppingCartCreate() {

		this(null, null, null, null, null);
	}

	public ShoppingCartCreate(ShoppingCartRef cart) {

		this(cart.name(), cart.destination(), cart.layout(), cart.archive(),
				cart.metadataOutput());
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

	@Override
	protected void messageServiceArgs(XmlWriter w) {

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

	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.shoppingcart.create";
	}

	@Override
	protected String instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			return xe.value("id");
		}
		return null;
	}

	@Override
	protected String objectTypeName() {

		return "shopping-cart";
	}

	@Override
	protected String idToString() {

		return "shopping-cart";
	}
}
