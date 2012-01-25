package daris.model.sc;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arc.mf.client.xml.XmlElement;
import daris.model.sc.archive.Archive;
import daris.model.transcode.Transcode;

public class ShoppingCart {

	private String _id;

	private String _name;

	private Status _status;

	private String _template;

	private boolean _selfServiced;

	private String _order;

	private Content _content;

	private Map<String, Transcode> _transcodes;

	private Destination _destination;

	private Layout _layout;

	private Archive _archive;

	private MetadataOutput _metadataOutput = MetadataOutput.none;

	public ShoppingCart(XmlElement ce) {

		parse(ce);
	}

	private void parse(XmlElement ce) {

		/*
		 * id
		 */
		_id = ce.value("@id");
		
		/*
		 * name
		 */
		
		_name = ce.value("name");
		
		/*
		 * status
		 */
		XmlElement statusElement = ce.element("status");
		if (statusElement != null) {
			_status = new Status(statusElement);
		}
		/*
		 * template
		 */
		_template = ce.value("template");
		try {
			_selfServiced = ce.booleanValue("self-serviced", true);
		} catch (Throwable t) {
			throw new AssertionError(t.getMessage());
		}
		/*
		 * order
		 */
		_order = ce.value("order");
		/*
		 * content
		 */
		XmlElement contentStatisticsElement = ce.element("content");
		if (contentStatisticsElement != null) {
			_content = new Content(contentStatisticsElement);
		}
		/*
		 * data-transformation/transcode
		 */
		List<XmlElement> transcodeElements = ce
				.elements("transcode");
		if (transcodeElements != null) {
			for (XmlElement transcodeElement : transcodeElements) {
				addTranscode(transcodeElement.value("@from"),
						transcodeElement.value("@to"));
			}
		}
		/*
		 * destination
		 */
		XmlElement destinationElement = ce.element("destination");
		if (destinationElement != null) {
			_destination = new Destination(destinationElement);
		}
		/*
		 * archive
		 */
		XmlElement archiveElement = ce.element("archive");
		if (archiveElement != null) {
			_archive = Archive.instantiate(archiveElement);
		}
		/*
		 * layout
		 */
		XmlElement layoutElement = ce.element("layout");
		if (layoutElement != null) {
			_layout = new Layout(layoutElement);
		}
		/*
		 * metadata-output
		 */
		String metadataOutput = ce.value("metadata-output");
		if (metadataOutput != null) {
			_metadataOutput = MetadataOutput.instantiate(metadataOutput);
		}
	}

	public void addTranscode(String from, String to) {

		addTranscode(new Transcode(from, to));
	}

	public void addTranscode(Transcode transcode) {

		if (_transcodes == null) {
			_transcodes = new HashMap<String, Transcode>();
		}
		_transcodes.put(transcode.from(), transcode);
	}

	public void removeTranscode(String from) {

		if (_transcodes != null) {
			_transcodes.remove(from);
		}
	}

	public String id() {

		return _id;
	}

	public String name() {

		return _name;
	}

	public Status status() {

		return _status;
	}

	public String template() {

		return _template;
	}

	public String order() {

		return _order;
	}

	public boolean selfServiced() {

		return _selfServiced;
	}

	public Content content() {

		return _content;
	}

	public Collection<Transcode> transcodes() {

		return _transcodes.values();
	}

	public Destination destination() {

		return _destination;
	}

	public Archive archive() {

		return _archive;
	}

	public Layout layout() {

		return _layout;
	}

	public MetadataOutput metadataOutput() {

		return _metadataOutput;
	}

	@Override
	public boolean equals(Object o) {

		if (o instanceof ShoppingCart) {
			return _id.equals(((ShoppingCart) o).id());
		}
		return false;
	}

	@Override
	public int hashCode() {

		return _id.hashCode();
	}
}
