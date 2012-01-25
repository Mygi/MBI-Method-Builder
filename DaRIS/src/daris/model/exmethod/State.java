package daris.model.exmethod;

public enum State {
	abandoned, complete, incomplete, waiting;
	public static State instantiate(String state) throws Exception {

		State[] ss = values();
		for (int i = 0; i < ss.length; i++) {
			if (ss[i].toString().equalsIgnoreCase(state)) {
				return ss[i];
			}
		}
		throw new Exception("Failed to instantiate state: " + state);
	}

	public static String[] stringValues() {

		State[] vs = values();
		String[] svs = new String[vs.length];
		for (int i = 0; i < values().length; i++) {
			svs[i] = vs[i].toString();
		}
		return svs;
	}
}
