package daris.client.ui.dti;

import java.util.List;
import java.util.Vector;

import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.button.ButtonBar.Alignment;
import arc.gui.gwt.widget.button.ButtonBar.Position;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.format.WidgetFormatter;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.list.ListGridRowContextMenuHandler;
import arc.gui.gwt.widget.menu.ActionMenu;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.progress.ProgressBar;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.window.Window;
import arc.gui.gwt.widget.window.WindowCloseListener;
import arc.gui.menu.Menu;
import arc.gui.window.WindowProperties;
import arc.mf.client.dti.task.DTITask;
import arc.mf.client.dti.task.DTITaskSetStatusHandler;
import arc.mf.client.dti.task.DTITaskStatusHandler;
import arc.mf.client.util.DateTime;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.user.client.Timer;

import daris.client.Resource;
import daris.client.ui.DObjectGUIRegistry;
import daris.client.ui.util.ButtonUtil;

public class DTITaskManagerDialog {

	public static final double WIDTH = 0.5;
	public static final double HEIGHT = 0.3;
	public static final int DELAY = 1000;

	public static final String ICON_CLEAR = Resource.INSTANCE.clear16()
			.getSafeUri().asString();
	public static final String ICON_ABORT = Resource.INSTANCE.abort24()
			.getSafeUri().asString();
	public static final String ICON_DISCARD = Resource.INSTANCE.delete16()
			.getSafeUri().asString();

	private List<DTITask> _tasks;
	private VerticalPanel _vp;
	private ListGrid<DTITask> _grid;
	private Button _abortButton;
	private Button _discardButton;
	private Button _clearButton;
	private Window _win;
	private boolean _showing;

	private DTITaskManagerDialog() {

		_showing = false;
		_grid = new ListGrid<DTITask>(ScrollPolicy.AUTO);
		_grid.addColumnDefn("id", "ID").setWidth(40);
		_grid.addColumnDefn("type", "Type").setWidth(80);
		_grid.addColumnDefn("status", "Status", "Status",
				new WidgetFormatter<DTITask, DTITask.State>() {

					@Override
					public BaseWidget format(DTITask task, DTITask.State status) {
						final HTML html = new HTML(status.toString()
								.toLowerCase());
						html.setFontSize(10);
						html.fitToParent();
						task.monitor(DELAY, false,
								new DTITaskStatusHandler<DTITask>() {

									@Override
									public void status(Timer t, DTITask task) {
										if (task != null) {
											DTITask.State status = task
													.status();
											html.setHTML(status.toString()
													.toLowerCase());
										}
									}
								});
						return html;
					}
				}).setWidth(50);
		_grid.addColumnDefn("duration", "Duration", "Duration",
				new WidgetFormatter<DTITask, Long>() {

					@Override
					public BaseWidget format(DTITask task, Long duration) {
						final HTML html = new HTML(DateTime.durationAsString(
								duration, false, false));
						html.setFontSize(10);
						html.fitToParent();
						task.monitor(DELAY, false,
								new DTITaskStatusHandler<DTITask>() {

									@Override
									public void status(Timer t, DTITask task) {
										if (task != null) {
											html.setHTML(DateTime
													.durationAsString(
															task.duration(),
															false, false));
										}
									}
								});
						return html;
					}
				}).setWidth(60);
		_grid.addColumnDefn("progress", "Progress", "Progress",
				new WidgetFormatter<DTITask, Double>() {

					@Override
					public BaseWidget format(DTITask task, Double progress) {
						final ProgressBar pb = new ProgressBar(false);
						pb.setWidth("90%");
						task.monitor(DELAY / 10, false,
								new DTITaskStatusHandler<DTITask>() {

									@Override
									public void status(Timer t, DTITask task) {
										if (task != null) {
											pb.setProgress(task.progress() / 100);
											if (task.finished()) {
												if (task.status()
														.equals(DTITask.State.COMPLETED)) {
													pb.setProgress(1);
												}
											}
										}
									}
								});
						return pb;
					}
				}).setWidth(120);
		_grid.addColumnDefn("progressSummary", "Summary", "Summary",
				new WidgetFormatter<DTITask, String>() {

					@Override
					public BaseWidget format(DTITask task, String summary) {
						final HTML html = new HTML(summary);
						html.setFontSize(10);
						html.fitToParent();
						task.monitor(DELAY, false,
								new DTITaskStatusHandler<DTITask>() {

									@Override
									public void status(Timer t, DTITask task) {
										if (task != null) {
											html.setHTML(task.progressSummary());
										}
									}
								});
						return html;
					}
				}).setWidth(300);
		_grid.setEmptyMessage("");
		_grid.setLoadingMessage("");
		_grid.setMultiSelect(false);
		_grid.setObjectRegistry(DObjectGUIRegistry.get());
		_grid.setRowContextMenuHandler(new ListGridRowContextMenuHandler<DTITask>() {

			@Override
			public void show(DTITask t, final ContextMenuEvent event) {
				final int x = event.getNativeEvent().getClientX();
				final int y = event.getNativeEvent().getClientY();
				DTITask.status(t.id(), true, false,
						new DTITaskStatusHandler<DTITask>() {

							@Override
							public void status(Timer t, DTITask task) {
								if (task == null) {
									return;
								}
								Menu m = DObjectGUIRegistry.get().guiFor(task)
										.actionMenu(null, task, null, true);
								new ActionMenu(m).showAt(x, y);
							}
						});
			}
		});
		_grid.setSelectionHandler(new SelectionHandler<DTITask>() {

			@Override
			public void selected(DTITask t) {
				updateButtons(_tasks);
			}

			@Override
			public void deselected(DTITask t) {

			}
		});
		_grid.fitToParent();

		_vp = new VerticalPanel();
		_vp.fitToParent();

		_vp.add(_grid);

		ButtonBar bb = ButtonUtil.createButtonBar(Position.BOTTOM,
				Alignment.RIGHT, 28);

		_abortButton = ButtonUtil.createButton(ICON_ABORT, 16, 16, "Abort",
				null, false, new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						_abortButton.disable();
						if (_tasks == null) {
							return;
						}
						List<DTITask> selections = _grid.selections();
						if (selections == null) {
							return;
						}
						for (DTITask task : selections) {
							int idx = _tasks.indexOf(task);
							if (idx != -1) {
								DTITask t = _tasks.get(idx);
								if (!t.finished()
										&& t.status() != DTITask.State.ABORTING) {
									t.abort();
								}
							}
						}
					}
				});
		_abortButton.disable();
		bb.add(_abortButton);

		_discardButton = ButtonUtil.createButton(ICON_DISCARD, 16, 16,
				"Discard", null, false, new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						_discardButton.disable();
						if (_tasks == null) {
							return;
						}
						List<DTITask> selections = _grid.selections();
						if (selections == null) {
							return;
						}
						for (DTITask task : selections) {
							int idx = _tasks.indexOf(task);
							if (idx != -1) {
								DTITask t = _tasks.get(idx);
								if (t.finished()) {
									t.discard();
								}
							}
						}
					}
				});
		_discardButton.disable();
		bb.add(_discardButton);

		_clearButton = ButtonUtil.createButton(ICON_CLEAR, 16, 16, "Clear",
				null, false, new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						_clearButton.disable();
						if (_tasks != null) {
							for (DTITask task : _tasks) {
								if (task.finished()) {
									task.discard();
								}
							}
						}
					}
				});
		_clearButton.disable();
		bb.add(_clearButton);
		_vp.add(bb);

		DTITask.monitorAll(DELAY, new DTITaskSetStatusHandler() {

			@Override
			public void status(List<DTITask> tasks) {
				updateTasks(tasks);
				updateButtons(tasks);
			}
		});

	}

	private void updateButtons(List<DTITask> tasks) {
		_abortButton.disable();
		_discardButton.disable();
		_clearButton.disable();
		if (tasks == null) {
			return;
		}
		if (!tasks.isEmpty()) {
			boolean hasFinished = false;
			for (DTITask t : tasks) {
				if (t.finished()) {
					hasFinished = true;
					break;
				}
			}
			if (hasFinished) {
				_clearButton.enable();
			}
		}
		List<DTITask> selections = _grid.selections();
		if (selections != null) {
			if (!selections.isEmpty()) {
				for (DTITask task : selections) {
					int idx = tasks.indexOf(task);
					if (idx != -1) {
						DTITask t = tasks.get(idx);
						if (t.finished()) {
							_discardButton.enable();
						} else if (t.status() != DTITask.State.ABORTING) {
							_abortButton.enable();
						}
					}
				}
			}
		}
	}

	private void updateTasks(List<DTITask> tasks) {

		if (_tasks == tasks) {
			return;
		}
		if (tasks == null || _tasks == null) {
			_tasks = tasks;
			updateGrid(_tasks);
			return;
		}
		boolean taskCreated = false;
		for (DTITask task : tasks) {
			int idx = _tasks.indexOf(task);
			if (idx == -1) {
				taskCreated = true;
			} else {
				// update the elements in _tasks
				_tasks.set(idx, task);
			}
		}
		if (taskCreated) {
			// update the grid if there is new task.
			_tasks = tasks;
			updateGrid(_tasks);
			return;
		}
		boolean taskDestroyed = false;
		for (DTITask task : _tasks) {
			if (!tasks.contains(task)) {
				taskDestroyed = true;
				break;
			}
		}
		if (taskDestroyed) {
			// update grid if there is task destroyed.
			_tasks = tasks;
			updateGrid(_tasks);
			return;
		}
	}

	private void updateGrid(List<DTITask> tasks) {
		if (tasks == null) {
			_grid.setData(null);
			return;
		}
		List<ListGridEntry<DTITask>> es = new Vector<ListGridEntry<DTITask>>(
				tasks.size());
		for (DTITask task : tasks) {
			ListGridEntry<DTITask> e = new ListGridEntry<DTITask>(task);
			e.set("id", task.id());
			e.set("type", task.type());
			e.set("createTime", task.createTime());
			e.set("description", task.description());
			e.set("duration", task.duration());
			e.set("progress", task.progress());
			e.set("type", task.type());
			e.set("endTime", task.endTime());
			e.set("error", task.error());
			e.set("status", task.status());
			e.set("progressSummary", task.progressSummary());
			e.set("userFriendlyTypeName", task.userFriendlyTypeName());
			es.add(e);
		}
		_grid.setData(es);
	}

	public void show(Window owner) {

		if (!_showing) {
			WindowProperties wp = new WindowProperties();
			wp.setModal(false);
			wp.setTitle("DTI Tasks");
			wp.setCanBeResized(true);
			wp.setCanBeClosed(true);
			wp.setCanBeMoved(true);
			wp.setSize(WIDTH, HEIGHT);
			wp.setOwnerWindow(owner);
			_win = Window.create(wp);
			_win.addCloseListener(new WindowCloseListener() {

				@Override
				public void closed(Window w) {
					_showing = false;
				}
			});
			_win.setContent(_vp);
			_win.show();
			_win.centerInPage();
			_showing = true;
		}
	}

	private static DTITaskManagerDialog _instance;

	public static DTITaskManagerDialog instance() {
		if (_instance == null) {
			_instance = new DTITaskManagerDialog();
		}
		return _instance;
	}

}
