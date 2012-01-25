package daris.client.ui.sc;

import java.util.List;

import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.image.LinearGradient;
import arc.gui.gwt.widget.list.ListGridHeader;
import arc.gui.gwt.widget.panel.HorizontalSplitPanel;
import arc.gui.gwt.widget.panel.VerticalPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import daris.client.Resource;
import daris.client.model.sc.ShoppingCartManager;
import daris.client.model.sc.ShoppingCartRef;
import daris.client.model.sc.Status;

public class ShoppingCartManagerPanel extends ContainerWidget implements
		ShoppingCartManager.Listener {

	public static final String REFRESH_ICON = Resource.INSTANCE.refreshBlue16()
			.getSafeUri().asString();
	public static final String CLEAR_ICON = Resource.INSTANCE.clear16()
			.getSafeUri().asString();

	private HorizontalSplitPanel _hsp;
	private ShoppingCartPanel _scp;
	private ShoppingCartGrid _grid;

	public ShoppingCartManagerPanel() {
		_hsp = new HorizontalSplitPanel();
		_hsp.fitToParent();

		VerticalPanel vp = new VerticalPanel();
		vp.setPreferredWidth(0.4);
		vp.setHeight100();

		_grid = new ShoppingCartGrid();
		_grid.setMultiSelect(false);
		_grid.fitToParent();
		_grid.setBorder(1, "#979797");
		_grid.setSelectionHandler(new SelectionHandler<ShoppingCartRef>() {

			@Override
			public void selected(ShoppingCartRef cart) {
				_scp.setCart(cart);
			}

			@Override
			public void deselected(ShoppingCartRef cart) {
				_scp.setCart(null);
			}
		});

		vp.add(_grid);

		ButtonBar bb = new ButtonBar(ButtonBar.Position.BOTTOM,
				ButtonBar.Alignment.RIGHT);
		bb.setHeight(28);
		bb.setWidth100();
		bb.setColourEnabled(false);
		bb.setBackgroundImage(new LinearGradient(
				LinearGradient.Orientation.TOP_TO_BOTTOM,
				ListGridHeader.HEADER_COLOUR_LIGHT,
				ListGridHeader.HEADER_COLOUR_DARK));
		Button refreshButton = new Button(
				"<div><img src=\""
						+ REFRESH_ICON
						+ "\" style=\"width:16px;height:16px;vertical-align:middle\"><span style=\"\">&nbsp;Refresh</span></div>",
				false);
		refreshButton.setHeight100();
		refreshButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				ShoppingCartManager.notifyOfCollectionModify();
			}
		});
		refreshButton.setToolTip("Reload the shopping cart list.");
		bb.add(refreshButton);
		Button clearButton = new Button(
				"<div><img src=\""
						+ CLEAR_ICON
						+ "\" style=\"width:16px;height:16px;vertical-align:middle\"><span style=\"\">&nbsp;Clear</span></div>",
				false);
		clearButton.setHeight100();
		clearButton.setMarginRight(20);
		clearButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				ShoppingCartManager.destroy(Status.error, Status.data_ready);
			}
		});
		clearButton
				.setToolTip("Remove the shopping carts in error or data_ready state.");
		bb.add(clearButton);
		vp.add(bb);

		_hsp.add(vp);

		_scp = new ShoppingCartPanel(null);
		_scp.fitToParent();
		_hsp.add(_scp);
		initWidget(_hsp);

		/*
		 * 
		 */
		ShoppingCartManager.addListener(this);

		refresh();

	}

	public void refresh() {

		_scp.setCart(null);
		_grid.refresh();
	}

	@Override
	public void created(ShoppingCartRef sc) {
		refresh();
	}

	@Override
	public void modified(ShoppingCartRef sc) {
		refresh();
	}

	@Override
	public void contentModified(ShoppingCartRef sc) {
		List<ShoppingCartRef> selections = _grid.selections();
		if (selections != null) {
			if (!selections.isEmpty()) {
				if (selections.contains(sc)) {
					_scp.refresh();
				}
			}
		}
	}

	@Override
	public void activated(ShoppingCartRef sc) {
		refresh();
	}

	@Override
	public void deactivated(ShoppingCartRef sc) {
		refresh();
	}

	@Override
	public void destroyed(ShoppingCartRef sc) {
		refresh();
	}

	@Override
	public void collectionModified() {
		refresh();
	}

	public void discard() {
		ShoppingCartManager.removeListener(this);
	}
}
