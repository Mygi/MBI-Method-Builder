package daris.client.ui.sc;

import java.util.List;

import arc.gui.gwt.dnd.DragWidget;
import arc.gui.gwt.dnd.DropCheck;
import arc.gui.gwt.dnd.DropHandler;
import arc.gui.gwt.dnd.DropListener;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.image.Image;
import arc.gui.menu.ActionEntry;
import arc.gui.menu.Menu;
import arc.gui.object.SelectedObjectSet;
import arc.gui.object.display.ObjectDetailsDisplay;
import arc.gui.object.register.ObjectGUI;
import arc.gui.window.Window;
import arc.mf.client.util.Action;
import arc.mf.object.ObjectResolveHandler;
import daris.client.Resource;
import daris.client.model.object.DObjectRef;
import daris.client.model.sc.DeliveryMethod;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartManager;
import daris.client.model.sc.ShoppingCartRef;
import daris.client.model.sc.Status;
import daris.client.model.sc.messages.ShoppingCartDestroy;

public class ShoppingCartGUI implements ObjectGUI {

	public static final ShoppingCartGUI INSANCE = new ShoppingCartGUI();
	public static final Image ICON_ORDER = new Image(Resource.INSTANCE
			.submit16().getSafeUri().asString(), 16, 16);
	public static final Image ICON_DESTROY = new Image(Resource.INSTANCE
			.delete16().getSafeUri().asString(), 16, 16);
	public static final Image ICON_DOWNLOAD = new Image(Resource.INSTANCE
			.download16().getSafeUri().asString(), 16, 16);
	public static final Image ICON_CLEAR = new Image(Resource.INSTANCE
			.clear16().getSafeUri().asString(), 16, 16);

	private ShoppingCartGUI() {

	}

	@Override
	public String idToString(Object o) {

		if (o instanceof ShoppingCartRef) {
			return Long.toString(((ShoppingCartRef) o).scid());
		}
		return null;
	}

	@Override
	public String icon(Object o, int size) {
		return null;
	}

	@Override
	public Object reference(Object o) {

		return null;
	}

	@Override
	public boolean needToResolve(Object o) {

		if (o instanceof ShoppingCartRef) {
			return ((ShoppingCartRef) o).needToResolve();
		}
		return false;
	}

	@Override
	public void displayDetails(Object o, ObjectDetailsDisplay dd,
			boolean forEdit) {

	}

	@Override
	public void open(Window w, Object o) {

	}

	@Override
	public DropHandler dropHandler(Object o) {
		if (o != null) {
			if (o instanceof ShoppingCartRef) {
				final ShoppingCartRef sc = (ShoppingCartRef) o;
				return new DropHandler() {

					@Override
					public DropCheck checkCanDrop(Object data) {
						if (Status.editable.equals(sc.status())) {
							return DropCheck.CAN;
						}
						return DropCheck.CANNOT;
					}

					@Override
					public void drop(BaseWidget target, List<Object> data,
							DropListener dl) {
						if (data != null && Status.editable.equals(sc.status())) {
							if (!data.isEmpty()) {
								final DObjectRef o = (DObjectRef) data.get(0);
								ShoppingCartManager.addContent(sc, o);
								dl.dropped(DropCheck.CAN);
								return;
							}
						}
						dl.dropped(DropCheck.CANNOT);
					}
				};
			}
		}
		return null;
	}

	@Override
	public DragWidget dragWidget(Object o) {

		return null;
	}

	@Override
	public Menu actionMenu(Window w, Object o, SelectedObjectSet selected,
			boolean readOnly) {
		final ShoppingCartRef cart = (ShoppingCartRef) o;
		final ActionEntry orderAE = new ActionEntry(ICON_ORDER,
				"Order shopping cart " + cart.scid(),
				"Order/Submit the shopping cart for processing.", new Action() {

					@Override
					public void execute() {
						ShoppingCartManager.order(cart);
					}
				});
		orderAE.disable();
		final ActionEntry downloadAE = new ActionEntry(ICON_DOWNLOAD,
				"Download shopping cart " + cart.scid(),
				"Download the shopping cart", new Action() {

					@Override
					public void execute() {
						ShoppingCartManager.download(cart);
					}
				});
		downloadAE.disable();
		final ActionEntry destroyAE = new ActionEntry(ICON_DESTROY,
				"Destroy shopping cart " + cart.scid(),
				"Destroy the shopping cart", new Action() {

					@Override
					public void execute() {
						new ShoppingCartDestroy(cart).send();
					}
				});
		destroyAE.disable();
		// final ActionEntry clearAE = new ActionInterfaceEntry(ICON_CLEAR,
		// new ShoppingCartClearAction(w));
		final ActionEntry clearAE = new ActionEntry(ICON_CLEAR,
				"Remove finished shopping carts", new Action() {

					@Override
					public void execute() {
						ShoppingCartManager.destroy(Status.data_ready,
								Status.aborted, Status.rejected,
								Status.withdrawn, Status.error);
					}
				});
		Menu menu = new Menu(cart.referentTypeName() + " - "
				+ cart.idToString()) {
			@Override
			public void preShow() {

				cart.reset();
				cart.resolve(new ObjectResolveHandler<ShoppingCart>() {
					@Override
					public void resolved(ShoppingCart co) {
						if (!(co.destination().method() == DeliveryMethod.download && co
								.status() == Status.data_ready)) {
							downloadAE
									.softDisable("You can only download the content to your browser when the shopping cart destination is set to browser and the status of the shopping cart become data ready.");
						} else {
							downloadAE.enable();
						}
						if (co.status() != Status.editable) {
							orderAE.softDisable("Shopping cart "
									+ co.scid()
									+ " status is "
									+ co.status().toString()
									+ ". You can only submit/order the shopping carts in editable status.");
						} else {
							if (co.totalNumberOfContentItems() <= 0) {
								orderAE.softDisable("Shopping cart "
										+ co.scid() + " is empty.");
							} else {
								orderAE.enable();
							}
						}
					}
				});
				if (ShoppingCartManager.isActive(cart)) {
					destroyAE.softDisable("The active shopping cart "
							+ cart.scid() + " is in use. Cannot be destroyed.");
				} else {
					destroyAE.enable();
				}
			}
		};
		menu.setShowTitle(false);
		menu.add(orderAE);
		menu.add(downloadAE);
		menu.add(destroyAE);
		menu.add(clearAE);
		return menu;
	}

	@Override
	public Menu memberActionMenu(Window w, Object o,
			SelectedObjectSet selected, boolean readOnly) {
		return null;
	}

}
