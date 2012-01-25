package daris.client.model.sc;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;

public class ContentItem {
	private String _assetId;
	private String _mimeType;
	private int _version;
	private long _size;
	private String _status;

	private String _type;
	private String _id;
	private String _name;
	private String _description;
	private ShoppingCartRef _sc;

	public ContentItem(XmlElement oe, ShoppingCartRef sc) throws Throwable {

		_type = oe.value("@type");
		_status = oe.value("@status");
		_mimeType = oe.value("@mime-type");
		_size = oe.longValue("@size", 0);
		_version = oe.intValue("@version", 0);
		_id = oe.value("id");
		_assetId = oe.value("id/@asset");
		_name = oe.value("name");
		_description = oe.value("description");
		_sc = sc;
	}

	public static ContentItem instantiate(XmlElement oe, ShoppingCartRef sc)
			throws Throwable {

		return new ContentItem(oe, sc);
	}

	public static List<ContentItem> instantiate(List<XmlElement> oes,
			ShoppingCartRef sc) throws Throwable {

		if (oes != null) {
			List<ContentItem> items = new Vector<ContentItem>(oes.size());
			for (XmlElement oe : oes) {
				items.add(instantiate(oe, sc));
			}
			if (!items.isEmpty()) {
				return items;
			}
		}
		return null;
	}

	public String assetId() {

		return _assetId;
	}

	public String mimeType() {

		return _mimeType;
	}

	public int version() {

		return _version;
	}

	public long size() {

		return _size;
	}

	public String status() {

		return _status;
	}

	public String type() {

		return _type;
	}

	public String id() {

		return _id;
	}

	public String name() {

		return _name;
	}

	public String description() {

		return _description;
	}

	public ShoppingCartRef cart() {
		return _sc;
	}

	public String toHTML() {
		String html = "<table><thead><tr><th align=\"center\" colspan=\"2\">Item:</th></tr><thead>";
		html += "<tbody>";
		html += "<tr><td><b>id:</b></td><td>" + _id + "</td></tr>";
		if (_name != null) {
			html += "<tr><td><b>name:</b></td><td>" + _name + "</td></tr>";
		}
		if (_description != null) {
			html += "<tr><td><b>description:</b></td><td>" + _description
					+ "</td></tr>";
		}
		html += "<tr><td><b>MIME type:</b></td><td>" + _mimeType
				+ " bytes</td></tr>";
		html += "<tr><td><b>size:</b></td><td>" + _size + " bytes</td></tr>";
		html += "<tr><td><b>status:</b></td><td>" + _status + "</td></tr>";
		html += "</tbody></table>";
		return html;
	}

}
