package daris.client.ui.object.action;

import arc.gui.object.action.precondition.ActionPrecondition;
import arc.gui.object.action.precondition.ActionPreconditionListener;
import arc.gui.object.action.precondition.ActionPreconditionOutcome;
import arc.gui.object.action.precondition.EvaluatePrecondition;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.dataset.messages.DataSetCount;
import daris.client.model.object.DObjectRef;

public class HasDataSetPrecondition implements ActionPrecondition {

	private DObjectRef _root;

	public HasDataSetPrecondition(DObjectRef root) {

		_root = root;
	}

	@Override
	public EvaluatePrecondition evaluate() {

		return EvaluatePrecondition.BEFORE_INTERACTION;
	}

	@Override
	public String description() {

		return "Checking if object " + _root.id() == null ? "" : _root.id() + " contains datasets.";
	}

	@Override
	public void execute(final ActionPreconditionListener l) {

		new DataSetCount(_root).send(new ObjectMessageResponse<Integer>() {

			@Override
			public void responded(Integer count) {

				if (count > 0) {
					l.executed(ActionPreconditionOutcome.PASS, "The object " + (_root.id() == null ? "" : _root.id())
							+ " contains " + count + " datasets.");
				} else {
					l.executed(ActionPreconditionOutcome.FAIL, "The object " + (_root.id() == null ? "" : _root.id())
							+ " contains no datasets.");
				}
			}
		});
	}

}