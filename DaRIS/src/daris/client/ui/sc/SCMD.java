package daris.client.ui.sc;

import java.util.List;

import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.menu.MenuButton;
import arc.gui.gwt.widget.menu.MenuToolBar;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.panel.VerticalSplitPanel;
import arc.gui.gwt.widget.window.Window;
import arc.gui.gwt.widget.window.WindowCloseListener;
import arc.gui.image.Image;
import arc.gui.object.SelectedObjectSet;
import arc.gui.window.WindowProperties;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import daris.client.Resource;
import daris.client.model.sc.ShoppingCartManager;
import daris.client.model.sc.ShoppingCartRef;
import daris.client.ui.util.ButtonUtil;

public class SCMD implements ShoppingCartManager.Listener {

	public static Image ICON_BACKGROUND = new arc.gui.image.Image(
			Resource.INSTANCE.background24().getSafeUri().asString(), 16, 16);

	private SimplePanel _sp;

	private boolean _showDetail = false;

	private VerticalPanel _scmPanel;
	private Button _showDetailButton;
	private MenuButton _actionMenuButton;
	private SCG _scmGrid;

	private ShoppingCartDetail _scPanel;

	private Window _win;

	private boolean _showing;

	private SCMD() {

		_sp = new SimplePanel();
		_sp.fitToParent();

		_scmPanel = new VerticalPanel();
		_scmPanel.setWidth100();

		MenuToolBar _scmToolBar = new MenuToolBar();
		_scmToolBar.setHeight(28);
		_scmToolBar.setWidth100();
		_actionMenuButton = ButtonUtil.createMenuButton(Resource.INSTANCE
				.action16().getSafeUri().asString(), 16, 16, "Action", null);
		_scmToolBar.add(_actionMenuButton);
		_showDetailButton = ButtonUtil.createButton(Resource.INSTANCE
				.detail24().getSafeUri().asString(), 16, 16, "Show Detail",
				null, false, new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						setShowDetail(!_showDetail);
					}
				});
		_scmToolBar.add(_showDetailButton);
		_scmPanel.add(_scmToolBar);

		_scmGrid = new SCG(null);
		_scmGrid.fitToParent();
		_scmPanel.add(_scmGrid);

		_scPanel = new ShoppingCartDetail(null);
		_scPanel.fitToParent();

		_scmGrid.setSelectionHandler(new SelectionHandler<ShoppingCartRef>() {

			@Override
			public void selected(ShoppingCartRef o) {
				_scPanel.setCart(o);
				_actionMenuButton.setMenu(ShoppingCartGUI.INSANCE.actionMenu(
						_sp.window(), o, new SelectedObjectSet() {

							@Override
							public List<ShoppingCartRef> selections() {
								return _scmGrid.selections();
							}
						}, false));
				_actionMenuButton.enable();
			}

			@Override
			public void deselected(ShoppingCartRef o) {
				_scPanel.setCart(null);
				_actionMenuButton.disable();

			}
		});

		ShoppingCartManager.addListener(this);

		setShowDetail(_showDetail);

		_showing = false;

	}

	private void setShowDetail(boolean showDetail) {
		if (showDetail) {
			showDetail();
		} else {
			hideDetail();
		}
	}

	private void showDetail() {
		VerticalSplitPanel vsp = new VerticalSplitPanel();
		vsp.fitToParent();

		_scmPanel.setWidth100();
		_scmPanel.setPreferredHeight(0.5);
		vsp.add(_scmPanel);

		_scPanel.setWidth100();
		_scPanel.setPreferredHeight(0.5);
		vsp.add(_scPanel);

		_sp.setContent(vsp);
		_showDetail = true;
		ButtonUtil.setButtonLabel(_showDetailButton, Resource.INSTANCE
				.detail24().getSafeUri().asString(), 16, 16, "Hide Detail");
	}

	private void hideDetail() {
		_scmPanel.fitToParent();
		_sp.setContent(_scmPanel);
		_showDetail = false;
		ButtonUtil.setButtonLabel(_showDetailButton, Resource.INSTANCE
				.detail24().getSafeUri().asString(), 16, 16, "Show Detail");
	}

	private void updateCartContent(ShoppingCartRef sc) {
		_scPanel.setCart(sc);
	}



	@Override
	public void activated(ShoppingCartRef sc) {
		_scmGrid.update(true);
	}

	@Override
	public void collectionModified() {
		_scmGrid.update(false);
	}

	@Override
	public void contentModified(ShoppingCartRef sc) {
		updateCartContent(sc);
	}

	@Override
	public void created(ShoppingCartRef sc) {
		_scmGrid.update(false);
	}

	@Override
	public void deactivated(ShoppingCartRef sc) {
		_scmGrid.update(true);
	}

	@Override
	public void destroyed(ShoppingCartRef sc) {
		_scmGrid.update(false);
	}

	@Override
	public void modified(ShoppingCartRef sc) {
		_scmGrid.update(false);
	}

	public void show(Window owner, double w, double h) {
		if (!_showing) {
			WindowProperties wp = new WindowProperties();
			wp.setModal(false);
			wp.setCanBeResized(true);
			wp.setCanBeClosed(true);
			wp.setCanBeMoved(true);
			wp.setSize(w, h);
			wp.setOwnerWindow(owner);
			wp.setTitle("Shopping Cart Manager");
			_win = Window.create(wp);
			_win.setContent(_sp);
			_win.addCloseListener(new WindowCloseListener() {
				@Override
				public void closed(Window w) {
					_showing = false;
				}
			});
			_win.centerInPage();
			_win.show();
			_showing = true;
		}
	}

	public void show(Window owner) {
		show(owner, 0.6, 0.5);
	}

	public void discard() {
		ShoppingCartManager.removeListener(this);
	}

	private static SCMD _instance;

	public static SCMD instance() {
		if (_instance == null) {
			_instance = new SCMD();
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
