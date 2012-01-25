package daris.client.model.sc;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;

public class DeliveryDestination {

	public static final DeliveryDestination BROWSER = new DeliveryDestination(
			"browser", DeliveryMethod.download, null);

	private String _name;

	private String _url;

	private DeliveryMethod _method;

	private DeliveryDestination(String name, DeliveryMethod method, String url) {

		_name = name;
		_url = url;
		_method = method;
	}

	public String name() {
		return _name;
	}

	public String url() {
		return _url;
	}

	public DeliveryMethod method() {
		return _method;
	}

	public DeliveryDestination(XmlElement dde) {

		this._name = dde.value();
		this._method = DeliveryMethod.instantiate(dde.value("@method"));
		assert this._method != null;
		this._url = dde.value("@url");
		if (this._method.equals(DeliveryMethod.deposit)) {
			assert this._url != null;
		}
	}

	public static DeliveryDestination instantiate(XmlElement dde) {

		return new DeliveryDestination(dde);
	}

	public static List<DeliveryDestination> instantiate(List<XmlElement> ddes) {

		if (ddes != null) {
			List<DeliveryDestination> dds = new Vector<DeliveryDestination>(
					ddes.size());
			for (XmlElement dde : ddes) {
				dds.add(instantiate(dde));
			}
			if (!dds.isEmpty()) {
				return dds;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return _name;
	}

}
