package daris.client.model.study.messages;

import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.DObjectCreate;
import daris.client.model.study.Study;

public class StudyCreate extends DObjectCreate {

	public StudyCreate(DObjectRef po, Study o) {

		super(po, o);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.study.create";
	}

}
