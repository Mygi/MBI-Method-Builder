package daris.client.model.exmethod.messages;

import java.util.List;

import daris.client.model.exmethod.ExMethod;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class ExMethodSubjectStepFind extends ObjectMessage<List<String>> {

	private String _exMethodId;

	public ExMethodSubjectStepFind(String exMethodId) {

		_exMethodId = exMethodId;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("id", _exMethodId);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.ex-method.subject.step.find";
	}

	@Override
	protected List<String> instantiate(XmlElement xe) throws Throwable {

		return xe.values("ex-method/step");
	}

	@Override
	protected String objectTypeName() {

		return ExMethod.TYPE_NAME;
	}

	@Override
	protected String idToString() {

		return _exMethodId;
	}

}
