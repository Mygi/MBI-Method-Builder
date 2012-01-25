package daris.gui.project;

import java.util.List;
import java.util.Vector;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.FieldSet;
import arc.gui.form.FieldSetListener;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItemListener;
import arc.gui.form.FormListener;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.EnumerationType;
import arc.mf.object.ObjectResolveHandler;
import daris.client.ui.form.XmlMetaForm;
import daris.gui.object.ObjectDetail;
import daris.model.datause.DataUseEnum;
import daris.model.method.Method;
import daris.model.method.MethodEnum;
import daris.model.method.MethodRef;
import daris.model.project.ProjectRef;

public class ProjectDetail extends ObjectDetail {

	public static final String MEMBER_TAB_NAME = "member";
	public static final String ROLE_MEMBER_TAB_NAME = "role member";
	public static final String META_TAB_NAME = "meta";

	private ProjectMemberGrid _memberGrid;

	private ProjectRoleMemberGrid _roleMemberGrid;

	private Form _metaForm;

	public ProjectDetail(ProjectRef o, FormEditMode mode) {

		super(o, mode);

		//
		// Member tab
		//
		_memberGrid = new ProjectMemberGrid(project(), mode());
		putTab(MEMBER_TAB_NAME, _memberGrid, false);

		//
		// Role-member tab
		//
		_roleMemberGrid = new ProjectRoleMemberGrid(project(), mode());
		putTab(ROLE_MEMBER_TAB_NAME, _roleMemberGrid, false);

		//
		// Meta tab
		//
		updateMetaTab();
	}

	public ProjectRef project() {

		return (ProjectRef) object();
	}

	private void updateMetaTab() {

		/*
		 * view
		 */
		if (mode() == FormEditMode.READ_ONLY) {
			project().metaForView(new ObjectResolveHandler<XmlElement>() {

				@Override
				public void resolved(XmlElement me) {

					if (me != null) {
						_metaForm = XmlMetaForm.formFor(me, mode());
						putTab(META_TAB_NAME, new ScrollPanel(_metaForm,
								ScrollPolicy.AUTO), false);
					} else {
						_metaForm = null;
						putTab(META_TAB_NAME, null, false);
					}
				}
			});
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
				w.push("meta",new String[]{"action", "replace"});
				f.save(w);
				w.pop();
				project().setMetaToSave(w);
			}

			@Override
			public void formStateUpdated(Form f, FormItem.Property p) {

			}
		};
		if (mode() == FormEditMode.CREATE) {
			// create
			project().metaForCreate(new ObjectResolveHandler<XmlElement>() {

				@Override
				public void resolved(XmlElement me) {

					if (me != null) {
						_metaForm = XmlMetaForm.formFor(me, mode());
						_metaForm.addListener(fl);
						putTab(META_TAB_NAME, new ScrollPanel(_metaForm,
								ScrollPolicy.AUTO), false);
					} else {
						_metaForm = null;
						putTab(META_TAB_NAME, null, false);
					}
				}
			});
		} else {
			// update
			project().metaForEdit(new ObjectResolveHandler<XmlElement>() {

				@Override
				public void resolved(XmlElement me) {

					if (me != null) {
						_metaForm = XmlMetaForm.formFor(me, mode());
						_metaForm.addListener(fl);
						putTab(META_TAB_NAME, new ScrollPanel(_metaForm,
								ScrollPolicy.AUTO), false);
					} else {
						_metaForm = null;
						putTab(META_TAB_NAME, null, false);
					}
				}
			});
		}

	}

	@Override
	protected void addToInterfaceForm(Form form) {

		//
		// method field(s)
		//
		FieldGroup methodsFieldGroup = new FieldGroup(new FieldDefinition(
				"Methods", ConstantType.DEFAULT, null, null, 1, 1));
		if (project().hasMethods()) {
			for (MethodRef m : project().methods()) {
				Field<MethodRef> methodField = new Field<MethodRef>(
						new FieldDefinition("method",
								new EnumerationType<MethodRef>(new MethodEnum(
										null)), "Method", null, 1,
								Integer.MAX_VALUE));
				methodField.setValue(m);
				methodsFieldGroup.add(methodField);
			}
		} else {
			if (mode() == FormEditMode.CREATE || mode() == FormEditMode.UPDATE) {
				Field<Method> methodField = new Field<Method>(
						new FieldDefinition("method",
								new EnumerationType<Method>(
										new MethodEnum(null)), "Method", null,
								1, Integer.MAX_VALUE));
				methodsFieldGroup.add(methodField);
			}
		}
		if (mode() != FormEditMode.READ_ONLY) {
			methodsFieldGroup.addListener(new FieldSetListener() {

				@SuppressWarnings("rawtypes")
				private List<MethodRef> getMethods(FieldSet fs) {

					if (fs == null) {
						return null;
					}
					List<FormItem> items = fs.fields();
					if (items == null) {
						return null;
					}
					if (items.isEmpty()) {
						return null;
					}
					List<MethodRef> methods = new Vector<MethodRef>();
					for (FormItem item : items) {
						MethodRef m = (MethodRef) item.value();
						if (m != null) {
							methods.add(m);
						}
					}
					return methods;
				}

				@Override
				@SuppressWarnings("rawtypes")
				public void addedField(FieldSet s, FormItem f, int idx,
						boolean lastUpdate) {

					project().setMethods(getMethods(s));
				}

				@Override
				@SuppressWarnings("rawtypes")
				public void removedField(FieldSet s, FormItem f, int idx,
						boolean lastUpdate) {

					project().setMethods(getMethods(s));
				}

				@Override
				public void updatedFields(FieldSet s) {

					project().setMethods(getMethods(s));
				}

				@Override
				@SuppressWarnings("rawtypes")
				public void updatedFieldValue(FieldSet s, FormItem f) {

					project().setMethods(getMethods(s));
				}

				@Override
				@SuppressWarnings("rawtypes")
				public void updatedFieldState(FieldSet s, FormItem f,
						FormItem.Property p) {

				}
			});
		}
		form.add(methodsFieldGroup);

		//
		// data-use field
		//
		Field<String> dataUseField = new Field<String>(new FieldDefinition(
				"data-use", new EnumerationType<String>(DataUseEnum.get()),
				"Data Use", null, 1, 1));
		dataUseField.setValue(project().dataUse());
		if (mode() != FormEditMode.READ_ONLY) {
			dataUseField.addListener(new FormItemListener<String>() {

				@Override
				public void itemValueChanged(FormItem<String> f) {

					project().setDataUse(f.value());
				}

				@Override
				public void itemPropertyChanged(FormItem<String> f,
						FormItem.Property p) {

				}
			});
		}
		form.add(dataUseField);

	}

	public void validate() {

		super.validate();
		if (!interfaceForm().valid().valid()) {
			selectTab(INTERFACE_TAB_NAME);
			Dialog.inform("Error",
					"Invalid or incomplete values in the interface form.");
			return;
		}

		String error = null;
		error = _memberGrid.validate();
		if (error != null) {
			selectTab(MEMBER_TAB_NAME);
			Dialog.inform("Error", error);
			return;
		}

		error = _roleMemberGrid.validate();
		if (error != null) {
			selectTab(ROLE_MEMBER_TAB_NAME);
			Dialog.inform("Error", error);
			return;
		}

		if (_metaForm != null) {
			if (!_metaForm.valid().valid()) {
				selectTab(META_TAB_NAME);
				_metaForm.validate();
				Dialog.inform("Error",
						"Invalid or incomplete values in the meta form.");
				return;
			}
		}
	}

	public boolean valid() {

		boolean valid = super.valid() && _memberGrid.valid()
				&& _roleMemberGrid.valid();
		if (_metaForm != null) {
			valid = valid && _metaForm.valid().valid();
		}
		return valid;
	}

}
