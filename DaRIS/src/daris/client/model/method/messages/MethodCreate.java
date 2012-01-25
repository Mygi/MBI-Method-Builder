package daris.client.model.method.messages;

import daris.client.model.method.Method;
import daris.client.model.object.messages.DObjectCreate;

public class MethodCreate extends DObjectCreate {

	public MethodCreate(Method o) {

		super(null, o);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.method.create";
	}

}
