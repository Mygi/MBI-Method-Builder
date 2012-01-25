package daris.client.model.exmethod;

import java.util.List;
import java.util.Vector;

import arc.mf.dtype.EnumerationType;

public enum State {
	abandoned, complete, incomplete, waiting;

	public static State parse(String state) {

		if (state == null) {
			return null;
		}
		State[] states = values();
		for (int i = 0; i < states.length; i++) {
			if (states[i].toString().equalsIgnoreCase(state)) {
				return states[i];
			}
		}
		return null;
	}

	public static EnumerationType<State> asEnumerationType() {

		List<EnumerationType.Value<State>> evs = new Vector<EnumerationType.Value<State>>(
				values().length);
		for (int i = 0; i < values().length; i++) {
			evs.add(new EnumerationType.Value<State>(values()[i].toString(),
					values()[i].toString(), values()[i]));
		}
		return new EnumerationType<State>(evs);
	}
	
}
