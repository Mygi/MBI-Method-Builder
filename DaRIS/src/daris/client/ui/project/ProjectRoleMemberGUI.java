package daris.client.ui.project;

import daris.client.model.project.ProjectMember;
import daris.client.model.project.ProjectRoleMember;
import arc.gui.gwt.dnd.DragWidget;
import arc.gui.gwt.dnd.DropHandler;
import arc.gui.gwt.widget.label.Label;
import arc.gui.menu.Menu;
import arc.gui.object.SelectedObjectSet;
import arc.gui.object.display.ObjectDetailsDisplay;
import arc.gui.object.register.ObjectGUI;
import arc.gui.window.Window;

public class ProjectRoleMemberGUI implements ObjectGUI {

	public static final ProjectRoleMemberGUI INSTANCE = new ProjectRoleMemberGUI();

	private ProjectRoleMemberGUI() {

	}

	@Override
	public String idToString(Object o) {

		return ((ProjectMember) o).toString();
	}

	@Override
	public String icon(Object o, int size) {

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

		ProjectRoleMember prm = (ProjectRoleMember) o;

		return new DragWidget("role-member", new Label(prm.member().member()));
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
