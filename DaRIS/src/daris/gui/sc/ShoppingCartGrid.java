package daris.gui.sc;

import java.util.List;
import java.util.Vector;

import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridDataSource;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.Action;
import arc.mf.client.util.Transformer;
import arc.mf.object.ObjectResolveHandler;
import daris.model.sc.ShoppingCartRef;
import daris.model.sc.ShoppingCartsRef;

public class ShoppingCartGrid extends ListGrid<ShoppingCartRef> {

	private ShoppingCartsRef _carts = ShoppingCartsRef.instance();

	private int _selectedIndex = -1;

	private ShoppingCartRef _selectedCart = null;

	private List<SelectionHandler<ShoppingCartRef>> _selectionHandlers;

	private Action _postLoadAction;

	private static class ShoppingCartEntryTransformer extends
			Transformer<ShoppingCartRef, ListGridEntry<ShoppingCartRef>> {

		@Override
		protected ListGridEntry<ShoppingCartRef> doTransform(ShoppingCartRef m)
				throws Throwable {

			ListGridEntry<ShoppingCartRef> lge = new ListGridEntry<ShoppingCartRef>(
					m);
			lge.set("id", m.id());
			lge.set("name", m.name());
			lge.set("status", m.status().value());
			lge.set("transcode", m.transcodes());
			lge.set("destination", m.destination());
			lge.set("archive", m.archive());
			lge.set("layout", m.layout());
			lge.set("metadata-output", m.metadataOutput());
			lge.setDescription("Shopping Cart");
			return lge;
		}
	}

	public ShoppingCartGrid() {

		super(new ListGridDataSource<ShoppingCartRef>(
				new ShoppingCartDataSource(),
				new ShoppingCartEntryTransformer()), ScrollPolicy.AUTO);
		addColumnDefn("id", "cart-id");
		addColumnDefn("name", "name");
		addColumnDefn("destination", "destination");
		addColumnDefn("archive", "archive");
		addColumnDefn("status", "status");
		setShowHeader(true);
		setHeight100();
		setWidth100();
		setFontSize(12);
		setCellSpacing(0);
		setCellPadding(2);
		setEmptyMessage("No Shopping Cart found!");
		setCursorSize(1000);

		super.setSelectionHandler(new SelectionHandler<ShoppingCartRef>() {

			@Override
			public void selected(final ShoppingCartRef cart) {

				if (cart != null) {
					_carts.resolve(new ObjectResolveHandler<List<ShoppingCartRef>>() {

						@Override
						public void resolved(List<ShoppingCartRef> carts) {

							if (carts != null) {
								_selectedIndex = carts.indexOf(cart);
								_selectedCart = cart;
							}
						}
					});
				}
				if (_selectionHandlers != null) {
					for (SelectionHandler<ShoppingCartRef> sh : _selectionHandlers) {
						sh.selected(cart);
					}
				}
			}

			@Override
			public void deselected(ShoppingCartRef cart) {

				/*
				 * 
				 */
				if (_selectionHandlers != null) {
					for (SelectionHandler<ShoppingCartRef> sh : _selectionHandlers) {
						sh.deselected(cart);
					}
				}
			}
		});
	}

	@Override
	protected void postLoad(long start, long end, long total,
			List<ListGridEntry<ShoppingCartRef>> entries) {

		_selectedIndex = -1;
		_selectedCart = null;
		if (total > 0) {
			select((int) (total - 1));
		}
		if (_postLoadAction != null) {
			_postLoadAction.execute();
			_postLoadAction = null;
		}
	}

	public void refresh(Action postLoadAction) {

		_postLoadAction = postLoadAction;
		super.refresh();
	}

	public void refreshKeepingSelection() {

		refreshKeepingSelection(null);
	}

	public void refreshKeepingSelection(final Action postAction) {

		final int index = _selectedIndex;
		refresh(new Action() {

			@Override
			public void execute() {

				if (index > -1) {
					select(index);
				}
				if (postAction != null) {
					postAction.execute();
				}
			}
		});

	}

	public void setSelectionHandler(SelectionHandler<ShoppingCartRef> sh) {

		addSelectionHandler(sh);
	}

	public void addSelectionHandler(SelectionHandler<ShoppingCartRef> sh) {

		if (_selectionHandlers == null) {
			_selectionHandlers = new Vector<SelectionHandler<ShoppingCartRef>>();
		}
		_selectionHandlers.add(sh);
	}

	public void removeSelectionHandler(SelectionHandler<ShoppingCartRef> sh) {

		if (_selectionHandlers != null) {
			_selectionHandlers.remove(sh);
		}
	}

	public ShoppingCartRef selected() {

		return _selectedCart;
	}

	public int selectedIndex() {

		return _selectedIndex;
	}
}
