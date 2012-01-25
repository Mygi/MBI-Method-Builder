package daris.model.exmethod;

import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;
import daris.client.model.IDUtil;
import daris.model.study.StudyRef;

public class StepsRef extends ObjectRef<List<XmlElement>> {

	public StepsRef(StudyRef study) {
		
		this(IDUtil.getParentId(study.id()), study.studyType());
	}

	public StepsRef(ExMethodRef xm) {
		
		this(xm.id());
	}

	public StepsRef(String id) {
		
		this(id, null);
	}

	public StepsRef(String id, String type) {
		_id = id;
		_type = type;
	}

	/**
	 * ex-method id
	 */
	private String _id;

	/**
	 * study type
	 */
	private String _type;

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		w.add("id", _id);
		if (_type != null) {
			w.add("type", _type);
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

		return "steps";
	}

	@Override
	public String idToString() {

		return "steps";
	}

}
