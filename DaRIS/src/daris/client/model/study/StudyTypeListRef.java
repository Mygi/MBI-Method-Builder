package daris.client.model.study;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class StudyTypeListRef extends ObjectRef<List<StudyType>> {

	private String _exMethodId;

	public StudyTypeListRef() {

		this(null);
	}

	public StudyTypeListRef(String exMethodId) {

		_exMethodId = exMethodId;
	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		if (_exMethodId != null) {
			w.add("id", _exMethodId);
		}
	}

	@Override
	protected String resolveServiceName() {

		if (_exMethodId != null) {
			return "om.pssd.ex-method.study.type.list";
		} else {
			return "om.pssd.study.type.describe";
		}

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

		return "list of study types";
	}

	@Override
	public String idToString() {

		return _exMethodId;
	}

}
