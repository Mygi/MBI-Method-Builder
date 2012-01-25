package daris.client.ui.sc;

import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.image.Image;
import arc.gui.gwt.widget.menu.ActionMenu;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.menu.ActionEntry;
import arc.gui.menu.Menu;
import arc.mf.client.util.Action;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import daris.client.Resource;
import daris.client.ui.DObjectBrowser;

public class ShoppingCartTrayIcon extends ContainerWidget {
	public static arc.gui.image.Image ICON_SHOPPINGCART = new arc.gui.image.Image(
			Resource.INSTANCE.shoppingcart24().getSafeUri().asString(), 16, 16);
	public static final arc.gui.image.Image ICON_ACTIVE = new arc.gui.image.Image(
			Resource.INSTANCE.active24().getSafeUri().asString(), 16, 16);
	public static final arc.gui.image.Image ICON_MANAGER = new arc.gui.image.Image(
			Resource.INSTANCE.manager16().getSafeUri().asString(), 16, 16);

	private SimplePanel _sp;
	private Image _icon;

	private ShoppingCartTrayIcon() {
		_sp = new SimplePanel();
		_sp.setWidth(16);
		_sp.setHeight(16);
		_icon = new Image(ICON_SHOPPINGCART);
		_sp.setContent(_icon);
		_sp.setCursor(Cursor.POINTER);
		_sp.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				Menu menu = new Menu();
				menu.add(new ActionEntry(ICON_ACTIVE,
						"Show active shopping cart", new Action() {

							@Override
							public void execute() {
								ASCD.instance().show(window(), 0.6, 0.5);
							}
						}));
				menu.add(new ActionEntry(ICON_MANAGER,
						"Show shopping cart manager", new Action() {

							@Override
							public void execute() {
								SCMD.instance().show(window(), 0.6, 0.5);
							}
						}));
				ActionMenu am = new ActionMenu(menu);
				am.showAt(event.getNativeEvent());
			}
		});
		initWidget(_sp);
	}

	private static ShoppingCartTrayIcon _instance;

	public static ShoppingCartTrayIcon get() {
		if (_instance == null) {
			_instance = new ShoppingCartTrayIcon();
		}
		return _instance;
	}
}
