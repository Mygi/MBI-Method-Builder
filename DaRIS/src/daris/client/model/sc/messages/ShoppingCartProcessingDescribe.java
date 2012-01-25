package daris.client.model.sc.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.sc.Progress;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartRef;

public class ShoppingCartProcessingDescribe extends ObjectMessage<Progress> {

	private long _scid;

	public ShoppingCartProcessingDescribe(long scid){
		_scid = scid;
	}
	
	public ShoppingCartProcessingDescribe(ShoppingCartRef sc) {
		this(sc.scid());
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {
		w.add("sid", _scid);
	}

	@Override
	protected String messageServiceName() {
		return "shopping.cart.processing.describe";
	}

	@Override
	protected Progress instantiate(XmlElement xe) throws Throwable {
		if (xe != null) {
			XmlElement pe = xe.element("process");
			if (pe != null) {
				return new Progress(pe);
			}
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
