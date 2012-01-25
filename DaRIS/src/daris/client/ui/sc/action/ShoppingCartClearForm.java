package daris.client.ui.sc.action;

import java.util.List;
import java.util.Vector;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.Form.BooleanAs;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.style.FontWeight;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.mf.client.util.Action;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.dtype.BooleanType;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.sc.ShoppingCartManager;
import daris.client.model.sc.Status;

public class ShoppingCartClearForm extends ValidatedInterfaceComponent
		implements Action {

	private VerticalPanel _vp;
	private Form _form;
	private List<Status> _states;
	private HTML _issues;

	public ShoppingCartClearForm() {
		_states = new Vector<Status>();

		_vp = new VerticalPanel();
		_vp.fitToParent();

		_form = new Form();
		_form.setBooleanAs(BooleanAs.CHECKBOX);
		_form.fitToParent();
		_form.setPaddingLeft(30);
		_form.setPaddingTop(15);
		Field<Boolean> dataReadyField = new Field<Boolean>(new FieldDefinition(
				Status.data_ready.toString(), BooleanType.DEFAULT_TRUE_FALSE,
				null, "Tick to remove the 'data ready' shopping carts", 0, 1));
		dataReadyField.addListener(new FormItemListener<Boolean>() {

			@Override
			public void itemValueChanged(FormItem<Boolean> f) {
				if (f.value()) {
					addStatus(Status.data_ready);
				} else {
					removeStatus(Status.data_ready);
				}
			}

			@Override
			public void itemPropertyChanged(FormItem<Boolean> f,
					Property property) {

			}
		});
		_form.add(dataReadyField);
		Field<Boolean> errorField = new Field<Boolean>(new FieldDefinition(
				Status.error.toString(), BooleanType.DEFAULT_TRUE_FALSE, null,
				"Tick to remove the 'error' shopping carts", 0, 1));

		errorField.addListener(new FormItemListener<Boolean>() {

			@Override
			public void itemValueChanged(FormItem<Boolean> f) {
				if (f.value()) {
					addStatus(Status.error);
				} else {
					removeStatus(Status.error);
				}
			}

			@Override
			public void itemPropertyChanged(FormItem<Boolean> f,
					Property property) {

			}
		});
		_form.add(errorField);
		Field<Boolean> rejectedField = new Field<Boolean>(new FieldDefinition(
				Status.rejected.toString(), BooleanType.DEFAULT_TRUE_FALSE,
				null, "Tick to remove the 'rejected' shopping carts", 0, 1));

		rejectedField.addListener(new FormItemListener<Boolean>() {

			@Override
			public void itemValueChanged(FormItem<Boolean> f) {
				if (f.value()) {
					addStatus(Status.rejected);
				} else {
					removeStatus(Status.rejected);
				}
			}

			@Override
			public void itemPropertyChanged(FormItem<Boolean> f,
					Property property) {

			}
		});
		_form.add(rejectedField);
		Field<Boolean> abortedField = new Field<Boolean>(new FieldDefinition(
				Status.aborted.toString(), BooleanType.DEFAULT_TRUE_FALSE,
				null, "Tick to remove the 'aborted' shopping carts", 0, 1));

		abortedField.addListener(new FormItemListener<Boolean>() {

			@Override
			public void itemValueChanged(FormItem<Boolean> f) {
				if (f.value()) {
					addStatus(Status.aborted);
				} else {
					removeStatus(Status.aborted);
				}
			}

			@Override
			public void itemPropertyChanged(FormItem<Boolean> f,
					Property property) {

			}
		});
		_form.add(abortedField);
		Field<Boolean> withdrawnField = new Field<Boolean>(new FieldDefinition(
				Status.withdrawn.toString(), BooleanType.DEFAULT_TRUE_FALSE,
				null, "Tick to remove the 'withdrawn' shopping carts", 0, 1));

		withdrawnField.addListener(new FormItemListener<Boolean>() {

			@Override
			public void itemValueChanged(FormItem<Boolean> f) {
				if (f.value()) {
					addStatus(Status.withdrawn);
				} else {
					removeStatus(Status.withdrawn);
				}
			}

			@Override
			public void itemPropertyChanged(FormItem<Boolean> f,
					Property property) {

			}
		});
		_form.add(withdrawnField);
		addMustBeValid(_form);
		_form.render();
		_vp.add(_form);

		_issues = new HTML();
		_issues.setHeight(20);
		_issues.setWidth100();
		_issues.setFontSize(12);
		_issues.setFontWeight(FontWeight.BOLD);
		_issues.setColour("red");
		_issues.style().setProperty("textAlign", "center");
		_vp.add(_issues);

		dataReadyField.setValue(true);
		errorField.setValue(true);
		abortedField.setValue(true);
		rejectedField.setValue(true);
		withdrawnField.setValue(true);
	}

	private void addStatus(Status status) {
		if (!_states.contains(status)) {
			_states.add(status);
		}
	}

	private void removeStatus(Status status) {
		if (_states.contains(status)) {
			_states.remove(status);
		}
	}

	@Override
	public Widget gui() {
		return _vp;
	}

	@Override
	public Validity valid() {
		Validity v = super.valid();
		if (!v.valid()) {
			_issues.setHTML(v.reasonForIssue());
			return v;
		}
		if (_states.isEmpty()) {
			final String reasonForIssue = "At least one of the states need to be selected.";
			_issues.setHTML(reasonForIssue);
			return new Validity() {

				@Override
				public boolean valid() {
					return false;
				}

				@Override
				public String reasonForIssue() {
					return reasonForIssue;
				}
			};
		}
		_issues.setHTML("");
		return new IsValid();
	}

	@Override
	public void execute() {
		ShoppingCartManager.destroy(_states);
	}

}
