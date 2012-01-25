package daris.gui.sc;

import java.util.List;
import java.util.Vector;

import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.mf.object.ObjectResolveHandler;
import daris.model.sc.ShoppingCartRef;
import daris.model.sc.ShoppingCartsRef;

public class ShoppingCartDataSource implements DataSource<ShoppingCartRef> {

	private ShoppingCartsRef _carts;

	public ShoppingCartDataSource() {

		_carts = ShoppingCartsRef.instance();
	}

	@Override
	public boolean isRemote() {

		return true;
	}

	@Override
	public boolean supportCursor() {

		return false;
	}

	@Override
	public void load(final Filter f, final long start, final long end,
			final DataLoadHandler<ShoppingCartRef> lh) {

		_carts.reset();
		_carts.resolve(new ObjectResolveHandler<List<ShoppingCartRef>>() {

			@Override
			public void resolved(List<ShoppingCartRef> carts) {

				if (carts != null) {
					if (f != null) {
						List<ShoppingCartRef> fcarts = new Vector<ShoppingCartRef>();
						for (ShoppingCartRef cart : carts) {
							if (f.matches(cart)) {
								fcarts.add(cart);
							}
						}
						carts = fcarts;
					}
					int total = carts.size();
					int start1 = (int) start;
					int end1 = (int) end;
					if (start1 > 0 || end1 < carts.size()) {
						if (start1 >= carts.size()) {
							carts = null;
						} else {
							if (end1 > carts.size()) {
								end1 = carts.size();
							}
							carts = carts.subList(start1, end1);
						}
					}
					lh.loaded(start1, end1, total, carts,
							DataLoadAction.REPLACE);
				} else {
					lh.loaded(0, 0, 0, null, null);
				}
			}
		});
	}

}
