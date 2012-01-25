package daris.model.subject.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.IDUtil;
import daris.model.object.PSSDObjectMeta;
import daris.model.subject.SubjectMeta;
import daris.model.subject.SubjectRef;

public class SubjectMetaDescribe extends ObjectMessage<PSSDObjectMeta> {

	private String _projectId;

	private String _methodId;

	public SubjectMetaDescribe(String projectId, String methodId) {

		_projectId = projectId;
		_methodId = methodId;
	}

	public SubjectMetaDescribe(SubjectRef subjectRef) {

		this(IDUtil.getParentId(subjectRef.id()), subjectRef.method().id());
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("mid", _methodId);
		w.add("pid", _projectId);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.subject.metadata.describe";
	}

	@Override
	protected PSSDObjectMeta instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			return new SubjectMeta(xe);
		}
		return null;
	}

	@Override
	protected String objectTypeName() {

		return "Subject Metadata Definition";
	}

	@Override
	protected String idToString() {

		return "subject-metadata-definition";
	}
}
