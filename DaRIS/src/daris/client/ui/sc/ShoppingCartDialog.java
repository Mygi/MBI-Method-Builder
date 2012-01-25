package daris.client.ui.sc;

import arc.gui.gwt.widget.image.Image;
import arc.gui.gwt.widget.panel.TabPanel;
import arc.gui.gwt.widget.window.Window;
import arc.gui.gwt.widget.window.WindowCloseListener;
import arc.gui.window.WindowProperties;

import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;

import daris.client.Resource;
import daris.client.model.sc.ShoppingCartManager;
import daris.client.model.sc.ShoppingCartRef;
import daris.client.ui.widget.MessageBox;

public class ShoppingCartDialog {

	public static final String TRAY_ICON_LIGHT = Resource.INSTANCE
			.shoppingcartGrey16().getSafeUri().asString();
	public static final String TRAY_ICON_DARK = Resource.INSTANCE
			.shoppingcartGreen16().getSafeUri().asString();

	private TabPanel _tp;
	private int _activeTabId;

	/*
	 * active shopping cart panel
	 */
	private int _ascTabId;
	private ActiveShoppingCartPanel _ascp;

	/*
	 * shopping cart manager panel
	 */
	private int _scmTabId;
	private ShoppingCartManagerPanel _scmp;

	private Window _win;

	/*
	 * indicates if the dialog window is visible
	 */
	private boolean _showing;

	private Image _trayIcon;

	private ShoppingCartManager.Logger _logger;

	private ShoppingCartDialog() {

		_showing = false;

		_tp = new TabPanel() {
			@Override
			protected void activated(int id) {
				_activeTabId = id;
				updateWindowTitle();
				updateWindowFooter();
			}
		};
		_tp.fitToParent();
		_tp.setBodyBorder(1, BorderStyle.SOLID, "#979797");

		_ascp = new ActiveShoppingCartPanel();
		_ascTabId = _tp.addTab("Active Shopping Cart",
				"Active shopping cart that is currently in use.", _ascp);

		_scmp = new ShoppingCartManagerPanel();
		_scmTabId = _tp
				.addTab("Shopping Cart Manager",
						"Shopping Cart Manager allows you manage all the shopping carts.",
						_scmp);

		_tp.setActiveTabById(_ascTabId);

		/*
		 * tray icon
		 */

		// TODO:
		_trayIcon = new Image(TRAY_ICON_DARK);
		_trayIcon.setDisabledImage(TRAY_ICON_LIGHT);
		_trayIcon.addDoubleClickHandler(new DoubleClickHandler() {

			@Override
			public void onDoubleClick(DoubleClickEvent event) {
				if (!_showing) {
					show(_trayIcon.window(), 0.6, 0.5, false);
				}
			}
		});

		/*
		 * logger
		 */
		_logger = new ShoppingCartManager.Logger() {

			@Override
			public void log(LogType type, String msg) {
				MessageBox.display(
						LogType.error.equals(type) ? "Shopping Cart Error"
								: "Shopping Cart", msg, 3);
			}
		};
		ShoppingCartManager.setLogger(_logger);
	}

	private void updateWindowTitle() {
		if (_showing) {
			if (_activeTabId == _ascTabId) {
				_win.setTitle("Active Shopping Cart");
				ShoppingCartRef asc = ShoppingCartManager.activeShoppingCart();
				if (asc != null) {
					_win.setTitle("Active Shopping Cart " + asc.scid());
				}
			} else if (_activeTabId == _scmTabId) {
				ShoppingCartRef asc = ShoppingCartManager.activeShoppingCart();
				_win.setTitle(asc != null ? "Shopping Cart Manager [Active: "
						+ asc.scid() + "]" : "Shopping Cart Manager");
			}
		}
	}

	private void updateWindowFooter() {
		// TODO:
	}

	public void show(Window owner, double w, double h, boolean showManager) {
		if (!_showing) {
			WindowProperties wp = new WindowProperties();
			wp.setModal(false);
			wp.setCanBeResized(true);
			wp.setCanBeClosed(true);
			wp.setCanBeMoved(true);
			wp.setSize(w, h);
			wp.setOwnerWindow(owner);

			ShoppingCartRef asc = ShoppingCartManager.activeShoppingCart();
			if (showManager) {
				wp.setTitle("Shopping Cart Manager"
						+ (asc != null ? ("[active: " + asc.scid() + "]") : ""));
			} else {
				wp.setTitle("Active Shopping Cart"
						+ (asc != null ? " " + asc.scid() : ""));
			}
			_win = Window.create(wp);
			_win.setContent(_tp);
			_win.addCloseListener(new WindowCloseListener() {
				@Override
				public void closed(Window w) {
					_showing = false;
					_trayIcon.enable();
				}
			});
			_win.centerInPage();
			_win.show();
			_showing = true;
			_trayIcon.disable();
		}
		if (showManager) {
			activateSCMTab();
		} else {
			activateASCTab();
		}
	}

	protected void activateASCTab() {
		_tp.setActiveTabById(_ascTabId);
	}

	protected void activateSCMTab() {
		_tp.setActiveTabById(_scmTabId);
	}

	public void discard() {
		if (_showing) {
			_win.hide();
		}
		_ascp.discard();
		_scmp.discard();
	}

	public Image trayIcon() {
		return _trayIcon;
	}

	private static ShoppingCartDialog _instance;

	private static ShoppingCartDialog instance() {
		if (_instance == null) {
			_instance = new ShoppingCartDialog();
		}
		return _instance;
	}

	public static void reset() {
		if (_instance != null) {
			_instance.discard();
			_instance = null;
		}
	}
}
