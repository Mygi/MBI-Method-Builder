package daris.client.ui.object.action;

import java.util.List;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.FormEditMode;
import arc.gui.gwt.message.StatusArea;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.object.action.precondition.ActionPrecondition;
import arc.gui.object.action.precondition.ActionPreconditionInterface;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.StateChangeListener;
import arc.mf.client.util.Validity;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.Model;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.ui.object.DObjectDetails;

public class DObjectCreateActionGUI extends ValidatedInterfaceComponent implements AsynchronousAction {

	private DObjectRef _po;
	private DObject _o;
	private SimplePanel _gui;
	private boolean _checkedPreconditions;

	public DObjectCreateActionGUI(DObjectRef po, DObject o, List<ActionPrecondition> preconditions) {

		_po = po;
		_o = o;
		_gui = new SimplePanel();
		_gui.fitToParent();

		StatusArea sa = new StatusArea();
		_gui.setContent(sa);
		_checkedPreconditions = false;
		ActionPreconditionInterface pci = new ActionPreconditionInterface(sa, preconditions);
		pci.addChangeListener(new StateChangeListener() {

			@Override
			public void notifyOfChangeInState() {

				_checkedPreconditions = true;
				DObjectDetails details = DObjectDetails.detailsFor(_po, _o, FormEditMode.CREATE);
				DObjectCreateActionGUI.this.addMustBeValid(details);
				_gui.setContent(details.gui());
			}
		});
		pci.execute();

	}

	@Override
	public Validity valid() {

		if (_checkedPreconditions) {
			return super.valid();
		} else {
			return IsNotValid.INSTANCE;
		}
	}

	@Override
	public Widget gui() {

		return _gui;
	}

	@Override
	public void execute(final ActionListener l) {

		_o.create(_po, new ObjectMessageResponse<DObjectRef>() {

			@Override
			public void responded(DObjectRef r) {

				l.executed(r != null);
				if (r != null) {
					Model.objectCreated(r);
				}
			}
		});
	}

}
