package daris.model.dataset.messages;

import daris.model.dataset.DerivationDataSet;
import daris.model.dataset.PrimaryDataSetRef;
import daris.model.object.messages.ObjectCreate;
import daris.model.study.StudyRef;

public class PrimaryDataSetCreate extends ObjectCreate {

	public PrimaryDataSetCreate(StudyRef parent, PrimaryDataSetRef dataset) {
		
		super(parent, dataset);

	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.dataset.derivation.create";

	}

	@Override
	protected String objectTypeName() {

		return DerivationDataSet.TYPE_NAME;

	}

}
