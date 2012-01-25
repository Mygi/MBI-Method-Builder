package daris.client.ui.object.action;

import java.util.Vector;

import arc.gui.InterfaceCreateHandler;
import arc.gui.object.action.CreateActionInterface;
import arc.gui.object.action.precondition.ActionPrecondition;
import arc.gui.window.Window;
import daris.client.model.IDUtil;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.subject.Subject;
import daris.client.ui.object.DObjectGUI;
import daris.client.util.StringUtil;

public class DObjectCreateAction extends CreateActionInterface<DObject> {

	private DObjectRef _po;
	private DObject _o;

	public DObjectCreateAction(DObjectRef po, Window owner) {

		this(po, owner, ActionIntefaceUtil.windowWidth(owner), ActionIntefaceUtil.windowHeight(owner));
	}

	public DObjectCreateAction(DObjectRef po, Window owner, int width, int height) {

		super(IDUtil.childTypeNameFromId(po.id()), new Vector<ActionPrecondition>(), owner, width, height);
		_po = po;
		_o = po.createEmptyChildObject();
		preconditions().add(new CanCreatePrecondition(_po));
		preconditions().add(new MetaForCreatePrecondition(_o));
		if(_o instanceof Subject){
			preconditions().add(new SubjectMethodPrecondition(_po));
		}
	}

	@Override
	public void createInterface(InterfaceCreateHandler ch) {

		ch.created(new DObjectCreateActionGUI(_po, _o, duringInteractionPreconditions()));
	}
	
	@Override
	public String actionName() {
		return ACTION_NAME + " " + IDUtil.childTypeNameFromId(_po.id());
	}
	
	@Override
	public String actionButtonName() {
		return "Create";
	}

	@Override
	public String title() {
		return actionName();
	}

}
