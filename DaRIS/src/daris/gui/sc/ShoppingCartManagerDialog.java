package daris.gui.sc;

import java.util.List;
import java.util.Vector;

import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.window.Window;
import arc.gui.window.WindowProperties;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import daris.model.sc.Destination;
import daris.model.sc.ShoppingCartListener;
import daris.model.sc.ShoppingCartManager;
import daris.model.sc.ShoppingCartRef;
import daris.model.sc.Status;

public class ShoppingCartManagerDialog implements ShoppingCartListener {

	private static ShoppingCartManagerDialog _instance;

	public static void init() {

		if (_instance == null) {
			_instance = new ShoppingCartManagerDialog();
		} else {
			_instance._grid.refresh();
		}
	}

	public static void show() {

		init();
		_instance.showMe();
	}

	private VerticalPanel _vp;
	private ShoppingCartGrid _grid;

	private Window _win;
	private WindowProperties _wp;

	private ShoppingCartManagerDialog() {

		/*
		 * Layout
		 */
		_vp = new VerticalPanel();
		_vp.fitToParent();

		_grid = new ShoppingCartGrid();
		_grid.refresh();
		_vp.add(_grid);

		ButtonBar bb = new ButtonBar(ButtonBar.Position.BOTTOM,
				ButtonBar.Alignment.CENTER);
		bb.setWidth100();
		bb.setHeight(28);
		bb.setColourEnabled(false);
		bb.setMargin(0);
		bb.setBackgroundColour("#DDDDDD");

		Button removeButton = new Button("Remove");
		removeButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				if (_grid.selections() == null) {
					return;
				}
				List<ShoppingCartRef> toDestroy = new Vector<ShoppingCartRef>();
				for (ShoppingCartRef cart : _grid.selections()) {
					// do not destroy editable shopping-cart.
					// TODO: validate.
					if (cart.status().value() != Status.Value.editable) {
						toDestroy.add(cart);
					}
				}
				ShoppingCartManager.instance().destroy(toDestroy);
			}

		});
		bb.add(removeButton);

		Button clearButton = new Button("Clear");
		clearButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				ShoppingCartManager.instance().clear(Status.Value.error);
				ShoppingCartManager.instance().clear(Status.Value.data_ready);
			}
		});
		bb.add(clearButton);

		_vp.add(bb);
		/*
		 * Window
		 */
		_wp = new WindowProperties();
		_wp.setModal(false);
		_wp.setCanBeResized(true);
		_wp.setCanBeClosed(true);
		_wp.setCanBeMoved(true);
		_wp.setCenterInPage(true);
		_wp.setTitle("Shopping Cart Manager");
		_wp.setSize(
				(int) (com.google.gwt.user.client.Window.getClientWidth() * 0.6),
				(int) (com.google.gwt.user.client.Window.getClientHeight() * 0.6));
		/*
		 * subscribe
		 */
		ShoppingCartManager.instance().addListener(this);
	}

	private void showMe() {

		_win = Window.create(_wp);
		_win.setContent(_vp);
		_win.centerInPage();
		_win.show();
	}

	@Override
	public void created(ShoppingCartRef cart) {

		_grid.refresh();

	}

	@Override
	public void deleted(ShoppingCartRef cart) {

		_grid.refresh();

	}

	@Override
	public void updated(ShoppingCartRef cart) {

		_grid.refresh();
	}

	@Override
	public void contentChanged(ShoppingCartRef cart) {

	}

	@Override
	public void dataReady(ShoppingCartRef cart) {

		_grid.refresh();
		if (cart.destination().type() == Destination.Type.deposit) {
			Dialog.inform("Data from Shopping-cart " + cart.id()
					+ " have been downloaded to " + cart.destination().name());
		}

	}

}
