package daris.client.model.exmethod.messages;

import daris.client.model.exmethod.ExMethod;
import daris.client.model.object.messages.DObjectUpdate;

public class ExMethodUpdate extends DObjectUpdate {

	public ExMethodUpdate(ExMethod o) {

		super(o);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.ex-method.update";
	}

}
