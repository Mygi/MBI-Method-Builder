package daris.model.method;

import java.util.List;

import arc.mf.client.xml.XmlElement;

public class SubjectActionStep extends Step {

	private List<XmlElement> _epsMeta;
	private List<XmlElement> _ersMeta;

	public SubjectActionStep(int id, String name, String description, List<XmlElement> editablePSubjectMeta,
			List<XmlElement> editableRSubjectMeta) {

		super(id, name, description);

		_epsMeta = editablePSubjectMeta;
		_ersMeta = editableRSubjectMeta;
	}

	public List<XmlElement> editablePSubjectMetadata() {

		return _epsMeta;
	}

	public List<XmlElement> editableRSubjectMetadata() {

		return _ersMeta;
	}
}