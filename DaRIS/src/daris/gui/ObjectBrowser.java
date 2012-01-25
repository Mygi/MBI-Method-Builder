package daris.gui;

import arc.gui.dialog.DialogCallback;
import arc.gui.dialog.DialogCloseAction;
import arc.gui.dialog.DialogFactory;
import arc.gui.form.FormEditMode;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.menu.MenuButton;
import arc.gui.gwt.widget.menu.MenuToolBar;
import arc.gui.gwt.widget.panel.HorizontalSplitPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.window.Window;
import arc.gui.menu.ActionEntry;
import arc.gui.menu.Menu;
import arc.gui.object.action.ActionInterface;
import arc.gui.window.WindowProperties;
import arc.mf.client.util.Action;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.event.SystemEventChannel;
import arc.mf.object.ObjectMessage;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.session.Session;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Timer;

import daris.client.Resource;
import daris.client.model.dicom.messages.DicomDataSetCount;
import daris.client.ui.dicom.DicomSendForm;
import daris.client.ui.dti.DTIManager;
import daris.gui.object.ObjectPanel;
import daris.gui.object.tree.ObjectTreeGUI;
import daris.gui.sc.ShoppingCartContentSelectDialog;
import daris.gui.sc.ShoppingCartDialog;
import daris.gui.sc.ShoppingCartManagerDialog;
import daris.gui.user.UserPasswordChangeDialog;
import daris.model.exmethod.ExMethodRef;
import daris.model.object.PSSDObjectRef;
import daris.model.object.PSSDObjectRefSet;
import daris.model.object.tree.ObjectTree;
import daris.model.object.tree.ObjectTreeNode;
import daris.model.project.ProjectRef;
import daris.model.repository.RepositoryRootRef;
import daris.model.sc.ShoppingCartManager;
import daris.model.study.StudyRef;
import daris.model.subject.SubjectRef;
import daris.model.user.messages.UserCanCreate;
import daris.model.user.messages.UserCanEdit;

public class ObjectBrowser {

	private static ObjectBrowser _instance;

	public static ObjectBrowser instance(boolean reset) {

		if (reset) {
			reset();
		}
		if (_instance == null) {
			_instance = new ObjectBrowser();
		}
		return _instance;
	}

	public static ObjectBrowser instance() {

		return instance(false);
	}

	public static void reset() {

		if (_instance != null) {
			_instance.close();
		}
		_instance = null;
	}

	private Window _win;

	private MenuToolBar _menuBar;

	private Button _reloadButton;

	private Button _createButton;

	private Button _editButton;

	private MenuButton _downloadMenuButton;

	private MenuButton _userMenuButton;

	private MenuButton _helpMenuButton;

	private SimplePanel _treePanel;

	private ObjectTreeGUI _treeGUI;

	private ObjectPanel _op;

	private ObjectBrowser() {

		VerticalPanel vp = new VerticalPanel();
		vp.setHeight100();
		vp.setWidth100();
		initMenuBar();
		vp.add(_menuBar);

		HorizontalSplitPanel hsp = new HorizontalSplitPanel();
		hsp.fitToParent();
		_treePanel = new SimplePanel();
		_treePanel.setPreferredWidth(0.3);
		_treePanel.setHeight100();
		_treeGUI = new ObjectTreeGUI() {
			protected void onSelected(ObjectTreeNode n) {

				updateButtonStates((PSSDObjectRef) n.object());
			}
		};
		_treeGUI.fitToParent();
		_treePanel.setContent(_treeGUI);
		hsp.add(_treePanel);
		_op = new ObjectPanel();
		hsp.add(_op);
		vp.add(hsp);

		WindowProperties wp = new WindowProperties();
		wp.setModal(false);
		wp.setCanBeResized(false);
		wp.setCanBeClosed(false);
		wp.setCanBeMoved(false);
		wp.setCentered(true);
		wp.setTitle("DaRIS - Distributed and Reflective Informatics System");
		wp.setPosition(0, 0);
		wp.setShowFooter(true);
		wp.setShowHeader(true);
		wp.setSize(com.google.gwt.user.client.Window.getClientWidth(),
				com.google.gwt.user.client.Window.getClientHeight());
		_win = Window.create(wp);
		_win.setContent(vp);
		_win.footer().add(DTIManager.INSTANCE.gui());

		com.google.gwt.user.client.Window
				.addResizeHandler(new com.google.gwt.event.logical.shared.ResizeHandler() {

					@Override
					public void onResize(ResizeEvent event) {

						// Fit to browser window
						_win.setPosition(0, 0);
						int dw = event.getWidth() - _win.width();
						int dh = event.getHeight() - _win.height();
						_win.resizeBy(dw, dh);

					}
				});

	}

	public PSSDObjectRef selectedObject() {

		return tree().getSelectedObject();
	}

	ObjectTree tree() {

		return (ObjectTree) _treeGUI.tree();
	}

	ObjectTreeGUI treeGUI() {

		return _treeGUI;
	}

	private void updateCreateButtonState(PSSDObjectRef o) {

		if (o instanceof RepositoryRootRef || o instanceof ProjectRef
				|| o instanceof ExMethodRef) {
			ObjectMessage<Boolean> msg = new UserCanCreate(o);
			msg.send(new ObjectMessageResponse<Boolean>() {

				@Override
				public void responded(Boolean canCreate) {

					if (canCreate) {
						_createButton.enable();
					}
				}
			});
		} else {
			_createButton.disable();
		}
	}

	private void updateEditButtonState(PSSDObjectRef o) {

		if (o instanceof ProjectRef || o instanceof SubjectRef
				|| o instanceof StudyRef) {
			ObjectMessage<Boolean> msg = new UserCanEdit(o);
			msg.send(new ObjectMessageResponse<Boolean>() {

				@Override
				public void responded(Boolean canEdit) {

					if (canEdit) {
						_editButton.enable();
					}
				}
			});
		} else {
			_editButton.disable();
		}
	}

	private void updateButtonStates(PSSDObjectRef o) {

		_createButton.disable();
		_editButton.disable();
		updateCreateButtonState(o);
		updateEditButtonState(o);
	}

	private Button createButton(ImageResource ir, String text, int width) {

		Button btn = new Button(
				"<div style=\"cursor: pointer\"><img width=\""
						+ ir.getWidth()
						+ "px\" height=\""
						+ ir.getHeight()
						+ "px\" src=\""
						+ ir.getSafeUri().asString()
						+ "\" style=\"vertical-align: top; padding-top: 0px;\"></img>&nbsp;"
						+ text + "</div>", false);
		// btn.setWidth(width);
		return btn;
	}

	private MenuButton createMenuButton(ImageResource ir, Menu menu, int width) {

		MenuButton menuButton = new MenuButton(menu);
		menuButton
				.setHTML("<div style=\"cursor: pointer\"><img width=\""
						+ ir.getWidth()
						+ "px\" height=\""
						+ ir.getHeight()
						+ "px\" src=\""
						+ ir.getSafeUri().asString()
						+ "\" style=\"vertical-align: top; padding-top: 0px;\"></img>&nbsp;"
						+ menu.label()
						+ "<img width=\"16\" height=\"11\" src=\""
						+ MenuButton.PULLDOWN_ICON.url() + "\"></img></div>");
		menuButton.setWidth(width);
		return menuButton;
	}

	private void initMenuBar() {

		/*
		 * Creates menu bar;
		 */
		_menuBar = new MenuToolBar();
		_menuBar.setHeight(28);
		_menuBar.setWidth100();
		_menuBar.setFontSize(11);
		_menuBar.setBackgroundColour("#F0F0F0");
		_menuBar.setPaddingTop(2);
		_menuBar.setPaddingBottom(5);

		/*
		 * Create refresh button;
		 */
		_reloadButton = createButton(Resource.INSTANCE.refreshBlue16(),
				"Reload", 80);
		_reloadButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				final Timer timer = new Timer() {
					@Override
					public void run() {

						_reloadButton.enable();
						cancel();
					}
				};
				timer.schedule(3000);
				_reloadButton.disable();
				Actions.reloadAction.execute();
			}
		});
		_menuBar.add(_reloadButton);

		/*
		 * Create create button;
		 */
		_createButton = createButton(Resource.INSTANCE.add16(), "Create", 80);
		_createButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				Actions.createAction.execute();
			}
		});
		_createButton.disable();
		_menuBar.add(_createButton);

		/*
		 * Create edit button;
		 */
		_editButton = createButton(Resource.INSTANCE.edit16(), "Edit", 80);
		_editButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				Actions.updateAction.execute();
			}
		});
		_editButton.disable();
		_menuBar.add(_editButton);

		//
		// Download
		//
		final ActionEntry addToShoppingCartEntry = new ActionEntry(
				"Add to shopping-cart interactively...",
				Actions.addToShoppingCartInteractively);
		final ActionEntry addRecursivelyToShoppingCartEntry = new ActionEntry(
				"Add to shopping-cart", Actions.addToShoppingCartAction);
		final ActionEntry showShoppingCartEntry = new ActionEntry(
				"Show shopping-cart...", Actions.showShoppingCart);
		final ActionEntry showShoppingCartManagerEntry = new ActionEntry(
				"Show shopping-cart manager...",
				Actions.showShoppingCartManagerAction);
		Menu downloadMenu = new Menu("Checkout") {

			@Override
			public void preShow() {

				// TODO:
			}
		};
		downloadMenu.add(addToShoppingCartEntry);
		downloadMenu.add(addRecursivelyToShoppingCartEntry);
		downloadMenu.add(showShoppingCartEntry);
		downloadMenu.add(showShoppingCartManagerEntry);
		_downloadMenuButton = createMenuButton(Resource.INSTANCE.download16(),
				downloadMenu, 100);
		_menuBar.add(_downloadMenuButton);

		//
		// User
		//
		ActionEntry userChangePasswordActionEntry = new ActionEntry(
				"Change Password...", Actions.changePasswordAction);
		ActionEntry userLogOffActionEntry = new ActionEntry("Log Off",
				Actions.logOffAction);
		Menu userMenu = new Menu("User") {

			@Override
			public void preShow() {

				// TODO:
			}
		};
		userMenu.add(userChangePasswordActionEntry);
		userMenu.add(userLogOffActionEntry);
		_userMenuButton = createMenuButton(Resource.INSTANCE.user16(),
				userMenu, 100);
		_menuBar.add(_userMenuButton);

		//
		// Help
		//
		ActionEntry aboutActionEntry = new ActionEntry("About",
				Actions.aboutAction);
		Menu helpMenu = new Menu("Help");
		helpMenu.add(aboutActionEntry);
		_helpMenuButton = createMenuButton(Resource.INSTANCE.help16(),
				helpMenu, 100);
		_menuBar.add(_helpMenuButton);
	}

	public void show() {

		_win.show();
	}

	public void hide() {

		_win.hide();
	}

	public void close() {

		_treeGUI.discard();
		_op.discard();
		_win.close();
	}

	public Window window() {

		return _win;
	}

	private void reloadTree() {

		if (_treeGUI != null) {
			_treeGUI.discard();
		}
		_treeGUI = new ObjectTreeGUI() {
			protected void onSelected(ObjectTreeNode n) {

				updateButtonStates((PSSDObjectRef) n.object());
			}
		};
		_treePanel.setContent(_treeGUI);
	}

	public static class Actions {

		public static Action reloadAction = new Action() {

			@Override
			public void execute() {

				ObjectBrowser.instance().reloadTree();
			}
		};

		public static Action createAction = new Action() {

			@Override
			public void execute() {

				PSSDObjectRef o = instance().selectedObject();
				/*
				 * Open the node.
				 */
				ObjectBrowser.instance().treeGUI().open(o);
				ObjectDialog dlg = ObjectDialog.dialogFor(o,
						FormEditMode.CREATE);
				dlg.show();
			}
		};

		public static Action updateAction = new Action() {

			@Override
			public void execute() {

				PSSDObjectRef o = instance().selectedObject();
				ObjectDialog dlg = ObjectDialog.dialogFor(o,
						FormEditMode.UPDATE);
				dlg.show();
			}
		};

		public static Action refreshRecursivelyAction = new Action() {

			@Override
			public void execute() {

				instance().tree().refreshSelectedObject(true);
			}
		};

		public static Action showShoppingCartManagerAction = new Action() {

			@Override
			public void execute() {

				ShoppingCartManagerDialog.show();

			}

		};

		public static Action addToShoppingCartInteractively = new Action() {

			@Override
			public void execute() {

				final PSSDObjectRef o = instance().selectedObject();
				if (o != null) {
					o.childrenRef().resolve(
							new ObjectResolveHandler<PSSDObjectRefSet>() {
								@Override
								public void resolved(PSSDObjectRefSet cos) {

									if (cos != null) {
										if (!cos.isEmpty()) {
											// has children
											ShoppingCartContentSelectDialog dlg = new ShoppingCartContentSelectDialog(
													o);
											dlg.show();
											return;
										}
									}
									// has no children
									ShoppingCartManager.instance()
											.addContentItem(o, true);
								}
							});
				}

			}

		};

		public static Action addToShoppingCartAction = new Action() {

			@Override
			public void execute() {

				final PSSDObjectRef o = instance().selectedObject();
				if (o != null) {
					ShoppingCartManager.instance().addContentItem(o, true);
				}
			}

		};

		public static Action showShoppingCart = new Action() {

			@Override
			public void execute() {

				ShoppingCartDialog.show();
			}

		};

		public static Action logOffAction = new Action() {

			@Override
			public void execute() {

				Session.logoff(true);
			}
		};

		public static Action changePasswordAction = new Action() {

			@Override
			public void execute() {

				UserPasswordChangeDialog.show();
			}
		};

		public static Action aboutAction = new Action() {

			@Override
			public void execute() {

				AboutDialog.show();
			}
		};

		public static Action dicomSendAction = new Action() {

			@Override
			public void execute() {

				final PSSDObjectRef o = instance().selectedObject();

				if (o != null) {
					final DicomSendForm dicomSendForm = new DicomSendForm(
							o.id());
					arc.gui.dialog.Dialog dlg = DialogFactory.create(instance()
							.window(), "Send DICOM datasets", "Send",
							dicomSendForm, 500, 300, true,
							new DialogCallback() {
								public void ok(final DialogCloseAction ca) {

									dicomSendForm.execute(new ActionListener() {
										public void executed(boolean succeeded) {

											ca.executed(succeeded);
										}
									});
								}

								public void cancel() {

								}
							});
					dlg.show();

				}
			}
		};

	}

}
