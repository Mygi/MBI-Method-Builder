package daris.client.ui.object.action;

import arc.gui.object.action.precondition.ActionPrecondition;
import arc.gui.object.action.precondition.ActionPreconditionListener;
import arc.gui.object.action.precondition.ActionPreconditionOutcome;
import arc.gui.object.action.precondition.EvaluatePrecondition;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.dicom.messages.DicomDataSetCount;
import daris.client.model.object.DObjectRef;

public class HasDicomDataPrecondition implements ActionPrecondition {

	private DObjectRef _root;

	public HasDicomDataPrecondition(DObjectRef root) {

		_root = root;
	}

	@Override
	public EvaluatePrecondition evaluate() {

		return EvaluatePrecondition.BEFORE_INTERACTION;
	}

	@Override
	public String description() {

		return "Checking if object " + _root.id() == null ? "" : _root.id() + " contains DICOM data sets.";
	}

	@Override
	public void execute(final ActionPreconditionListener l) {

		new DicomDataSetCount(_root).send(new ObjectMessageResponse<Integer>() {

			@Override
			public void responded(Integer count) {

				if (count > 0) {
					l.executed(ActionPreconditionOutcome.PASS, "The object " + (_root.id() == null ? "" : _root.id())
							+ " contains " + count + " DICOM datasets.");
				} else {
					l.executed(ActionPreconditionOutcome.FAIL, "The object " + (_root.id() == null ? "" : _root.id())
							+ " contains no DICOM datasets.");
				}
			}
		});
	}

}
