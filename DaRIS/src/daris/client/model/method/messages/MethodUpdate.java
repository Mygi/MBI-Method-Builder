package daris.client.model.method.messages;

import daris.client.model.method.Method;
import daris.client.model.object.messages.DObjectUpdate;

public class MethodUpdate extends DObjectUpdate {

	public MethodUpdate(Method o) {

		super(o);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.method.update";
	}

}
