package daris.client.model.sc;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import arc.mf.client.xml.XmlElement;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.EnumerationType.Value;

public class Archive {

	public static class Constants {
		public static final String PARAMETER_ISO_TYPE = "iso-type";
		public static final String PARAMETER_ENABLE_ROCKRIDGE = "enable-rockridge";
		public static final String PARAMETER_ENABLE_JOLIET = "enable-joliet";
		public static final String PARAMETER_PUBLISHER = "publisher";
		public static final String PARAMETER_VOLUME_NAME = "volume-name";
		public static final String PARAMETER_COMPRESSION_LEVEL = "compression-level";
		public static final int DEFAULT_COMPRESSION_LEVEL = 6;
		public static final boolean DEFAULT_ENABLE_JOLIET = true;
		public static final boolean DEFAULT_ENABLE_ROCKRIDGE = false;
	}

	public static enum Type {
		none, zip, aar, jar, tar, compressed_tar, iso9660;

		@Override
		public String toString() {

			return super.toString().replace('_', '-');
		}

		public boolean compressible() {

			return this.equals(zip) || this.equals(aar) || this.equals(jar) || this.equals(compressed_tar);
		}

		public String extension() {

			if (this.equals(none)) {
				return null;
			}
			if (this.equals(compressed_tar)) {
				return "tar.gz";
			}
			if (this.equals(iso9660)) {
				return "iso";
			}
			return this.toString();
		}

		public static Type instantiate(String type) {

			if (type == null) {
				return none;
			}
			if (type.equalsIgnoreCase(none.toString())) {
				return none;
			} else if (type.equalsIgnoreCase(zip.toString())) {
				return zip;
			} else if (type.equalsIgnoreCase(aar.toString())) {
				return aar;
			} else if (type.equalsIgnoreCase(tar.toString())) {
				return tar;
			} else if (type.equalsIgnoreCase(compressed_tar.toString())) {
				return compressed_tar;
			} else if (type.equalsIgnoreCase(jar.toString())) {
				return jar;
			} else if (type.equalsIgnoreCase(iso9660.toString())) {
				return iso9660;
			} else {
				return none;
			}
		}

		public static EnumerationType<Type> asEnumerationType() {

			Type[] values = values();
			@SuppressWarnings("unchecked")
			Value<Type>[] vs = new Value[values.length];
			for (int i = 0; i < values.length; i++) {
				vs[i] = new Value<Type>(values[i].toString(), values[i].toString(), values[i]);
			}
			return new EnumerationType<Type>(vs);
		}

	}

	public static enum ISOType {
		cd, dvd_single, dvd_double;
		@Override
		public String toString() {

			return super.toString().replace("_", "-");
		}

		public static ISOType instantiate(String mediaType) {

			if (mediaType != null) {
				if (mediaType.equalsIgnoreCase(dvd_single.toString())) {
					return dvd_single;
				} else if (mediaType.equalsIgnoreCase(dvd_double.toString())) {
					return dvd_double;
				}
			}
			return cd;
		}

		public static EnumerationType<ISOType> asEnumerationType() {

			ISOType[] values = values();
			@SuppressWarnings("unchecked")
			Value<ISOType>[] vs = new Value[values.length];
			for (int i = 0; i < values.length; i++) {
				vs[i] = new Value<ISOType>(values[i].toString(), values[i].toString(), values[i]);
			}
			return new EnumerationType<ISOType>(vs);
		}
	}

	public static class Parameter {
		private String _name;
		private Object _value;

		public Parameter(String name, Object value) {

			_name = name;
			_value = value;
		}

		public String name() {

			return _name;
		}

		public Object value() {

			return _value;
		}

		public void setValue(Object value) {

			_value = value;
		}
	}

	private Type _type;
	private Map<String, Parameter> _parameters;

	public Archive(Type type) {

		_type = type;
		setParameters(_type);
	}

	public void setParameter(String name, Object value) {

		if (name == null) {
			return;
		}
		if (_parameters == null) {
			_parameters = new java.util.HashMap<String, Parameter>();
		}
		Parameter param = _parameters.get(name);
		if (param == null) {
			_parameters.put(name, new Parameter(name, value));
		} else {
			param.setValue(value);
		}
	}

	protected void removeParameter(String name) {

		if (_parameters != null) {
			_parameters.remove(name);
		}
	}

	private void clearParameters() {

		if (_parameters != null) {
			_parameters.clear();
		}
	}

	public Parameter getParameter(String name) {

		if (_parameters != null) {
			return _parameters.get(name);
		}
		return null;
	}

	public Collection<Parameter> parameters() {

		if (_parameters != null) {
			return _parameters.values();
		}
		return null;
	}

	public boolean hasParameters() {

		if (_parameters != null) {
			return !_parameters.isEmpty();
		}
		return false;
	}

	public Type type() {

		return _type;
	}

	public void setType(Type type) {

		if (type != null) {
			if (!type.equals(_type)) {
				_type = type;
				setParameters(_type);
			}
		}
	}

	private void setParameters(Type type) {

		clearParameters();
		switch (type) {
		case zip:
		case aar:
		case jar:
		case compressed_tar:
			setParameter(Constants.PARAMETER_COMPRESSION_LEVEL, Constants.DEFAULT_COMPRESSION_LEVEL);
			break;
		case iso9660:
			setParameter(Constants.PARAMETER_ENABLE_JOLIET, Constants.DEFAULT_ENABLE_JOLIET);
			setParameter(Constants.PARAMETER_ENABLE_ROCKRIDGE, Constants.DEFAULT_ENABLE_ROCKRIDGE);
			setParameter(Constants.PARAMETER_ISO_TYPE, ISOType.cd);
			setParameter(Constants.PARAMETER_PUBLISHER, (String) null);
			setParameter(Constants.PARAMETER_VOLUME_NAME, (String) null);
			break;
		default:
			break;
		}
	}

	public static Archive instantiate(XmlElement ae) throws Throwable {

		Type type = Type.instantiate(ae.value("type"));
		Archive arc = new Archive(type);
		List<XmlElement> pes = ae.elements("parameter");
		if (pes != null) {
			for (XmlElement pe : pes) {
				arc.setParameter(pe.value("@name"), pe.value());
			}
		}
		return arc;
	}


}
