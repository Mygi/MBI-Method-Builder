package daris.model.dataset;

import arc.mf.client.xml.XmlElement;

/**
 * The class for primary datasets.
 * 
 * @author Wilson Liu
 * 
 */
public class PrimaryDataSet extends DataSet {

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

	/**
	 * The constructor
	 * 
	 * @param xe
	 *            The XML element represents the primary dataset.
	 * @throws Throwable
	 */
	public PrimaryDataSet(XmlElement xe) throws Throwable {

		super(xe);
		_subjectId = xe.value("acquisition/subject/id");
		_subjectState = xe.value("acquisition/subject/state");
		_methodId = xe.value("acquisition/method");
		_methodStep = xe.value("acquisition/method/@step");

	}

	/**
	 * Returns the citeable id of the method.
	 * 
	 * @return
	 */
	public String methodId() {
		return _methodId;
	}

	/**
	 * Returns the id of the method step.
	 * 
	 * @return
	 */
	public String methodStep() {
		return _methodStep;
	}

	/**
	 * Sets the method of this primary dataset.
	 * 
	 * @param id
	 *            the id of the method.
	 * @param step
	 *            the step of the method.
	 */
	public void setMethod(String id, String step) {
		_methodId = id;
		_methodStep = step;
	}

	/**
	 * Sets the subject.
	 * 
	 * @param id
	 *            the id of the subject.
	 * @param state
	 *            the state of the subject.
	 */
	public void setSubject(String id, String state) {
		_subjectId = id;
		_subjectState = state;
	}

	/**
	 * Returns the subject (citeable) id.
	 * @return
	 */
	public String subjectId() {
		return _subjectId;
	}

	/**
	 * Returns the subject state.
	 * @return
	 */
	public String subjectState() {
		return _subjectState;
	}

	@Override
	public String typeName() {
		return DataSet.TYPE_NAME;
	}

}
