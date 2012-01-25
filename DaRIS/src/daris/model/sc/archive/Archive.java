package daris.model.sc.archive;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arc.mf.client.xml.XmlElement;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.EnumerationType.Value;
import daris.model.sc.Destination;

public abstract class Archive {
	public enum Type {
		none, zip, aar, jar, tar, gzipped_tar, iso9660;
		public String extension() {

			if (this == gzipped_tar) {
				return "tar.gz";
			}
			if (this == iso9660) {
				return "iso";
			}
			return toString();
		}

		public static Type typeFor(String type) {

			if (type.equalsIgnoreCase("none")) {
				return none;
			} else if (type.equalsIgnoreCase("zip")) {
				return zip;
			} else if (type.equalsIgnoreCase("aar")) {
				return aar;
			} else if (type.equalsIgnoreCase("tar")) {
				return tar;
			} else if (type.equalsIgnoreCase("compressed-tar")
					|| type.equalsIgnoreCase("gzipped_tar")
					|| type.equalsIgnoreCase("tar.gz")
					|| type.equalsIgnoreCase("tgz")) {
				return gzipped_tar;
			} else if (type.equalsIgnoreCase("jar")) {
				return jar;
			} else if (type.equalsIgnoreCase("iso9660")
					|| type.equalsIgnoreCase("iso")) {
				return iso9660;
			}
			throw new AssertionError("Invalid archive type:" + type);
		}

		public static EnumerationType<Archive.Type> asEnumerationType() {
			Type[] vs = values();
			@SuppressWarnings("unchecked")
			Value<Type>[] es = new Value[vs.length];
			for (int i = 0; i < vs.length; i++) {
				es[i] = new Value<Type>(vs[i].toString(), vs[i].toString(),
						vs[i]);
			}
			return new EnumerationType<Archive.Type>(es);
		}

		@SuppressWarnings("unchecked")
		public static EnumerationType<Archive.Type> asEnumerationType(
				Destination.Type dstType) {
			if (dstType.equals(Destination.Type.deposit)) {
				return new EnumerationType<Archive.Type>(
						new Value[] { new Value<Type>(none.toString(),
								none.toString(), none) });
			} else {
				return asEnumerationType();
			}
		}
	}

	public static class Parameter {
		public static String[] names = {};

		private String _name;

		private String _value;

		public Parameter(String name, String value) {

			_name = name;
			_value = value;
		}

		public String name() {

			return _name;
		}

		public String value() {

			return _value;
		}
	}

	private Type _type;

	private Map<String, Parameter> _parameters;

	protected Archive(Type type) {

		_type = type;
	}

	public Type type() {

		return _type;
	}

	public String extension() {

		return _type.extension();
	}

	protected void setParameter(String name, String value) {

		addParameter(new Parameter(name, value));
	}

	private void addParameter(Parameter pm) {

		if (pm == null) {
			return;
		}
		if (_parameters == null) {
			_parameters = new HashMap<String, Parameter>();
		}
		_parameters.put(pm.name(), pm);
	}

	public void removeParameter(Parameter pm) {

		if (_parameters == null) {
			return;
		}
		_parameters.remove(pm.name());
	}

	public Collection<Parameter> parameters() {

		if (_parameters == null) {
			return null;
		}
		return _parameters.values();
	}

	protected Parameter parameter(String name) {

		if (_parameters == null) {
			return null;
		}
		return _parameters.get(name);
	}

	protected String parameterValue(String name) {

		Parameter p = parameter(name);
		if (p == null) {
			return null;
		}
		return p.value();
	}

	public String toString() {

		return _type.toString();
	}

	// public static EnumerationType<Archive> enumerationType() {
	//
	// List<Value<Archive>> vs = new Vector<Value<Archive>>();
	// Archive arc = new ZipArchive();
	// vs.add(new Value<Archive>(arc.type().toString(), arc.type().toString(),
	// arc));
	// arc = new AarArchive();
	// vs.add(new Value<Archive>(arc.type().toString(), arc.type().toString(),
	// arc));
	// arc = new JarArchive();
	// vs.add(new Value<Archive>(arc.type().toString(), arc.type().toString(),
	// arc));
	// arc = new TarArchive();
	// vs.add(new Value<Archive>(arc.type().toString(), arc.type().toString(),
	// arc));
	// arc = new GZippedTarArchive();
	// vs.add(new Value<Archive>(arc.type().toString(), arc.type().toString(),
	// arc));
	// arc = new ISO9660Archive();
	// vs.add(new Value<Archive>(arc.type().toString(), arc.type().toString(),
	// arc));
	// arc = new NoneArchive();
	// vs.add(new Value<Archive>(arc.type().toString(), arc.type().toString(),
	// arc));
	// return new EnumerationType<Archive>(vs);
	// }
	//
	// public static EnumerationType<Archive> enumerationType(Destination dst) {
	// List<Value<Archive>> vs = new Vector<Value<Archive>>();
	// if (Destination.Type.deposit == dst.type()) {
	// Archive arc = new NoneArchive();
	// vs.add(new Value<Archive>(arc.type().toString(), arc.type()
	// .toString(), arc));
	// } else {
	// Archive arc = new ZipArchive();
	// vs.add(new Value<Archive>(arc.type().toString(), arc.type()
	// .toString(), arc));
	// arc = new AarArchive();
	// vs.add(new Value<Archive>(arc.type().toString(), arc.type()
	// .toString(), arc));
	// arc = new JarArchive();
	// vs.add(new Value<Archive>(arc.type().toString(), arc.type()
	// .toString(), arc));
	// arc = new TarArchive();
	// vs.add(new Value<Archive>(arc.type().toString(), arc.type()
	// .toString(), arc));
	// arc = new GZippedTarArchive();
	// vs.add(new Value<Archive>(arc.type().toString(), arc.type()
	// .toString(), arc));
	// arc = new ISO9660Archive();
	// vs.add(new Value<Archive>(arc.type().toString(), arc.type()
	// .toString(), arc));
	// arc = new NoneArchive();
	// vs.add(new Value<Archive>(arc.type().toString(), arc.type()
	// .toString(), arc));
	// }
	// return new EnumerationType<Archive>(vs);
	// }

	public static Archive instantiate(XmlElement ae) {

		Type type = Type.typeFor(ae.value("@type"));
		Archive arc;
		switch (type) {
		case zip:
			arc = new ZipArchive();
			break;
		case jar:
			arc = new JarArchive();
			break;
		case aar:
			arc = new AarArchive();
			break;
		case gzipped_tar:
			arc = new GZippedTarArchive();
			break;
		case tar:
			arc = new TarArchive();
			break;
		case none:
			arc = new NoneArchive();
			break;
		case iso9660:
			arc = new ISO9660Archive();
			break;
		default:
			arc = new ZipArchive();
		}
		List<XmlElement> pes = ae.elements("parameter");
		if (pes != null) {
			for (XmlElement pe : pes) {
				arc.setParameter(pe.value("@name"), pe.value());
			}
		}
		return arc;
	}

	public static Archive create(Archive.Type type) {
		Archive arc;
		switch (type) {
		case zip:
			arc = new ZipArchive();
			break;
		case jar:
			arc = new JarArchive();
			break;
		case aar:
			arc = new AarArchive();
			break;
		case gzipped_tar:
			arc = new GZippedTarArchive();
			break;
		case tar:
			arc = new TarArchive();
			break;
		case none:
			arc = new NoneArchive();
			break;
		case iso9660:
			arc = new ISO9660Archive();
			break;
		default:
			arc = new ZipArchive();
		}
		return arc;
	}

	public static final Archive DEFAULT = new ZipArchive();
}
