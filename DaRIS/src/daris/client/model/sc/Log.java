package daris.client.model.sc;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import arc.mf.client.util.UnhandledException;
import arc.mf.client.xml.XmlElement;

public class Log {

	private Status _status;
	private String _message;
	private Date _changed;

	public Log(XmlElement le) {
		_status = Status.instantiate(le.value("@status"));
		try {
			_changed = le.dateValue("@changed");
		} catch (Throwable e) {
			UnhandledException.report("Parsing shopping cart log", e);
		}
		_message = le.value();
	}

	public Date changed() {
		return _changed;
	}

	public Status status() {
		return _status;
	}

	public String message() {
		return _message;
	}

	public static List<Log> instantiate(List<XmlElement> les) {
		if (les != null) {
			List<Log> logs = new Vector<Log>();
			for (XmlElement le : les) {
				logs.add(new Log(le));
			}
			if (!logs.isEmpty()) {
				return logs;
			}
		}
		return null;
	}
}
