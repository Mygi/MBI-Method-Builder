package daris.client.ui.object.action;

import arc.gui.object.action.precondition.ActionPrecondition;
import arc.gui.object.action.precondition.ActionPreconditionListener;
import arc.gui.object.action.precondition.ActionPreconditionOutcome;
import arc.gui.object.action.precondition.EvaluatePrecondition;
import arc.mf.client.util.ActionListener;
import daris.client.model.object.DObject;
import daris.client.model.project.Project;

public class MetaForCreatePrecondition implements ActionPrecondition {

	private DObject _o;

	public MetaForCreatePrecondition(DObject o) {

		_o = o;
	}

	@Override
	public EvaluatePrecondition evaluate() {

		return EvaluatePrecondition.DURING_INTERACTION;
	}

	@Override
	public String description() {

		return "Retrieving the metadata with defintions required for creating the " + _o.typeName() + ".";
	}

	@Override
	public void execute(final ActionPreconditionListener l) {

		if (_o instanceof Project) {
			Project.setMetaForEdit((Project) _o, new ActionListener() {

				@Override
				public void executed(boolean succeeded) {

					if (succeeded) {
						l.executed(ActionPreconditionOutcome.PASS,
								"metadata with definitions for creating the project is set.");
					} else {
						l.executed(ActionPreconditionOutcome.FAIL,
								"Failed to retrieve metadata with definitions for creating project.");
					}
				}
			});
		} else {
			l.executed(ActionPreconditionOutcome.PASS,
					"Do not need to retrieve metadata with definitions before interaction.");
		}

	}
}
