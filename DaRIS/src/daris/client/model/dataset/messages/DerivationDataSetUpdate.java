package daris.client.model.dataset.messages;

import daris.client.model.object.DObject;
import daris.client.model.object.messages.DObjectUpdate;

public class DerivationDataSetUpdate extends DObjectUpdate {

	protected DerivationDataSetUpdate(DObject o) {

		super(o);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.dataset.derivation.update";
	}

}
