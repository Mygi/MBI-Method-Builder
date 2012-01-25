package daris.client.model.exmethod.messages;

import daris.client.model.exmethod.ExMethod;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.DObjectCreate;

public class ExMethodCreate extends DObjectCreate {

	public ExMethodCreate(DObjectRef po, ExMethod o) {

		super(po, o);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.ex-method.create";
	}

}
