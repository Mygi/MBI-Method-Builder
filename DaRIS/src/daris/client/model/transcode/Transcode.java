package daris.client.model.transcode;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;

public class Transcode {

	public static final String NONE_MIME_TYPE = "none";
	
	private String _from;
	private String _to;
	private String _description;
	private String _toDescription;

	public Transcode(String from, String to, String description,
			String toDescription) {

		_from = from;
		_to = to;
		_description = description;
		_toDescription = toDescription;
	}

	public Transcode(XmlElement te) throws Throwable {

		_from = te.value("from");
		_to = te.value("to");
		_description = te.value("description");
		_toDescription = te.value("to/@description");
		if (_from == null) {
			throw new Exception("from MIME type is null.");
		}
		if (_to == null) {
			throw new Exception("to MIME type is null.");
		}
	}

	public Transcode(String from, String to) {
		this(from, to, null, null);
	}
	
	public Transcode(String from) {
		this(from, NONE_MIME_TYPE, null, null);
	}

	public String from() {
		return _from;
	}

	public String to() {
		return _to;
	}
	
	@Override
	public String toString(){
		return _to;
	}

	public String description() {
		return _description;
	}

	public String toDescription() {
		return _toDescription;
	}

	public static List<Transcode> instantiate(List<XmlElement> tes)
			throws Throwable {

		if (tes != null) {
			if (!tes.isEmpty()) {
				List<Transcode> transcodes = new Vector<Transcode>();
				for (XmlElement te : tes) {
					Transcode transcode = new Transcode(te);
					if (transcode != null) {
						transcodes.add(transcode);
					}
				}
				if (transcodes.size() > 0) {
					return transcodes;
				}
			}
		}
		return null;
	}
}
