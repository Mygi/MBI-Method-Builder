package daris.model.study.messages;

import arc.mf.client.xml.XmlWriter;
import daris.model.exmethod.ExMethodRef;
import daris.model.object.messages.ObjectCreate;
import daris.model.study.Study;
import daris.model.study.StudyRef;

public class StudyCreate extends ObjectCreate {

	public StudyCreate(ExMethodRef parent, StudyRef study) {

		super(parent, study);

	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.study.create";

	}

	@Override
	protected String objectTypeName() {

		return Study.TYPE_NAME;

	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		super.messageServiceArgs(w);

		w.add("pid", parent().id());

		StudyRef s = (StudyRef) object();
		if (s.studyType() != null) {
			w.add("type", s.studyType());
		}
		if (s.methodStep() != null) {
			w.add("step", s.methodStep());
		}
		if (s.metaToSave() != null) {
			w.add(s.metaToSave(), true);
		}
	}

}
