package daris.client.ui.user;

import java.util.List;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.dialog.DialogProperties;
import arc.gui.dialog.DialogProperties.Type;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.label.Label;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.window.Window;
import arc.mf.client.Output;
import arc.mf.client.RemoteServer;
import arc.mf.client.ResponseHandler;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.client.util.ObjectUtil;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.dtype.PasswordType;

import com.google.gwt.user.client.ui.Widget;

public class SelfPasswordSetDialog {

	static class SelfPasswordSetForm extends ValidatedInterfaceComponent {

		private VerticalPanel _vp;
		private Form _form;
		private Label _label;
		private String _oldPassword;
		private String _newPassword;
		private String _cfmPassword;

		SelfPasswordSetForm() {

			Field<String> oldPwdField = new Field<String>(new FieldDefinition(
					"Old Password", PasswordType.DEFAULT, "The old password",
					null, 1, 1));
			oldPwdField.addListener(new FormItemListener<String>() {

				@Override
				public void itemValueChanged(FormItem<String> f) {
					_oldPassword = f.value();
				}

				@Override
				public void itemPropertyChanged(FormItem<String> f,
						Property property) {

				}
			});
			Field<String> newPwdField = new Field<String>(new FieldDefinition(
					"New Password", PasswordType.DEFAULT, "The new password",
					null, 1, 1));
			newPwdField.addListener(new FormItemListener<String>() {

				@Override
				public void itemValueChanged(FormItem<String> f) {
					_newPassword = f.value();
				}

				@Override
				public void itemPropertyChanged(FormItem<String> f,
						Property property) {

				}
			});
			Field<String> cfmPwdField = new Field<String>(new FieldDefinition(
					"Confirm Password", PasswordType.DEFAULT,
					"Retype the new password", null, 1, 1));
			cfmPwdField.addListener(new FormItemListener<String>() {

				@Override
				public void itemValueChanged(FormItem<String> f) {
					_cfmPassword = f.value();
				}

				@Override
				public void itemPropertyChanged(FormItem<String> f,
						Property property) {

				}
			});
			_form = new Form();
			_form.fitToParent();
			_form.setShowHelp(true);
			_form.setSubmitOnEnter(true);
			_form.fitToParent();
			_form.setMarginLeft(15);
			_form.setMarginRight(15);
			_form.setHeight(80);
			_form.setPadding(5);
			_form.add(oldPwdField);
			_form.add(newPwdField);
			_form.add(cfmPwdField);
			_form.render();
			addMustBeValid(_form);

			_vp = new VerticalPanel();
			_vp.fitToParent();
			_vp.add(new ScrollPanel(_form, ScrollPolicy.AUTO));

			_label = new Label();
			_label.setWidth100();
			_label.setHeight(20);
			_label.setMarginLeft(20);
			_label.setColour("red");
			_label.setFontSize(12);
			_vp.add(_label);

		}

		@Override
		public Widget gui() {
			return _vp;
		}

		String password() {
			return _newPassword;
		}

		String oldPassword() {
			return _oldPassword;
		}
		
		void setErrorText(String text){
			_label.setText(text);
		}

		@Override
		public Validity valid() {
			Validity valid = super.valid();
			if (valid.valid() && !ObjectUtil.equals(_newPassword, _cfmPassword)) {
				valid = new Validity() {

					@Override
					public boolean valid() {
						return false;
					}

					@Override
					public String reasonForIssue() {
						return "The new passwords inputed do not match.";
					}
				};
			}
			if (valid.valid() && ObjectUtil.equals(_newPassword, _oldPassword)) {
				valid = new Validity() {

					@Override
					public boolean valid() {
						return false;
					}

					@Override
					public String reasonForIssue() {
						return "The new password is the same as the old one.";
					}
				};
			}			
			if (valid.valid()) {
				_label.setText(null);
			} else {
				_label.setText(valid.reasonForIssue());
			}
			return valid;
		}
	}

	public static void show(Window owner) {

		final SelfPasswordSetForm spsf = new SelfPasswordSetForm();
		DialogProperties dp = new DialogProperties(Type.ACTION,
				"Self password set", spsf);
		dp.setActionEnabled(false);
		dp.setButtonLabel("Set");
		dp.setModal(true);

		dp.setScrollPolicy(ScrollPolicy.AUTO);
		dp.setOwner(owner);
		dp.setSize(320, 200);
		dp.setButtonAction(new AsynchronousAction() {

			@Override
			public void execute(final ActionListener l) {
//				new UserSelfPasswordSet(spsf.oldPassword(), spsf.password())
//						.send(new ObjectMessageResponse<Boolean>() {
//
//							@Override
//							public void responded(Boolean r) {
//								if (r != null) {
//									if (r) {
//										l.executed(true);
//									}
//								}
//								l.executed(false);
//							}
//						});
				RemoteServer.execute("user.self.password.set", "<old-password>"
						+ spsf.oldPassword() + "</old-password><password>"
						+ spsf.password() + "</password>",
						new ResponseHandler() {
							@Override
							public void processResponse(XmlElement xe,
									List<Output> outputs) {

								l.executed(true);
							}

							@Override
							public void processError(Throwable se) {

								spsf.setErrorText("Authenticate failure.");
								l.executed(false);
							}
						});
			}
		});
		Dialog.postDialog(dp, new ActionListener() {

			@Override
			public void executed(boolean succeeded) {
				if (succeeded) {
					Dialog.inform("Your password has been successfully set.");
				}
			}
		});
	}
}