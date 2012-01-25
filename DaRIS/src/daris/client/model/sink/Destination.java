package daris.client.model.sink;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arc.mf.client.xml.XmlElement;

public class Destination {

	public static final String ARG_DIRECTORY = "directory";
	public static final String ARG_DECOMPRESSION = "decompression";

	private String _type;
	private Map<String, String> _args;

	public Destination(XmlElement de) {
		_type = de.value("type");
		List<XmlElement> aes = de.elements("arg");
		if (aes != null) {
			_args = new HashMap<String, String>();
			for (XmlElement ae : aes) {
				_args.put(ae.value("@name"), ae.value());
			}
		}
	}

	public String argValue(String name) {
		if (_args != null) {
			return _args.get(name);
		}
		return null;
	}

	public Map<String, String> args() {
		return _args;
	}

	public String type() {
		return _type;
	}

	public String directory() {
		return argValue(ARG_DIRECTORY);
	}

	public String decompression() {
		return argValue(ARG_DECOMPRESSION);
	}
}
