package daris.model.sc;

import arc.mf.client.xml.XmlElement;
import arc.mf.dtype.EnumerationType;

public class Status {

	public enum Value {
		editable, await_processing, assigned, processing, data_ready, fulfilled, rejected, error, withdrawn;

		@Override
		public String toString() {

			String s = super.toString();
			s = s.replace("_", " ");
			return s;
		}

		public static Value instantiate(String value) {

			if (value.equalsIgnoreCase(editable.toString())) {
				return editable;
			}
			if (value.equalsIgnoreCase(await_processing.toString())) {
				return await_processing;
			}
			if (value.equalsIgnoreCase("assigned")) {
				return assigned;
			}
			if (value.equals("processing")) {
				return processing;
			}
			if (value.equalsIgnoreCase("data ready")) {
				return data_ready;
			} else if (value.equalsIgnoreCase("fulfilled")) {
				return fulfilled;
			}
			if (value.equalsIgnoreCase("rejected")) {
				return rejected;
			}
			if (value.equalsIgnoreCase("error")) {
				return error;
			}
			if (value.equalsIgnoreCase("withdrawn")) {
				return withdrawn;
			}
			throw new AssertionError("invalid status value:" + value);
		}

		public static EnumerationType<Value> enumerationType() {

			Value[] vs = values();
			@SuppressWarnings("unchecked")
			EnumerationType.Value<Value>[] evs = new EnumerationType.Value[vs.length];
			for (int i = 0; i < vs.length; i++) {
				evs[i] = new EnumerationType.Value<Value>(vs[i].toString(),
						vs[i].toString(), vs[i]);
			}
			return new EnumerationType<Value>(evs);
		}
	}

	private Value _value;

	private String _log;

	public Status(String value) {

		this(value, null);
	}

	public Status(XmlElement se) {

		this(se.value(), se.value("@log"));
	}

	public Status(String value, String log) {

		this(Value.instantiate(value), log);
	}

	public Status(Value value) {

		this(value, null);
	}

	public Status(Value value, String log) {

		_value = value;
		_log = log;
	}

	public Value value() {

		return _value;
	}

	public String log() {

		return _log;
	}

}
