package daris.gui.object;

import java.util.List;
import java.util.Vector;

import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridColumn;
import arc.gui.gwt.widget.list.ListGridDataSource;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.Transformer;
import arc.mf.object.ObjectResolveHandler;
import daris.model.object.PSSDObjectRef;
import daris.model.object.attachment.Attachment;
import daris.model.object.attachment.AttachmentsRef;

public class AttachmentGrid extends ListGrid<Attachment> {

	public static class AttachmentDataSource implements DataSource<Attachment> {

		private AttachmentsRef _attachments;

		public AttachmentDataSource(PSSDObjectRef o) {

			_attachments = new AttachmentsRef(o);
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
		public void load(final Filter f, final long start, final long end, final DataLoadHandler<Attachment> lh) {
			
			_attachments.reset();
			_attachments.resolve(new ObjectResolveHandler<List<Attachment>>() {

				@Override
				public void resolved(List<Attachment> as) {

					if (as != null) {
						List<Attachment> ras = as;
						if (f != null) {
							List<Attachment> fas = new Vector<Attachment>();
							for (Attachment a : as) {
								if (f.matches(a)) {
									fas.add(a);
								}
							}
							ras = fas;
						}
						long total = ras.size();
						int start1 = (int) start;
						int end1 = (int) end;
						if (start1 > 0 || end1 < ras.size()) {
							if (start1 >= ras.size()) {
								ras = null;
							} else {
								if (end1 > ras.size()) {
									end1 = ras.size();
								}
								ras = ras.subList(start1, end1);
							}
						}
						if (ras != null) {
							if (ras.isEmpty()) {
								ras = null;
							}
						}
						lh.loaded(start1, end1, total, ras, ras == null ? null : DataLoadAction.REPLACE);
					} else {
						lh.loaded(0, 0, 0, null, null);
					}
				}
			});

		}

	}

	private static class AttachmentTransformer extends Transformer<Attachment, ListGridEntry<Attachment>> {

		public static final Transformer<Attachment, ListGridEntry<Attachment>> INSTANCE = new AttachmentTransformer();

		@Override
		protected ListGridEntry<Attachment> doTransform(Attachment m) throws Throwable {

			ListGridEntry<Attachment> lge = new ListGridEntry<Attachment>(m);
			lge.set("id", m.assetId());
			lge.set("name", m.name());
			lge.set("mimeType", m.mimeType());
			lge.set("size", m.size());
			lge.set("extension", m.extension());
			lge.setDescription("Attachment");
			return lge;

		}

	}

	@SuppressWarnings("rawtypes")
	public AttachmentGrid(PSSDObjectRef o) {

		super(ScrollPolicy.AUTO);
		
		setMultiSelect(true);

		ListGridColumn idColumn = addColumnDefn("id", "id");
		idColumn.setWidth(60);

		ListGridColumn nameColumn = addColumnDefn("name", "name");
		nameColumn.setWidth(160);

		ListGridColumn mimeTypeColumn = addColumnDefn("mimeType", "mime-type");
		mimeTypeColumn.setWidth(160);

		ListGridColumn extensionColumn = addColumnDefn("extension", "ext");
		extensionColumn.setWidth(60);

		ListGridColumn sizeColumn = addColumnDefn("size", "size(bytes)");
		sizeColumn.setWidth(80);
		
		setEmptyMessage("");

		if (o != null) {
			setData(o);
		}
	}

	public AttachmentGrid() {

		this(null);
	}

	public void setData(PSSDObjectRef o) {

		setDataSource(new ListGridDataSource<Attachment>(new AttachmentDataSource(o), AttachmentTransformer.INSTANCE));
	}
}
