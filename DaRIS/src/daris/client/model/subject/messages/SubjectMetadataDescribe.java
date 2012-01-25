package daris.client.model.subject.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class SubjectMetadataDescribe extends ObjectMessage<XmlElement> {

	private String _projectId;
	private String _methodId;

	public SubjectMetadataDescribe(String projectId, String methodId) {

		_projectId = projectId;
		_methodId = methodId;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("pid", _projectId);
		w.add("mid", _methodId);

	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.subject.metadata.describe";
	}

	@Override
	protected XmlElement instantiate(XmlElement xe) throws Throwable {

		return xe;
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
