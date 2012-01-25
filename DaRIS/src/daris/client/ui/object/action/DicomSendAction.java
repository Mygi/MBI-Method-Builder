package daris.client.ui.object.action;

import java.util.Vector;

import arc.gui.InterfaceCreateHandler;
import arc.gui.object.action.ActionInterface;
import arc.gui.object.action.precondition.ActionPrecondition;
import arc.gui.window.Window;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.ui.dicom.DicomSendForm;

public class DicomSendAction extends ActionInterface<DObject> {

	private DObjectRef _root;

	public DicomSendAction(DObjectRef root, Window owner) {

		this(root, owner, ActionIntefaceUtil.windowWidth(owner), ActionIntefaceUtil.windowHeight(owner));
	}

	public DicomSendAction(DObjectRef root, Window owner, int width, int height) {

		super(root.referentTypeName(), root, new Vector<ActionPrecondition>(), owner, width, height);
		_root = root;
		preconditions().add(new CanSendDicomPrecondition(_root));
		preconditions().add(new HasDicomDataPrecondition(_root));
	}

	@Override
	public void createInterface(InterfaceCreateHandler ch) {

		ch.created(new DicomSendForm(_root));
	}

	@Override
	public String actionName() {

		return "DICOM Send";
	}

}
