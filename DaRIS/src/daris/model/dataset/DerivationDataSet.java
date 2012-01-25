package daris.model.dataset;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;

/**
 * The class represents derivation dataset objects.
 * 
 * @author Wilson Liu
 * 
 */
public class DerivationDataSet extends DataSet {

	/**
	 * The nested class to represent input datasets.
	 * 
	 * @author Wilson Liu
	 * 
	 */
	public static class Input {

		/**
		 * citeable id of the input dataset.
		 */
		private String _id;

		/**
		 * version id of the input dataset.
		 */
		private String _vid;

		/**
		 * Constructor.
		 * 
		 * @param id
		 *            the citeable id of the input dataset.
		 * @param vid
		 *            the version id of the input dataset.
		 */
		public Input(String id, String vid) {
			_id = id;
			_vid = vid;
		}

		/**
		 * 
		 * @return the citeable id of the input dataset.
		 */
		public String id() {
			return _id;
		}

		/**
		 * 
		 * @return the version id of the input dataset.
		 */
		public String vid() {
			return _vid;
		}
	}

	/**
	 * The List of the input datasets.
	 */
	private List<Input> _inputs;

	/**
	 * The citeable id of the method.
	 */
	private String _methodId;

	/**
	 * The method step id.
	 */
	private String _methodStep;

	/**
	 * The constructor.
	 * 
	 * @param xe
	 *            The XML element represents the dataset object.
	 * @throws Throwable
	 */
	public DerivationDataSet(XmlElement xe) throws Throwable {

		super(xe);
		List<XmlElement> ies = xe.elements("derivation/input");
		if (ies != null) {
			for (int i = 0; i < ies.size(); i++) {
				XmlElement ie = ies.get(i);
				Input input = new Input(ie.value(), ie.value("@vid"));
				addInput(input);
			}
		}
		_methodId = xe.value("derivation/method");
		_methodStep = xe.value("derivation/method/@step");

	}

	/**
	 * Add a input dataset.
	 * 
	 * @param input
	 */
	private void addInput(DerivationDataSet.Input input) {

		if (_inputs == null) {
			_inputs = new Vector<Input>(1);
		}
		_inputs.add(input);

	}

	/**
	 * Set the input list.
	 * 
	 * @param inputs
	 */
	public void setInputs(List<Input> inputs) {
		_inputs = inputs;
	}

	/**
	 * Returns the method citeable id.
	 * 
	 * @return
	 */
	public String methodId() {
		return _methodId;
	}

	/**
	 * returns the method step.
	 * 
	 * @return
	 */
	public String methodStep() {
		return _methodStep;
	}

	/**
	 * Sets the method.
	 * 
	 * @param id
	 *            the citeable id of the method.
	 * @param step
	 *            the step of the method.
	 */
	public void setMethod(String id, String step) {
		_methodId = id;
		_methodStep = step;
	}

	/**
	 * Returns the inputs.
	 * 
	 * @return
	 */
	public List<Input> inputs() {
		return _inputs;
	}

	@Override
	public String typeName() {

		return DataSet.TYPE_NAME;
	
	}

}
