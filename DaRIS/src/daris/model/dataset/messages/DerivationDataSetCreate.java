package daris.model.dataset.messages;

import daris.model.dataset.DerivationDataSetRef;
import daris.model.dataset.PrimaryDataSet;
import daris.model.object.messages.ObjectCreate;
import daris.model.study.StudyRef;

public class DerivationDataSetCreate extends ObjectCreate {

	public DerivationDataSetCreate(StudyRef parent, DerivationDataSetRef dataset) {

		super(parent, dataset);

	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.dataset.primary.create";

	}

	@Override
	protected String objectTypeName() {

		return PrimaryDataSet.TYPE_NAME;

	}

}
