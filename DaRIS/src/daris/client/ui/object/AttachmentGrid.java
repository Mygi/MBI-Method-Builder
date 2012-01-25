package daris.client.ui.object;

import java.util.List;
import java.util.Vector;

import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.gui.gwt.dnd.DropCheck;
import arc.gui.gwt.dnd.DropHandler;
import arc.gui.gwt.dnd.DropListener;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.list.ListGridRowContextMenuHandler;
import arc.gui.gwt.widget.menu.ActionMenu;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.tip.ToolTip;
import arc.gui.gwt.widget.tip.ToolTipHandler;
import arc.gui.menu.ActionEntry;
import arc.gui.menu.Menu;
import arc.mf.client.file.LocalFile;
import arc.mf.client.util.Action;
import arc.mf.client.util.Transformer;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.event.dom.client.ContextMenuEvent;

import daris.client.model.object.Attachment;
import daris.client.model.object.DObject;
import daris.client.ui.DObjectGUIRegistry;
import daris.client.ui.dti.file.LocalFileBrowser;

public class AttachmentGrid extends ListGrid<Attachment> implements DropHandler {

	private static class AttachmentTransformer extends
			Transformer<Attachment, ListGridEntry<Attachment>> {

		public static final AttachmentTransformer INSTANCE = new AttachmentTransformer();

		private AttachmentTransformer() {

		}

		@Override
		protected ListGridEntry<Attachment> doTransform(Attachment a)
				throws Throwable {

			if (a == null) {
				return null;
			}
			ListGridEntry<Attachment> e = new ListGridEntry<Attachment>(a);
			e.set("assetId", a.assetId());
			e.set("name", a.name());
			e.set("extension", a.extension());
			e.set("mimeType", a.mimeType());
			e.set("size", a.size());
			return e;
		}

	}

	private static class AttachmentListTransformer extends
			Transformer<List<Attachment>, List<ListGridEntry<Attachment>>> {

		public static final AttachmentListTransformer INSTANCE = new AttachmentListTransformer();

		private AttachmentListTransformer() {

		}

		@Override
		protected List<ListGridEntry<Attachment>> doTransform(
				List<Attachment> as) throws Throwable {

			if (as == null) {
				return null;
			}
			if (as.isEmpty()) {
				return null;
			}
			List<ListGridEntry<Attachment>> es = new Vector<ListGridEntry<Attachment>>(
					as.size());
			for (Attachment a : as) {
				es.add(AttachmentTransformer.INSTANCE.transform(a));
			}
			return es;
		}

	}

	private static class AttachmentDataSource implements
			DataSource<ListGridEntry<Attachment>> {

		private DObject _o;

		private AttachmentDataSource(DObject o) {

			_o = o;
		}

		@Override
		public boolean isRemote() {

			return true;
		}

		@Override
		public boolean supportCursor() {

			return false;
		}

		@Override
		public void load(final Filter f, final long start, final long end,
				final DataLoadHandler<ListGridEntry<Attachment>> lh) {

			_o.attachments(new ObjectMessageResponse<List<Attachment>>() {

				@Override
				public void responded(List<Attachment> as) {

					if (as != null) {
						doLoad(f, start, end, as, lh);
					} else {
						lh.loaded(0, 0, 0, null, null);
					}
				}
			});
		}

		private void doLoad(Filter f, long start, long end,
				List<Attachment> attachments,
				DataLoadHandler<ListGridEntry<Attachment>> lh) {

			List<ListGridEntry<Attachment>> es = AttachmentListTransformer.INSTANCE
					.transform(attachments);
			int total = es.size();
			int start1 = start < 0 ? 0 : (start > total ? total : (int) start);
			int end1 = end > total ? total : (int) end;
			if (start1 < 0 || end1 > total || start1 > end) {
				lh.loaded(start, end, total, null, null);
			} else {
				es = es.subList(start1, end1);
				lh.loaded(start1, end1, total, es, DataLoadAction.REPLACE);
			}
		}
	}

	private DObject _o;

	private ActionEntry _addActionEntry;
	private ActionEntry _removeActionEntry;
	private ActionEntry _downloadActionEntry;

	public AttachmentGrid(DObject o) {

		super(new AttachmentDataSource(o), ScrollPolicy.AUTO);
		_o = o;
		addColumnDefn("assetId", "Asset ID").setWidth(80);
		addColumnDefn("name", "Name").setWidth(120);
		addColumnDefn("extension", "Extension").setWidth(90);
		addColumnDefn("mimeType", "MIME Type").setWidth(120);
		addColumnDefn("size", "Size(Bytes)").setWidth(120);

		fitToParent();
		setShowHeader(true);
		setShowRowSeparators(true);
		setMultiSelect(true);
		setFontSize(10);
		setCellSpacing(0);
		setCellPadding(1);
		setEmptyMessage("");
		setLoadingMessage("Loading attachment list from Mediaflux server...");
		setCursorSize(Integer.MAX_VALUE);

		setRowToolTip(new ToolTip<Attachment>() {

			@Override
			public void generate(Attachment a, ToolTipHandler th) {

				th.setTip(new HTML(a.toHTML()));
			}
		});

		/*
		 * init action entries
		 */
		_addActionEntry = new ActionEntry("Add", new Action() {

			@Override
			public void execute() {

				LocalFileBrowser.instance().show(window());
			}
		});
		_removeActionEntry = new ActionEntry("Remove", new Action() {

			@Override
			public void execute() {

				if (hasSelections()) {
					_o.detach(AttachmentGrid.this.selections(),
							new ObjectMessageResponse<Boolean>() {
								@Override
								public void responded(Boolean r) {

									if (r) {
										AttachmentGrid.this.refresh();
									}
								}
							});
				}
			}
		});
		_removeActionEntry.disable();
		_downloadActionEntry = new ActionEntry("Download", new Action() {

			@Override
			public void execute() {

				if (hasSelections()) {
					_o.retrieveAttachments(AttachmentGrid.this.selections(),
							null);
				}
			}
		});
		_downloadActionEntry.disable();

		setSelectionHandler(new SelectionHandler<Attachment>() {

			@Override
			public void selected(Attachment a) {

				boolean hasSelections = hasSelections();
				_removeActionEntry.setEnabled(hasSelections);
				_downloadActionEntry.setEnabled(hasSelections);
			}

			@Override
			public void deselected(Attachment o) {

				boolean hasSelections = hasSelections();
				_removeActionEntry.setEnabled(hasSelections);
				_downloadActionEntry.setEnabled(hasSelections);
			}
		});

		/*
		 * context menu
		 */
		final Menu contextMenu = new Menu("Attachment");
		_o.editable(new ObjectMessageResponse<Boolean>() {

			@Override
			public void responded(Boolean editable) {

				if (editable) {
					contextMenu.add(_addActionEntry);
					contextMenu.add(_removeActionEntry);
				}
				contextMenu.add(_downloadActionEntry);
			}
		});
		setContextMenu(contextMenu);
		setRowContextMenuHandler(new ListGridRowContextMenuHandler<Attachment>() {

			@Override
			public void show(Attachment attachment, ContextMenuEvent event) {

				new ActionMenu(contextMenu).showAt(event.getNativeEvent());
			}
		});

		/*
		 * enable drag from grid
		 */
		setObjectRegistry(DObjectGUIRegistry.get());
		enableRowDrag();

		/*
		 * 
		 */
		enableDropTarget(false);
		setDropHandler(this);

		/*
		 * 
		 */

		refresh();
	}

	private boolean hasSelections() {

		List<Attachment> selections = AttachmentGrid.this.selections();
		if (selections != null) {
			if (!selections.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public DropCheck checkCanDrop(Object object) {

		if (object == null) {
			return DropCheck.CANNOT;
		}
		if (object instanceof LocalFile) {
			return DropCheck.CAN;
		}
		return DropCheck.CANNOT;
	}

	@Override
	public void drop(BaseWidget target, List<Object> objects, DropListener dl) {

		if (objects == null) {
			dl.dropped(DropCheck.CANNOT);
			return;
		}
		if (objects.isEmpty()) {
			dl.dropped(DropCheck.CANNOT);
			return;
		}
		dl.dropped(DropCheck.CAN);
		List<LocalFile> files = new Vector<LocalFile>(objects.size());
		for (Object object : objects) {
			files.add((LocalFile) object);
		}
		_o.attach(files, new ObjectMessageResponse<List<Attachment>>() {

			@Override
			public void responded(List<Attachment> r) {

				if (r != null) {
					AttachmentGrid.this.refresh();
				}
			}
		});
	}

}
