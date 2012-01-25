package daris.client.ui.object.action;

import arc.gui.object.action.precondition.ActionPrecondition;
import arc.gui.object.action.precondition.ActionPreconditionListener;
import arc.gui.object.action.precondition.ActionPreconditionOutcome;
import arc.gui.object.action.precondition.EvaluatePrecondition;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.IDUtil;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.CanEdit;
import daris.client.model.repository.RepositoryRef;

public class CanEditPrecondition implements ActionPrecondition {

	private DObjectRef _o;

	public CanEditPrecondition(DObjectRef o) {

		_o = o;
	}

	@Override
	public EvaluatePrecondition evaluate() {

		return EvaluatePrecondition.BEFORE_INTERACTION;
	}

	@Override
	public String description() {

		return "Check if the object can be editted by the user.";
	}

	@Override
	public void execute(final ActionPreconditionListener l) {

		if (_o instanceof RepositoryRef) {
			l.executed(ActionPreconditionOutcome.FAIL, "Repository object is not editable.");
			return;
		}
		String id = _o.id();
		if (!(IDUtil.isProjectId(id) || IDUtil.isSubjectId(id) || IDUtil.isStudyId(id))) {
			l.executed(ActionPreconditionOutcome.FAIL, "Only project, subject and study can be editted.");
			return;
		}
		new CanEdit(_o).send(new ObjectMessageResponse<Boolean>() {

			@Override
			public void responded(Boolean canEdit) {

				if (canEdit) {
					l.executed(ActionPreconditionOutcome.PASS, "You have privilege to edit object " + _o.id() + ".");
				} else {
					l.executed(ActionPreconditionOutcome.FAIL, "You do not have privilege to edit object " + _o.id()
							+ ".");
				}
			}
		});

	}

}
