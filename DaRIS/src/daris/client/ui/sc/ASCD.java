package daris.client.ui.sc;

import arc.gui.gwt.style.FontWeight;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.label.Label;
import arc.gui.gwt.widget.panel.CenteringPanel;
import arc.gui.gwt.widget.panel.CenteringPanel.Axis;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.TabPanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.window.Window;
import arc.gui.gwt.widget.window.WindowCloseListener;
import arc.gui.image.Image;
import arc.gui.window.WindowProperties;
import arc.mf.client.util.StateChangeListener;
import arc.mf.client.util.Validity;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import daris.client.Resource;
import daris.client.model.sc.ContentItem;
import daris.client.model.sc.DeliveryMethod;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartManager;
import daris.client.model.sc.ShoppingCartRef;
import daris.client.model.sc.Status;
import daris.client.ui.DObjectBrowser;
import daris.client.ui.util.ButtonUtil;
import daris.client.ui.widget.LoadingMessage;
import daris.client.util.ByteUtil;

public class ASCD implements ShoppingCartManager.Listener {

	public static Image ICON_REFRESH = new arc.gui.image.Image(
			Resource.INSTANCE.refreshGreen16().getSafeUri().asString(), 16, 16);
	public static Image ICON_CLEAR = new arc.gui.image.Image(Resource.INSTANCE
			.clear16().getSafeUri().asString(), 16, 16);
	public static Image ICON_ORDER = new arc.gui.image.Image(Resource.INSTANCE
			.submit16().getSafeUri().asString(), 16, 16);
	public static Image ICON_DOWNLOAD = new arc.gui.image.Image(
			Resource.INSTANCE.download16().getSafeUri().asString(), 16, 16);
	public static Image ICON_REMOVE = new arc.gui.image.Image(Resource.INSTANCE
			.remove16().getSafeUri().asString(), 16, 16);

	private SimplePanel _sp;
	private VerticalPanel _vp;
	private TabPanel _tp;
	private int _activeTabId;

	private int _contentTabId;
	private VerticalPanel _contentVP;
	private CenteringPanel _contentSummaryCP;
	private HTML _contentSummaryHtml;
	private ShoppingCartContentGrid _contentGrid;

	private Button _refreshButton;
	private Button _removeContentButton;
	private Button _clearContentButton;
	private Button _orderButton;
	private Button _downloadButton;

	private int _settingsTabId;
	private VerticalPanel _settingsVP;
	private SimplePanel _settingsSP;
	private ShoppingCartSettingsForm _settingsForm;
	private Label _settingsSB;

	private SimplePanel _bbSP;

	private boolean _showing;

	private Window _win;

	private ASCD() {

		_sp = new SimplePanel();
		_sp.fitToParent();

		_vp = new VerticalPanel();
		_vp.fitToParent();

		_tp = new TabPanel() {
			protected void activated(int id) {
				_activeTabId = id;
				updateButtonBar();
			}
		};
		_tp.fitToParent();
		_tp.setBodyBorder(1, BorderStyle.SOLID, "#979797");

		/*
		 * content tab
		 */
		_contentVP = new VerticalPanel();
		_contentVP.fitToParent();

		_contentGrid = new ShoppingCartContentGrid(null);
		_contentGrid.setSelectionHandler(new SelectionHandler<ContentItem>() {

			@Override
			public void selected(ContentItem o) {
				if (_contentGrid.hasSelections()) {
					_removeContentButton.enable();
				} else {
					_removeContentButton.disable();
				}
			}

			@Override
			public void deselected(ContentItem o) {
				if (_contentGrid.hasSelections()) {
					_removeContentButton.enable();
				} else {
					_removeContentButton.disable();
				}
			}
		});
		_contentVP.add(_contentGrid);

		_contentSummaryCP = new CenteringPanel(Axis.HORIZONTAL);
		_contentSummaryCP.setHeight(20);
		_contentSummaryCP.setWidth100();
		_contentSummaryCP.setBorderTop(1, BorderStyle.DOTTED, "#ddd");

		_contentSummaryHtml = new HTML("Shopping Cart");
		_contentSummaryHtml.setFontFamily("Helvetica");
		_contentSummaryHtml.setFontWeight(FontWeight.BOLD);
		_contentSummaryHtml.setFontSize(12);
		_contentSummaryHtml.setMarginTop(3);

		_contentSummaryCP.add(_contentSummaryHtml);

		_contentVP.add(_contentSummaryCP);

		_contentTabId = _tp.addTab("Content", null, _contentVP);

		/*
		 * settings tab
		 */
		_settingsVP = new VerticalPanel();
		_settingsVP.fitToParent();

		_settingsSP = new SimplePanel();
		_settingsSP.fitToParent();
		_settingsVP.add(_settingsSP);

		_settingsSB = new Label();
		_settingsSB.setColour("red");
		_settingsSB.setFontSize(10);
		_settingsSB.setHeight(20);
		_settingsSB.setPaddingLeft(20);

		_settingsVP.add(_settingsSB);

		_settingsTabId = _tp.addTab("Settings", null, _settingsVP);

		_vp.add(_tp);

		_bbSP = new SimplePanel();
		_bbSP.setHeight(28);
		_bbSP.setWidth100();
		_vp.add(_bbSP);

		/*
		 * buttons
		 */
		_refreshButton = ButtonUtil.createButton(ICON_REFRESH, "Refresh",
				"Refresh the shopping cart contents.", false,
				new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						_refreshButton.disable();
						refresh();
					}
				});

		_removeContentButton = ButtonUtil.createButton(ICON_REMOVE, "Remove",
				"Remove the selected content items.", false,
				new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						if (_contentGrid.hasSelections()) {
							ShoppingCartRef asc = ShoppingCartManager
									.activeShoppingCart();
							if (asc != null) {
								ShoppingCartManager.removeContent(asc,
										_contentGrid.selections());
							}
						}
					}
				});

		_clearContentButton = ButtonUtil.createButton(ICON_CLEAR, "Clear",
				"Clear the shopping cart contents.", false, new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						ShoppingCartRef asc = ShoppingCartManager
								.activeShoppingCart();
						if (asc != null) {
							ShoppingCartManager.clearContent(asc);
						}
					}
				});

		_orderButton = ButtonUtil.createButton(ICON_ORDER, "Order",
				"Submit the shopping cart for processing.", false,
				new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						ShoppingCartRef asc = ShoppingCartManager
								.activeShoppingCart();
						if (asc != null) {
							_orderButton.disable();
							ShoppingCartManager.order(asc);
							_win.close();
							SCMD.instance().show(
									DObjectBrowser.instance().window(), 0.6,
									0.5);
						}
					}
				});
		_downloadButton = ButtonUtil.createButton(ICON_DOWNLOAD, "Download",
				"Download the shopping cart output archive.", false,
				new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						ShoppingCartRef asc = ShoppingCartManager
								.activeShoppingCart();
						if (asc != null) {
							_downloadButton.disable();
							ShoppingCartManager.download(asc);
						}
					}
				});

		/*
		 * activate content tab
		 */
		_tp.setActiveTabById(_contentTabId);

		ShoppingCartManager.addListener(this);

		_showing = false;

	}

	public void refresh() {

		if (!_showing) {
			return;
		}

		final ShoppingCartRef asc = ShoppingCartManager.activeShoppingCart();
		if (asc == null) {
			_win.setTitle("Active Shopping Cart");
			LoadingMessage lm = new LoadingMessage("Loading shopping cart ...");
			lm.fitToParent();
			_sp.setContent(lm);

			return;
		}

		/*
		 * disable the buttons;
		 */
		_refreshButton.disable();
		_removeContentButton.disable();
		_clearContentButton.disable();
		_orderButton.disable();
		_downloadButton.disable();

		/*
		 * 
		 */
		_win.setTitle("Active Shopping Cart " + asc.scid());
		asc.reset();
		asc.resolve(new ObjectResolveHandler<ShoppingCart>() {

			@Override
			public void resolved(ShoppingCart cart) {

				/*
				 * update content grid
				 */
				_contentGrid.setCart(asc);

				/*
				 * update settings
				 */
				_settingsForm = new ShoppingCartSettingsForm(cart);
				_settingsForm.addChangeListener(new StateChangeListener() {

					@Override
					public void notifyOfChangeInState() {
						_settingsForm.validate();
						Validity valid = _settingsForm.valid();
						if (valid.valid()) {
							_settingsSB.setText(null);
							_settingsForm.execute(null);
						} else {
							_settingsSB.setText(valid.reasonForIssue());
						}
					}
				});
				_settingsForm.render();
				_settingsSP.setContent(new ScrollPanel(_settingsForm,
						ScrollPolicy.AUTO));

				updateContentSummary();

				updateButtonBar();

				/*
				 * 
				 */
				_sp.setContent(_vp);

			}
		});

	}

	protected void activateContentTab() {
		_tp.setActiveTabById(_contentTabId);
	}

	protected void activateSettingsTab() {
		_tp.setActiveTabById(_settingsTabId);
	}

	private void updateContentSummary() {
		ShoppingCartRef asc = ShoppingCartManager.activeShoppingCart();
		if (asc == null) {
			_contentSummaryHtml.setHTML("");
			return;
		}
		asc.resolve(new ObjectResolveHandler<ShoppingCart>() {
			@Override
			public void resolved(ShoppingCart cart) {
				String title = " [Total number of datasets: "
						+ cart.totalNumberOfContentItems() + "; ";
				title += "Total size: "
						+ ByteUtil.humanReadableByteCount(
								cart.totalSizeOfContentItems(), true) + "]";
				_contentSummaryHtml.setHTML(title);
			}
		});
	}

	private void updateButtonBar() {

		final ButtonBar bb = ButtonUtil.createButtonBar(
				ButtonBar.Position.BOTTOM, ButtonBar.Alignment.RIGHT, 28);
		_bbSP.setContent(bb);
		ShoppingCartRef asc = ShoppingCartManager.activeShoppingCart();
		if (asc == null) {
			return;
		}
		asc.resolve(new ObjectResolveHandler<ShoppingCart>() {
			@Override
			public void resolved(ShoppingCart cart) {

				_refreshButton.enable();
				bb.add(_refreshButton);
				if (Status.editable.equals(cart.status())) {
					if (cart.totalNumberOfContentItems() > 0) {

						if (_contentGrid.hasSelections()) {
							_removeContentButton.enable();
						}
						if (_activeTabId == _contentTabId) {
							bb.add(_removeContentButton);
						}

						_clearContentButton.enable();
						if (_activeTabId == _contentTabId) {
							bb.add(_clearContentButton);
						}

						_orderButton.enable();
						bb.add(_orderButton);
					}
				} else if (Status.data_ready.equals(cart.status())) {
					if (DeliveryMethod.download.equals(cart.destination()
							.method())) {
						_downloadButton.enable();
						bb.add(_downloadButton);
					}
				}
				_bbSP.setContent(bb);
			}
		});
	}

	public void discard() {
		ShoppingCartManager.removeListener(this);
	}

	private static ASCD _instance;

	public static ASCD instance() {
		if (_instance == null) {
			_instance = new ASCD();
		}
		return _instance;
	}

	public static void reset() {
		if (_instance != null) {
			_instance.discard();
			_instance = null;
		}
	}

	@Override
	public void activated(ShoppingCartRef sc) {
		refresh();
	}

	@Override
	public void collectionModified() {

	}

	@Override
	public void contentModified(ShoppingCartRef sc) {
		if (ShoppingCartManager.isActive(sc)) {
			refresh();
		}
	}

	@Override
	public void created(ShoppingCartRef sc) {

	}

	@Override
	public void deactivated(ShoppingCartRef sc) {

	}

	@Override
	public void destroyed(ShoppingCartRef sc) {

	}

	@Override
	public void modified(ShoppingCartRef sc) {
		if (ShoppingCartManager.isActive(sc)) {
			refresh();
		}
	}

	public void show(Window owner, double w, double h) {
		if (!_showing) {
			ShoppingCartRef asc = ShoppingCartManager.activeShoppingCart();
			WindowProperties wp = new WindowProperties();
			wp.setModal(false);
			wp.setCanBeResized(true);
			wp.setCanBeClosed(true);
			wp.setCanBeMoved(true);
			wp.setSize(w, h);
			wp.setOwnerWindow(owner);
			wp.setTitle("Active Shopping Cart "
					+ (asc != null ? Long.toString(asc.scid()) : ""));
			_win = Window.create(wp);
			_win.setContent(_sp);
			_win.addCloseListener(new WindowCloseListener() {
				@Override
				public void closed(Window w) {
					_showing = false;
				}
			});
			_win.centerInPage();
			_win.show();
			_showing = true;
			refresh();
		}
	}

	public void show(Window owner) {
		show(owner, 0.6, 0.5);
	}
}