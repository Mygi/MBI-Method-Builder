package daris.client.model.sc.messages;

import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.sc.ContentItem;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartRef;

public class ShoppingCartContentList extends ObjectMessage<List<ContentItem>> {

	private ShoppingCartRef _sc;


	public ShoppingCartContentList(ShoppingCartRef sc) {

		_sc = sc;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("scid", _sc.scid());
		w.add("size", "infinity");
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.shoppingcart.content.list";
	}

	@Override
	protected List<ContentItem> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			List<XmlElement> oes = xe.elements("object");
			if (oes != null) {
				return ContentItem.instantiate(oes, _sc);
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

		return Long.toString(_sc.scid());
	}

}
