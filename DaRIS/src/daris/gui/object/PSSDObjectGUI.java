package daris.gui.object;

import arc.gui.gwt.dnd.DragWidget;
import arc.gui.gwt.dnd.DropHandler;
import arc.gui.menu.ActionEntry;
import arc.gui.menu.Menu;
import arc.gui.object.SelectedObjectSet;
import arc.gui.object.display.ObjectDetailsDisplay;
import arc.gui.window.Window;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.dicom.messages.DicomDataSetCount;
import daris.gui.ObjectBrowser;
import daris.model.object.PSSDObjectRef;
import daris.model.repository.RepositoryRootRef;

public class PSSDObjectGUI implements arc.gui.object.register.ObjectGUI {

	public static arc.gui.object.register.ObjectGUI instance() {

		if (_instance == null) {
			_instance = new PSSDObjectGUI();
		}
		return _instance;
	}

	private static arc.gui.object.register.ObjectGUI _instance;

	private PSSDObjectGUI() {

	}

	@Override
	public String idToString(Object o) {

		if (o != null) {
			if (o instanceof PSSDObjectRef) {
				return ((PSSDObjectRef) o).id();
			}
		}
		return null;
	}

	@Override
	public String icon(Object o, int size) {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Menu actionMenu(Window w, final Object o, SelectedObjectSet selected, boolean readOnly) {

		if (o != null) {
			if (o instanceof PSSDObjectRef) {
				final ActionEntry aeDicomSend = new ActionEntry("Send DICOM datasets...",
						ObjectBrowser.Actions.dicomSendAction);
				aeDicomSend.disable();
				Menu menu = new Menu(((PSSDObjectRef) o).referentTypeName()) {
					public void preShow() {

						if (!(o instanceof RepositoryRootRef)) {
							new DicomDataSetCount(((PSSDObjectRef)o).id()).send(new ObjectMessageResponse<Integer>() {

								@Override
								public void responded(Integer r) {

									if (r != null) {
										if (r > 0) {
											aeDicomSend.enable();
										}
									}
								}
							});
						}
					}
				};
				menu.add(new ActionEntry("Add to shopping-cart", ObjectBrowser.Actions.addToShoppingCartAction));
				menu.add(new ActionEntry("Add to shopping-cart interactively...",
						ObjectBrowser.Actions.addToShoppingCartInteractively));
				menu.add(aeDicomSend);

				return menu;
			}
		}
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
	public void displayDetails(Object o, ObjectDetailsDisplay dd, boolean forEdit) {

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

	@Override
	public Menu memberActionMenu(Window w, Object o,
			SelectedObjectSet selected, boolean readOnly) {
		// TODO Auto-generated method stub
		return null;
	}

}
