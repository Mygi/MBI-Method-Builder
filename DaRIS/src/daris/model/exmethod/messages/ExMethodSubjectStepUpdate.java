package daris.model.exmethod.messages;

import java.util.List;

import daris.model.exmethod.ExMethodSubjectStep;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class ExMethodSubjectStepUpdate extends ObjectMessage<Boolean> {

	private String _exMethodId;
	private String _stepPath;
	private String _state;
	private String _notes;
	private List<XmlElement> _psMeta;
	private List<XmlElement> _rsMeta;

	public ExMethodSubjectStepUpdate(ExMethodSubjectStep step) {

		this(step.exMethodId(), step.stepPath(), step.state().toString(), step.notes(), step.psPublicMetadata(), step
				.rsPublicMetadata());
	}

	public ExMethodSubjectStepUpdate(String exMethodId, String stepPath, String state, String notes,
			List<XmlElement> psMeta, List<XmlElement> rsMeta) {

		_exMethodId = exMethodId;
		_stepPath = stepPath;
		_state = state;
		_notes = notes;
		_psMeta = psMeta;
		_rsMeta = rsMeta;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("id", _exMethodId);
		w.add("step", _stepPath);
		if (_state != null) {
			w.add("state", _state);
		}
		if (_notes != null) {
			w.add("notes", _notes);
		}
		if (_psMeta != null) {
			if (_psMeta.size() > 0) {
				w.push("ps-meta");
				for (XmlElement me : _psMeta) {
					w.add(me);
				}
				w.pop();
			}
		}
		if (_rsMeta != null) {
			if (_rsMeta.size() > 0) {
				w.push("rs-meta");
				for (XmlElement me : _rsMeta) {
					w.add(me);
				}
				w.pop();
			}
		}
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.ex-method.subject.step.update";
	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {

		return true;
	}

	@Override
	protected String objectTypeName() {

		return "ex-method.step";
	}

	@Override
	protected String idToString() {

		return _exMethodId + "_" + _stepPath;
	}

}
