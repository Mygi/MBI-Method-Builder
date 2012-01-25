package daris.client.model.exmethod.messages;

import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class ExMethodStudyStepFind extends ObjectMessage<List<XmlElement>> {

	private String _exMethodId;
	private String _type;

	public ExMethodStudyStepFind(String exMethodId, String type) {

		_exMethodId = exMethodId;
		_type = type;
	}

	public ExMethodStudyStepFind(String exMethodId) {

		this(exMethodId, null);
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		if (_type != null) {
			w.add("type", _type);
		}
		w.add("id", _exMethodId);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.ex-method.study.step.find";
	}

	@Override
	protected List<XmlElement> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			return xe.elements("ex-method/step");
		}
		return null;
	}

	@Override
	protected String objectTypeName() {

		return null;
	}

	@Override
	protected String idToString() {

		return null;
	}

}
