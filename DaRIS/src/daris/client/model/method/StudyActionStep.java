package daris.client.model.method;

import java.util.List;

import arc.mf.client.xml.XmlElement;

public class StudyActionStep extends Step {

	private List<XmlElement> _esMeta;
	private String _type;

	public StudyActionStep(int id, String name, String description, String type, List<XmlElement> editableStudyMeta) {

		super(id, name, description);

		_type = type;
		_esMeta = editableStudyMeta;
	}

	public List<XmlElement> editableStudyMeta() {

		return _esMeta;
	}

	public String type() {

		return _type;
	}
}