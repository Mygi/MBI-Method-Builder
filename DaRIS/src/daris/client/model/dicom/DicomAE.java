package daris.client.model.dicom;

import arc.mf.client.util.ObjectUtil;
import arc.mf.client.xml.XmlElement;

public class DicomAE {

	public static final int DEFAULT_DICOM_PORT = 104;

	private String _name;
	private String _host;
	private int _port;
	private String _aet;

	public DicomAE(XmlElement ae) {

		_name = ae.value("@name");
		_host = ae.value("host");
		try {
			_port = ae.intValue("port", 0);
		} catch (Throwable e) {
			_port = 0;
		}
		_aet = ae.value("aet");
	}

	public DicomAE(String name, String aet, String host, int port) {

		_name = name;
		_host = host;
		_port = port;
		_aet = aet;
	}

	public String host() {

		return _host;
	}

	public int port() {

		return _port;
	}

	public String aet() {

		return _aet;
	}

	public String name() {

		return _name;
	}

	@Override
	public String toString() {

		return _name + ":" + _aet + ":" + _host + ":" + _port;
	}

	@Override
	public boolean equals(Object o) {

		if (o == null) {
			return false;
		}
		if (o instanceof DicomAE) {
			DicomAE ae = (DicomAE) o;
			return ObjectUtil.equals(_name, ae.name()) && _host.equals(ae.host()) && _port == ae.port()
					&& _aet.equals(ae.aet());
		}
		return false;
	}
}
