package daris.client.ui.dicom;

import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.window.Window;
import arc.gui.window.WindowProperties;
import arc.mf.model.service.BackgroundService;
import arc.mf.model.service.BackgroundServiceMonitor;
import arc.mf.model.service.BackgroundServiceMonitorHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import daris.client.ui.widget.LoadingBar;

public class DicomSendMonitorDialog {

	public static final String ACTION = "Send";

	private long _id;

	private BackgroundServiceMonitor _bsm;

	private arc.gui.gwt.widget.window.Window _win;

	private LoadingBar _loadingBar;

	private SimplePanel _errorLogPanel;

	private Button _abortDismiss;

	private boolean _completed;

	private String _pid;

	private String _remoteAE;

	public DicomSendMonitorDialog(long id, String pid, String remoteAE,
			Window owner) {

		_id = id;
		_pid = pid;
		_remoteAE = remoteAE;
		_completed = false;
		VerticalPanel vp = new VerticalPanel();
		vp.fitToParent();
		_loadingBar = new LoadingBar(ACTION + "ing " + _pid + " to "
				+ _remoteAE);
		_loadingBar.setHeight(60);
		_loadingBar.setWidth100();
		vp.add(_loadingBar);

		_errorLogPanel = new SimplePanel();
		_errorLogPanel.fitToParent();
		vp.add(_errorLogPanel);

		vp.add(createButtonBar());

		WindowProperties wp = new WindowProperties();
		wp.setOwnerWindow(owner);
		wp.setTitle(ACTION + "ing DICOM(Service ID:" + _id + ")...");
		wp.setSize(380, 220);
		wp.setCanBeClosed(false);
		wp.setContent(vp);
		_win = arc.gui.gwt.widget.window.Window.create(wp);
		if (owner == null) {
			_win.centerInPage();
		} else {
			_win.centerOnOwner();
		}
	}

	public void show() {

		if (_bsm == null) {
			_bsm = new BackgroundServiceMonitor(_id,
					new BackgroundServiceMonitorHandler() {
						@Override
						public void checked(BackgroundService bs) {

							updateStatus(bs);
						}
					});

			_bsm.execute(1000);
			_win.show();
		}
	}

	public void hide() {

		if (_bsm == null) {
			return;
		}
		_bsm.cancel();
		_bsm = null;
		_win.hide();
	}

	private void updateStatus(BackgroundService bs) {

		if (bs.aborted()) {
			hide();
			return;
		}

		if (bs.failed()) {
			_completed = true;
			_win.setNotBusy();
			_loadingBar.failed("Failed. See error log below:");
			HTML errMsg = new HTML(bs.error());
			errMsg.setFontSize(10);
			_errorLogPanel
					.setContent(new ScrollPanel(errMsg, ScrollPolicy.AUTO));
			_errorLogPanel.setBackgroundColour("#f0f0f0");
			_errorLogPanel.setBorder(1, "#ddd");
			_abortDismiss.setText("Dismiss");
			_abortDismiss.setColour("red");
			_win.resizeBy(100, 100);
		} else if (bs.finished()) {
			_completed = true;
			_win.setNotBusy();
			_loadingBar.finished("Finished");
			_abortDismiss.setText("Dismiss");
			_abortDismiss.setToolTip("Close status monitor");
		} else if (bs.aborted()) {
			_completed = true;
			_win.setNotBusy();
			_loadingBar.failed("Aborted");
			_abortDismiss.setText("Dismiss");
			_abortDismiss.setColour("red");
		} else {
			if (bs.currentActivity() != null) {
				_loadingBar.setMessage(bs.currentActivity());
			} else {
				_loadingBar.setMessage(ACTION + "ing " + _pid + " to "
						+ _remoteAE + "...");
			}
		}
	}

	private ButtonBar createButtonBar() {

		ButtonBar bb = new ButtonBar(ButtonBar.Position.BOTTOM,
				ButtonBar.Alignment.CENTER);
		bb.setMarginTop(10);
		bb.setMarginBottom(5);
		bb.setButtonSpacing(10);
		_abortDismiss = bb.addButton("Abort");
		_abortDismiss.setToolTip("Stop sending");
		_abortDismiss.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				abortOrDismiss();
			}
		});
		/*
		 * _background = bb.addButton("Background"); _background.setToolTip(
		 * "Close this monitor and run the import in the background.");
		 * _background.addClickHandler(new ClickHandler() { public void
		 * onClick(ClickEvent event) { hide(); } });
		 */
		return bb;
	}

	private void abortOrDismiss() {

		if (_completed) {
			hide();
		} else {
			// Dialog.confirm(_win, StandardImages.QUESTION, "Confirm Abort",
			// "Please confirm you wish to abort the task.",
			// new ActionListener() {
			// public void executed(boolean succeeded) {
			//
			// if (succeeded) {
			_win.setBusy("Aborting..", null);
			BackgroundService.abort(_id);

//			BackgroundService.abort(_id, new AbortRequestResponse() {
//
//				@Override
//				public void abortRequested() {
//
//					_win.setNotBusy();
//					_completed = true;
//					_loadingBar.failed("Aborted");
//					_abortDismiss.setText("Dismiss");
//					_abortDismiss.setColour("red");
//				}
//			});
			// }
			// }
			// });
		}
	}
}