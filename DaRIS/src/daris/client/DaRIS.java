package daris.client;

import arc.mf.event.SystemEventChannel;
import arc.mf.model.asset.task.AssetTasks;
import arc.mf.model.shopping.events.ShoppingEvents;
import arc.mf.session.DefaultLoginDialog;
import arc.mf.session.LoginDialog;
import arc.mf.session.Session;
import arc.mf.session.SessionHandler;

import com.google.gwt.core.client.EntryPoint;

import daris.client.model.sc.ShoppingCartManager;
import daris.client.ui.DObjectBrowser;
import daris.client.ui.sc.ASCD;
import daris.client.ui.sc.SCMD;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class DaRIS implements EntryPoint {

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

//		Session.setAutoLogonCredentials("system", "manager", "Ask_me9");

		Session.setLoginTitle("DaRIS Logon");
		LoginDialog dlg = new DefaultLoginDialog();
		dlg.setVersion(Version.VERSION);
		dlg.setTitle("DaRIS");
		Session.setLoginDialog(dlg);
		Session.setLoginTitle("DaRIS");
		Session.initialize(new SessionHandler() {

			@Override
			public void sessionCreated(boolean initial) {

				initialize();
				DObjectBrowser.instance().show();

				// ShoppingCartManager.reset();
				// ShoppingCartManagerDialog.init();
				// ObjectBrowser.instance(true).show();
			}

			@Override
			public void sessionExpired() {

				terminate();
				// ObjectBrowser.reset();
			}

			@Override
			public void sessionTerminated() {

				terminate();
				// ObjectBrowser.reset();

			}
		});

	}

	private void initialize() {

		ShoppingEvents.initialize();
		AssetTasks.declare();
		SystemEventChannel.subscribe();
		ShoppingCartManager.initialize();
	}

	private void terminate() {
		SystemEventChannel.unsubscribe(false);
		ASCD.reset();
		SCMD.reset();
		ShoppingCartManager.reset();
		DObjectBrowser.reset();
	}
}
