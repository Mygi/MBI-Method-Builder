package daris.client.ui.object.action;

import arc.gui.object.action.precondition.ActionPrecondition;
import arc.gui.object.action.precondition.ActionPreconditionListener;
import arc.gui.object.action.precondition.ActionPreconditionOutcome;
import arc.gui.object.action.precondition.EvaluatePrecondition;
import daris.client.model.object.DObjectRef;
import daris.client.model.repository.RepositoryRef;

public class CanSendDicomPrecondition implements ActionPrecondition {

	private DObjectRef _root;

	public CanSendDicomPrecondition(DObjectRef root) {

		_root = root;
	}

	@Override
	public EvaluatePrecondition evaluate() {

		return EvaluatePrecondition.BEFORE_INTERACTION;
	}

	@Override
	public String description() {

		return "Check if the object can send DICOM data.";
	}

	@Override
	public void execute(ActionPreconditionListener l) {

		if (_root instanceof RepositoryRef) {
			l.executed(ActionPreconditionOutcome.FAIL,
					"You cannot send the whole repository.");
		} else {
			l.executed(ActionPreconditionOutcome.PASS,
					"You can send this object.");
		}
	}
}
