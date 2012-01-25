package daris.client.ui.sc;

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
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.list.ListGridRowContextMenuHandler;
import arc.gui.gwt.widget.menu.ActionContextMenu;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.tip.ToolTip;
import arc.gui.gwt.widget.tip.ToolTipHandler;
import arc.gui.menu.Menu;
import arc.gui.object.SelectedObjectSet;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ContextMenuEvent;

import daris.client.model.object.DObjectRef;
import daris.client.model.sc.ContentItem;
import daris.client.model.sc.ShoppingCartManager;
import daris.client.model.sc.ShoppingCartRef;
import daris.client.model.sc.Status;
import daris.client.model.sc.messages.ShoppingCartContentList;
import daris.client.ui.DObjectGUIRegistry;
import daris.client.util.ByteUtil;

public class ShoppingCartContentGrid extends ListGrid<ContentItem> {

	private static class ShoppingCartContentDataSource implements
			DataSource<ListGridEntry<ContentItem>> {

		private ShoppingCartRef _cart;

		public ShoppingCartContentDataSource(ShoppingCartRef cart) {
			_cart = cart;
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
				final DataLoadHandler<ListGridEntry<ContentItem>> lh) {

			if (_cart == null) {
				lh.loaded(0, 0, 0, null, null);
				return;
			}
			new ShoppingCartContentList(_cart)
					.send(new ObjectMessageResponse<List<ContentItem>>() {

						@Override
						public void responded(List<ContentItem> items) {
							if (items != null) {
								doLoad(f, start, end, items, lh);
							} else {
								lh.loaded(0, 0, 0, null, null);
							}

						}
					});

		}

		private void doLoad(Filter f, long start, long end,
				List<ContentItem> items,
				DataLoadHandler<ListGridEntry<ContentItem>> lh) {

			List<ListGridEntry<ContentItem>> es = new Vector<ListGridEntry<ContentItem>>();
			for (ContentItem item : items) {
				ListGridEntry<ContentItem> e = new ListGridEntry<ContentItem>(
						item);
				e.set("id", item.id());
				e.set("name", item.name());
				e.set("description", item.description());
				e.set("size",
						ByteUtil.humanReadableByteCount(item.size(), true));
				e.set("mimeType", item.mimeType());
				e.set("type", item.type());
				e.set("assetId", item.assetId());
				e.set("status", item.status());
				es.add(e);
			}
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

	private ShoppingCartRef _cart;

	public ShoppingCartContentGrid(ShoppingCartRef cart) {

		super(new ShoppingCartContentDataSource(cart), ScrollPolicy.AUTO);
		_cart = cart;

		addColumnDefn("id", "ID").setWidth(100);
		addColumnDefn("name", "Name").setWidth(120);
		addColumnDefn("mimeType", "MIME Type").setWidth(120);
		addColumnDefn("size", "Size").setWidth(120);
		addColumnDefn("status", "Status").setWidth(120);

		fitToParent();
		setShowHeader(true);
		setShowRowSeparators(true);
		setMultiSelect(false);
		setFontSize(10);
		setCellSpacing(0);
		setCellPadding(1);
		setEmptyMessage("");
		setLoadingMessage("Loading shopping-cart contents ...");
		setCursorSize(Integer.MAX_VALUE);
		setMultiSelect(true);
		setObjectRegistry(DObjectGUIRegistry.get());

		/*
		 * Context Menu
		 */
		setRowContextMenuHandler(new ListGridRowContextMenuHandler<ContentItem>() {

			@Override
			public void show(ContentItem data, ContextMenuEvent event) {
				SelectedObjectSet selected = new SelectedObjectSet() {

					@Override
					public List<?> selections() {
						return ShoppingCartContentGrid.this.selections();
					}
				};
				Menu menu = ContentItemGUI.INSANCE.actionMenu(
						ShoppingCartContentGrid.this.window(), data, selected,
						true);
				if (menu != null) {
					ActionContextMenu acm = new ActionContextMenu(menu);
					NativeEvent ne = event.getNativeEvent();
					acm.showAt(ne);
				}
			}
		});

		/*
		 * D & D
		 */
		enableDropTarget(false);
		setDropHandler(new DropHandler() {

			@Override
			public DropCheck checkCanDrop(Object data) {
				if (_cart == null) {
					return DropCheck.CANNOT;
				}
				if (!Status.editable.equals(_cart.status())) {
					return DropCheck.CANNOT;
				}
				if (data instanceof DObjectRef) {
					return DropCheck.CAN;
				} else {
					return DropCheck.CANNOT;
				}
			}

			@Override
			public void drop(BaseWidget target, final List<Object> data,
					final DropListener dl) {

				if (_cart != null && data != null
						&& Status.editable.equals(_cart.status())) {
					if (!data.isEmpty()) {
						final DObjectRef o = (DObjectRef) data.get(0);
						ShoppingCartManager.addContent(_cart, o);
						dl.dropped(DropCheck.CAN);
						return;
					}
				}
				dl.dropped(DropCheck.CANNOT);
			}
		});

		setRowToolTip(new ToolTip<ContentItem>() {

			@Override
			public void generate(ContentItem item, ToolTipHandler th) {

				th.setTip(new HTML(item.toHTML()));
			}
		});
		if (cart != null) {
			refresh();
		}
	}

	public void setCart(ShoppingCartRef cart) {
		_cart = cart;
		setDataSource(new ShoppingCartContentDataSource(_cart));
	}

	public boolean hasSelections() {
		List<ContentItem> selections = selections();
		if (selections != null) {
			if (!selections.isEmpty()) {
				return true;
			}
		}
		return false;
	}

}
