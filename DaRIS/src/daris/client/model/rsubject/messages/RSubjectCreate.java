package daris.client.model.rsubject.messages;

import daris.client.model.object.messages.DObjectCreate;
import daris.client.model.rsubject.RSubject;

public class RSubjectCreate extends DObjectCreate {

	public RSubjectCreate(RSubject o) {

		super(null, o);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.r-subject.create";
	}

}
