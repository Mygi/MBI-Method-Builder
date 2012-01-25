package daris.gui.subject;

import java.util.List;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItemListener;
import arc.gui.form.FormListener;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.EnumerationType;
import arc.mf.object.ObjectResolveHandler;
import daris.client.model.IDUtil;
import daris.client.ui.form.XmlMetaForm;
import daris.gui.object.ObjectDetail;
import daris.model.datause.DataUseEnum;
import daris.model.method.MethodEnum;
import daris.model.method.MethodRef;
import daris.model.subject.SubjectRef;

public class SubjectDetail extends ObjectDetail {

	public static final String PRIVATE_META_TAB_NAME = "private meta";
	public static final String PUBLIC_META_TAB_NAME = "public meta";

	private Form _privateMetaForm;

	private Form _publicMetaForm;

	public SubjectDetail(SubjectRef subject, FormEditMode mode) {

		super(subject, mode);

		//
		// Private Meta tab
		//
		updatePrivateMetaTab();

		//
		// Public Meta tab
		//
		updatePublicMetaTab();

	}

	public SubjectRef subject() {

		return (SubjectRef) object();
	}

	private void updatePrivateMetaTab(XmlElement me, FormListener formListener) {

		if (me != null) {
			_privateMetaForm = XmlMetaForm.formFor(me, mode());
			if (formListener != null) {
				_privateMetaForm.addListener(formListener);
			}
			putTab(PRIVATE_META_TAB_NAME, new ScrollPanel(_privateMetaForm,
					ScrollPolicy.AUTO), false);
		} else {
			removeTab(PRIVATE_META_TAB_NAME);
		}
	}

	private void updatePrivateMetaTab() {

		/*
		 * view
		 */
		if (mode() == FormEditMode.READ_ONLY) {
			subject().privateMetaForView(
					new ObjectResolveHandler<XmlElement>() {

						@Override
						public void resolved(XmlElement me) {

							updatePrivateMetaTab(me, null);
						}
					}, false);
			return;
		}
		/*
		 * create or update
		 */
		final FormListener fl = new FormListener() {

			@Override
			public void rendering(Form f) {

			}

			@Override
			public void rendered(Form f) {

			}

			@Override
			public void formValuesUpdated(Form f) {

				XmlStringWriter w = new XmlStringWriter();
				w.push("private");
				f.save(w);
				w.pop();
				subject().setPrivateMetaToSave(w.document());
			}

			@Override
			public void formStateUpdated(Form f, FormItem.Property p) {

			}
		};
		if (mode() == FormEditMode.CREATE) {
			// create
			if (subject().method() != null) {
				subject().privateMetaForCreate(
						new ObjectResolveHandler<XmlElement>() {

							@Override
							public void resolved(XmlElement me) {

								updatePrivateMetaTab(me, fl);
							}
						});
			} else {
				updatePrivateMetaTab(null, null);
			}
		} else {
			// update
			subject().privateMetaForEdit(
					new ObjectResolveHandler<XmlElement>() {

						@Override
						public void resolved(XmlElement me) {

							updatePrivateMetaTab(me, fl);
						}
					}, false);
		}

	}

	private void updatePublicMetaTab(XmlElement me, FormListener formListener) {

		if (me != null) {
			_publicMetaForm = XmlMetaForm.formFor(me, mode());
			if (formListener != null) {
				_publicMetaForm.addListener(formListener);
			}
			putTab(PUBLIC_META_TAB_NAME, new ScrollPanel(_publicMetaForm,
					ScrollPolicy.AUTO), false);
		} else {
			removeTab(PUBLIC_META_TAB_NAME);
		}
	}

	private void updatePublicMetaTab() {

		/*
		 * view
		 */
		if (mode() == FormEditMode.READ_ONLY) {
			subject().publicMetaForView(new ObjectResolveHandler<XmlElement>() {

				@Override
				public void resolved(XmlElement me) {

					updatePublicMetaTab(me, null);
				}
			}, false);
			return;
		}
		/*
		 * create or update
		 */
		final FormListener fl = new FormListener() {

			@Override
			public void rendering(Form f) {

			}

			@Override
			public void rendered(Form f) {

			}

			@Override
			public void formValuesUpdated(Form f) {

				XmlStringWriter w = new XmlStringWriter();
				w.push("public");
				f.save(w);
				w.pop();
				subject().setPublicMetaToSave(w.document());
			}

			@Override
			public void formStateUpdated(Form f, FormItem.Property p) {

			}
		};
		if (mode() == FormEditMode.CREATE) {
			// create
			if (subject().method() != null) {
				subject().publicMetaForCreate(
						new ObjectResolveHandler<XmlElement>() {

							@Override
							public void resolved(XmlElement me) {

								updatePublicMetaTab(me, fl);
							}
						});
			} else {
				updatePublicMetaTab(null, null);
			}
		} else {
			// update
			subject().publicMetaForEdit(new ObjectResolveHandler<XmlElement>() {

				@Override
				public void resolved(XmlElement me) {

					updatePublicMetaTab(me, fl);
				}
			}, false);
		}
	}

	@Override
	protected void addToInterfaceForm(final Form interfaceForm) {

		//
		// data-use field
		//
		Field<String> dataUseField = new Field<String>(new FieldDefinition(
				"data-use", new EnumerationType<String>(DataUseEnum.get()),
				"Data Use", null, 0, 1));
		dataUseField.setValue(subject().dataUse());
		if (mode() != FormEditMode.READ_ONLY) {
			dataUseField.addListener(new FormItemListener<String>() {

				@Override
				public void itemValueChanged(FormItem<String> f) {

					subject().setDataUse(f.value());
				}

				@Override
				public void itemPropertyChanged(FormItem<String> f,
						FormItem.Property p) {

				}
			});
		}
		interfaceForm.add(dataUseField);

		//
		// method field
		//
		if (mode() == FormEditMode.READ_ONLY || mode() == FormEditMode.UPDATE) {
			// We do not allow to change subject method at the moment.
			Field<MethodRef> methodField = new Field<MethodRef>(
					new FieldDefinition("method", ConstantType.DEFAULT,
							"Method", null, 1, 1));
			methodField.setValue(subject().method());
			interfaceForm.add(methodField);
		} else {
			subject().availableMethods(
					new ObjectResolveHandler<List<MethodRef>>() {
						@Override
						public void resolved(List<MethodRef> ms) {

							if (ms != null) {
								if (!ms.isEmpty()) {
									Field<MethodRef> methodField;
									if (ms.size() == 1) {
										subject().setMethod(ms.get(0));
										methodField = new Field<MethodRef>(
												new FieldDefinition("method",
														ConstantType.DEFAULT,
														"Method", null, 1, 1));
									} else {
										String projectId = IDUtil
												.getParentId(subject().id());
										EnumerationType<MethodRef> methodEnumType = new EnumerationType<MethodRef>(
												new MethodEnum(subject()
														.proute(), projectId));
										methodField = new Field<MethodRef>(
												new FieldDefinition("method",
														methodEnumType,
														"Method", null, 1, 1));
										methodField
												.addListener(new FormItemListener<MethodRef>() {

													@Override
													public void itemValueChanged(
															FormItem<MethodRef> f) {

														if (f.value() != null) {
															subject()
																	.setMethod(
																			f.value());
															updatePrivateMetaTab();
															updatePublicMetaTab();
														}
													}

													@Override
													public void itemPropertyChanged(
															FormItem<MethodRef> f,
															FormItem.Property p) {

													}
												});
									}
									methodField.setValue(subject().method());
									updatePrivateMetaTab();
									updatePublicMetaTab();
									interfaceForm.add(methodField);
								}
							}

						}
					});
		}

	}

	public void validate() {

		super.validate();
		Validity v = interfaceForm().valid();
		if (!v.valid()) {
			selectTab(INTERFACE_TAB_NAME);
			Dialog.inform(
					"Error",
					"Invalid or incomplete values in the interface form: "
							+ v.reasonForIssue());
			return;
		}
		if (_privateMetaForm != null) {
			_privateMetaForm.validate();
			Validity valid = _privateMetaForm.valid();
			if (!valid.valid()) {
				selectTab(PRIVATE_META_TAB_NAME);
				Dialog.inform("Error",
						"Invalid or incomplete values in the private meta form: "
								+ valid.reasonForIssue());
				return;
			}
		}
		if (_publicMetaForm != null) {
			_publicMetaForm.validate();
			Validity valid = _publicMetaForm.valid();
			if (!valid.valid()) {
				selectTab(PUBLIC_META_TAB_NAME);
				Dialog.inform("Error",
						"Invalid or incomplete values in the public meta form: "
								+ valid.reasonForIssue());
				return;
			}
		}
	}

	public boolean valid() {

		boolean valid = super.valid();
		if (_privateMetaForm != null) {
			valid = valid && _privateMetaForm.valid().valid();
		}
		if (_publicMetaForm != null) {
			valid = valid && _publicMetaForm.valid().valid();
		}
		return valid;
	}

}
