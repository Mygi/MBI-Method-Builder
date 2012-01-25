package daris.client.ui.object.action;

import java.util.Vector;

import arc.gui.InterfaceCreateHandler;
import arc.gui.object.action.UpdateActionInterface;
import arc.gui.object.action.precondition.ActionPrecondition;
import arc.gui.window.Window;
import daris.client.model.IDUtil;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;

public class DObjectEditAction extends UpdateActionInterface<DObject> {

	public DObjectEditAction(DObjectRef o, Window owner) {

		this(o, owner, ActionIntefaceUtil.windowWidth(owner),
				ActionIntefaceUtil.windowHeight(owner));
	}

	public DObjectEditAction(DObjectRef o, Window owner, int width, int height) {

		super(o, new Vector<ActionPrecondition>(), owner, width, height);
		preconditions().add(new CanEditPrecondition(o));
		preconditions().add(new MetaForEditPrecondition(o));
	}

	@Override
	public void createInterface(InterfaceCreateHandler ch) {

		ch.created(new DObjectEditActionGUI((DObjectRef) object(),
				duringInteractionPreconditions()));
	}

	@Override
	public String actionName() {
		DObjectRef o = (DObjectRef)object();
		return ACTION_NAME + " " + (o.id() == null ? "repository" : (IDUtil
				.typeNameFromId(o.id()) + " " + o.id()));
	}
	
	@Override
	public String actionButtonName() {
		return "Update";
	}

	@Override
	public String title() {
		return actionName();
	}

}
