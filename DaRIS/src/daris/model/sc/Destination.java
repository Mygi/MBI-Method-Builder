package daris.model.sc;

import java.util.List;

import arc.mf.client.util.ObjectUtil;
import arc.mf.client.xml.XmlElement;
import arc.mf.object.ObjectResolveHandler;

public class Destination {
	public enum Type {
		download, deposit;
		public static Type typeFor(String type) {

			if (type.equalsIgnoreCase(deposit.toString())) {
				return deposit;
			} else {
				return download;
			}
		}
	}

	private String _name;

	private Type _type;

	private String _url;

	public Destination(XmlElement de) {

		this(de.value(), de.value("@type"), de.value("@url"));
	}

	public Destination(String name, String type, String url) {

		assert name != null && type != null;
		_name = name;
		_type = Type.typeFor(type);
		_url = url;
	}

	public String name() {

		return _name;
	}

	public Type type() {

		return _type;
	}

	public String url() {

		return _url;
	}

	public boolean equals(Object o) {

		if (o instanceof Destination) {
			String name = ((Destination) o).name();
			Type type = ((Destination) o).type();
			String url = ((Destination) o).url();
			if (_name.equals(name) && _type.equals(type)
					&& ObjectUtil.equals(_url, url)) {
				return true;
			}
		}
		return false;
	}

	public String toString() {

		return _name;
	}

	public static String DEFAULT_DESTINATION_NAME = "browser";

	public static void defaultDestination(
			final ObjectResolveHandler<Destination> rh) {

		DestinationSetRef.get().resolve(
				new ObjectResolveHandler<List<Destination>>() {
					@Override
					public void resolved(List<Destination> ds) {

						if (ds != null) {
							for (Destination d : ds) {
								if (d.name().equalsIgnoreCase(
										DEFAULT_DESTINATION_NAME)) {
									rh.resolved(d);
									return;
								}
							}
						}
						rh.resolved(null);
					}
				});
	}
}
