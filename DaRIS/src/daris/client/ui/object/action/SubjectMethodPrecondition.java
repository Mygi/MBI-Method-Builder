package daris.client.ui.object.action;

import java.util.List;

import arc.gui.object.action.precondition.ActionPrecondition;
import arc.gui.object.action.precondition.ActionPreconditionListener;
import arc.gui.object.action.precondition.ActionPreconditionOutcome;
import arc.gui.object.action.precondition.EvaluatePrecondition;
import arc.mf.object.ObjectResolveHandler;
import daris.client.model.method.MethodRef;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.project.Project;

public class SubjectMethodPrecondition implements ActionPrecondition {

	private DObjectRef _po;

	public SubjectMethodPrecondition(DObjectRef po) {

		_po = po;
	}

	@Override
	public EvaluatePrecondition evaluate() {

		return EvaluatePrecondition.BEFORE_INTERACTION;
	}

	@Override
	public String description() {

		return "Checking if the parent project has methods.";
	}

	@Override
	public void execute(final ActionPreconditionListener l) {

		_po.reset();
		_po.resolve(new ObjectResolveHandler<DObject>() {

			@Override
			public void resolved(DObject o) {

				if (o == null) {
					l.executed(ActionPreconditionOutcome.FAIL, "Failed to resolve the parent project " + _po.id() + ".");
					return;
				}
				List<MethodRef> methods = ((Project) o).methods();
				if (methods != null) {
					if (!methods.isEmpty()) {
						l.executed(ActionPreconditionOutcome.PASS,
								"The parent project has methods available for the new subject.");
						return;
					}
				}
				l.executed(ActionPreconditionOutcome.FAIL, "The parent project " + _po.id()
						+ " has no methods set. Set the method(s) for the project first.");
			}
		});
	}

}
