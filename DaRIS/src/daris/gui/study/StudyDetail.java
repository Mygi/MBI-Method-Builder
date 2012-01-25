package daris.gui.study;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
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
import daris.client.ui.form.XmlMetaForm;
import daris.gui.object.ObjectDetail;
import daris.model.exmethod.StepEnum;
import daris.model.study.StudyRef;
import daris.model.study.StudyTypeEnum;

public class StudyDetail extends ObjectDetail {

	public static final String META_TAB_NAME = "meta";
	public static final String METHOD_META_TAB_NAME = "method meta";

	private Form _metaForm;
	private Form _methodMetaForm;

	public StudyDetail(StudyRef study, FormEditMode mode) {

		super(study, mode);

		//
		// meta tab
		//
		updateMetaTab();

		//
		// method meta tab
		//
		updateMethodMetaTab();

	}

	public StudyRef study() {

		return (StudyRef) object();
	}

	private void updateMethodMetaTab(XmlElement me, FormListener formListener) {

		if (me != null) {
			_methodMetaForm = XmlMetaForm.formFor(me, mode());
			if (formListener != null) {
				_methodMetaForm.addListener(formListener);
			}
			putTab(METHOD_META_TAB_NAME, new ScrollPanel(_methodMetaForm,
					ScrollPolicy.AUTO), false);
		} else {
			removeTab(METHOD_META_TAB_NAME);
		}
	}

	private void updateMethodMetaTab() {

		/*
		 * view
		 */
		if (mode() == FormEditMode.READ_ONLY) {
			study().methodMetaForView(new ObjectResolveHandler<XmlElement>() {

				@Override
				public void resolved(XmlElement me) {

					updateMethodMetaTab(me, null);
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

				String step = study().methodStep();
				if (step != null) {
					XmlStringWriter w = new XmlStringWriter();
					w.push("meta", new String[]{"action", "replace"});
					f.save(w);
					w.pop();
					study().setMethodMetaToSave(w.document());
				}
			}

			@Override
			public void formStateUpdated(Form f, FormItem.Property p) {

			}
		};
		if (mode() == FormEditMode.CREATE) {
			// create
			if (study().methodStep() != null) {
				study().methodMetaForCreate(
						new ObjectResolveHandler<XmlElement>() {

							@Override
							public void resolved(XmlElement me) {

								updateMethodMetaTab(me, fl);
							}
						});
			} else {
				// TODO: validate
				updateMethodMetaTab(null, null);
			}
		} else {
			// update
			study().methodMetaForEdit(new ObjectResolveHandler<XmlElement>() {

				@Override
				public void resolved(XmlElement me) {

					updateMethodMetaTab(me, fl);
				}
			}, false);
		}

	}

	private void updateMetaTab(XmlElement me, FormListener formListener) {

		if (me != null) {
			_metaForm = XmlMetaForm.formFor(me, mode());
			if (formListener != null) {
				_metaForm.addListener(formListener);
			}
			putTab(META_TAB_NAME,
					new ScrollPanel(_metaForm, ScrollPolicy.AUTO), false);
		} else {
			removeTab(META_TAB_NAME);
		}
	}

	private void updateMetaTab() {

		/*
		 * view
		 */
		if (mode() == FormEditMode.READ_ONLY) {
			study().metaForView(new ObjectResolveHandler<XmlElement>() {

				@Override
				public void resolved(XmlElement me) {

					updateMetaTab(me, null);
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
				w.push("meta", new String[]{"action", "replace"});
				f.save(w);
				w.pop();
				study().setMetaToSave(w.document());
			}

			@Override
			public void formStateUpdated(Form f, FormItem.Property p) {

			}
		};
		if (mode() == FormEditMode.CREATE) {
			// create
			if (study().methodStep() != null) {
				study().metaForCreate(new ObjectResolveHandler<XmlElement>() {

					@Override
					public void resolved(XmlElement me) {

						updateMetaTab(me, fl);
					}
				});
			} else {
				// TODO: validate
				updateMetaTab(null, null);
			}
		} else {
			// update
			study().metaForEdit(new ObjectResolveHandler<XmlElement>() {

				@Override
				public void resolved(XmlElement me) {

					updateMetaTab(me, fl);
				}
			}, false);
		}

	}

	@Override
	protected void addToInterfaceForm(Form interfaceForm) {

		/*
		 * study type
		 */
		Field<String> studyTypeField;
		if (study().studyType() != null || FormEditMode.READ_ONLY == mode()) {
			studyTypeField = new Field<String>(new FieldDefinition("type",
					ConstantType.DEFAULT, "Study Type", null, 1, 1));
			studyTypeField.setValue(study().studyType());
		} else {
			studyTypeField = new Field<String>(new FieldDefinition("type",
					new EnumerationType<String>(new StudyTypeEnum(study()
							.methodId())), "Study Type", null, 1, 1));
			studyTypeField.addListener(new FormItemListener<String>() {

				@Override
				public void itemValueChanged(FormItem<String> f) {

					study().setStudyType(f.value());
					study().setMethodStep(null);
					updateInterfaceTab();
					updateMethodMetaTab();
				}

				@Override
				public void itemPropertyChanged(FormItem<String> f,
						FormItem.Property p) {

				}
			});
		}
		studyTypeField.setValue(study().studyType());
		interfaceForm.add(studyTypeField);

		/*
		 * method { id, step }
		 */
		FieldGroup methodFieldGroup = new FieldGroup(new FieldDefinition(
				"method", ConstantType.DEFAULT, "method", null, 1, 1));
		Field<String> methodIdField = new Field<String>(new FieldDefinition(
				"id", ConstantType.DEFAULT, "id", null, 1, 1));
		methodIdField.setValue(study().methodId());
		methodFieldGroup.add(methodIdField);
		Field<String> methodStepField;

		if (study().methodStep() != null || FormEditMode.READ_ONLY == mode()) {

			methodStepField = new Field<String>(new FieldDefinition("step",
					ConstantType.DEFAULT, "step", null, 1, 1));
		} else {

			methodStepField = new Field<String>(new FieldDefinition("step",
					new EnumerationType<String>(new StepEnum(study())), "step",
					null, 1, 1));
			methodStepField.addListener(new FormItemListener<String>() {

				@Override
				public void itemValueChanged(FormItem<String> f) {

					study().setMethodStep(f.value());
					updateMethodMetaTab();
				}

				@Override
				public void itemPropertyChanged(FormItem<String> f,
						FormItem.Property p) {

				}
			});
		}
		methodStepField.setValue(study().methodStep());
		methodFieldGroup.add(methodStepField);
		interfaceForm.add(methodFieldGroup);
	}

	public void validate() {

		interfaceForm().validate();
		Validity v = interfaceForm().valid();
		if (!v.valid()) {
			selectTab(INTERFACE_TAB_NAME);
			Dialog.inform(
					"Error",
					"Invalid or incomplete values in the interface form: "
							+ v.reasonForIssue());
			return;
		}
		if (_metaForm != null) {
			_metaForm.validate();
			Validity valid = interfaceForm().valid();
			if (!valid.valid()) {
				selectTab(META_TAB_NAME);
				Dialog.inform("Error",
						"Invalid or incomplete values in the meta form: "
								+ valid.reasonForIssue());
				return;
			}
		}
		if (_methodMetaForm != null) {
			_methodMetaForm.validate();
			Validity valid = interfaceForm().valid();
			if (!valid.valid()) {
				selectTab(METHOD_META_TAB_NAME);
				Dialog.inform("Error",
						"Invalid or incomplete values in the method meta form: "
								+ valid.reasonForIssue());
				return;
			}
		}
	}

	public boolean valid() {

		boolean valid = interfaceForm().valid().valid();
		if (_metaForm != null) {
			valid = valid && _metaForm.valid().valid();
		}
		if (_methodMetaForm != null) {
			valid = valid && _methodMetaForm.valid().valid();
		}
		return valid;
	}

}
