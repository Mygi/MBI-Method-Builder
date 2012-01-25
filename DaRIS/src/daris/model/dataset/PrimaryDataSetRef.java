package daris.model.dataset;

import arc.mf.client.xml.XmlElement;
import daris.model.dataset.messages.PrimaryDataSetUpdate;
import daris.model.object.PSSDObjectRef;
import daris.model.object.messages.ObjectCreate;
import daris.model.object.messages.ObjectUpdate;

public class PrimaryDataSetRef extends DataSetRef {

	/**
	 * The citeable id of the method.
	 */
	private String _methodId;

	/**
	 * The id of the method step.
	 */
	private String _methodStep;

	/**
	 * The citeable id of the subject.
	 */
	private String _subjectId;

	/**
	 * The state of the subject.
	 */
	private String _subjectState;

	public PrimaryDataSetRef(XmlElement oe) {

		super(oe);

	}

	public PrimaryDataSetRef(String proute, String id, String name,
			String description, boolean isleaf) {

		super(proute, id, name, description, isleaf);

	}

	@Override
	protected void parse(XmlElement oe) {
		super.parse(oe);
		_subjectId = oe.value("acquisition/subject/id");
		_subjectState = oe.value("acquisition/subject/state");
		_methodId = oe.value("acquisition/method");
		_methodStep = oe.value("acquisition/method/@step");
	}

	@Override
	protected ObjectCreate getChildCreateMessage(PSSDObjectRef dataObject) {

		// TODO:
		// return new DataObjectCreate(this);
		return null;

	}

	@Override
	protected ObjectUpdate getUpdateMessage() {

		return new PrimaryDataSetUpdate(this);

	}

	public String subjectId() {
		return _subjectId;
	}

	public String subjectState() {
		return _subjectState;
	}

	public String methodId() {
		return _methodId;
	}

	public String methodStep() {
		return _methodStep;
	}

}
