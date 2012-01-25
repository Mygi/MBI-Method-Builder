package daris.client.ui.dti;

import arc.gui.gwt.dnd.DragWidget;
import arc.gui.gwt.dnd.DropHandler;
import arc.gui.menu.ActionEntry;
import arc.mf.client.util.*;
import arc.gui.menu.Menu;
import arc.gui.object.SelectedObjectSet;
import arc.gui.object.display.ObjectDetailsDisplay;
import arc.gui.object.register.ObjectGUI;
import arc.gui.window.Window;
import arc.mf.client.dti.task.DTITask;

public class DTITaskGUI implements ObjectGUI {

	public static final DTITaskGUI INSTANCE = new DTITaskGUI();

	private DTITaskGUI() {

	}

	@Override
	public String idToString(Object o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String icon(Object o, int size) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Menu actionMenu(Window w, Object o, SelectedObjectSet selected,
			boolean readOnly) {
		final DTITask task = (DTITask) o;
		final ActionEntry abort = new ActionEntry("Abort", new Action() {

			@Override
			public void execute() {
				task.abort();
			}
		});
		abort.disable();
		final ActionEntry discard = new ActionEntry("Discard", new Action() {

			@Override
			public void execute() {
				task.discard();

			}
		});
		Menu menu = new Menu("Task " + task.id()) {
			public void preShow() {
				DTITask.State s = task.status();
				if (s == DTITask.State.INITIAL || s == DTITask.State.SUBMITTED
						|| s == DTITask.State.RUNNING) {
					abort.enable();
				} else {
					abort.softDisable("Cannot abort task " + task.id()
							+ ". Status: "
							+ task.status().toString().toLowerCase());
				}
				if (s.finished()) {
					discard.enable();
				} else {
					discard.softDisable("Cannot discard task " + task.id()
							+ ". Status: "
							+ task.status().toString().toLowerCase());
				}
			}
		};
		menu.add(abort);
		menu.add(discard);
		return menu;
	}

	@Override
	public Menu memberActionMenu(Window w, Object o,
			SelectedObjectSet selected, boolean readOnly) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object reference(Object o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean needToResolve(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void displayDetails(Object o, ObjectDetailsDisplay dd,
			boolean forEdit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void open(Window w, Object o) {
		// TODO Auto-generated method stub

	}

	@Override
	public DropHandler dropHandler(Object o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DragWidget dragWidget(Object o) {
		// TODO Auto-generated method stub
		return null;
	}

}
