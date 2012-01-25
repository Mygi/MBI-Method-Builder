package daris.client.ui.dti.file;

import arc.gui.gwt.dnd.DragWidget;
import arc.gui.gwt.dnd.DropHandler;
import arc.gui.gwt.widget.label.Label;
import arc.gui.menu.Menu;
import arc.gui.object.SelectedObjectSet;
import arc.gui.object.display.ObjectDetailsDisplay;
import arc.gui.object.register.ObjectGUI;
import arc.gui.window.Window;
import arc.mf.client.file.LocalFile;

public class DTIDirectoryGUI implements ObjectGUI {

	public static final DTIDirectoryGUI INSTANCE = new DTIDirectoryGUI();

	private DTIDirectoryGUI() {

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

		LocalFile f = (LocalFile) o;
		return new DragWidget("Local "
				+ (f.isDirectory() ? "directory" : "file"), new Label(f.path()));
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
