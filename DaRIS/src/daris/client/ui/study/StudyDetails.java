package daris.client.ui.study;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.form.FormListener;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.EnumerationType;
import daris.client.model.exmethod.StepEnum;
import daris.client.model.object.DObjectRef;
import daris.client.model.study.Study;
import daris.client.model.study.StudyType;
import daris.client.model.study.StudyTypeEnum;
import daris.client.ui.form.XmlMetaForm;
import daris.client.ui.object.DObjectDetails;

public class StudyDetails extends DObjectDetails {

	private int _methodMetaTabId = 0;
	private Form _methodMetaForm;

	public StudyDetails(DObjectRef po, Study o, FormEditMode mode) {

		super(po, o, mode);

		updateMethodMetaTab();
	}

	private void removeMethodMetaTab() {

		if (_methodMetaTabId > 0) {
			tabs().removeTabById(_methodMetaTabId);
			_methodMetaTabId = 0;
		}
	}

	private void updateMethodMetaTab() {

		if (_methodMetaForm != null) {
			removeMustBeValid(_methodMetaForm);
		}
		Study so = (Study) object();
		if (mode().equals(FormEditMode.READ_ONLY)) {
			if (so.methodMeta() == null) {
				removeMethodMetaTab();
				return;
			}
			_methodMetaForm = XmlMetaForm.formFor(so.methodMeta(), mode());
		} else {
			if (so.methodMetaForEdit() == null) {
				removeMethodMetaTab();
				return;
			}
			_methodMetaForm = XmlMetaForm.formFor(so.methodMetaForEdit(),
					mode());
			addMustBeValid(_methodMetaForm);
			_methodMetaForm.addListener(new FormListener() {

				@Override
				public void rendering(Form f) {

				}

				@Override
				public void rendered(Form f) {
					BaseWidget.resized(f);
				}

				@Override
				public void formValuesUpdated(Form f) {

					XmlStringWriter w = new XmlStringWriter();
					if (mode().equals(FormEditMode.CREATE)) {
						w.push("meta");
					} else {
						w.push("meta", new String[] { "action", "replace" });
					}
					f.save(w);
					w.pop();
					Study study = (Study) object();
					// TODO:
					assert study.stepPath() != null;
					assert study.exMethodId() != null;
					study.setMethodMeta(w);
				}

				@Override
				public void formStateUpdated(Form f, Property p) {

				}
			});
		}
		if (_methodMetaTabId > 0) {
			tabs().setTabContent(_methodMetaTabId,
					new ScrollPanel(_methodMetaForm, ScrollPolicy.AUTO));
		} else {
			_methodMetaTabId = tabs().addTab("Method Metadata",
					"Method metadata",
					new ScrollPanel(_methodMetaForm, ScrollPolicy.AUTO));
		}
	}

	protected void addInterfaceFields(Form interfaceForm) {

		super.addInterfaceFields(interfaceForm);

		final Study so = (Study) object();

		/*
		 * study type
		 */
		Field<StudyType> studyTypeField;
		if (so.studyType() != null || FormEditMode.READ_ONLY == mode()) {
			studyTypeField = new Field<StudyType>(new FieldDefinition("type",
					ConstantType.DEFAULT, "Study Type", null, 1, 1));
			studyTypeField.setValue(so.studyType());
		} else {
			studyTypeField = new Field<StudyType>(new FieldDefinition("type",
					new EnumerationType<StudyType>(new StudyTypeEnum(
							so.exMethodId())), "Study Type", null, 1, 1));
			studyTypeField.addListener(new FormItemListener<StudyType>() {

				@Override
				public void itemValueChanged(FormItem<StudyType> f) {

					so.setStudyType(f.value());
					so.setStepPath(null);
					updateInterfaceTab();
					updateMethodMetaTab();
				}

				@Override
				public void itemPropertyChanged(FormItem<StudyType> f,
						FormItem.Property p) {

				}
			});
		}
		studyTypeField.setValue(so.studyType());
		interfaceForm.add(studyTypeField);

		/*
		 * method { id, step }
		 */
		FieldGroup methodFieldGroup = new FieldGroup(new FieldDefinition(
				"method", ConstantType.DEFAULT, "method", null, 1, 1));
		Field<String> methodIdField = new Field<String>(new FieldDefinition(
				"id", ConstantType.DEFAULT, "id", null, 1, 1));
		methodIdField.setValue(so.exMethodId());
		methodFieldGroup.add(methodIdField);
		Field<String> methodStepField;

		if (so.stepPath() != null || FormEditMode.READ_ONLY == mode()) {

			methodStepField = new Field<String>(new FieldDefinition("step",
					ConstantType.DEFAULT, "step", null, 1, 1));
		} else {

			methodStepField = new Field<String>(new FieldDefinition("step",
					new EnumerationType<String>(new StepEnum(so)), "step",
					null, 1, 1));
			methodStepField.addListener(new FormItemListener<String>() {

				@Override
				public void itemValueChanged(FormItem<String> f) {

					so.setStepPath(f.value());
					updateMethodMetaTab();
				}

				@Override
				public void itemPropertyChanged(FormItem<String> f,
						FormItem.Property p) {

				}
			});
		}
		methodStepField.setValue(so.stepPath());
		methodFieldGroup.add(methodStepField);
		interfaceForm.add(methodFieldGroup);
	}

}
