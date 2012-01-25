package daris.model.datacontent;

public class Checksum {

	public static int DEFAULT_BASE = 16;

	private long _value;

	private int _base;

	public Checksum(long value) {

		this(value, DEFAULT_BASE);
	}

	public Checksum(long value, int base) {

		_value = value;		
		_base = base;
	}

	public long value() {

		return _value;
	}

	public int base() {

		return _base;
	}

	public String toString() {

		return Long.toString(_value, _base);
	}

}
