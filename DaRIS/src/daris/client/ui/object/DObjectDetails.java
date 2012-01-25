package daris.client.ui.object;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.form.FormListener;
import arc.gui.gwt.style.FontWeight;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.image.LinearGradient;
import arc.gui.gwt.widget.label.Label;
import arc.gui.gwt.widget.panel.CenteringPanel;
import arc.gui.gwt.widget.panel.CenteringPanel.Axis;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.TabPanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.StringType;
import arc.mf.dtype.TextType;

import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.user.client.ui.Widget;

import daris.client.model.IDUtil;
import daris.client.model.dataobject.DataObject;
import daris.client.model.dataset.DataSet;
import daris.client.model.exmethod.ExMethod;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.project.Project;
import daris.client.model.repository.Repository;
import daris.client.model.study.Study;
import daris.client.model.subject.Subject;
import daris.client.ui.dataobject.DataObjectDetails;
import daris.client.ui.dataset.DataSetDetails;
import daris.client.ui.exmethod.ExMethodDetails;
import daris.client.ui.form.XmlMetaForm;
import daris.client.ui.project.ProjectDetails;
import daris.client.ui.repository.RepositoryDetails;
import daris.client.ui.study.StudyDetails;
import daris.client.ui.subject.SubjectDetails;
import daris.client.util.StringUtil;

public abstract class DObjectDetails extends ValidatedInterfaceComponent {

	public static final String BORDER_COLOR = "#979797";
	public static final String BORDER_COLOR_LIGHT = "#cdcdcd";
	public static final int BORDER_RADIUS = 5;
	public static final int BORDER_WIDTH = 1;

	private FormEditMode _mode;

	private VerticalPanel _vp;

	private TabPanel _tp;

	private int _interfaceTabId = 0;

	private Form _interfaceForm;

	private int _attachmentTabId = 0;

	private AttachmentGrid _attachmentGrid;

	private int _metaTabId = 0;

	private Form _metaForm;

	private Label _statusLabel;

	private DObject _o;

	private DObjectRef _po;

	protected DObjectDetails(DObjectRef po, DObject o, FormEditMode mode) {

		this(po, o, mode, mode.equals(FormEditMode.READ_ONLY) ? true : false);
	}

	protected DObjectDetails(DObjectRef po, DObject o, FormEditMode mode,
			boolean showHeader) {

		_o = o;
		_po = po;
		_mode = mode;
		_vp = new VerticalPanel();
		_vp.fitToParent();
		if (showHeader) {
			_vp.add(headerFor(o));
		}
		SimplePanel sp = new SimplePanel();
		sp.fitToParent();
		sp.setBorder(BORDER_WIDTH, BORDER_COLOR);
		_tp = new TabPanel();
		_tp.fitToParent();
		_tp.setBodyBorder(2, BorderStyle.SOLID, "#888");
		sp.setContent(_tp);
		_vp.add(sp);

		_statusLabel = new Label();
		_statusLabel.setHeight(20);
		_statusLabel.setWidth100();
		_statusLabel.setPaddingLeft(20);
		_statusLabel.setFontSize(12);
		_statusLabel.setFontWeight(FontWeight.BOLD);
		_statusLabel.setColour("red");

		if (FormEditMode.READ_ONLY != _mode) {
			_vp.add(_statusLabel);
		}

		updateInterfaceTab();

		updateAttachmentTab();

		updateMetaTab();
	}

	@Override
	public Widget gui() {

		return _vp;
	}

	@Override
	public Validity valid() {
		Validity v = super.valid();
		if (v.valid()) {
			_statusLabel.setText("");
		} else {
			_statusLabel.setText(v.reasonForIssue());
		}
		return v;
	}

	protected TabPanel tabs() {

		return _tp;
	}

	protected DObjectRef parentObject() {

		return _po;
	}

	protected DObject object() {

		return _o;
	}

	protected FormEditMode mode() {

		return _mode;
	}

	protected void updateInterfaceTab() {

		if (_interfaceForm != null && !_mode.equals(FormEditMode.READ_ONLY)) {
			removeMustBeValid(_interfaceForm);
		}
		_interfaceForm = new Form(_mode);
		_interfaceForm.fitToParent();
		addInterfaceFields(_interfaceForm);
		_interfaceForm.render();
		if (!_mode.equals(FormEditMode.READ_ONLY)) {
			addMustBeValid(_interfaceForm);
		}
		if (_interfaceTabId > 0) {
			_tp.setTabContent(_interfaceTabId, new ScrollPanel(_interfaceForm,
					ScrollPolicy.AUTO));
		} else {
			_interfaceTabId = _tp.addTab("Interface", "Interface metadata",
					new ScrollPanel(_interfaceForm, ScrollPolicy.AUTO));
			_tp.setActiveTabById(_interfaceTabId);
		}

	}

	protected void addInterfaceFields(Form interfaceForm) {

		if (!_mode.equals(FormEditMode.CREATE)) {
			Field<String> idField = new Field<String>(new FieldDefinition("id",
					ConstantType.DEFAULT, "object id", null, 1, 1));
			idField.setValue(_o.id());
			interfaceForm.add(idField);
			// if (_o.assetId() != null) {
			// Field<String> assetIdField = new Field<String>(
			// new FieldDefinition("asset_id", ConstantType.DEFAULT,
			// "Asset ID", null, 1, 1));
			// assetIdField.setValue(_o.assetId());
			// interfaceForm.add(assetIdField);
			// }
			// if (_o.proute() != null) {
			// Field<String> prouteField = new Field<String>(
			// new FieldDefinition("PRoute", ConstantType.DEFAULT,
			// "proute", null, 1, 1));
			// prouteField.setValue(_o.proute());
			// interfaceForm.add(prouteField);
			// }
		}
		Field<String> nameField = new Field<String>(new FieldDefinition("name",
				StringType.DEFAULT, "object name", null, 0, 1));
		nameField.setValue(_o.name());
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

		String ddesc = _o.typeName() + " description";
		if (_o instanceof Project) {
			ddesc = "Paragraph description of project, including aims and brief description of methods; mention where participants were recruited";
		}
		Field<String> descField = new Field<String>(new FieldDefinition(
				"description", TextType.DEFAULT, ddesc, null, 0, 1));
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
	}

	private static Widget headerFor(DObject o) {

		String text = StringUtil.upperCaseFirst(o.typeName());
		if (o.id() != null) {
			text += " - " + o.id();
		}
		Label label = new Label(text);
		label.setFontSize(12);
		label.setFontWeight(FontWeight.BOLD);

		CenteringPanel cp = new CenteringPanel(Axis.HORIZONTAL);
		cp.setWidth100();
		cp.setHeight(20);
		cp.setMarginTop(1);
		cp.setBorderTop(BORDER_WIDTH, BorderStyle.SOLID, BORDER_COLOR);
		cp.setBorderLeft(BORDER_WIDTH, BorderStyle.SOLID, BORDER_COLOR);
		cp.setBorderRight(BORDER_WIDTH, BorderStyle.SOLID, BORDER_COLOR);
		cp.setBorderRadiusTopLeft(BORDER_RADIUS);
		cp.setBorderRadiusTopRight(BORDER_RADIUS);
		cp.setBackgroundImage(new LinearGradient(
				LinearGradient.Orientation.TOP_TO_BOTTOM, BORDER_COLOR_LIGHT,
				BORDER_COLOR));
		cp.add(label);
		return cp;
	}

	public static DObjectDetails detailsFor(DObject o, FormEditMode mode) {

		// No parent object provided, must be viewing or editing.
		assert !mode.equals(FormEditMode.CREATE);
		return detailsFor(new DObjectRef(IDUtil.getParentId(o.id()),
				o.proute(), false, false), o, mode);
	}

	public static DObjectDetails detailsFor(DObjectRef po, DObject o,
			FormEditMode mode) {

		if (o instanceof Repository) {
			return new RepositoryDetails((Repository) o, mode);
		} else if (o instanceof Project) {
			return new ProjectDetails((Project) o, mode);
		} else if (o instanceof Subject) {
			return new SubjectDetails(po, (Subject) o, mode);
		} else if (o instanceof ExMethod) {
			return new ExMethodDetails(po, (ExMethod) o, mode);
		} else if (o instanceof Study) {
			return new StudyDetails(po, (Study) o, mode);
		} else if (o instanceof DataSet) {
			return new DataSetDetails(po, (DataSet) o, mode);
		} else if (o instanceof DataObject) {
			return new DataObjectDetails(po, (DataObject) o, mode);
		} else {
			throw new AssertionError(
					"Failed to instantiate details(GUI) for object " + o.id());
		}
	}

	private void updateAttachmentTab() {

		if (_o instanceof Repository) {
			return;
		}

		if (_mode.equals(FormEditMode.CREATE)) {
			// TODO: enhance to include attachments when creation.
			return;
		}
		if (_attachmentGrid == null) {
			_attachmentGrid = new AttachmentGrid(_o);
			_attachmentTabId = _tp.addTab("Attachments", "Attachments",
					_attachmentGrid);
		} else {
			_attachmentGrid.refresh();
		}
	}

	private void removeMetaTab() {

		if (_metaTabId > 0) {
			tabs().removeTabById(_metaTabId);
			_metaTabId = 0;
		}
	}

	private void updateMetaTab() {

		if (_metaForm != null) {
			removeMustBeValid(_metaForm);
		}
		if (_mode.equals(FormEditMode.READ_ONLY)) {
			if (_o.meta() == null) {
				removeMetaTab();
				return;
			}
			_metaForm = XmlMetaForm.formFor(_o.meta(), mode());
		} else {
			if (_o.metaForEdit() == null) {
				removeMetaTab();
				return;
			}
			_metaForm = XmlMetaForm.formFor(_o.metaForEdit(), mode());
			addMustBeValid(_metaForm);
			_metaForm.addListener(new FormListener() {

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
					_o.setMeta(w);
				}

				@Override
				public void formStateUpdated(Form f, Property p) {

				}
			});
		}
		if (_metaTabId > 0) {
			tabs().setTabContent(_metaTabId,
					new ScrollPanel(_metaForm, ScrollPolicy.AUTO));
		} else {
			_metaTabId = tabs().addTab(" Metadata", "Domain metadata",
					new ScrollPanel(_metaForm, ScrollPolicy.AUTO));
		}

	}

	protected int interfaceTabId() {

		return _interfaceTabId;

	}

	protected int metaTabId() {

		return _metaTabId;
	}

	protected int attachmentTabId() {

		return _attachmentTabId;
	}

}
