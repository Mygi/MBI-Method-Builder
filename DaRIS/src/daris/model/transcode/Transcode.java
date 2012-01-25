package daris.model.transcode;

public class Transcode {

	public static String DEFAULT_TO_MIME_TYPE = "none";

	private String _from;

	private String _to;

	private String _toDesc;

	private String _desc;

	public Transcode(String from, String to, String toDesc, String description) {

		_from = from;
		_to = to;
		_toDesc = toDesc;
		_desc = description;
	}

	public Transcode(String from, String to) {

		this(from, to, null, null);
	}

	public Transcode(String from) {

		this(from, DEFAULT_TO_MIME_TYPE);
	}

	public String from() {

		return _from;
	}

	public String to() {

		return _to;
	}

	public String toDescription() {

		return _toDesc;
	}

	public String description() {

		return _desc;
	}

	public void setTo(String to) {

		_to = to;
	}

	public String toString() {
		return _to;
	}
}
