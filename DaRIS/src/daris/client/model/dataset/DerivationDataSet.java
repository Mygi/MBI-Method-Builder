package daris.client.model.dataset;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import daris.client.model.dataset.messages.DerivationDataSetCreate;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.DObjectCreate;
import daris.client.model.object.messages.DObjectUpdate;

public class DerivationDataSet extends DataSet {

	public class Input {
		private String _id;
		private String _vid;

		public Input(String id, String vid) {

			_id = id;
			_vid = vid;
		}

		public String id() {

			return _id;
		}

		public String vid() {

			return _vid;
		}
	}

	private List<Input> _inputs;

	private String _methodId;

	private String _methodStep;

	public DerivationDataSet(XmlElement dde) {

		super(dde);
		List<XmlElement> ies = dde.elements("derivation/input");
		if (ies != null) {
			if (!ies.isEmpty()) {
				_inputs = new Vector<Input>(ies.size());
				for (XmlElement ie : ies) {
					Input input = new Input(ie.value(), ie.value("@vid"));
					_inputs.add(input);
				}
			}
		}
		_methodId = dde.value("derivation/method");
		_methodStep = dde.value("derivation/method/@step");
	}

	public List<Input> inputs() {

		return _inputs;
	}

	public String methodId() {

		return _methodId;
	}

	public String methodStep() {

		return _methodStep;
	}

	@Override
	protected DObjectCreate objectCreateMessage(DObjectRef po) {

		return new DerivationDataSetCreate(po, this);
	}

	@Override
	protected DObjectUpdate objectUpdateMessage() {

		// TODO Auto-generated method stub
		return null;
	}

}
