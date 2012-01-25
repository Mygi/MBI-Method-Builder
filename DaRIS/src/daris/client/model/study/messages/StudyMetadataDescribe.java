package daris.client.model.study.messages;

import daris.client.model.study.Study;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class StudyMetadataDescribe extends ObjectMessage<XmlElement> {

	private String _exMethodId;
	private String _stepPath;

	public StudyMetadataDescribe(Study study) {

		this(study.exMethodId(), study.stepPath());
	}

	public StudyMetadataDescribe(String exMethodId, String stepPath) {

		_exMethodId = exMethodId;
		_stepPath = stepPath;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("pid", _exMethodId);
		w.add("step", _stepPath);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.study.metadata.describe";
	}

	@Override
	protected XmlElement instantiate(XmlElement xe) throws Throwable {

		return xe;
	}

	@Override
	protected String objectTypeName() {

		return "study metadata";
	}

	@Override
	protected String idToString() {

		return null;
	}
}
