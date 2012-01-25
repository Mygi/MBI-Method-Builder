package daris.gui.sc;

import java.util.List;

import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.window.Window;
import arc.gui.window.WindowProperties;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import daris.gui.object.ObjectTable;
import daris.model.object.PSSDObjectRef;
import daris.model.object.PSSDObjectRefSet;
import daris.model.sc.ShoppingCartManager;

public class ShoppingCartContentSelectDialog {

	private Window _win;
	private ObjectTable _list;

	public ShoppingCartContentSelectDialog(PSSDObjectRef parent) {
		WindowProperties wp = new WindowProperties();
		wp.setModal(true);
		wp.setCanBeResized(true);
		wp.setCanBeClosed(true);
		wp.setCanBeMoved(false);
		wp.setCenterInPage(true);
		wp.setTitle("Select and Add to shopping-cart ...");
		wp.setSize(800, 600);

		VerticalPanel vp = new VerticalPanel();
		vp.fitToParent();
		final SimplePanel sp = new SimplePanel();
		sp.fitToParent();
		parent.childrenRef().resolve(
				new ObjectResolveHandler<PSSDObjectRefSet>() {

					@Override
					public void resolved(PSSDObjectRefSet cos) {
						if (cos != null) {
							_list = new ObjectTable(cos);
							sp.setContent(new ScrollPanel(_list,
									ScrollPolicy.AUTO));
						}
					}
				});

		vp.add(sp);
		ButtonBar bb = new ButtonBar(ButtonBar.Position.BOTTOM,
				ButtonBar.Alignment.CENTER);
		bb.setWidth100();
		bb.setHeight(28);
		bb.setMargin(15);
		bb.setColourEnabled(false);
		bb.setMargin(0);
		bb.setBackgroundColour("#DDDDDD");
		Button addButton = new Button("Add");
		addButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				final List<PSSDObjectRef> selections = _list.selections();
				if (selections != null) {
					if (!selections.isEmpty()) {
						ShoppingCartManager.instance().addContentItems(
								selections, true);
						// TODO: inform
						_win.close();
					}
				}

			}
		});
		bb.add(addButton);
		vp.add(bb);
		_win = Window.create(wp);
		_win.setContent(vp);
		_win.centerInPage();
	}
	
	public void show(){
		_win.show();
	}
}
