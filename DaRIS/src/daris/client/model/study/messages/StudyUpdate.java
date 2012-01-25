package daris.client.model.study.messages;

import daris.client.model.object.messages.DObjectUpdate;
import daris.client.model.study.Study;

public class StudyUpdate extends DObjectUpdate {

	public StudyUpdate(Study o) {

		super(o);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.study.update";
	}

}
