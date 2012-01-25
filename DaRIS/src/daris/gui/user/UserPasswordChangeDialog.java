package daris.gui.user;

import java.util.List;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.gwt.style.FontWeight;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.label.Label;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.window.Window;
import arc.gui.window.WindowProperties;
import arc.mf.client.Output;
import arc.mf.client.RemoteServer;
import arc.mf.client.ResponseHandler;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.StateChangeListener;
import arc.mf.client.xml.XmlElement;
import arc.mf.dtype.PasswordType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class UserPasswordChangeDialog {
	private Field<String> _oldPwdField;

	private Field<String> _newPwdField;

	private Field<String> _cfmPwdField;

	private Label _infoLabel;

	private Button _okButton;

	private Window _win;

	private UserPasswordChangeDialog() {

		WindowProperties wp = new WindowProperties();
		wp.setModal(true);
		wp.setCanBeResized(false);
		wp.setCanBeClosed(true);
		wp.setCanBeMoved(false);
		wp.setCentered(true);
		wp.setTitle("Change Password...");
		wp.setSize(330, 200);
		_win = Window.create(wp);
		VerticalPanel vp = new VerticalPanel();
		vp.fitToParent();
		final Form form = new Form();
		form.setShowHelp(false);
		form.setSubmitOnEnter(true);
		form.setWidth100();
		form.setMarginLeft(15);
		form.setMarginRight(15);
		form.setHeight(80);
		form.setPadding(5);
		_oldPwdField = new Field<String>(new FieldDefinition("Old Password",
				PasswordType.DEFAULT, "The old password", null, 1, 1));
		form.add(_oldPwdField);
		_newPwdField = new Field<String>(new FieldDefinition("New Password",
				PasswordType.DEFAULT, "The new password", null, 1, 1));
		form.add(_newPwdField);
		_cfmPwdField = new Field<String>(new FieldDefinition(
				"Confirm Password", PasswordType.DEFAULT,
				"Retype the new password", null, 1, 1));
		form.add(_cfmPwdField);
		form.addChangeListener(new StateChangeListener() {
			@Override
			public void notifyOfChangeInState() {

				if (_oldPwdField.value() != null
						&& _newPwdField.value() != null
						&& _cfmPwdField.value() != null) {
					_okButton.enable();
				} else {
					_okButton.disable();
				}
			}
		});
		vp.add(form.gui());
		_infoLabel = new Label();
		_infoLabel.setWidth100();
		_infoLabel.setHeight100();
		_infoLabel.setPaddingLeft(15);
		_infoLabel.setMarginBottom(15);
		_infoLabel.setFontSize(11);
		_infoLabel.setFontWeight(FontWeight.BOLD);
		_infoLabel.setWordWrap(true);
		_infoLabel.setForegroundColour("#ff8080");
		vp.add(_infoLabel);
		ButtonBar bb = new ButtonBar(ButtonBar.Alignment.RIGHT);
		bb.setWidth100();
		bb.setHeight(28);
		bb.setMargin(15);
		bb.setColourEnabled(false);
		_okButton = new Button("Change");
		_okButton.disable();
		_okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				String newPwd = (String) _newPwdField.value();
				String cfmPwd = (String) _cfmPwdField.value();
				if (!cfmPwd.equals(newPwd)) {
					_newPwdField.markInvalid("Does not match!");
					_cfmPwdField.markInvalid("Does not match!");
					_infoLabel.setText("The new passwords do not match.");
					return;
				}
				doChangePassword();
			}
		});
		bb.add(_okButton);
		vp.add(bb);
		_win.setContent(vp);
	}

	private void doChangePassword() {

		RemoteServer.execute("user.self.password.set", "<old-password>"
				+ _oldPwdField.valueAsString() + "</old-password><password>"
				+ _newPwdField.valueAsString() + "</password>",
				new ResponseHandler() {
					@Override
					public void processResponse(XmlElement xe,
							List<Output> outputs) {

						Dialog.inform("Note",
								"Your password has been changed successfully.",
								new ActionListener() {
									@Override
									public void executed(boolean succeeded) {

										if (succeeded) {
											_win.close();
										}
									}
								});
					}

					@Override
					public void processError(Throwable se) {

						_infoLabel.setText("Authentication failure.");
					}
				});
	}

	private void showModal() {

		_win.show();
	}

	public static void show() {

		new UserPasswordChangeDialog().showModal();
	}
}
