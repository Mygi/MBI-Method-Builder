package daris.client.model.sc.messages;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartRef;

public class ShoppingCartDestroy extends ObjectMessage<Boolean> {

	private List<Long> _scids;

	public ShoppingCartDestroy(List<ShoppingCartRef> carts) {
		_scids = new Vector<Long>(carts.size());
		for (ShoppingCartRef cart : carts) {
			_scids.add(cart.scid());
		}
	}

	public ShoppingCartDestroy(ShoppingCartRef cart) {

		this(cart.scid());
	}

	public ShoppingCartDestroy(long scid) {

		_scids = new Vector<Long>(1);
		_scids.add(scid);
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {
		for (Long scid : _scids) {
			w.add("scid", scid);
		}

	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.shoppingcart.destroy";
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

		return null;
	}

}
