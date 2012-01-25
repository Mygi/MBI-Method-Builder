package daris.gui.object;

import com.google.gwt.user.client.ui.Widget;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldRenderOptions;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.StringType;
import arc.mf.dtype.TextType;
import daris.client.ui.widget.SimpleTabPanel;
import daris.gui.dataobject.DataObjectDetail;
import daris.gui.dataset.DataSetDetail;
import daris.gui.exmethod.ExMethodDetail;
import daris.gui.project.ProjectDetail;
import daris.gui.repository.RepositoryRootDetail;
import daris.gui.study.StudyDetail;
import daris.gui.subject.SubjectDetail;
import daris.model.dataobject.DataObjectRef;
import daris.model.dataset.DataSetRef;
import daris.model.exmethod.ExMethodRef;
import daris.model.object.PSSDObjectRef;
import daris.model.project.ProjectRef;
import daris.model.repository.RepositoryRootRef;
import daris.model.study.StudyRef;
import daris.model.subject.SubjectRef;

public abstract class ObjectDetail extends SimpleTabPanel {

	public static final String INTERFACE_TAB_NAME = "interface";

	public static final String ATTACHMENT_TAB_NAME = "attachments";

	private PSSDObjectRef _o;

	private FormEditMode _mode;

	private Form _interfaceForm;

	private AttachmentPanel _attachmentPanel;

	protected ObjectDetail(PSSDObjectRef o, FormEditMode mode) {

		assert o != null;
		_o = o;
		_mode = mode;
		fitToParent();

		/*
		 * interface tab
		 */
		updateInterfaceTab();

		/*
		 * attachment tab
		 */
		updateAttachmentTab();
	}

	protected PSSDObjectRef object() {

		return _o;
	}

	protected FormEditMode mode() {

		return _mode;
	}

	protected void setInterfaceTabContent(Widget content) {

		putTab(INTERFACE_TAB_NAME, content, true);
	}

	protected void updateInterfaceTab() {

		_interfaceForm = createInterfaceForm();
		_interfaceForm.render();
		putTab(INTERFACE_TAB_NAME, new ScrollPanel(interfaceForm(),
				ScrollPolicy.AUTO), true);
	}

	private void updateAttachmentTab() {

		if (_mode == FormEditMode.CREATE) {
			// TODO: NOTE: attachment operations are only available after
			// the object is created.
			return;
		}
		if (_attachmentPanel == null) {
			// Note: lazy render: only load the data when the tab is selected
			_attachmentPanel = new AttachmentPanel(_o, false);
		}
		putTab(ATTACHMENT_TAB_NAME, _attachmentPanel, false);
		setTabListener(ATTACHMENT_TAB_NAME, new SimpleTabPanel.TabListener() {

			@Override
			public void selected(String tabName) {

				_attachmentPanel.render();
			}

			@Override
			public void deselected(String tabName) {

			}
		});
	}

	public Form interfaceForm() {

		return _interfaceForm;
	}

	protected void setInterfaceForm(Form interfaceForm) {

		_interfaceForm = interfaceForm;
	}

	protected Form createInterfaceForm() {

		Form interfaceForm = new Form(_mode);
		if (_mode != FormEditMode.CREATE) {
			Field<String> idField = new Field<String>(new FieldDefinition("id",
					ConstantType.DEFAULT, "Object id", null, 1, 1));
			idField.setValue(_o.id());
			interfaceForm.add(idField);
			if (_o.assetId() != null) {
				Field<String> assetIdField = new Field<String>(
						new FieldDefinition("asset_id", ConstantType.DEFAULT,
								"asset id", null, 1, 1));
				assetIdField.setValue(_o.assetId());
				interfaceForm.add(assetIdField);
			}
			if (_o.proute() != null) {
				Field<String> prouteField = new Field<String>(
						new FieldDefinition("proute", ConstantType.DEFAULT,
								"proute", null, 1, 1));
				prouteField.setValue(_o.proute());
				interfaceForm.add(prouteField);
			}
		}
		Field<String> nameField = new Field<String>(new FieldDefinition("name",
				TextType.DEFAULT, "object name", null, 0, 1));
		nameField.setValue(_o.name());
		
		FieldRenderOptions fro = new FieldRenderOptions();
		fro.setWidth(0.6);
		nameField.setRenderOptions(fro);
		nameField.addListener(new FormItemListener<String>() {
			@Override
			public void itemValueChanged(FormItem<String> f) {

				_o.setName(f.value());
			}

			@Override
			public void itemPropertyChanged(FormItem<String> f,
					FormItem.Property p) {

			}
		});
		interfaceForm.add(nameField);
		Field<String> descField = new Field<String>(new FieldDefinition(
				"description", TextType.DEFAULT, "object description", null,
				0, 1));
		descField.setValue(_o.description());
		descField.addListener(new FormItemListener<String>() {

			@Override
			public void itemValueChanged(FormItem<String> f) {

				_o.setDescription(f.value());
			}

			@Override
			public void itemPropertyChanged(FormItem<String> f,
					FormItem.Property p) {

			}
		});
		interfaceForm.add(descField);
		addToInterfaceForm(interfaceForm);
		return interfaceForm;
	}

	protected void addToInterfaceForm(Form interfaceForm) {

	}

	public static ObjectDetail detailFor(PSSDObjectRef o, FormEditMode mode) {

		if (o instanceof RepositoryRootRef) {
			return new RepositoryRootDetail();
		} else if (o instanceof ProjectRef) {
			return new ProjectDetail((ProjectRef) o, mode);
		} else if (o instanceof SubjectRef) {
			return new SubjectDetail((SubjectRef) o, mode);
		} else if (o instanceof ExMethodRef) {
			return new ExMethodDetail((ExMethodRef) o, mode);
		} else if (o instanceof StudyRef) {
			return new StudyDetail((StudyRef) o, mode);
		} else if (o instanceof DataSetRef) {
			return new DataSetDetail((DataSetRef) o, mode);
		} else if (o instanceof DataObjectRef) {
			return new DataObjectDetail((DataObjectRef) o, mode);
		} else {
			throw new AssertionError("Unable to generate interface for "
					+ o.getClass().getName());
		}

	}

	public void validate() {

		interfaceForm().validate();
	}

	public boolean valid() {

		return interfaceForm().valid().valid();
	}

}
