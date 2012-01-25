package nig.mf.plugin.pssd.sc;

import java.util.List;
import java.util.Vector;

import nig.mf.plugin.pssd.Asset;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

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

	public ContentItem(String assetId, String mimeType, int version, long size, String status, String type, String id,
			String name, String description) throws Throwable {

		_assetId = assetId;
		_mimeType = mimeType;
		_version = version;
		_size = size;
		_status = status;
		_type = type;
		_id = id;
		_name = name;
		_description = description;
	}

	public void describe(XmlWriter w) throws Throwable {

		w.push("object",
				new String[] { "type", _type, "status", _status, "mime-type", _mimeType, "size", Long.toString(_size),
						"version", Integer.toString(_version) });
		w.add("id", new String[] { "asset", _assetId }, _id);
		if (_name != null) {
			w.add("name", _name);
		}
		if (_description != null) {
			w.add("description", _description);
		}
		w.pop();
	}

	public static ContentItem instantiate(ServiceExecutor executor, XmlDoc.Element xe) throws Throwable {

		String assetId = xe.value("@id");
		String mimeType = xe.value("@type");
		int version = xe.intValue("@version", 0);
		long size = xe.longValue("@size", 0);
		String status = xe.value("@status");
		XmlDoc.Element ae = null;
		try {
			ae = Asset.getById(executor, null, assetId);
		} catch (Throwable e) {
			// asset does not exist any more. could be destroyed.
			ae = null;
		}
		if (ae == null) {
			return null;
		}
		String type = ae.value("meta/pssd-object/type");
		String id = ae.value("cid");
		String name = ae.value("meta/pssd-object/name");
		String description = ae.value("meta/pssd-object/description");
		return new ContentItem(assetId, mimeType, version, size, status, type, id, name, description);
	}

	public static List<ContentItem> instantiate(ServiceExecutor executor, List<XmlDoc.Element> xes) throws Throwable {

		if (xes != null) {
			List<ContentItem> items = new Vector<ContentItem>();
			for (XmlDoc.Element xe : xes) {
				ContentItem ci = instantiate(executor, xe);
				if (ci != null) {
					items.add(ci);
				}
			}
			if (!items.isEmpty()) {
				return items;
			}
		}
		return null;
	}

}
