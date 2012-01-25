package daris.client.model.exmethod;

import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;
import daris.client.model.study.Study;
import daris.client.model.study.StudyType;

public class StepElementListRef extends ObjectRef<List<XmlElement>> {

	public StepElementListRef(Study study) {

		this(study.exMethodId(), study.studyType() == null ? null : study.studyType().name());
	}

	public StepElementListRef(ExMethod exMethod) {

		this(exMethod.id());
	}

	public StepElementListRef(String exMethodId) {

		this(exMethodId, (String) null);
	}


	public StepElementListRef(String exMethodId, String studyType) {

		_exMethodId = exMethodId;
		_studyType = studyType;
	}

	private String _exMethodId;

	private String _studyType;

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		w.add("id", _exMethodId);
		if (_studyType != null) {
			w.add("type", _studyType);
		}
	}

	@Override
	protected String resolveServiceName() {

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
	public String referentTypeName() {

		return "list of step (XML) elements";
	}

	@Override
	public String idToString() {

		return null;
	}

}
