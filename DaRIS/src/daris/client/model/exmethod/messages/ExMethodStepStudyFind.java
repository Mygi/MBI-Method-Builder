package daris.client.model.exmethod.messages;

import java.util.List;
import java.util.Vector;

import daris.client.model.study.Study;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class ExMethodStepStudyFind extends ObjectMessage<List<Study>> {

	private String _exMethodId;
	private String _stepPath;

	public ExMethodStepStudyFind(String exMethodId, String stepPath) {

		_exMethodId = exMethodId;
		_stepPath = stepPath;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("id", _exMethodId);
		w.add("step", _stepPath);

	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.ex-method.step.study.find";
	}

	@Override
	protected List<Study> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			List<XmlElement> oes = xe.elements("object");
			if (oes != null) {
				List<Study> studies = new Vector<Study>(oes.size());
				for (XmlElement oe : oes) {
					studies.add(new Study(oe));
				}
				if (studies.size() > 0) {
					return studies;
				}
			}
		}
		return null;
	}

	@Override
	protected String objectTypeName() {

		return "list of studies for the step";
	}

	@Override
	protected String idToString() {

		return _exMethodId + "_" + _stepPath;
	}

}
