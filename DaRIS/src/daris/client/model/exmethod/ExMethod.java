package daris.client.model.exmethod;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import daris.client.model.exmethod.messages.ExMethodCreate;
import daris.client.model.exmethod.messages.ExMethodUpdate;
import daris.client.model.method.Method;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.DObjectCreate;
import daris.client.model.object.messages.DObjectUpdate;

public class ExMethod extends DObject {

	public static final String TYPE_NAME = "ex-method";

	private Method _method;
	private List<ExMethodStep> _steps;
	private State _state;

	public ExMethod(XmlElement oe) {

		super(oe);

		try {
			_state = State.parse(oe.stringValue("state", State.incomplete.toString()));
		} catch (Throwable e) {
			_state = State.incomplete;
		}
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
				ExMethodStep ems = new ExMethodStep(id(), stepPath, null, state == null ? null : State.parse(state),
						notes, false);
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

	@Override
	protected DObjectCreate objectCreateMessage(DObjectRef po) {

		return new ExMethodCreate(po, this);
	}

	@Override
	protected DObjectUpdate objectUpdateMessage() {

		return new ExMethodUpdate(this);
	}

}
