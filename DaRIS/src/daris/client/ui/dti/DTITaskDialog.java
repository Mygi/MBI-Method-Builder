package daris.client.ui.dti;

import arc.gui.gwt.style.FontWeight;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.button.ButtonBar.Alignment;
import arc.gui.gwt.widget.button.ButtonBar.Position;
import arc.gui.gwt.widget.image.LinearGradient;
import arc.gui.gwt.widget.list.ListGridHeader;
import arc.gui.gwt.widget.panel.CenteringPanel;
import arc.gui.gwt.widget.panel.CenteringPanel.Axis;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.progress.ProgressBar;
import arc.gui.window.Window;
import arc.gui.window.WindowProperties;
import arc.mf.client.dti.task.DTITask;
import arc.mf.client.dti.task.DTITaskStatusHandler;
import arc.mf.client.util.DateTime;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;

import daris.client.Resource;
import daris.client.ui.util.ButtonUtil;

public class DTITaskDialog {

	public static final String ICON_ABORT = Resource.INSTANCE.abort24()
			.getSafeUri().asString();
	public static final String ICON_DISMISS = Resource.INSTANCE.active24()
			.getSafeUri().asString();
	public static final String ICON_BACKGROUND = Resource.INSTANCE
			.background24().getSafeUri().asString();

	public static final int DELAY = 1000;
	public final int WIDTH = 420;
	public final int HEIGHT = 190;
	private DTITask _task;
	private VerticalPanel _vp;
	private HTML _status;
	private HTML _duration;
	private ProgressBar _pb;
	private HTML _summary;
	private SimplePanel _bbSP;
	private Button _abortButton;
	private Button _backgroundButton;
	private Button _dismissButton;

	private arc.gui.gwt.widget.window.Window _win;

	public DTITaskDialog(DTITask task, Window owner) {

		_task = task;
		_vp = new VerticalPanel();
		_vp.setWidth100();
		/*
		 * status
		 */
		CenteringPanel cp = new CenteringPanel(Axis.HORIZONTAL);
		cp.setHeight(20);
		cp.setWidth100();
		cp.setMarginTop(10);
		_status = new HTML("Status: " + task.status().toString().toLowerCase());
		_status.setFontSize(11);
		_status.setFontWeight(FontWeight.BOLD);
		cp.add(_status);
		_vp.add(cp);
		/*
		 * progress bar
		 */
		cp = new CenteringPanel(Axis.HORIZONTAL);
		cp.setHeight(20);
		cp.setWidth100();
		cp.setMarginLeft(30);
		cp.setMarginRight(30);
		_pb = new ProgressBar(false);
		_pb.setHeight100();
		_pb.setWidth("80%");
		_pb.setProgress(task.progress() / 100);
		cp.add(_pb);
		_vp.add(cp);
		/*
		 * duration
		 */
		cp = new CenteringPanel(Axis.HORIZONTAL);
		cp.setHeight(20);
		cp.setWidth100();
		_duration = new HTML("Time elapsed: "
				+ DateTime.durationAsString(task.duration(), false, false));
		_duration.setFontSize(11);
		_duration.setFontWeight(FontWeight.BOLD);
		cp.add(_duration);
		_vp.add(cp);
		/*
		 * progress summary
		 */
		cp = new CenteringPanel(Axis.HORIZONTAL);
		cp.setHeight(20);
		cp.fitToParent();
		_summary = new HTML(task.progressSummary());
		_summary.setFontSize(11);
		cp.add(_summary);
		_vp.add(cp);

		/*
		 * button bar
		 */

		_abortButton = ButtonUtil.createButton(ICON_ABORT, 16, 16, "Abort",
				null, false, new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						_abortButton.disable();
						_task.abort();
					}
				});
		_abortButton.disable();

		_backgroundButton = ButtonUtil.createButton(ICON_BACKGROUND, 16, 16,
				"Background", null, false, new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						_win.close();
					}
				});
		_backgroundButton.setMarginRight(30);

		_dismissButton = ButtonUtil.createButton(ICON_DISMISS, 16, 16,
				"Dismiss", null, false, new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						_win.close();
						if (_task != null) {
							_task.discard();
						}
					}
				});
		_dismissButton.setMarginRight(30);

		_bbSP = new SimplePanel();
		_bbSP.setHeight(28);
		_bbSP.setWidth100();
		_vp.add(_bbSP);

		updateButtons(_task);

		_task.monitor(1000, false, new DTITaskStatusHandler<DTITask>() {

			@Override
			public void status(Timer t, DTITask task) {
				if (task == null) {
					return;
				}
				_status.setHTML("Status: "
						+ task.status().toString().toLowerCase());
				_duration.setHTML("Time elapsed: "
						+ DateTime.durationAsString(task.duration(), false,
								false));
				_pb.setProgress(task.progress() / 100);
				_summary.setHTML(task.progressSummary());
				updateButtons(task);
			}
		});

		if (owner != null) {
			show(owner);
		}
	}

	private void updateButtons(DTITask task) {
		ButtonBar bb = new ButtonBar(Position.BOTTOM, Alignment.RIGHT);
		bb.setHeight(28);
		bb.setWidth100();
		bb.setColourEnabled(false);
		bb.setBackgroundImage(new LinearGradient(
				LinearGradient.Orientation.TOP_TO_BOTTOM,
				ListGridHeader.HEADER_COLOUR_LIGHT,
				ListGridHeader.HEADER_COLOUR_DARK));
		if (task == null) {
			bb.add(_dismissButton);
			_bbSP.setContent(bb);
			return;
		}
		if (task.finished()) {
			bb.add(_dismissButton);
		} else {
			bb.add(_abortButton);
			bb.add(_backgroundButton);
			DTITask.State status = _task.status();
			if (status == DTITask.State.INITIAL
					|| status == DTITask.State.SUBMITTED
					|| status == DTITask.State.RUNNING
					|| status == DTITask.State.FAILED_RETRY) {
				_abortButton.enable();
			} else {
				_abortButton.disable();
			}
		}
		_bbSP.setContent(bb);
	}

	public void show(Window owner) {
		WindowProperties wp = new WindowProperties();
		wp.setModal(false);
		wp.setCanBeResized(true);
		wp.setCanBeClosed(false);
		wp.setTitle("DTI Task " + _task.id());
		wp.setCanBeMoved(true);
		wp.setSize(WIDTH, HEIGHT);
		wp.setOwnerWindow(owner);
		_win = arc.gui.gwt.widget.window.Window.create(wp);
		_win.setContent(_vp);
		_win.centerInPage();
		_win.show();
	}

}
