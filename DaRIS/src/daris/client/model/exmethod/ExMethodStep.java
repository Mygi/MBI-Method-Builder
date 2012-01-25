package daris.client.model.exmethod;


public class ExMethodStep {

	private String _exMethodId;
	private String _stepPath;
	private String _name;
	private State _state;
	private String _notes;
	private boolean _editable;

	public ExMethodStep(String exMethodId, String stepPath, String name,
			State state, String notes, boolean editable) {

		_exMethodId = exMethodId;
		_stepPath = stepPath;
		_name = name;
		_state = state;
		_notes = notes;
		_editable = editable;
	}

	public String exMethodId() {

		return _exMethodId;
	}

	public String stepPath() {

		return _stepPath;
	}

	public String name() {

		return _name;
	}

	public State state() {

		return _state;
	}

	public void setState(State state) {

		_state = state;
	}

	public String notes() {

		return _notes;
	}

	public void setNotes(String notes) {

		_notes = notes;
	}

	public boolean editable() {

		return _editable;
	}
}