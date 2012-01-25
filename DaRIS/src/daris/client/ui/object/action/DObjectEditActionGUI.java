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

import daris.client.model.object.DObjectRef;
import daris.client.ui.object.DObjectDetails;

public class DObjectEditActionGUI extends ValidatedInterfaceComponent implements
		AsynchronousAction {

	private DObjectRef _o;
	private SimplePanel _gui;
	private boolean _checkedDuringPreconditions;

	public DObjectEditActionGUI(DObjectRef o,
			List<ActionPrecondition> duringPreconditions) {

		_o = o;
		_gui = new SimplePanel();
		_gui.fitToParent();

		StatusArea sa = new StatusArea();
		_gui.setContent(sa);
		_checkedDuringPreconditions = false;
		if (duringPreconditions != null) {
			if (!duringPreconditions.isEmpty()) {
				ActionPreconditionInterface pci = new ActionPreconditionInterface(
						sa, duringPreconditions);
				pci.addChangeListener(new StateChangeListener() {

					@Override
					public void notifyOfChangeInState() {

						_checkedDuringPreconditions = true;
						updateObjectDetails();
					}
				});
				pci.execute();
				return;
			}
		}
		_checkedDuringPreconditions = true;
		updateObjectDetails();

	}

	private void updateObjectDetails() {
		DObjectDetails details = DObjectDetails.detailsFor(_o.referent(),
				FormEditMode.UPDATE);
		DObjectEditActionGUI.this.addMustBeValid(details);
		_gui.setContent(details.gui());
	}

	@Override
	public Validity valid() {

		if (_checkedDuringPreconditions) {
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

		_o.referent().update(new ObjectMessageResponse<Boolean>() {

			@Override
			public void responded(Boolean r) {

				if (r != null) {
					l.executed(r);
					if (r) {
						_o.refresh(false);
					}
				} else {
					l.executed(false);
				}

			}
		});

	}

}
