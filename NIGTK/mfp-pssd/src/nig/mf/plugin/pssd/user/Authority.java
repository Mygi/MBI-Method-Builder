package nig.mf.plugin.pssd.user;

import arc.xml.XmlDoc;

public class Authority {

	private String _name;
	private String _protocol;

	public static Authority instantiate(XmlDoc.Element ae) throws Throwable {

		if (!ae.nameEquals("authority")) {
			throw new Exception("Invalid XML element name: " + ae.name() + ". Should be 'authority'.");
		}
		String name = ae.value();
		if (name == null) {
			throw new Exception("Authority element value cannot be null.");
		}
		return new Authority(name, ae.value("@protocol"));
	}

	public Authority(String name, String protocol) {

		_name = name;
		assert _name != null;
		_protocol = protocol;
	}

	public String name() {

		return _name;
	}

	public String protocol() {

		return _protocol;
	}

	@Override
	public String toString() {

		return _name;
	}

	@Override
	public boolean equals(Object o) {

		if (o != null) {
			if (o instanceof Authority) {
				Authority ao = (Authority) o;
				if (ao.name().equals(name())) {
					if (ao.protocol() == null && protocol() == null) {
						return true;
					} else if (ao.protocol() != null && protocol() != null) {
						if (ao.protocol().equals(protocol())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public XmlDoc.Element toXmlElement() throws Throwable {

		return toXmlElement(this);
	}

	public static XmlDoc.Element toXmlElement(Authority authority) throws Throwable {

		return toXmlElement(authority.name(), authority.protocol());
	}

	public static XmlDoc.Element toXmlElement(String authority, String protocol) throws Throwable {

		if (authority == null) {
			throw new Exception("Failed to create XML element: authority. Value is null.");
		}
		XmlDoc.Element ae = new XmlDoc.Element("authority", authority);
		if (protocol != null) {
			ae.add(new XmlDoc.Attribute("protocol", protocol));
		}
		return ae;
	}
}
