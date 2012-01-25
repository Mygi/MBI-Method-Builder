package daris.client.model.rsubject;

import arc.mf.client.xml.XmlElement;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.DObjectCreate;
import daris.client.model.object.messages.DObjectUpdate;
import daris.client.model.rsubject.messages.RSubjectCreate;
import daris.client.model.rsubject.messages.RSubjectUpdate;

public class RSubject extends DObject {

	public static final String TYPE_NAME = "r-subject";

	public RSubject(XmlElement xe) {

		super(xe);
	}

	@Override
	public String typeName() {

		return RSubject.TYPE_NAME;
	}

	@Override
	protected DObjectCreate objectCreateMessage(DObjectRef po) {

		return new RSubjectCreate(this);
	}

	@Override
	protected DObjectUpdate objectUpdateMessage() {

		return new RSubjectUpdate(this);
	}

}
