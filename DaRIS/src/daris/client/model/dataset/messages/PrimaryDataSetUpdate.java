package daris.client.model.dataset.messages;

import daris.client.model.dataset.PrimaryDataSet;
import daris.client.model.object.messages.DObjectUpdate;

public class PrimaryDataSetUpdate extends DObjectUpdate {

	public PrimaryDataSetUpdate(PrimaryDataSet o) {

		super(o);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.dataset.primary.update";
	}

}
