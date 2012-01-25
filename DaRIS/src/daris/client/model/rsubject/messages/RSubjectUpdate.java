package daris.client.model.rsubject.messages;

import daris.client.model.object.messages.DObjectUpdate;
import daris.client.model.rsubject.RSubject;

public class RSubjectUpdate extends DObjectUpdate {

	public RSubjectUpdate(RSubject o) {

		super(o);
	}

	@Override
	protected String messageServiceName() {

		// TODO service not yet implemented.
		return "om.pssd.r-subject.update";
	}

}
