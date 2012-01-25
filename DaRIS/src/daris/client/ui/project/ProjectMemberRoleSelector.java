package daris.client.ui.project;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.style.FontWeight;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.image.LinearGradient;
import arc.gui.gwt.widget.label.Label;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.popup.PopupCloseHandler;
import arc.gui.gwt.widget.popup.PopupPanel;
import arc.mf.client.util.Validity;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;

import daris.client.model.project.DataUse;
import daris.client.model.project.ProjectRole;

public class ProjectMemberRoleSelector {

	public static final int MIN_WIDTH = 320;
	public static final int MIN_HEIGHT = 150;

	public static interface RoleSelectionListener {

		void roleSelected(ProjectRole role, DataUse dataUse);
	}

	private PopupPanel _pp;
	private Button _okButton;

	private ProjectRole _role;
	private DataUse _dataUse;

	private boolean _succeeded = false;

	private ProjectMemberRoleSelector(final int x, final int y, int width,
			int height, final RoleSelectionListener rsl) {

		VerticalPanel vp = new VerticalPanel();
		vp.setBorderRadius(3);
		vp.setBoxShadow(1, 1, 5, 5, "rgba(0,0,0,0.2)");
		vp.fitToParent();

		HorizontalPanel header = new HorizontalPanel();
		header.setWidth100();
		header.setHeight(20);
		header.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		header.setBackgroundImage(new LinearGradient(
				LinearGradient.Orientation.TOP_TO_BOTTOM, "#cdcdcd", "#979797"));

		Label headerLabel = new Label("Select project role and data-use ...");
		headerLabel.setFontSize(10);
		headerLabel.setFontWeight(FontWeight.BOLD);
		headerLabel.setHeight100();
		header.add(headerLabel);

		vp.add(header);

		final Form form = new Form() {

			@Override
			public Validity valid() {

				Validity v = super.valid();
				if (!v.valid()) {
					return v;
				}
				if ((_role.equals(ProjectRole.MEMBER) || _role
						.equals(ProjectRole.GUEST)) && _dataUse == null) {
					return new Validity() {
						@Override
						public boolean valid() {

							return false;
						}

						@Override
						public String reasonForIssue() {

							return "data-use is not set for " + _role;
						}
					};
				}
				return v;
			}
		};
		form.fitToParent();
		Field<ProjectRole> roleField = new Field<ProjectRole>(
				new FieldDefinition("Role", ProjectRole.asEnumerationType(),
						"Project role", null, 1, 1));
		roleField.addListener(new FormItemListener<ProjectRole>() {

			@Override
			public void itemValueChanged(FormItem<ProjectRole> f) {

				_role = f.value();
				if (form.valid().valid()) {
					_okButton.enable();
				} else {
					_okButton.disable();
				}
			}

			@Override
			public void itemPropertyChanged(FormItem<ProjectRole> f,
					Property property) {

			}
		});
		form.add(roleField);
		Field<DataUse> dataUseField = new Field<DataUse>(new FieldDefinition(
				"Data Use", DataUse.asEnumerationType(), "Project data use",
				null, 0, 1));
		dataUseField.addListener(new FormItemListener<DataUse>() {

			@Override
			public void itemValueChanged(FormItem<DataUse> f) {

				_dataUse = f.value();
				if (form.valid().valid()) {
					_okButton.enable();
				} else {
					_okButton.disable();
				}
			}

			@Override
			public void itemPropertyChanged(FormItem<DataUse> f,
					Property property) {

			}
		});
		form.add(dataUseField);
		form.render();
		vp.add(form);

		ButtonBar bb = new ButtonBar(ButtonBar.Position.BOTTOM,
				ButtonBar.Alignment.RIGHT);
		bb.setHeight(26);
		_okButton = new Button("OK");
		_okButton.disable();
		_okButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				_succeeded = true;
				_pp.hide();
			}
		});
		bb.add(_okButton);
		Button cancelButton = new Button("Cancel");
		cancelButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				_succeeded = false;
				_pp.hide();
			}
		});
		bb.add(cancelButton);
		bb.setMarginRight(5);
		vp.add(bb);

		_pp = new PopupPanel();
		_pp.setAutoHideEnabled(true);
		_pp.setWidth(width > MIN_WIDTH ? width : MIN_WIDTH);
		_pp.setHeight(height > MIN_HEIGHT ? height : MIN_HEIGHT);
		_pp.setBackgroundColour("#f0f0f0");
		_pp.setBorder(1, "#a0a080");
		_pp.setContent(vp);
		_pp.addCloseHander(new PopupCloseHandler() {

			@Override
			public void closed(PopupPanel p) {

				if (_succeeded) {
					rsl.roleSelected(_role, _dataUse);
				} else {
					rsl.roleSelected(null, null);
				}
			}
		});
		_pp.setPopupPositionAndShow(new PositionCallback() {

			@Override
			public void setPosition(int offsetWidth, int offsetHeight) {

				_pp.setPopupPosition(x - offsetWidth / 2, y - offsetHeight / 2);
			}
		});
	}

	public static void showAt(int x, int y, RoleSelectionListener rsl) {

		showAt(x, y, MIN_WIDTH, MIN_HEIGHT, rsl);
	}

	public static void showAt(int x, int y, int width, int height,
			RoleSelectionListener rsl) {

		new ProjectMemberRoleSelector(x, y, width, height, rsl);
	}
}
