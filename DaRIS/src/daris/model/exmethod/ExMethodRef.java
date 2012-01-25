package daris.model.exmethod;

import arc.mf.client.xml.XmlElement;
import daris.model.exmethod.messages.ExMethodUpdate;
import daris.model.object.PSSDObjectRef;
import daris.model.object.messages.ObjectCreate;
import daris.model.object.messages.ObjectUpdate;
import daris.model.study.StudyRef;
import daris.model.study.messages.StudyCreate;

public class ExMethodRef extends PSSDObjectRef {

	public ExMethodRef(XmlElement oe) {

		super(oe);

	}

	public ExMethodRef(String proute, String id, String name,
			String description, boolean isleaf) {

		super(proute, id, name, description, isleaf);

	}

	@Override
	protected ObjectCreate getChildCreateMessage(PSSDObjectRef study) {

		return new StudyCreate(this, (StudyRef) study);

	}

	@Override
	protected ObjectUpdate getUpdateMessage() {

		return new ExMethodUpdate(this);

	}

	@Override
	public String referentTypeName() {

		return "ex-method";
	}

}
