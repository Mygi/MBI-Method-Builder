package daris.gui.sc;

import java.util.List;

import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridDataSource;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.Action;
import arc.mf.client.util.Transformer;
import daris.model.sc.ContentItem;
import daris.model.sc.ShoppingCartRef;

public class ShoppingCartContentGrid extends ListGrid<ContentItem> {

	private Action _postLoadAction = null;

	private static class ContentItemEntryTransformer extends
			Transformer<ContentItem, ListGridEntry<ContentItem>> {

		@Override
		protected ListGridEntry<ContentItem> doTransform(ContentItem item)
				throws Throwable {

			ListGridEntry<ContentItem> lge = new ListGridEntry<ContentItem>(
					item);
			lge.set("id", item.id());
			lge.set("asset-id", item.assetId());
			lge.set("object-type", item.objectType());
			lge.set("name", item.name());
			lge.set("description", item.description());
			lge.set("mime-type", item.mimeType());
			lge.set("size", item.size());
			lge.set("status", item.status());
			lge.setDescription("Item");
			return lge;
		}
	}

	public ShoppingCartContentGrid(ShoppingCartRef cart) {

		super(ScrollPolicy.AUTO);
		addColumnDefn("id", "object-id");
		addColumnDefn("object-type", "type");
		addColumnDefn("name", "name");
		addColumnDefn("mime-type", "mime-type");
		addColumnDefn("size", "size");
		addColumnDefn("status", "status");
		setShowHeader(true);
		setHeight100();
		setWidth100();
		setFontSize(12);
		setCellSpacing(0);
		setCellPadding(2);
		setEmptyMessage("No Content Items found!");
		setCursorSize(500);
		setMultiSelect(true);
		setData(cart);
	}
		

	public ShoppingCartContentGrid() {

		this(null);
	}

	@Override
	protected void postLoad(long start, long end, long total,
			List<ListGridEntry<ContentItem>> entries) {

		if (total > 0) {
			select((int) (total - 1));
		}
		if (_postLoadAction != null) {
			_postLoadAction.execute();
			_postLoadAction = null;
		}
	}

	public void refresh(Action postLoadAction) {

		_postLoadAction = postLoadAction;
		super.refresh();
	}

	public void setData(ShoppingCartRef cart) {

		if (cart != null) {
			setDataSource(new ListGridDataSource<ContentItem>(
					new ShoppingCartContentDataSource(cart),
					new ContentItemEntryTransformer()));
		} else {
			setDataSource(null);
		}
	}

}
