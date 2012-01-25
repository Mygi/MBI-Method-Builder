package daris.client.model.sc;

public class Layout {

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

}
