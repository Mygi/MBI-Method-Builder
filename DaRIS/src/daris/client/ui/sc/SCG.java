package daris.client.ui.sc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.format.WidgetFormatter;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.list.ListGridRowContextMenuHandler;
import arc.gui.gwt.widget.menu.ActionContextMenu;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.menu.Menu;
import arc.gui.object.SelectedObjectSet;
import arc.mf.client.util.DateTime;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.user.client.Timer;

import daris.client.model.sc.DeliveryMethod;
import daris.client.model.sc.Log;
import daris.client.model.sc.Progress;
import daris.client.model.sc.ProgressHandler;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartManager;
import daris.client.model.sc.ShoppingCartRef;
import daris.client.model.sc.Status;
import daris.client.model.sc.messages.ShoppingCartList;
import daris.client.ui.DObjectGUIRegistry;
import daris.client.ui.widget.ProgressBar;

public class SCG extends ListGrid<ShoppingCartRef> {

	private List<ShoppingCartRef> _scs;
	private Map<ShoppingCartRef, Timer> _timers;

	public SCG(List<ShoppingCartRef> scs) {
		super(ScrollPolicy.AUTO);
		_timers = new HashMap<ShoppingCartRef, Timer>();

		addColumnDefn("scid", "Cart", "Shopping Cart",
				new WidgetFormatter<ShoppingCartRef, Long>() {
					@Override
					public BaseWidget format(ShoppingCartRef sc, Long scid) {
						String idAndName = "" + scid
								+ (sc.name() == null ? "" : (": " + sc.name()));
						String icon = sc.status().icon();
						if (ShoppingCartManager.isActive(sc)) {
							icon = ShoppingCart.ACTIVE_ICON;
						}
						return new HTML(
								"<div><img src=\""
										+ icon
										+ "\" style=\"width:16px;height:16px;vertical-align:middle\"><span style=\"\">&nbsp;"
										+ idAndName + "</span></div>");
					}
				}).setWidth(120);
		addColumnDefn("status", "Status", "Status",
				new WidgetFormatter<ShoppingCartRef, Status>() {
					@Override
					public BaseWidget format(final ShoppingCartRef sc,
							Status status) {
						final HTML w = new HTML();
						if (sc.status() == null) {
							sc.resolve(new ObjectResolveHandler<ShoppingCart>() {

								@Override
								public void resolved(ShoppingCart c) {
									w.setHTML(c.status().toString());
								}
							});
						} else {
							w.setHTML(sc.status().toString());
						}
						return w;
					}
				}).setWidth(80);
		addColumnDefn("progress", "Progress", "Progress",
				new WidgetFormatter<ShoppingCartRef, Status>() {
					@Override
					public BaseWidget format(final ShoppingCartRef sc,
							Status status) {
						final ProgressBar pw = new ProgressBar();
						pw.setWidth(200);
						if (Status.data_ready == status) {
							pw.setProgress(1.0);
						} else if (Status.processing == status) {
							Timer t = sc.monitorProgress(1000,
									new ProgressHandler() {

										@Override
										public void progress(Progress progress) {
											if (progress != null) {
												String msg = DateTime
														.durationAsString((long) (progress
																.duration() * 1000))
														+ " - "
														+ progress.completed()
														+ "/"
														+ progress.total()
														+ " processed.";
												pw.setProgress(
														(double) progress
																.completed()
																/ (double) progress
																		.total(),
														msg);
											} else {
												_timers.remove(sc);
											}
										}
									});
							_timers.put(sc, t);
						} else {
							pw.setProgress(0.0);
						}
						return pw;
					}
				}).setWidth(220);
		addColumnDefn("log", "Log", "Log",
				new WidgetFormatter<ShoppingCartRef, Status>() {
					@Override
					public BaseWidget format(ShoppingCartRef sc, Status status) {
						final HTML w = new HTML();
						sc.resolve(new ObjectResolveHandler<ShoppingCart>() {

							@Override
							public void resolved(ShoppingCart c) {
								updateLog(w, c);
							}
						});
						return w;
					}
				}).setWidth(300);
		setMultiSelect(false);
		setPreferredHeight(0.5);
		setEmptyMessage("");
		setLoadingMessage("");
		setRowContextMenuHandler(new ListGridRowContextMenuHandler<ShoppingCartRef>() {

			@Override
			public void show(ShoppingCartRef cart, ContextMenuEvent event) {
				Menu menu = ShoppingCartGUI.INSANCE.actionMenu(
						SCG.this.window(), cart, new SelectedObjectSet() {

							@Override
							public List<ShoppingCartRef> selections() {
								return SCG.this.selections();
							}
						}, true);
				ActionContextMenu acm = new ActionContextMenu(menu);
				NativeEvent ne = event.getNativeEvent();
				acm.showAt(ne);
			}
		});
		setObjectRegistry(DObjectGUIRegistry.get());
		enableDropTarget(true);

		update(false);
	}

	public void update(boolean local) {
		if (local) {
			update(_scs);
		} else {
			new ShoppingCartList(null)
					.send(new ObjectMessageResponse<List<ShoppingCartRef>>() {

						@Override
						public void responded(List<ShoppingCartRef> scs) {
							update(scs);
						}
					});
		}
	}

	private void update(List<ShoppingCartRef> scs) {
		_scs = scs;

		for (Timer t : _timers.values()) {
			t.cancel();
		}
		_timers.clear();

		if (scs == null) {
			setData(null);
			return;
		}
		List<ListGridEntry<ShoppingCartRef>> es = new Vector<ListGridEntry<ShoppingCartRef>>(
				scs.size());
		for (ShoppingCartRef sc : scs) {
			ListGridEntry<ShoppingCartRef> e = new ListGridEntry<ShoppingCartRef>(
					sc);
			e.set("scid", sc.scid());
			e.set("status", sc.status());
			e.set("progress", sc.status());
			e.set("log", sc.status());
			es.add(e);
		}
		if (!es.isEmpty()) {
			ShoppingCartRef selected = null;
			List<ShoppingCartRef> selections = selections();
			if (selections != null) {
				if (!selections.isEmpty()) {
					selected = selections.get(0);
				}
			}
			setData(es);
			if (selected != null && scs.contains(selected)) {
				select(selected);
			} else {
				if (ShoppingCartManager.activeShoppingCart() != null) {
					select(ShoppingCartManager.activeShoppingCart());
				} else {
					select(scs.get(0));
				}
			}
		} else {
			setData(null);
		}
	}

	// public void add(ShoppingCartRef sc) {
	// if (_scs != null) {
	// if (_scs.contains(sc)) {
	// return;
	// }
	// }
	// if (_scs == null) {
	// _scs = new ArrayList<ShoppingCartRef>();
	// }
	// _scs.add(sc);
	// update(_scs);
	// }
	//
	// public void remove(ShoppingCartRef sc) {
	// if (_scs == null) {
	// return;
	// }
	// if (!_scs.contains(sc)) {
	// return;
	// }
	// _scs.remove(sc);
	// update(_scs);
	// }

	private void updateLog(HTML w, ShoppingCart c) {

		Log log = null;
		if (c.logs() != null) {
			if (!c.logs().isEmpty()) {
				log = c.logs().get(0);
			}
		}
		switch (c.status()) {
		case editable:
			if (ShoppingCartManager.isActive(c)) {
				w.setHTML("Cart is active. "
						+ (log != null ? log.message() : ""));
			} else {
				w.setHTML(log != null ? log.message() : "");
			}
			break;
		case data_ready:
			if (c.destination().method() == DeliveryMethod.deposit) {
				w.setHTML("Data has been transfered to "
						+ c.destination().name());
			} else {
				w.setHTML("Data archive has been prepared. Ready to download");
			}
			break;
		default:
			w.setHTML(log != null ? log.message() : "");
		}
	}
}
