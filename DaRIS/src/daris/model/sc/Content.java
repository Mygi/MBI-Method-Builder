package daris.model.sc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import arc.mf.client.xml.XmlElement;

public class Content {

	private int _count;

	private long _size;

	private String _units;

	private Map<String, Integer> _mimeTypes;

	public Content(XmlElement ce) {

		try {
			_count = ce.intValue("count", -1);
			_size = ce.longValue("size", -1);
			_units = ce.value("size/@units");
			List<XmlElement> mtes = ce.elements("mime-type");
			if (mtes != null) {
				_mimeTypes = new HashMap<String, Integer>();
				for (XmlElement mte : mtes) {
					_mimeTypes.put(mte.value(), mte.intValue("@count"));
				}
			}
		} catch (Throwable t) {
			throw new AssertionError(t.getMessage());
		}
	}

	public Set<String> mimeTypes() {

		if (_mimeTypes != null) {
			return _mimeTypes.keySet();
		}
		return null;
	}

	public int itemCount(String mimeType) {

		if (_mimeTypes != null) {
			return _mimeTypes.get(mimeType);
		}
		return 0;
	}

	public int totalCount() {

		return _count;
	}

	public long totalSize() {

		return _size;
	}

	public String units() {

		return _units;
	}

}
