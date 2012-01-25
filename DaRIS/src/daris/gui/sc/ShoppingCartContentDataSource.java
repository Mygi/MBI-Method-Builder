package daris.gui.sc;

import java.util.List;
import java.util.Vector;

import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.mf.object.ObjectResolveHandler;
import daris.model.sc.ContentItem;
import daris.model.sc.ShoppingCartRef;

public class ShoppingCartContentDataSource implements DataSource<ContentItem> {

	private ShoppingCartRef _cart;

	public ShoppingCartContentDataSource(ShoppingCartRef cart) {

		_cart = cart;
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
			final DataLoadHandler<ContentItem> lh) {

		_cart.contentItems(new ObjectResolveHandler<List<ContentItem>>() {

			@Override
			public void resolved(List<ContentItem> items) {

				if (items != null) {
					if (f != null) {
						List<ContentItem> fitems = new Vector<ContentItem>();
						for (ContentItem item : items) {
							if (f.matches(items)) {
								fitems.add(item);
							}
						}
						items = fitems;
					}
					int total = items.size();
					int start1 = (int) start;
					int end1 = (int) end;
					if (start1 > 0 || end1 < items.size()) {
						if (start1 >= items.size()) {
							items = null;
						} else {
							if (end1 > items.size()) {
								end1 = items.size();
							}
							items = items.subList(start1, end1);
						}
					}
					lh.loaded(start1, end1, total, items,
							DataLoadAction.REPLACE);
				} else {
					lh.loaded(0, 0, 0, null, null);
				}

			}

		}, true);
	}

}
