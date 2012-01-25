package daris.client.ui.object;

import java.util.List;
import java.util.Vector;

import arc.gui.form.FormEditMode;
import arc.gui.gwt.dnd.DragWidget;
import arc.gui.gwt.dnd.DropCheck;
import arc.gui.gwt.dnd.DropHandler;
import arc.gui.gwt.dnd.DropListener;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.menu.ActionMenu;
import arc.gui.image.Image;
import arc.gui.menu.ActionEntry;
import arc.gui.menu.Menu;
import arc.gui.object.SelectedObjectSet;
import arc.gui.object.action.ActionInterfaceEntry;
import arc.gui.object.display.ObjectDetailsDisplay;
import arc.gui.object.menu.ObjectMenu;
import arc.gui.object.register.ObjectGUI;
import arc.gui.window.Window;
import arc.mf.client.dti.file.DTIDirectory;
import arc.mf.client.dti.file.DTIFile;
import arc.mf.client.file.LocalFile;
import arc.mf.client.util.Action;
import arc.mf.client.util.ObjectUtil;
import arc.mf.object.ObjectResolveHandler;
import daris.client.Resource;
import daris.client.model.IDUtil;
import daris.client.model.exmethod.ExMethod;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.repository.Repository;
import daris.client.model.study.Study;
import daris.client.model.subject.Subject;
import daris.client.ui.object.action.AddToShoppingCartAction;
import daris.client.ui.object.action.DObjectAttachAction;
import daris.client.ui.object.action.DObjectCreateAction;
import daris.client.ui.object.action.DObjectEditAction;
import daris.client.ui.object.action.DerivationDataSetImportAction;
import daris.client.ui.object.action.DicomIngestAction;
import daris.client.ui.object.action.DicomSendAction;
import daris.client.ui.object.action.PrimaryDataSetImportAction;
import daris.client.util.StringUtil;

public class DObjectGUI implements ObjectGUI {
	public static final Image ICON_RELOAD = new Image(Resource.INSTANCE
			.refreshBlue16().getSafeUri().asString(), 16, 16);
	public static final Image ICON_CREATE = new Image(Resource.INSTANCE.add16()
			.getSafeUri().asString(), 16, 16);
	public static final Image ICON_EDIT = new Image(Resource.INSTANCE.edit16()
			.getSafeUri().asString(), 16, 16);
	public static final Image ICON_DICOM_SEND = new Image(Resource.INSTANCE
			.send16().getSafeUri().asString(), 16, 16);
	public static final Image ICON_DICOM_INGEST = new Image(Resource.INSTANCE
			.upload16().getSafeUri().asString(), 16, 16);
	public static final Image ICON_ADD_TO_SHOPPINGCART = new Image(
			Resource.INSTANCE.shoppingcart24().getSafeUri().asString(), 16, 16);

	public static final DObjectGUI INSANCE = new DObjectGUI();

	private DObjectGUI() {

	}

	@Override
	public String idToString(Object o) {

		if (o instanceof DObjectRef) {
			String id = ((DObjectRef) o).id();
			if (id == null) {
				id = "repository";
			} else {
				id = IDUtil.typeNameFromId(id) + " " + id;
			}
			return id;
		}
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

		return ((DObjectRef) o).needToResolve();
	}

	@Override
	public void displayDetails(Object o, final ObjectDetailsDisplay dd,
			final boolean forEdit) {

		final FormEditMode mode = forEdit ? FormEditMode.UPDATE
				: FormEditMode.READ_ONLY;
		final DObjectRef ro = ((DObjectRef) o);
		ro.setForEdit(forEdit);
		if (ro.resolved()) {
			dd.display(ro, DObjectDetails.detailsFor(ro.referent(), mode).gui());
		} else {
			ro.reset();
			ObjectResolveHandler<DObject> rh = new ObjectResolveHandler<DObject>() {
				@Override
				public void resolved(DObject oo) {

					if (oo != null) {
						dd.display(ro, DObjectDetails.detailsFor(oo, mode)
								.gui());
					}
				}
			};
			if (mode == FormEditMode.UPDATE) {
				ro.resolveAndLock(rh);
			} else {
				ro.resolve(rh);
			}
		}
	}

	@Override
	public void open(Window w, Object o) {

		// TODO Auto-generated method stub

	}

	@Override
	public DropHandler dropHandler(final Object o) {
		if (o != null) {
			if (o instanceof DObjectRef) {
				final DObjectRef oo = (DObjectRef) o;
				final String type = oo.referentTypeName();
				return new DropHandler() {

					@Override
					public DropCheck checkCanDrop(Object data) {
						if (ObjectUtil.equals(type, Repository.TYPE_NAME)) {
							return DropCheck.CANNOT;
						}
						if (data != null) {
							if (data instanceof DTIFile
									|| data instanceof DTIDirectory) {
								return DropCheck.CAN;
							}
						}
						return DropCheck.CANNOT;
					}

					@Override
					public void drop(BaseWidget target, List<Object> data,
							DropListener dl) {
						List<LocalFile> fs = new Vector<LocalFile>(data.size());
						for (Object d : data) {
							fs.add((LocalFile) d);
						}
						ObjectMenu<DObject> m = new ObjectMenu<DObject>(
								(DObjectRef) o);
						m.add(new DObjectAttachAction(fs, oo, target.window()));
						if (ObjectUtil.equals(type, Subject.TYPE_NAME)
								|| ObjectUtil.equals(type, ExMethod.TYPE_NAME)
								|| ObjectUtil.equals(type, Study.TYPE_NAME)) {
							m.add(new DicomIngestAction(fs, oo, target.window()));
						}
						if (ObjectUtil.equals(type, Study.TYPE_NAME)) {
							m.add(new PrimaryDataSetImportAction(fs, oo, target
									.window()));
							m.add(new DerivationDataSetImportAction(fs, oo,
									target.window()));
						}
						new ActionMenu(m).showAt(target.absoluteLeft(),
								target.absoluteBottom());
						dl.dropped(DropCheck.CAN);
					}
				};
			}
		}
		return null;
	}

	@Override
	public DragWidget dragWidget(Object o) {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Menu actionMenu(Window w, final Object o,
			SelectedObjectSet selected, boolean readOnly) {

		if (o == null) {
			return null;
		}
		DObjectRef ro = (DObjectRef) o;
		String title = idToString(o);

		ObjectMenu<DObject> menu = new ObjectMenu<DObject>(title);

		menu.setShowTitle(true);

		menu.addSeparator();

		menu.add(new ActionEntry(ICON_RELOAD, "Refresh " + title, "Refresh "
				+ title, new Action() {
			@Override
			public void execute() {

				((DObjectRef) o).refresh(true);
			}
		}, true));

		ActionInterfaceEntry ae;
		ae = new ActionInterfaceEntry(ICON_EDIT, new DObjectEditAction(
				(DObjectRef) o, w));
		menu.add(ae);

		ae = new ActionInterfaceEntry(ICON_CREATE, new DObjectCreateAction(
				(DObjectRef) o, w));
		menu.add(ae);

		ae = new ActionInterfaceEntry(ICON_ADD_TO_SHOPPINGCART,
				new AddToShoppingCartAction((DObjectRef) o, w));
		if (ro.id() != null) {
			// not repository node.
			menu.add(ae);
		}

		ae = new ActionInterfaceEntry(ICON_DICOM_SEND, new DicomSendAction(
				(DObjectRef) o, w));
		menu.add(ae);

		return menu;
	}

	@Override
	public Menu memberActionMenu(Window w, Object o,
			SelectedObjectSet selected, boolean readOnly) {
		// TODO Auto-generated method stub
		return null;
	}

}
