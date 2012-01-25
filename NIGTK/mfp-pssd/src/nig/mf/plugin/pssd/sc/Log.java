package nig.mf.plugin.pssd.sc;

import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class Log {
	private Status _status;
	private String _message;
	private String _changed;

	public Log(XmlDoc.Element le) throws Throwable {
		_status = Status.instantiate(le.value("@status"));
		_changed = le.value("@changed");
		_message = le.value();
	}

	public String changed() {
		return _changed;
	}

	public Status status() {
		return _status;
	}

	public String message() {
		return _message;
	}

	public void describe(XmlWriter w) throws Throwable {
		w.add("log", new String[] { "status", _status.toString(), "changed",
				_changed }, _message);
	}
}