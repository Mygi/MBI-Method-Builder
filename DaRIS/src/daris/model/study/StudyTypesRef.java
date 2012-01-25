package daris.model.study;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class StudyTypesRef extends ObjectRef<List<StudyType>> {

	private String _service;
	private String _exMethodId;
	
	public StudyTypesRef() {
		this(null);
	}

	public StudyTypesRef(String exMethodId) {
		if (exMethodId != null) {
			_exMethodId = exMethodId;
			_service = "om.pssd.ex-method.study.type.list";
		} else {
			_exMethodId = null;
			_service = "om.pssd.study.type.describe";
		}
	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		if(_exMethodId!=null){
			w.add("id", _exMethodId);
		}
	}

	@Override
	protected String resolveServiceName() {

		return _service;
	}

	@Override
	protected List<StudyType> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			List<XmlElement> stes = xe.elements("type");
			if (stes != null) {
				Vector<StudyType> sts = new Vector<StudyType>(stes.size());
				for (XmlElement ste : stes) {
					sts.add(new StudyType(ste.value("name"), ste
							.value("description")));
				}
				return sts;
			}
		}
		return null;
	}

	@Override
	public String referentTypeName() {

		return "study_types";
	}

	@Override
	public String idToString() {

		return "study_types";
	}

}
