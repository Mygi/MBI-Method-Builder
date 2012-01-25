package daris.client.model.sc.messages;

import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.sc.ContentItem;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartRef;

public class ShoppingCartContentRemove extends ObjectMessage<Boolean> {

	private long _scid;
	private List<ContentItem> _items;

	public ShoppingCartContentRemove(long scid, List<ContentItem> items) {

		_scid = scid;
		_items = items;
	}

	public ShoppingCartContentRemove(ShoppingCartRef cart, List<ContentItem> items) {

		this(cart.scid(), items);
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("scid", _scid);
		for (ContentItem item : _items) {
			w.add("id", item.id());
		}
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.shoppingcart.content.remove";
	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			return true;
		}
		return null;
	}

	@Override
	protected String objectTypeName() {

		return ShoppingCart.TYPE_NAME;
	}

	@Override
	protected String idToString() {

		return Long.toString(_scid);
	}

}
