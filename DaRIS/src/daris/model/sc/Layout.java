package daris.model.sc;

import java.util.List;

import arc.mf.client.util.ObjectUtil;
import arc.mf.client.xml.XmlElement;
import arc.mf.object.ObjectResolveHandler;

public class Layout {
	public enum Type {
		preserved, flat, custom;
		public static Type typeFor(String type) {

			if (type.equals(preserved.toString())) {
				return preserved;
			} else if (type.equals(flat.toString())) {
				return flat;
			} else {
				return custom;
			}
		}
	}

	private String _name;

	private Type _type;

	private String _pattern;

	public Layout(XmlElement le) {

		this(le.value(), le.value("@type"), le.value("@pattern"));
	}

	public Layout(String name, String type, String pattern) {

		assert name != null && type != null;
		_name = name;
		_type = Type.typeFor(type);
		_pattern = pattern;
	}

	public String name() {

		return _name;
	}

	public Type type() {

		return _type;
	}

	public String pattern() {

		return _pattern;
	}

	public boolean equals(Object o) {

		if (o instanceof Layout) {
			String name = ((Layout) o).name();
			Type type = ((Layout) o).type();
			String pattern = ((Layout) o).pattern();
			if (_name.equals(name) && _type.equals(type)
					&& ObjectUtil.equals(_pattern, pattern)) {
				return true;
			}
		}
		return false;
	}

	public String toString() {

		return _name;
	}

	public static final String DEFAULT_LAYOUT_NAME = "pssd";

	public static void defaultLayout(final ObjectResolveHandler<Layout> rh) {

		LayoutSetRef.get().resolve(new ObjectResolveHandler<List<Layout>>() {
			@Override
			public void resolved(List<Layout> ls) {

				if (ls != null) {
					for (Layout l : ls) {
						if (l.name().equalsIgnoreCase(DEFAULT_LAYOUT_NAME)) {
							rh.resolved(l);
							return;
						}
					}
				}
				rh.resolved(null);
			}
		});
	}
}
