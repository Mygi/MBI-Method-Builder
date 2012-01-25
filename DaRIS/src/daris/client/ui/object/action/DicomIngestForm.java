package daris.client.ui.object.action;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.dnd.DropCheck;
import arc.gui.gwt.dnd.DropHandler;
import arc.gui.gwt.dnd.DropListener;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.format.WidgetFormatter;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.panel.VerticalSplitPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.dti.task.DTITask;
import arc.mf.client.dti.task.DTITaskCreateHandler;
import arc.mf.client.dti.task.DTITaskStatusHandler;
import arc.mf.client.file.LocalFile;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.client.util.DateTime;
import arc.mf.client.util.Validity;
import arc.mf.dtype.BooleanType;
import arc.mf.dtype.ConstantType;
import arc.mf.model.asset.namespace.NamespaceRef;
import arc.mf.model.asset.task.AssetImportTask;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;

import daris.client.Resource;
import daris.client.model.fcp.DicomIngestFCP;
import daris.client.model.object.DObjectRef;
import daris.client.ui.dti.DTITaskDialog;
import daris.client.ui.widget.MessageBox;
import daris.client.util.ByteUtil;

public class DicomIngestForm extends ValidatedInterfaceComponent implements
		AsynchronousAction {

	public static final String FILE_ICON = Resource.INSTANCE.file16()
			.getSafeUri().asString();
	public static final String DIRECTORY_ICON = Resource.INSTANCE
			.folderViolet16().getSafeUri().asString();

	private DObjectRef _po;
	private DicomIngestFCP _fcp;
	private List<LocalFile> _files;
	private ListGrid<LocalFile> _fileGrid;
	private VerticalSplitPanel _vsp;

	public DicomIngestForm(List<LocalFile> files, DObjectRef po,
			DicomIngestFCP fcp) {
		_po = po;
		_fcp = fcp;
		_fcp.setIdCitable(_po.id());
		_files = files;

		_vsp = new VerticalSplitPanel();
		_vsp.fitToParent();

		/*
		 * fcp form
		 */
		Form form = new Form();
		Field<String> fcpField = new Field<String>(new FieldDefinition(
				"File compilation profile", ConstantType.DEFAULT,
				"File compilation profile", null, 1, 1));
		fcpField.setValue(_fcp.name());
		Field<String> idCitableField = new Field<String>(new FieldDefinition(
				"Parent " + _po.referentTypeName(), ConstantType.DEFAULT,
				"Id of the parent " + _po.referentTypeName(), null, 1, 1));
		idCitableField.setValue(_fcp.idCitable());
		form.add(idCitableField);

		Field<Boolean> anonymizeField = new Field<Boolean>(new FieldDefinition(
				"Anonymize", BooleanType.DEFAULT_TRUE_FALSE,
				"Anonymize the patient name", null, 1, 1));
		anonymizeField.setValue(_fcp.anonymize());
		anonymizeField.addListener(new FormItemListener<Boolean>() {

			@Override
			public void itemValueChanged(FormItem<Boolean> f) {
				_fcp.setAnonymize(f.value());
			}

			@Override
			public void itemPropertyChanged(FormItem<Boolean> f,
					Property property) {

			}
		});
		form.add(anonymizeField);
		form.setWidth100();
		form.setPreferredHeight(0.5);
		form.render();
		addMustBeValid(form);
		_vsp.add(form);

		/*
		 * files grid
		 */
		_fileGrid = new ListGrid<LocalFile>(ScrollPolicy.AUTO);
		_fileGrid.setEmptyMessage("");
		_fileGrid.setLoadingMessage("loading...");
		_fileGrid.setCursorSize(1000);
		_fileGrid.addColumnDefn("path", "File", "File",
				new WidgetFormatter<LocalFile, String>() {

					@Override
					public BaseWidget format(LocalFile f, String path) {
						String icon = f.isFile() ? FILE_ICON : DIRECTORY_ICON;
						HTML html = new HTML(
								"<div><img src=\""
										+ icon
										+ "\" style=\"width:16px;height:16px;vertical-align:middle\"><span style=\"\">&nbsp;"
										+ path + "</span></div>");
						html.setFontSize(11);
						return html;
					}
				}).setWidth(300);
		_fileGrid.addColumnDefn("type", "Type");
		_fileGrid.addColumnDefn("size", "Size");
		_fileGrid.setBorder(1, "#ddd");
		_fileGrid.setMultiSelect(false);
		_fileGrid.enableDropTarget(false);
		_fileGrid.setDropHandler(new DropHandler() {

			@Override
			public DropCheck checkCanDrop(Object data) {
				if (data != null) {
					if (data instanceof LocalFile) {
						return DropCheck.CAN;
					}
				}
				return DropCheck.CANNOT;
			}

			@Override
			public void drop(BaseWidget target, List<Object> data,
					DropListener dl) {
				if (_files == null) {
					_files = new Vector<LocalFile>();
				}
				boolean added = false;
				for (Object o : data) {
					LocalFile f = (LocalFile) o;
					if (!_files.contains(f)) {
						_files.add(f);
						added = true;
					}
				}
				if (added) {
					updateFileGrid();
				}
				dl.dropped(DropCheck.CAN);
			}
		});
		updateFileGrid();
		_vsp.add(_fileGrid);

	}

	private void updateFileGrid() {
		if (_files == null) {
			_fileGrid.setData(null);
		} else {
			List<ListGridEntry<LocalFile>> es = new Vector<ListGridEntry<LocalFile>>(
					_files.size());
			for (LocalFile f : _files) {
				ListGridEntry<LocalFile> e = new ListGridEntry<LocalFile>(f);
				e.set("path", f.path());
				e.set("size",
						f.isDirectory() ? "" : ByteUtil.humanReadableByteCount(
								f.length(), true));
				e.set("type", f.isDirectory() ? "directory" : "file");
				es.add(e);
			}
			_fileGrid.setData(es, false);
		}
	}

	@Override
	public Validity valid() {
		Validity valid = super.valid();
		if (hasFiles()) {
			return valid;
		} else {
			return new Validity() {

				@Override
				public boolean valid() {
					return false;
				}

				@Override
				public String reasonForIssue() {
					return "No local files or directories are added.";
				}
			};
		}
	}

	private boolean hasFiles() {
		if (_files != null) {
			if (!_files.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Widget gui() {
		return _vsp;
	}

	@Override
	public void execute(final ActionListener l) {
		AssetImportTask.create((NamespaceRef) null, _files,
				_fcp.assetImportControls(),
				new DTITaskCreateHandler<AssetImportTask>() {

					@Override
					public void created(AssetImportTask task) {
						l.executed(true);
						new DTITaskDialog(task, _vsp.window());
						task.monitor(1000, false,
								new DTITaskStatusHandler<DTITask>() {

									@Override
									public void status(Timer t, DTITask task) {
										if (task != null) {
											if (task.finished()) {
												MessageBox.display(
														"DTI Task " + task.id(),
														"DICOM ingest task "
																+ task.status()
																		.toString()
																		.toLowerCase()
																+ ".", 3);
											}
											if (task.status() == DTITask.State.COMPLETED) {
												_po.refresh(true);
											}
										}
									}
								});
					}

					@Override
					public void completed(AssetImportTask task) {
						l.executed(true);
						MessageBox.display("DTI Task " + task.id(),
								"Task completed.", 3);

					}

					@Override
					public void failed() {
						l.executed(false);
						Dialog.inform("Error",
								"Failed to create DTI task for DICOM ingest");

					}
				});
	}
}