package nig.mf.plugin.pssd.sc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import arc.xml.XmlDoc;

public class Archive {
	
	public static final String PARAMETER_ISO_TYPE = "iso-type";
	public static final String PARAMETER_ENABLE_ROCKRIDGE = "enable-rockridge";
	public static final String PARAMETER_ENABLE_JOLIET = "enable-joliet";
	public static final String PARAMETER_PUBLISHER = "publisher";
	public static final String PARAMETER_VOLUME_NAME = "volume-name";
	public static final String PARAMETER_COMPRESSION_LEVEL = "compression-level";
	public static final int DEFAULT_COMPRESSION_LEVEL = 6;
	public static final boolean DEFAULT_ENABLE_JOLIET = true;
	public static final boolean DEFAULT_ENABLE_ROCKRIDGE = false;

	public static enum Type {
		none, zip, aar, jar, tar, compressed_tar, iso9660;

		@Override
		public String toString() {
			return super.toString().replace('_', '-');
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

		public static String[] stringValues() {
			Type[] values = values();
			String[] stringValues = new String[values.length];
			for (int i = 0; i < values.length; i++) {
				stringValues[i] = values[i].toString();
			}
			return stringValues;
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
	}
	
	private Type _type;
	private Map<String, String> _parameters;

	protected Archive(Type type) {
		_type = type;
	}

	protected void setParameter(String name, String value) {
		if (name == null || value == null) {
			return;
		}
		if (_parameters == null) {
			_parameters = new java.util.HashMap<String, String>();
		}
		_parameters.put(name, value);
	}

	protected void removeParameter(String name) {
		if (_parameters != null) {
			_parameters.remove(name);
		}
	}

	protected String getParameterValue(String name) {
		if (_parameters != null) {
			return _parameters.get(name);
		}
		return null;
	}

	public Set<String> getParameterNames() {
		if (_parameters != null) {
			return _parameters.keySet();
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

	public static Archive instantiate(XmlDoc.Element arcElement)
			throws Throwable {

		Type type = Type.instantiate(arcElement.value("type"));
		Archive arc = new Archive(type);
		List<XmlDoc.Element> pes = arcElement.elements("parameter");
		if (pes != null) {
			for (XmlDoc.Element pe : pes) {
				arc.setParameter(pe.value("@name"), pe.value());
			}
		}
		return arc;
	}
	
	public static Archive create(Type type)throws Throwable {
		
		Archive arc = new Archive(type);
		switch(type){
		case none:
		case tar:
			break;
		case zip:
		case aar:
		case jar:
		case compressed_tar:
			arc.setParameter("compression-level", "6");
			break;
		case iso9660:
			arc.setParameter(PARAMETER_ENABLE_JOLIET, Boolean.toString(DEFAULT_ENABLE_JOLIET));
			arc.setParameter(PARAMETER_ENABLE_ROCKRIDGE, Boolean.toString(DEFAULT_ENABLE_ROCKRIDGE));
			arc.setParameter(PARAMETER_ISO_TYPE, ISOType.cd.toString());
			arc.setParameter(PARAMETER_PUBLISHER, (String) null);
			arc.setParameter(PARAMETER_VOLUME_NAME, (String) null);
			break;
		}
		return arc;
	}

}
