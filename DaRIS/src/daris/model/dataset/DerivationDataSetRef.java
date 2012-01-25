package daris.model.dataset;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import daris.model.dataset.DerivationDataSet.Input;
import daris.model.dataset.messages.DerivationDataSetUpdate;
import daris.model.object.PSSDObjectRef;
import daris.model.object.messages.ObjectCreate;
import daris.model.object.messages.ObjectUpdate;

public class DerivationDataSetRef extends DataSetRef {
	
	private List<DerivationDataSet.Input> _inputs;

	private String _methodId;

	private String _methodStep;
	
	public DerivationDataSetRef(XmlElement oe) {

		super(oe);
	}

	public DerivationDataSetRef(String proute, String id, String name,
			String description, boolean isleaf) {

		super(proute, id, name, description, isleaf);
	}
	
	protected void parse(XmlElement oe){
		super.parse(oe);
		List<XmlElement> ies = oe.elements("derivation/input");
		if (ies != null) {
			for (int i = 0; i < ies.size(); i++) {
				XmlElement ie = ies.get(i);
				Input input = new Input(ie.value(), ie.value("@vid"));
				addInput(input);
			}
		} else {
			_inputs = null;
		}
		_methodId = oe.value("derivation/method");
		_methodStep = oe.value("derivation/method/@step");
	}
	
	private void addInput(DerivationDataSet.Input input) {

		if (_inputs == null) {
			_inputs = new Vector<Input>(1);
		}
		_inputs.add(input);

	}
	
	public List<DerivationDataSet.Input> inputs() {
		return _inputs;
	}
	
	public String methodId() {
		return _methodId;
	}

	public String methodStep() {
		return _methodStep;
	}

	@Override
	protected ObjectCreate getChildCreateMessage(PSSDObjectRef dataObject) {

		// TODO:
		// return new DataObjectCreate(this);
		return null;
	}

	@Override
	protected ObjectUpdate getUpdateMessage() {

		return new DerivationDataSetUpdate(this);
	}
}
