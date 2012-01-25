package daris.model.sc.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.model.sc.ShoppingCartRef;

public class ShoppingCartOrder extends ObjectMessage<Boolean> {

	private String _cartId;

	public ShoppingCartOrder(ShoppingCartRef cart) {

		this(cart.id());
	}

	public ShoppingCartOrder(String cartId) {

		_cartId = cartId;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("id", _cartId);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.shoppingcart.order";
	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {

		return true;
	}

	@Override
	protected String objectTypeName() {

		return "shopping-cart";
	}

	@Override
	protected String idToString() {

		return _cartId;
	}
}
