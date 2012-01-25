package daris.client.ui.subject;

import java.util.List;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldRenderOptions;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.form.FormListener;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.ActionListener;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.EnumerationType;
import arc.mf.object.ObjectResolveHandler;
import daris.client.model.method.MethodEnum;
import daris.client.model.method.MethodRef;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.project.DataUse;
import daris.client.model.project.Project;
import daris.client.model.subject.Subject;
import daris.client.ui.form.XmlMetaForm;
import daris.client.ui.object.DObjectDetails;

public class SubjectDetails extends DObjectDetails {

	private int _privateMetaTabId = 0;

	Form _privateMetaForm;

	private int _publicMetaTabId = 0;

	Form _publicMetaForm;

	public SubjectDetails(DObjectRef po, Subject o, FormEditMode mode) {

		super(po, o, mode);

		updatePublicMetaTab();

		updatePrivateMetaTab();
	}

	@Override
	protected void addInterfaceFields(Form interfaceForm) {

		super.addInterfaceFields(interfaceForm);
		final Subject so = (Subject) object();
		/*
		 * method
		 */
		if (mode() == FormEditMode.READ_ONLY || mode() == FormEditMode.UPDATE) {
			// We do not allow to change subject method at the moment.
			Field<MethodRef> methodField = new Field<MethodRef>(
					new FieldDefinition("method", ConstantType.DEFAULT,
							"Method", null, 1, 1));
			methodField.setValue(so.method());
			interfaceForm.add(methodField);
		} else {
			final Field<MethodRef> methodField = new Field<MethodRef>(
					new FieldDefinition("method",
							new EnumerationType<MethodRef>(new MethodEnum(
									parentObject())), "Method", null, 1, 1));
			FieldRenderOptions fro = new FieldRenderOptions();
			fro.setWidth(500);
			methodField.setRenderOptions(fro);
			methodField.addListener(new FormItemListener<MethodRef>() {

				@Override
				public void itemValueChanged(FormItem<MethodRef> f) {

					if (f.value() != null) {
						so.setMethod(f.value());
						Subject.setMetaForEdit(so, parentObject().id(), f
								.value().id(), new ActionListener() {

							@Override
							public void executed(boolean succeeded) {

								if (succeeded) {
									updatePublicMetaTab();
									updatePrivateMetaTab();
								}
							}
						});
					}
				}

				@Override
				public void itemPropertyChanged(FormItem<MethodRef> f,
						FormItem.Property p) {

				}

			});
			interfaceForm.add(methodField);
			if (parentObject().needToResolve()) {
				parentObject().reset();
			}
			parentObject().resolve(new ObjectResolveHandler<DObject>() {
				@Override
				public void resolved(DObject o) {

					if (o != null) {
						Project po = (Project) o;
						List<MethodRef> methods = po.methods();
						if (methods != null) {
							if (methods.size() == 1) {
								methodField.setValue(methods.get(0));
							}
						}
					}

				}
			});
		}

		/*
		 * data-use
		 */
		Field<DataUse> dataUseField = new Field<DataUse>(
				new FieldDefinition("data-use", DataUse.asEnumerationType(),
						"data-use", null, 0, 1));
		dataUseField.setValue(so.dataUse());
		if (mode() != FormEditMode.READ_ONLY) {
			dataUseField.addListener(new FormItemListener<DataUse>() {

				@Override
				public void itemValueChanged(FormItem<DataUse> f) {

					so.setDataUse(f.value());
				}

				@Override
				public void itemPropertyChanged(FormItem<DataUse> f,
						FormItem.Property p) {

				}
			});
		}
		interfaceForm.add(dataUseField);
	}

	private void removePublicMetaTab() {

		if (_publicMetaTabId > 0) {
			tabs().removeTabById(_publicMetaTabId);
			_publicMetaTabId = 0;
		}
		if (_publicMetaForm != null) {
			removeMustBeValid(_publicMetaForm);
			_publicMetaForm = null;
		}
	}

	private void updatePublicMetaTab() {

		removePublicMetaTab();
		Subject so = (Subject) object();
		if (mode().equals(FormEditMode.READ_ONLY)) {
			if (so.publicMeta() == null) {
				return;
			}
			_publicMetaForm = XmlMetaForm.formFor(so.publicMeta(), mode());
		} else {
			if (so.publicMetaForEdit() == null) {
				return;
			}
			_publicMetaForm = XmlMetaForm.formFor(so.publicMetaForEdit(),
					mode());
			addMustBeValid(_publicMetaForm);
			_publicMetaForm.addListener(new FormListener() {

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
					w.push("public");
					f.save(w);
					w.pop();
					((Subject) object()).setPublicMeta(w);
				}

				@Override
				public void formStateUpdated(Form f, Property p) {

				}
			});
		}
		_publicMetaTabId = tabs().addTab("Public Metadata", "Public metadata",
				new ScrollPanel(_publicMetaForm, ScrollPolicy.AUTO));
	}

	private void removePrivateMetaTab() {

		if (_privateMetaTabId > 0) {
			tabs().removeTabById(_privateMetaTabId);
			_privateMetaTabId = 0;
		}
		if (_privateMetaForm != null) {
			removeMustBeValid(_privateMetaForm);
			_privateMetaForm = null;
		}
	}

	private void updatePrivateMetaTab() {

		removePrivateMetaTab();
		Subject so = (Subject) object();
		if (mode().equals(FormEditMode.READ_ONLY)) {
			if (so.privateMeta() == null) {
				return;
			}
			_privateMetaForm = XmlMetaForm.formFor(so.privateMeta(), mode());
		} else {
			if (so.privateMetaForEdit() == null) {
				return;
			}
			_privateMetaForm = XmlMetaForm.formFor(so.privateMetaForEdit(),
					mode());
			addMustBeValid(_privateMetaForm);
			_privateMetaForm.addListener(new FormListener() {

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
					w.push("private");
					f.save(w);
					w.pop();
					((Subject) object()).setPrivateMeta(w);
				}

				@Override
				public void formStateUpdated(Form f, Property p) {

				}
			});
		}
		_privateMetaTabId = tabs().addTab("Private Metadata",
				"Private metadata",
				new ScrollPanel(_privateMetaForm, ScrollPolicy.AUTO));
	}

	protected int privateMetaTabId() {

		return _privateMetaTabId;
	}

	protected int publicMetaTabId() {

		return _publicMetaTabId;
	}
}
