package daris.model.exmethod;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import daris.model.method.Method;
import daris.model.object.PSSDObject;

public class ExMethod extends PSSDObject {

	public static final String TYPE_NAME = "ex-method";
	private Method _method;
	private List<ExMethodStep> _steps;
	private State _state;

	public ExMethod(XmlElement oe) throws Throwable {

		super(oe);
		_state = State.instantiate(oe.stringValue("state",
				State.incomplete.toString()));
		_method = null;
		if (oe.element("method") != null) {
			_method = new Method(oe.element("method"));
		}
		_steps = null;
		List<XmlElement> ses = oe.elements("step");
		if (ses != null) {
			_steps = new Vector<ExMethodStep>(ses.size());
			for (XmlElement se : ses) {
				String stepPath = se.value("@path");
				String state = se.value("state");
				String notes = se.value("notes");
				ExMethodStep ems = new ExMethodStep(id(), stepPath, null,
						state == null ? null : State.instantiate(state), notes,
						false);
				_steps.add(ems);
			}
		}
	}

	public Method method() {

		return _method;
	}

	public void setNotesForStep(String stepPath, String notes) {

		ExMethodStep ems = step(stepPath);
		if (ems != null) {
			ems.setNotes(notes);
		}
	}

	public void setStateForStep(String stepPath, State state) {

		ExMethodStep ems = step(stepPath);
		if (ems != null) {
			ems.setState(state);
		}
	}

	public ExMethodStep step(String stepPath) {

		if (_steps == null) {
			return null;
		}

		for (ExMethodStep ems : _steps) {
			if (ems.stepPath().equals(stepPath)) {
				return ems;
			}
		}
		return null;
	}

	public List<ExMethodStep> steps() {

		return _steps;
	}

	public State state() {

		return _state;
	}

	@Override
	public String typeName() {

		return ExMethod.TYPE_NAME;

	}

}
