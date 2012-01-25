package daris.client.model.dataset.messages;

import daris.client.model.dataset.DerivationDataSet;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.DObjectCreate;

public class DerivationDataSetCreate extends DObjectCreate {

	public DerivationDataSetCreate(DObjectRef po, DerivationDataSet o) {

		super(po, o);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.dataset.derivation.create";
	}

}
