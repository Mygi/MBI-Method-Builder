package daris.client.ui.object.action;

import arc.gui.object.action.precondition.ActionPrecondition;
import arc.gui.object.action.precondition.ActionPreconditionListener;
import arc.gui.object.action.precondition.ActionPreconditionOutcome;
import arc.gui.object.action.precondition.EvaluatePrecondition;
import arc.mf.object.ObjectResolveHandler;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;

public class MetaForEditPrecondition implements ActionPrecondition {

	private DObjectRef _o;

	public MetaForEditPrecondition(DObjectRef o) {

		_o = o;
	}

	@Override
	public EvaluatePrecondition evaluate() {

		return EvaluatePrecondition.DURING_INTERACTION;
	}

	@Override
	public String description() {

		return "Retrieve the metadata with definitions for editting the "
				+ _o.referentTypeName() + ".";
	}

	@Override
	public void execute(final ActionPreconditionListener l) {

		if (!_o.forEdit() || _o.needToResolve(true)) {
			_o.setForEdit(true);
			_o.resolveAndLock(new ObjectResolveHandler<DObject>() {

				@Override
				public void resolved(DObject obj) {

					if (obj != null) {
						l.executed(ActionPreconditionOutcome.PASS,
								"Metadata with definitions has been retrieved.");
					} else {
						l.executed(ActionPreconditionOutcome.FAIL,
								"Failed to retrieve the object (metadata)");
					}
				}
			});
		} else {
			l.executed(ActionPreconditionOutcome.PASS,
					"Metadata with definitions has been retrieved.");
		}
	}

}
