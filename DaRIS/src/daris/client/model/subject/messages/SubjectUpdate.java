package daris.client.model.subject.messages;

import daris.client.model.object.messages.DObjectUpdate;
import daris.client.model.subject.Subject;

public class SubjectUpdate extends DObjectUpdate {

	public SubjectUpdate(Subject o) {

		super(o);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.subject.update";
	}

}
