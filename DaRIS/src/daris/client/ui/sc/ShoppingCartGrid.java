package daris.client.ui.sc;

import java.util.List;
import java.util.Vector;

import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.format.WidgetFormatter;
import arc.gui.gwt.widget.image.Image;
import arc.gui.gwt.widget.image.ImageAndText;
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
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.resources.client.ImageResource;

import daris.client.Resource;
import daris.client.model.sc.DeliveryMethod;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartManager;
import daris.client.model.sc.ShoppingCartRef;
import daris.client.model.sc.Status;
import daris.client.model.sc.messages.ShoppingCartList;
import daris.client.ui.DObjectGUIRegistry;

public class ShoppingCartGrid extends ListGrid<ShoppingCartRef> {

	public static final String ACTIVE_ICON = Resource.INSTANCE.active24()
			.getSafeUri().asString();
	public static final String DEPOSIT_ICON = Resource.INSTANCE.deposit24()
			.getSafeUri().asString();

	private static class ShoppingCartDataSource implements
			DataSource<ListGridEntry<ShoppingCartRef>> {

		public ShoppingCartDataSource() {

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
				final DataLoadHandler<ListGridEntry<ShoppingCartRef>> lh) {

			new ShoppingCartList(null)
					.send(new ObjectMessageResponse<List<ShoppingCartRef>>() {

						@Override
						public void responded(List<ShoppingCartRef> carts) {

							if (carts != null) {
								doLoad(f, start, end, carts, lh);
							} else {
								lh.loaded(0, 0, 0, null, null);
							}
						}
					});

		}

		private void doLoad(Filter f, long start, long end,
				List<ShoppingCartRef> carts,
				DataLoadHandler<ListGridEntry<ShoppingCartRef>> lh) {

			List<ListGridEntry<ShoppingCartRef>> es = new Vector<ListGridEntry<ShoppingCartRef>>();
			for (ShoppingCartRef cart : carts) {
				ListGridEntry<ShoppingCartRef> e = new ListGridEntry<ShoppingCartRef>(
						cart);

				e.set("scid", cart.scid());
				e.set("name", cart.name());
				e.set("status", cart.status());
				e.set("active", ShoppingCartManager.isActive(cart));
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

	public ShoppingCartGrid() {

		super(new ShoppingCartDataSource(), ScrollPolicy.AUTO);
		addColumnDefn("active", "Cart", "Cart",
				new WidgetFormatter<ShoppingCartRef, Boolean>() {
					@Override
					public BaseWidget format(ShoppingCartRef cart,
							Boolean active) {
						String idAndName = "" + cart.scid();
						if (cart.name() != null) {
							idAndName += ": " + cart.name();
						}
						String icon = active ? ACTIVE_ICON : cart.status()
								.icon();
						final String cartName = idAndName;
						final HTML html = new HTML(
								"<div><img src=\""
										+ icon
										+ "\" style=\"width:16px;height:16px;vertical-align:middle\"><span style=\"\">&nbsp;"
										+ idAndName + "</span></div>");
						html.setFontSize(12);
						if (Status.data_ready.equals(cart.status())) {
							cart.resolve(new ObjectResolveHandler<ShoppingCart>() {

								@Override
								public void resolved(ShoppingCart c) {
									if (DeliveryMethod.deposit.equals(c
											.destination().method())) {
										html.setHTML("<div><img src=\""
												+ DEPOSIT_ICON
												+ "\" style=\"width:16px;height:16px;vertical-align:middle\"><span style=\"\">&nbsp;"
												+ cartName + "</span></div>");
									}
								}
							});
						}
						return html;
					}
				}).setWidth(120);
		addColumnDefn("status", "Status", "Status",
				new WidgetFormatter<ShoppingCartRef, Status>() {
					@Override
					public BaseWidget format(ShoppingCartRef cart, Status status) {

						final HTML html = new HTML(status.toString());
						html.setFontSize(12);
						cart.statusDescription(new ObjectResolveHandler<String>() {

							@Override
							public void resolved(String statusDesc) {
								html.setHTML(statusDesc);
							}
						});
						return html;
					}
				}).setWidth(120);
		fitToParent();
		setShowHeader(true);
		setShowRowSeparators(true);
		setMultiSelect(false);
		setFontSize(10);
		setCellSpacing(0);
		setCellPadding(1);
		setEmptyMessage("");
		setLoadingMessage("Loading Shopping-carts...");
		setCursorSize(Integer.MAX_VALUE);

		setObjectRegistry(DObjectGUIRegistry.get());

		setRowContextMenuHandler(new ListGridRowContextMenuHandler<ShoppingCartRef>() {

			@Override
			public void show(ShoppingCartRef cart, ContextMenuEvent event) {
				Menu menu = ShoppingCartGUI.INSANCE.actionMenu(
						ShoppingCartGrid.this.window(), cart,
						new SelectedObjectSet() {

							@Override
							public List<ShoppingCartRef> selections() {
								return ShoppingCartGrid.this.selections();
							}
						}, true);
				ActionContextMenu acm = new ActionContextMenu(menu);
				NativeEvent ne = event.getNativeEvent();
				acm.showAt(ne);
			}
		});

		setRowToolTip(new ToolTip<ShoppingCartRef>() {

			@Override
			public void generate(ShoppingCartRef cart, ToolTipHandler th) {

				th.setTip(new HTML(cart.toHTML()));
			}
		});
		refresh();
	}

	private List<ShoppingCartRef> _preRefreshSelections;

	@Override
	public void refresh() {
		_preRefreshSelections = selections();
		super.refresh();
	}

	@Override
	protected void postLoad(long start, long end, long total,
			List<ListGridEntry<ShoppingCartRef>> entries) {
		if (entries == null) {
			return;
		}
		if (entries.isEmpty()) {
			return;
		}
		boolean hasPreLoadSelections = false;
		if (_preRefreshSelections != null) {
			if (!_preRefreshSelections.isEmpty()) {
				hasPreLoadSelections = true;
			}
		}
		boolean selected = false;
		ShoppingCartRef firstEditable = null;
		for (ListGridEntry<ShoppingCartRef> e : entries) {
			ShoppingCartRef sc = e.data();
			if (firstEditable == null) {
				if (sc.status().equals(Status.editable)) {
					firstEditable = sc;
				}
			}
			if (hasPreLoadSelections) {
				if (_preRefreshSelections.contains(sc)) {
					select(e.data());
					selected = true;
				}
			}
		}
		if (selected) {
			return;
		}
		ShoppingCartRef asc = ShoppingCartManager.activeShoppingCart();
		if (asc != null) {
			select(asc);
			return;
		}
		if (firstEditable != null) {
			select(firstEditable);
			return;
		}
		select(entries.get(0).data());
	}
}
