package daris.client.model.object;

import arc.mf.client.xml.XmlElement;

public class DataContent {

	private String _atime;
	private long _atimeMillisec;
	private String _type;
	private String _ext;
	private long _size;
	private String _sizeUnits;
	private String _csum;
	private int _csumBase;
	private String _store;
	private String _url;

	public DataContent(XmlElement de) {

		_atime = de.value("atime");
		try {
			_atimeMillisec = de.longValue("atime/@millisec");
		} catch (Throwable e) {

		}

		try {
			_size = de.intValue("size", 0);
		} catch (Throwable e) {

		}
		try {
			_csumBase = de.intValue("csum/@base", 16);
		} catch (Throwable e) {

		}

		_type = de.value("type");
		_ext = de.value("type/@ext");
		_sizeUnits = de.value("size/@units");
		_csum = de.value("csum");
		_store = de.value("store");
		_url = de.value("url");
	}

	public String atime() {

		return _atime;
	}

	public long atimeMillisecs() {

		return _atimeMillisec;
	}

	public String mimeType() {

		return _type;
	}

	public String extension() {

		return _ext;
	}

	public long size() {

		return _size;
	}

	public String sizeUnits() {

		return _sizeUnits;
	}

	public String checksum() {

		return _csum;
	}

	public int checksumBase() {

		return _csumBase;
	}

	public String store() {

		return _store;
	}

	public String url() {

		return _url;
	}
}
