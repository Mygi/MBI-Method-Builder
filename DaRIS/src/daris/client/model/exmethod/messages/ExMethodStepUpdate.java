package daris.client.model.exmethod.messages;

import daris.client.model.exmethod.ExMethodStep;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class ExMethodStepUpdate extends ObjectMessage<Boolean> {

	private String _exMethodId;
	private String _stepPath;
	private String _state;
	private String _notes;

	public ExMethodStepUpdate(String exMethodId, String stepPath, String state,
			String notes) {

		_exMethodId = exMethodId;
		_stepPath = stepPath;
		_state = state;
		_notes = notes;
	}

	public ExMethodStepUpdate(String exMethodId, String stepPath, String state) {

		this(exMethodId, stepPath, state, null);
	}

	public ExMethodStepUpdate(ExMethodStep step) {

		this(step.exMethodId(), step.stepPath(), step.state().toString(), step
				.notes());
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("id", _exMethodId);
		w.add("step", _stepPath);
		w.add("state", _state);
		if (_notes != null) {
			w.add("notes", _notes);
		}
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.ex-method.step.update";
	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {

		return xe != null;
	}

	@Override
	protected String objectTypeName() {

		return "ex-method step";
	}

	@Override
	protected String idToString() {

		return _exMethodId + "_" + _stepPath;
	}

}
