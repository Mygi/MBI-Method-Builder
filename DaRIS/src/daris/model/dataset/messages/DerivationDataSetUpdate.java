package daris.model.dataset.messages;

import daris.model.dataset.DerivationDataSet;
import daris.model.dataset.DerivationDataSetRef;
import daris.model.object.messages.ObjectUpdate;

public class DerivationDataSetUpdate extends ObjectUpdate {

	public DerivationDataSetUpdate(DerivationDataSetRef ref) {
		
		super(ref);
		
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.dataset.derivation.update";

	}

	@Override
	protected String objectTypeName() {
		
		return DerivationDataSet.TYPE_NAME;
		
	}

}
