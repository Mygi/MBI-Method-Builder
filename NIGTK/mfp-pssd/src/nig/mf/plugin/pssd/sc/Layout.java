package nig.mf.plugin.pssd.sc;

public class Layout {

	public static final Type DEFAULT_TYPE = Type.custom;

	public static enum Type {
		custom, flat, preserved;
		public static Type instantiate(String type) {
			if (type != null) {
				if (type.equalsIgnoreCase(custom.toString())) {
					return custom;
				}
				if (type.equalsIgnoreCase(flat.toString())) {
					return flat;
				}
				if (type.equalsIgnoreCase(preserved.toString())) {
					return preserved;
				}
			}
			return null;
		}
	}

	public static final String DEFAULT_PATTERN = "cid(-7,-5)/cid(-7,-4)/cid(-7,-3)/cid(-7,-2)/replace(if-null(variable(tx-to-type), xpath(asset/type)),'/','_')/cid(-1)if-null(xpath(pssd-object/name),'','_')xpath(pssd-object/name)";

}
