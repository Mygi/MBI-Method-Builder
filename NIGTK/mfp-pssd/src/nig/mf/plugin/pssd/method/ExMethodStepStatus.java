package nig.mf.plugin.pssd.method;


public class ExMethodStepStatus {
	
	public static final String STATUS_INCOMPLETE = "incomplete";
	public static final String STATUS_WAITING    = "waiting";
	public static final String STATUS_COMPLETE   = "complete";
	public static final String STATUS_ABANDONED  = "abandoned";
	
	
	private String _step;
	private String _status;
	private String _notes;
	
	public ExMethodStepStatus(String stepPath,String status) {
		_step = stepPath;
		_status = status;
		_notes = null;
	}
	
	public String stepPath() {
		return _step;
	}
	
	public String status() {
		return _status;
	}
	
	public void setStatus(String status) {
		_status = status;
	}
	
	public String notes() {
		return _notes;
	}
	
	public void setNotes(String notes) {
		_notes = notes;
	}
	
}
