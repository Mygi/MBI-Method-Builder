package daris.client.ui.user;

import arc.gui.gwt.dnd.DragWidget;
import arc.gui.gwt.dnd.DropHandler;
import arc.gui.gwt.widget.label.Label;
import arc.gui.menu.Menu;
import arc.gui.object.SelectedObjectSet;
import arc.gui.object.display.ObjectDetailsDisplay;
import arc.gui.object.register.ObjectGUI;
import arc.gui.window.Window;
import daris.client.model.user.RoleUser;
import daris.client.model.user.User;

public class RoleUserGUI implements ObjectGUI {

	public static final RoleUserGUI INSTANCE = new RoleUserGUI();

	private RoleUserGUI() {

	}

	@Override
	public String idToString(Object o) {

		return ((User) o).id();
	}

	@Override
	public String icon(Object o, int size) {

		return null;
	}

	@Override
	public Object reference(Object o) {

		return null;
	}

	@Override
	public boolean needToResolve(Object o) {

		return false;
	}

	@Override
	public void displayDetails(Object o, ObjectDetailsDisplay dd,
			boolean forEdit) {

	}

	@Override
	public void open(Window w, Object o) {

	}

	@Override
	public DropHandler dropHandler(Object o) {

		return null;
	}

	@Override
	public DragWidget dragWidget(Object o) {

		RoleUser ru = (RoleUser) o;
		return new DragWidget(RoleUser.TYPE_NAME, new Label(ru.member()));
	}

	@Override
	public Menu actionMenu(Window w, Object o, SelectedObjectSet selected,
			boolean readOnly) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Menu memberActionMenu(Window w, Object o,
			SelectedObjectSet selected, boolean readOnly) {
		// TODO Auto-generated method stub
		return null;
	}

}