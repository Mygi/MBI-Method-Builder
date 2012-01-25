package daris.client.model.dataset.messages;

import daris.client.model.dataset.PrimaryDataSet;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.DObjectCreate;

public class PrimaryDataSetCreate extends DObjectCreate {

	public PrimaryDataSetCreate(DObjectRef po, PrimaryDataSet o) {

		super(po, o);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.dataset.primary.create";
	}

}
