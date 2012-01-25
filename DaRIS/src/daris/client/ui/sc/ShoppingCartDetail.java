package daris.client.ui.sc;

import arc.gui.gwt.style.FontWeight;
import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.label.Label;
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

import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartRef;
import daris.client.ui.widget.LoadingMessage;
import daris.client.util.ByteUtil;

public class ShoppingCartDetail extends ContainerWidget {

	private SimplePanel _sp;
	private VerticalPanel _vp;
	private HTML _header;
	private TabPanel _tp;
	private int _activeTabId;

	private int _contentTabId;
	private VerticalPanel _contentVP;
	private CenteringPanel _contentSummaryCP;
	private HTML _contentSummaryHtml;
	private ShoppingCartContentGrid _contentGrid;

	private int _settingsTabId;
	private VerticalPanel _settingsVP;
	private SimplePanel _settingsSP;
	private ShoppingCartSettingsForm _settingsForm;
	private Label _settingsSB;

	private ShoppingCartRef _sc;

	public ShoppingCartDetail(ShoppingCartRef sc) {

		_sp = new SimplePanel();
		_sp.fitToParent();

		_vp = new VerticalPanel();
		_vp.fitToParent();

		SimplePanel headerSP = new SimplePanel();
		headerSP.setHeight(20);
		headerSP.setWidth100();
		_header = new HTML();
		_header.fitToParent();
		_header.setFontSize(12);
		_header.setFontWeight(FontWeight.BOLD);
		_header.style().setProperty("textAlign", "center");
		_header.setPaddingTop(3);
		headerSP.setContent(_header);
		_vp.add(headerSP);

		_tp = new TabPanel() {
			protected void activated(int id) {
				_activeTabId = id;
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
		 * 
		 */
		_sc.reset();
		_sc.resolve(new ObjectResolveHandler<ShoppingCart>() {

			@Override
			public void resolved(ShoppingCart cart) {
				/*
				 * update header
				 */
				_header.setHTML("Shopping Cart " + cart.scid());

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
				String title = "[Shopping Cart: " + cart.scid()
						+ "; Total number of datasets: "
						+ cart.totalNumberOfContentItems() + "; ";
				title += "Total size: "
						+ ByteUtil.humanReadableByteCount(
								cart.totalSizeOfContentItems(), true) + "]";
				_contentSummaryHtml.setHTML(title);
			}
		});
	}

}