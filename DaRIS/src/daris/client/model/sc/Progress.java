package daris.client.model.sc;

import java.util.Date;

import arc.mf.client.xml.XmlElement;

public class Progress {
	
	

	private long _scid;
	private Date _startTime;
	private double _duration;
	private String _durationUnit;
	private int _completed;
	private int _total;

	public Progress(XmlElement pe) throws Throwable {
		_scid = pe.longValue("@order");
		_startTime = pe.dateValue("start-time");
		_duration = pe.doubleValue("duration");
		_durationUnit = pe.stringValue("duration/@unit");
		_completed = pe.intValue("completed", 0);
		_total = pe.intValue("total", 0);
	}

	public long scid() {
		return _scid;
	}

	public Date startTime() {
		return _startTime;
	}

	public double duration() {
		return _duration;
	}

	public String durationUnit() {
		return _durationUnit;
	}

	public int completed() {
		return _completed;
	}

	public int total() {
		return _total;
	}

}
