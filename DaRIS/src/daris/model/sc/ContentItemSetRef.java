package daris.model.sc;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class ContentItemSetRef extends ObjectRef<List<ContentItem>> {

	private String _cartId;

	public ContentItemSetRef(String cartId) {

		_cartId = cartId;
	}

	public ContentItemSetRef(ShoppingCartRef cart) {

		this(cart.id());
	}

	@Override
	protected List<ContentItem> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			List<XmlElement> oes = xe.elements("object");
			if (oes != null) {
				List<ContentItem> cis = new Vector<ContentItem>(oes.size());
				for (XmlElement oe : oes) {
					cis.add(new ContentItem(oe));
				}
				if (cis.size() > 0) {
					return cis;
				}
			}
		}
		return null;
	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		w.add("id", _cartId);
		w.add("size", "infinity");
	}

	@Override
	protected String resolveServiceName() {

		return "om.pssd.shoppingcart.content.list";
	}

	@Override
	public String referentTypeName() {

		return "shopping-cart-content-set";
	}

	@Override
	public String idToString() {

		return _cartId;
	}

}
