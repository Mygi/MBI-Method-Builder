package daris.client.model.sc.messages;

import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartRef;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class ShoppingCartProcessingAbort extends ObjectMessage<Boolean> {

	private long _scid;

	public ShoppingCartProcessingAbort(ShoppingCartRef sc) {
		_scid = sc.scid();
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {
		w.add("sid", _scid);
	}

	@Override
	protected String messageServiceName() {
		return "shopping.cart.processing.abort";
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
