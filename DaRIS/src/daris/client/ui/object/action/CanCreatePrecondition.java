package daris.client.ui.object.action;

import arc.gui.object.action.precondition.ActionPrecondition;
import arc.gui.object.action.precondition.ActionPreconditionListener;
import arc.gui.object.action.precondition.ActionPreconditionOutcome;
import arc.gui.object.action.precondition.EvaluatePrecondition;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.IDUtil;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.CanCreate;
import daris.client.model.project.Project;
import daris.client.model.study.Study;
import daris.client.model.subject.Subject;

public class CanCreatePrecondition implements ActionPrecondition {

	private DObjectRef _parent;

	public CanCreatePrecondition(DObjectRef parent) {

		_parent = parent;
	}

	@Override
	public EvaluatePrecondition evaluate() {

		return EvaluatePrecondition.BEFORE_INTERACTION;
	}

	@Override
	public String description() {

		return "Check if the user can create a child object for the parent object"
				+ (_parent.id() == null ? "." : (" " + _parent.id() + "."));
	}

	@Override
	public void execute(final ActionPreconditionListener l) {

		String parentId = _parent.id();
		String projectId = null;
		String type = null;
		if (parentId != null) {
			if (IDUtil.isProjectId(parentId)) {
				projectId = parentId;
				type = Subject.TYPE_NAME;
			} else if (IDUtil.isExMethodId(parentId)) {
				projectId = IDUtil.getParentId(parentId, 2);
				type = Study.TYPE_NAME;
			} else {
				l.executed(ActionPreconditionOutcome.FAIL, "Currently, only project, subject and study can be created.");
				return;
			}
		} else {
			projectId = null;
			type = Project.TYPE_NAME;
		}
		new CanCreate(type, projectId).send(new ObjectMessageResponse<Boolean>() {

			@Override
			public void responded(Boolean canCreate) {

				if (canCreate) {
					l.executed(ActionPreconditionOutcome.PASS, "You have privilege to create child object for "
							+ _parent);
				} else {
					l.executed(ActionPreconditionOutcome.FAIL, "You do not have privilege to create child object for "
							+ _parent);
				}
			}
		});
	}

}
