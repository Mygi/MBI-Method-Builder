package daris.client.model.dicom;

import java.util.List;

import arc.mf.client.xml.XmlElement;

public class DicomElement {

	private String _group;
	private String _element;
	private String _vr;
	private String _definition;
	private List<String> _values;
	private long _offset = -1;
	private String _offsetUnit;
	private int _length = -1;
	private String _lengthUnit;

	public DicomElement(XmlElement de) {
		if (de.name().equals("de")) {
			_group = de.value("@grp");
			_element = de.value("@ele");
			_vr = de.value("@type");
			_values = de.values("value");
			_definition = de.value("defn");
			try {
				_offset = de.longValue("offset", (long) -1, 10);
			} catch (Throwable t) {
				_offset = -1;
			}
			try {
				_length = de.intValue("length", -1, 10);
			} catch (Throwable t) {
				_length = -1;
			}
			_offsetUnit = de.value("offset/@unit");
			_lengthUnit = de.value("length/@unit");
		}
	}

	public String group() {
		return _group;
	}

	public String element() {
		return _element;
	}

	public String vr() {
		return _vr;
	}

	public String definition() {
		return _definition;
	}

	public String tag() {
		return _group + _element;
	}

	public List<String> values() {
		return _values;
	}

	public String valueAsString() {
		String s = "";
		if (_values != null) {
			for (int i = 0; i < _values.size(); i++) {
				s += _values.get(i);
				if (i < _values.size() - 1) {
					s += ", ";
				}
			}
		}
		if (_offset >= 0) {
			s += "offset: " + offsetAsString() + ";";
		}
		if (_length >= 0) {
			s += "length: " + lengthAsString() + ";";
		}
		return s;
	}

	public long offset() {
		return _offset;
	}

	public String offsetUnit() {
		return _offsetUnit;
	}

	public String offsetAsString() {
		String s = "";
		if (_offset >= 0) {
			s += _offset;
		}
		if (_offsetUnit != null) {
			s += " " + _offsetUnit + (_offset > 1 ? "s" : "");
		}
		return s;
	}

	public int length() {
		return _length;
	}

	public String lengthUnit() {
		return _lengthUnit;
	}

	public String lengthAsString() {
		String s = "";
		if (_length >= 0) {
			s += _length;
		}
		if (_lengthUnit != null) {
			s += " " + _lengthUnit + (_length > 1 ? "s" : "");
		}
		return s;
	}
}
