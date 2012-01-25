package daris.client.ui;

import arc.gui.gwt.object.ObjectDetailedView;
import arc.gui.gwt.object.ObjectEventHandler;
import arc.gui.gwt.object.ObjectNavigator;
import arc.gui.gwt.object.ObjectNavigator.DisplayOn;
import arc.gui.gwt.object.ObjectNavigatorSelectionHandler;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.menu.MenuButton;
import arc.gui.gwt.widget.menu.MenuToolBar;
import arc.gui.gwt.widget.panel.HorizontalSplitPanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.window.Window;
import arc.gui.menu.ActionEntry;
import arc.gui.menu.Menu;
import arc.gui.window.WindowProperties;
import arc.mf.client.util.Action;
import arc.mf.client.util.ActionListener;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.object.tree.Node;
import arc.mf.object.tree.NodeListener;
import arc.mf.session.Session;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;

import daris.client.Resource;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.tree.DObjectTree;
import daris.client.model.object.tree.DObjectTreeNode;
import daris.client.model.repository.RepositoryRef;
import daris.client.ui.dti.DTIManager;
import daris.client.ui.dti.file.LocalFileBrowser;
import daris.client.ui.object.DObjectGUI;
import daris.client.ui.sc.ASCD;
import daris.client.ui.sc.SCMD;
import daris.client.ui.sc.ShoppingCartTrayIcon;
import daris.client.ui.user.SelfPasswordSetDialog;
import daris.client.ui.util.ButtonUtil;

public class DObjectBrowser {

	public static final arc.gui.image.Image ICON_RELOAD = new arc.gui.image.Image(
			Resource.INSTANCE.reload24().getSafeUri().asString(), 16, 16);
	public static final arc.gui.image.Image ICON_CREATE = new arc.gui.image.Image(
			Resource.INSTANCE.create24().getSafeUri().asString(), 16, 16);
	public static final arc.gui.image.Image ICON_EDIT = new arc.gui.image.Image(
			Resource.INSTANCE.edit24().getSafeUri().asString(), 16, 16);
	public static final arc.gui.image.Image ICON_SHOPPINGCART = new arc.gui.image.Image(
			Resource.INSTANCE.shoppingcart24().getSafeUri().asString(), 16, 16);
	public static final arc.gui.image.Image ICON_USER = new arc.gui.image.Image(
			Resource.INSTANCE.user24().getSafeUri().asString(), 16, 16);
	public static final arc.gui.image.Image ICON_LOGOUT = new arc.gui.image.Image(
			Resource.INSTANCE.logout24().getSafeUri().asString(), 16, 16);
	public static final arc.gui.image.Image ICON_IMPORT = new arc.gui.image.Image(
			Resource.INSTANCE.import24().getSafeUri().asString(), 16, 16);
	public static final arc.gui.image.Image ICON_FILE_EXPLORER = new arc.gui.image.Image(
			Resource.INSTANCE.fileExplorer16().getSafeUri().asString(), 16, 16);
	public static final arc.gui.image.Image ICON_ACTION = new arc.gui.image.Image(
			Resource.INSTANCE.action16().getSafeUri().asString(), 16, 16);
	public static final arc.gui.image.Image ICON_PASSWORD = new arc.gui.image.Image(
			Resource.INSTANCE.key16().getSafeUri().asString(), 16, 16);
	public static final arc.gui.image.Image ICON_ACTIVE = new arc.gui.image.Image(
			Resource.INSTANCE.active24().getSafeUri().asString(), 16, 16);
	public static final arc.gui.image.Image ICON_MANAGER = new arc.gui.image.Image(
			Resource.INSTANCE.manager16().getSafeUri().asString(), 16, 16);

	private VerticalPanel _vp;
	private MenuToolBar _tb;
	private MenuButton _actionMenuButton;
	private ObjectNavigator _ov;
	private ObjectDetailedView _dv;
	private NodeListener _nl;
	private Window _win;

	private DObjectBrowser() {

		_vp = new VerticalPanel();
		_vp.fitToParent();

		_tb = new MenuToolBar();
		_tb.setHeight(28);

		Button reloadButton = ButtonUtil.createButton(ICON_RELOAD, "Reload",
				"Reload the repository", false, new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						Dialog.confirm(
								"Reload repository",
								"Are you sure you want to reload the repository?",
								new ActionListener() {

									@Override
									public void executed(boolean succeeded) {
										if (succeeded) {
											_ov.select(RepositoryRef.INSTANCE);
											RepositoryRef.INSTANCE
													.refresh(true);
										}
									}
								});
					}
				});
		_tb.add(reloadButton);

		_actionMenuButton = ButtonUtil.createMenuButton(ICON_ACTION, "Action",
				null);
		_tb.add(_actionMenuButton);

		Menu downloadMenu = new Menu("Download");
		downloadMenu.add(new ActionEntry(ICON_ACTIVE,
				"Show active shopping cart...", new Action() {

					@Override
					public void execute() {
						ASCD.instance().show(
								DObjectBrowser.instance().window(), 0.6, 0.5);
					}
				}));
		downloadMenu.add(new ActionEntry(ICON_MANAGER,
				"Show shopping cart manager...", new Action() {

					@Override
					public void execute() {
						SCMD.instance().show(
								DObjectBrowser.instance().window(), 0.6, 0.5);
					}
				}));

		downloadMenu.setShowTitle(false);
		MenuButton downloadButton = ButtonUtil.createMenuButton(
				ICON_SHOPPINGCART, downloadMenu.label(), downloadMenu);
		_tb.add(downloadButton);

		Menu importMenu = new Menu("Import");
		importMenu.add(new ActionEntry(ICON_FILE_EXPLORER,
				"Show local files...", "Show local file browser", new Action() {

					@Override
					public void execute() {
						LocalFileBrowser.instance().show(window());
					}
				}, true));
		importMenu.setShowTitle(false);

		MenuButton importButton = ButtonUtil.createMenuButton(ICON_IMPORT,
				importMenu.label(), importMenu);
		_tb.add(importButton);

		Menu userMenu = new Menu("User");
		userMenu.add(new ActionEntry(ICON_PASSWORD, "Change Password",
				new Action() {

					@Override
					public void execute() {
						SelfPasswordSetDialog.show(window());
					}
				}));
		MenuButton userButton = ButtonUtil.createMenuButton(ICON_USER,
				userMenu.label(), userMenu);
		_tb.add(userButton);

		Button _logOutButton = ButtonUtil.createButton(ICON_LOGOUT, "Log out",
				"Log out DaRIS", false, new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						Session.logoff(true);
					}
				});
		_tb.add(_logOutButton);

		_vp.add(_tb);

		_dv = new ObjectDetailedView(ScrollPolicy.NONE);
		_dv.setDisplayLoadingMessage(true);
		_dv.setForEdit(false);
		_dv.setObjectRegistry(DObjectGUIRegistry.get());
		_dv.fitToParent();

		_nl = new NodeListener() {

			@Override
			public void added(Node pn, Node n, int idx) {

			}

			@Override
			public void removed(Node pn, Node n) {

			}

			@Override
			public void modified(Node n) {

				if (n != null) {
					DObjectTreeNode dn = (DObjectTreeNode) n;
					if (_dv.displaying(dn.object())) {
						_dv.loadAndDisplayObject(dn.object());
					}
				}
			}

			@Override
			public void changeInMembers(Node n) {

			}
		};
		_ov = new ObjectNavigator(new DObjectTree(),
				new ObjectNavigatorSelectionHandler() {

					@Override
					public void clickedObject(Node n, Object o, boolean readOnly) {

						/*
						 * [1] called when a (new) node is clicked while
						 * selected node has not changed.
						 */
					}

					@Override
					public void selectedObject(Node n, Object o,
							boolean readOnly) {

						// loadAndDisplayObject() right before this method is
						// called.
						/*
						 * [3] called after deselectedObject()
						 */
						if (o != null) {
							objectSelected((DObjectRef) o);
						}
						if (n != null) {
							// start listening to the current selected node.
							n.subscribe(true, _nl);
						}
					}

					@Override
					public void deselectedObject(Node n, Object o) {

						/*
						 * [2] called after clickedObject()
						 */
						// cancel the ObjectRef because another node is clicked
						if (o != null) {
							((DObjectRef) o).cancel();
							objectDeselected((DObjectRef) o);
						}
						if (n != null) {
							// stop listening to the deselected node.
							n.unsubscribe(_nl);
						}
					}
				}, new ObjectEventHandler() {

					@Override
					public void added(Object o) {

					}

					@Override
					public void modified(Object o) {

					}

					@Override
					public void changeInMembers(Object o) {

					}

					@Override
					public void removed(Object o) {

					}
				});

		_ov.setDisplayObjectOn(DisplayOn.SELECT);
		_ov.setPreferredWidth(0.4);
		_ov.setHeight100();
		_ov.setObjectDetailView(_dv);

		RepositoryRef.INSTANCE.resolve(new ObjectResolveHandler<DObject>() {

			@Override
			public void resolved(DObject o) {

				_ov.select(RepositoryRef.INSTANCE);
			}
		});

		HorizontalSplitPanel hsp = new HorizontalSplitPanel();
		hsp.add(_ov);
		hsp.add(_dv);
		hsp.fitToParent();

		_vp.add(hsp);

		WindowProperties wp = new WindowProperties();
		wp.setModal(false);
		wp.setCanBeResized(true);
		wp.setCanBeClosed(false);
		wp.setCanBeMoved(false);
		wp.setCentered(true);
		wp.setShowFooter(true);
		wp.setTitle("DaRIS");
		wp.setSize(1.0, 1.0);
		// wp.setSize(com.google.gwt.user.client.Window.getClientWidth(),
		// com.google.gwt.user.client.Window.getClientHeight());
		_win = Window.create(wp);
		_win.setContent(_vp);

		/*
		 * add DTI monitor tray icon inside the window footer
		 */
		BaseWidget dtiTrayIcon = (BaseWidget) DTIManager.INSTANCE.gui();
		dtiTrayIcon.setPosition(Position.ABSOLUTE);
		dtiTrayIcon.setRight(20);
		dtiTrayIcon.setTop(1);
		_win.footer().add(dtiTrayIcon);

		/*
		 * add shopping cart tray icon
		 */
		ShoppingCartTrayIcon scti = ShoppingCartTrayIcon.get();
		scti.setPosition(Position.ABSOLUTE);
		scti.setRight(40);
		scti.setTop(1);
		_win.footer().add(scti);

		// TODO: simplify.
		com.google.gwt.user.client.Window
				.addResizeHandler(new com.google.gwt.event.logical.shared.ResizeHandler() {

					@Override
					public void onResize(ResizeEvent event) {

						// Fit to browser window
						_win.setPosition(0, 0);
						int dw = event.getWidth() - _win.width();
						int dh = event.getHeight() - _win.height();
						_win.resizeBy(dw, dh);
						// _win.resizeTo(com.google.gwt.user.client.Window.getClientWidth(),
						// com.google.gwt.user.client.Window.getClientHeight());
					}
				});
	}

	private void objectSelected(DObjectRef o) {

		Menu menu = o == null ? new Menu() : DObjectGUI.INSANCE.actionMenu(
				window(), o, null, false);
		_actionMenuButton.setMenu(menu);
	}

	private void objectDeselected(DObjectRef o) {

	}

	public Window window() {
		return _win;
	}

	private static DObjectBrowser _instance;

	public static DObjectBrowser instance() {

		if (_instance == null) {
			_instance = new DObjectBrowser();
		}
		return _instance;
	}

	public void show() {

		_win.centerInPage();
		_win.show();
	}

	public static void reset() {
		if (_instance != null) {
			_instance.window().close();
		}
		RepositoryRef.INSTANCE.childrenRef().reset();
		_instance = null;
	}

}
