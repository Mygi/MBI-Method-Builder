package daris.client.ui.dti;

import arc.gui.InterfaceComponent;
import arc.gui.gwt.widget.menu.ActionMenu;
import arc.gui.image.Image;
import arc.gui.menu.ActionEntry;
import arc.gui.menu.Menu;
import arc.mf.client.dti.DTI;
import arc.mf.client.dti.DTIApplet;
import arc.mf.client.util.Action;
import arc.mf.session.Session;
import arc.mf.session.SessionHandler;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import daris.client.Resource;
import daris.client.ui.widget.MessageBox;

public class DTIManager implements arc.mf.client.dti.DTIAppletStatusMonitor,
		InterfaceComponent {

	public static final Image ICON_DTI_ENABLED = new Image(Resource.INSTANCE
			.connect16().getSafeUri().asString(), 16, 16);
	public static final Image ICON_DTI_DISABLED = new Image(Resource.INSTANCE
			.disconnect16().getSafeUri().asString(), 16, 16);

	public static final DTIManager INSTANCE = new DTIManager();

	private arc.gui.gwt.widget.image.Image _trayIcon;

	private String _lastMessage;

	private DTIManager() {

		/*
		 * 
		 */
		_trayIcon = new arc.gui.gwt.widget.image.Image(ICON_DTI_ENABLED);
		_trayIcon.setDisabledImage(ICON_DTI_DISABLED);
		_trayIcon.setEnabledImage(ICON_DTI_ENABLED);
		_trayIcon.setCursor(Cursor.POINTER);
		_trayIcon.disable();

		_trayIcon.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				Menu menu = new Menu("DTI: active");
				menu.setShowTitle(true);
				menu.add(new ActionEntry("Show DTI tasks", new Action() {

					@Override
					public void execute() {

						DTITaskManagerDialog.instance()
								.show(_trayIcon.window());
					}
				}));
				new ActionMenu(menu).showAt(event.getNativeEvent());
			}
		});

		/*
		 * Install it
		 */
		if (Session.created()) {
			DTI.install(this);
		} else {
			Session.addSessionHandler(new SessionHandler() {

				@Override
				public void sessionCreated(boolean initial) {

					DTI.install(DTIManager.this);

				}

				@Override
				public void sessionExpired() {

				}

				@Override
				public void sessionTerminated() {

				}
			});
		}
	}

	@Override
	public boolean appletIsNotReady(DTIApplet a) {

		return true;
	}

	@Override
	public void appletIsReady(DTIApplet a) {

		_lastMessage = "Applet is ready.";
		MessageBox.display("DTI Applet", _lastMessage, 3);
	}

	@Override
	public void appletFailedToLoad(DTIApplet a, String reason) {

		_lastMessage = "Applet failed to load: " + reason;
		MessageBox.display("DTI Applet", _lastMessage, 3);
	}

	@Override
	public void agentIsReady(DTIApplet a, int port) {

		_trayIcon.enable();
		_lastMessage = "Agent is ready.";
		MessageBox.display("DTI Agent", _lastMessage, 3);

	}

	@Override
	public boolean agentIsNotReady(DTIApplet a) {

		return true;
	}

	@Override
	public void agentFailedToStart(DTIApplet a, String reason) {

		_lastMessage = "Agent failed to start: " + reason;
		MessageBox.display("DTI Agent", _lastMessage, 3);
	}

	public Widget gui() {

		return _trayIcon;
	}

}
