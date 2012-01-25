package daris.model.sc;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;
import arc.mf.object.ObjectResolveHandler;

public class ShoppingCartsRef extends ObjectRef<List<ShoppingCartRef>> {

	private static ShoppingCartsRef _instance;

	public static ShoppingCartsRef instance() {

		if (_instance == null) {
			_instance = new ShoppingCartsRef();
		}
		return _instance;
	}

	private ShoppingCartsRef() {

	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		w.add("size", "infinity");
		// if (_status != null) {
		// w.add("status", _status.value().toString());
		// }
	}

	@Override
	protected String resolveServiceName() {

		return "om.pssd.shoppingcart.describe";
	}

	@Override
	protected List<ShoppingCartRef> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			List<XmlElement> ces = xe.elements("cart");
			if (ces != null) {
				List<ShoppingCartRef> cs = new Vector<ShoppingCartRef>(
						ces.size());
				for (XmlElement ce : ces) {
					cs.add(new ShoppingCartRef(ce));
				}
				if (cs.size() > 0) {
					return cs;
				}
			}
		}
		return null;
	}

	@Override
	public String referentTypeName() {

		return "shopping-carts";
	}

	@Override
	public String idToString() {

		return "shopping-carts";
	}

	public void hasMembers(final ObjectResolveHandler<Boolean> rh) {

		resolve(new ObjectResolveHandler<List<ShoppingCartRef>>() {

			@Override
			public void resolved(List<ShoppingCartRef> o) {

				if (o != null) {
					if (o.size() > 0) {
						rh.resolved(true);
						return;
					}
				}
				rh.resolved(false);
			}
		});
	}

	public void resolveMember(final String cartId,
			final ObjectResolveHandler<ShoppingCartRef> rh) {

		resolve(new ObjectResolveHandler<List<ShoppingCartRef>>() {

			@Override
			public void resolved(List<ShoppingCartRef> carts) {

				if (carts != null) {
					for (ShoppingCartRef cart : carts) {
						if (cart.id().equals(cartId)) {
							rh.resolved(cart);
							return;
						}
					}
				}
				rh.resolved(null);

			}
		});
	}
}
