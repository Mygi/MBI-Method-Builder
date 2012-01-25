package daris.model.sc.messages;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.model.sc.ShoppingCartRef;

public class ShoppingCartDestroy extends ObjectMessage<Boolean> {

	private List<String> _ids;

	public ShoppingCartDestroy(String id) {

		_ids = new Vector<String>();
		_ids.add(id);
	}

	public ShoppingCartDestroy(ShoppingCartRef cart) {
		this(cart.id());
	}

	public ShoppingCartDestroy(List<ShoppingCartRef> carts) {
		assert carts != null;
		assert !carts.isEmpty();
		_ids = new Vector<String>();
		for (ShoppingCartRef cart : carts) {
			_ids.add(cart.id());
		}
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		for (String id : _ids) {
			w.add("id", id);
		}

	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.shoppingcart.destroy";
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

		return _ids.toString();
	}

}
