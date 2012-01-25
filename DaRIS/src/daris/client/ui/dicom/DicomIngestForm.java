package daris.client.ui.dicom;

import java.util.List;
import java.util.Vector;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldRenderOptions;
import arc.gui.form.Form;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.dnd.DropCheck;
import arc.gui.gwt.dnd.DropHandler;
import arc.gui.gwt.dnd.DropListener;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.mf.client.dti.file.DTIDirectory;
import arc.mf.client.dti.task.DTITask;
import arc.mf.client.dti.task.DTITaskCreateHandler;
import arc.mf.client.dti.task.DTITaskStatusHandler;
import arc.mf.client.file.LocalFile;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.dtype.BooleanType;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.StringType;
import arc.mf.model.asset.namespace.NamespaceRef;
import arc.mf.model.asset.task.AssetImportControls;
import arc.mf.model.asset.task.AssetImportTask;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;

import daris.client.model.dicom.DicomIngestFCP;
import daris.client.model.object.DObjectRef;
import daris.client.ui.dti.DTITaskDialog;
import daris.client.ui.dti.file.LocalFileBrowserPanel;

public class DicomIngestForm extends ValidatedInterfaceComponent implements
		AsynchronousAction {

	private DObjectRef _po;
	private DicomIngestFCP _fcp;
	private boolean _anonymize = true;
	private String _dcmDirPath;

	private VerticalPanel _vp;
	private Form _form;

	public DicomIngestForm(DObjectRef po, DicomIngestFCP fcp) {
		_po = po;
		_fcp = fcp;
		_form = new Form();
		Field<String> idField = new Field<String>(new FieldDefinition(
				"Parent ID", ConstantType.DEFAULT, "Parent "
						+ po.referentTypeName() + " ID", null, 1, 1));
		idField.setValue(_po.id());
		_form.add(idField);
		Field<String> fcpField = new Field<String>(new FieldDefinition(
				"File Compilation Profile", ConstantType.DEFAULT,
				"File Compilation Profile (.fcp)", null, 1, 1));
		fcpField.setValue(_fcp.name());
		_form.add(fcpField);

		Field<Boolean> anonymizeField = new Field<Boolean>(new FieldDefinition(
				"Anonymize", BooleanType.DEFAULT_TRUE_FALSE,
				"Anonymize patient name", null, 1, 1));
		anonymizeField.setValue(_anonymize);
		anonymizeField.addListener(new FormItemListener<Boolean>() {

			@Override
			public void itemValueChanged(FormItem<Boolean> f) {
				_anonymize = f.value();
			}

			@Override
			public void itemPropertyChanged(FormItem<Boolean> f,
					Property property) {

			}
		});
		_form.add(anonymizeField);

		final Field<String> dcmDirPathField = new Field<String>(
				new FieldDefinition(
						"DICOM Directory",
						StringType.DEFAULT,
						"Local DICOM directory to ingest.",
						"Drag directory contains DICOM files from local file system browser below and drop it here.",
						1, 1));
		dcmDirPathField.addListener(new FormItemListener<String>() {

			@Override
			public void itemValueChanged(FormItem<String> f) {
				_dcmDirPath = f.value();
			}

			@Override
			public void itemPropertyChanged(FormItem<String> f,
					Property property) {

			}
		});
		FieldRenderOptions fro = new FieldRenderOptions();
		fro.setWidth(0.6);
		dcmDirPathField.setRenderOptions(fro);
		dcmDirPathField.setReadOnly();
		_form.add(dcmDirPathField);
		_form.setHeight(120);
		_form.setWidth100();
		_form.setBorder(1, "#979797");
		_form.setPaddingLeft(25);
		_form.render();
		_form.makeDropTarget(new DropHandler() {

			@Override
			public DropCheck checkCanDrop(Object data) {
				if (data == null) {
					return DropCheck.CANNOT;
				}
				if (data instanceof LocalFile) {
					if (((LocalFile) data).isDirectory()) {
						return DropCheck.CAN;
					}
				}
				return DropCheck.CANNOT;
			}

			@Override
			public void drop(BaseWidget target, List<Object> data,
					DropListener dl) {

				if (data == null) {
					return;
				}
				if (data.isEmpty()) {
					return;
				}
				LocalFile dir = (LocalFile) data.get(0);
				dcmDirPathField.setValue(dir.path());
				dl.dropped(DropCheck.CAN);
			}
		});
		addMustBeValid(_form);

		_vp = new VerticalPanel();
		_vp.fitToParent();
		_vp.add(_form);

		_vp.add(new LocalFileBrowserPanel("Local File System", true, null,
				LocalFile.Filter.ANY, null, false).gui());

	}

	@Override
	public Widget gui() {
		return _vp;
	}

	@Override
	public void execute(final ActionListener l) {

		AssetImportControls aic = _fcp.createAssetImportControls(_po.id(),
				_anonymize);
		List<LocalFile> dirs = new Vector<LocalFile>();
		dirs.add(new DTIDirectory(_dcmDirPath));
		final AssetImportTask task = AssetImportTask.create(
				(NamespaceRef) null, dirs, aic,
				new DTITaskCreateHandler<AssetImportTask>() {

					@Override
					public void created(AssetImportTask task) {

						new DTITaskDialog(task, _vp.window());
						task.monitor(1000, false,
								new DTITaskStatusHandler<DTITask>() {

									@Override
									public void status(Timer timer, DTITask t) {
										if (t != null) {
											if (t.finished()) {
												Dialog.inform("DICOM data has been ingested successfully");
												if (t.status() == DTITask.State.COMPLETED) {
													_po.refresh(true);
												}
											}											
										}
									}
								});
						l.executed(true);
					}

					@Override
					public void completed(AssetImportTask task) {
						Dialog.inform("DICOM Ingest DTI Task " + task.id(),
								"DTI Task " + task.id() + " completed");
						l.executed(true);
					}

					@Override
					public void failed() {
						Dialog.inform("Failed to create DTI Task");
						l.executed(false);
					}
				});
	}

}
