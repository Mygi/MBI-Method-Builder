package daris.model.study.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.IDUtil;
import daris.model.object.PSSDObjectMeta;
import daris.model.study.StudyMeta;
import daris.model.study.StudyRef;

public class StudyMetaDescribe extends ObjectMessage<PSSDObjectMeta> {
	private String _exMethodId;

	private String _step;

	public StudyMetaDescribe(String exMethodId, String stepId) {

		_exMethodId = exMethodId;
		_step = stepId;
	}

	public StudyMetaDescribe(StudyRef study) {
		this(IDUtil.getParentId(study.id()), study.methodStep());
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("pid", _exMethodId);
		w.add("step", _step);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.study.metadata.describe";
	}

	@Override
	protected PSSDObjectMeta instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			return new StudyMeta(xe);
		}
		return null;
	}

	@Override
	protected String objectTypeName() {

		return "Study Metadata Definition";
	}

	@Override
	protected String idToString() {

		return "study-metadata-definition";
	}
}
