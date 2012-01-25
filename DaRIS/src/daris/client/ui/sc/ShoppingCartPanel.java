package daris.client.ui.sc;

import arc.gui.gwt.style.FontWeight;
import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.image.LinearGradient;
import arc.gui.gwt.widget.label.Label;
import arc.gui.gwt.widget.list.ListGridHeader;
import arc.gui.gwt.widget.panel.CenteringPanel;
import arc.gui.gwt.widget.panel.CenteringPanel.Axis;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.TabPanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
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
import daris.client.ui.util.ButtonUtil;
import daris.client.ui.widget.LoadingMessage;
import daris.client.util.ByteUtil;

public class ShoppingCartPanel extends ContainerWidget {

	public static final String REFRESH_ICON = Resource.INSTANCE
			.refreshGreen16().getSafeUri().asString();
	public static final String CLEAR_ICON = Resource.INSTANCE.clear16()
			.getSafeUri().asString();
	public static final String ORDER_ICON = Resource.INSTANCE.submit16()
			.getSafeUri().asString();
	public static final String DOWNLOAD_ICON = Resource.INSTANCE.download16()
			.getSafeUri().asString();
	public static final String REMOVE_ICON = Resource.INSTANCE.remove16()
			.getSafeUri().asString();

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

	private ShoppingCartRef _sc;

	public ShoppingCartPanel(ShoppingCartRef sc) {

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
		_refreshButton = ButtonUtil.createButton(REFRESH_ICON, 16, 16,
				"Refresh", "Refresh the shopping cart contents.", false,
				new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						_refreshButton.disable();
						refresh();
					}
				});

		_removeContentButton = ButtonUtil.createButton(REMOVE_ICON, 16, 16,
				"Remove", "Remove the selected content items.", false,
				new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						if (_sc == null) {
							return;
						}
						if (_contentGrid.hasSelections()) {
							ShoppingCartManager.removeContent(_sc,
									_contentGrid.selections());
						}
					}
				});

		_clearContentButton = ButtonUtil.createButton(CLEAR_ICON, 16, 16,
				"Clear", "Clear the shopping cart contents.", false,
				new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						if (_sc != null) {
							ShoppingCartManager.clearContent(_sc);
						}
					}
				});

		_orderButton = ButtonUtil.createButton(ORDER_ICON, 16, 16, "Order",
				"Submit the shopping cart for processing.", false,
				new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						if (_sc != null) {
							_orderButton.disable();
							ShoppingCartManager.order(_sc);
						}
					}
				});
		_downloadButton = ButtonUtil.createButton(DOWNLOAD_ICON, 16, 16,
				"Download", "Download the shopping cart output archive.",
				false, new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						if (_sc != null) {
							_downloadButton.disable();
							ShoppingCartManager.download(_sc);
						}
					}
				});

		/*
		 * activate content tab
		 */
		_tp.setActiveTabById(_contentTabId);

		initWidget(_sp);

		setCart(sc);
	}

	public void refresh() {

		if (_sc == null) {
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
		_sc.reset();
		_sc.resolve(new ObjectResolveHandler<ShoppingCart>() {

			@Override
			public void resolved(ShoppingCart cart) {

				/*
				 * update content grid
				 */
				_contentGrid.setCart(_sc);

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

	public void setCart(ShoppingCartRef sc) {

		_sc = sc;
		refresh();
	}

	protected ShoppingCartRef cart() {
		return _sc;
	}

	protected void activateContentTab() {
		_tp.setActiveTabById(_contentTabId);
	}

	protected void activateSettingsTab() {
		_tp.setActiveTabById(_settingsTabId);
	}

	private void updateContentSummary() {
		if (_sc == null) {
			_contentSummaryHtml.setHTML("");
			return;
		}
		_sc.resolve(new ObjectResolveHandler<ShoppingCart>() {
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

		final ButtonBar bb = createButtonBar();
		_bbSP.setContent(bb);
		if (_sc == null) {
			return;
		}
		_sc.resolve(new ObjectResolveHandler<ShoppingCart>() {
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
					if (DeliveryMethod.download.equals(cart.destination().method())) {
						_downloadButton.enable();
						bb.add(_downloadButton);
					}
				}
				_bbSP.setContent(bb);
			}
		});
	}

	private static ButtonBar createButtonBar() {
		ButtonBar bb = new ButtonBar(ButtonBar.Position.BOTTOM,
				ButtonBar.Alignment.RIGHT);
		bb.setHeight(28);
		bb.setWidth100();
		bb.setColourEnabled(false);
		bb.setBackgroundImage(new LinearGradient(
				LinearGradient.Orientation.TOP_TO_BOTTOM,
				ListGridHeader.HEADER_COLOUR_LIGHT,
				ListGridHeader.HEADER_COLOUR_DARK));
		return bb;
	}

}