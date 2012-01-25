package daris.model.exmethod.messages;

import daris.model.exmethod.ExMethod;
import daris.model.exmethod.ExMethodRef;
import daris.model.object.messages.ObjectCreate;
import daris.model.subject.SubjectRef;

public class ExMethodCreate extends ObjectCreate {

	public ExMethodCreate(SubjectRef parent, ExMethodRef exmethod) {
		
		super(parent, exmethod);

	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.ex-method.create";

	}

	@Override
	protected String objectTypeName() {

		return ExMethod.TYPE_NAME;

	}

}
