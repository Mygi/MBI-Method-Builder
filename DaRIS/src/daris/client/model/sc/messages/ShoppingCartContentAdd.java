package daris.client.model.sc.messages;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.object.DObjectRef;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartRef;

public class ShoppingCartContentAdd extends ObjectMessage<Boolean> {

	private long _scid;
	private List<String> _ids;

	protected ShoppingCartContentAdd(long scid, List<String> ids) {

		_scid = scid;
		_ids = ids;
	}

	public ShoppingCartContentAdd(ShoppingCartRef cart, List<DObjectRef> os) {

		_scid = cart.scid();
		_ids = new Vector<String>(os.size());
		for (DObjectRef o : os) {
			String id = o.id();
			_ids.add(id);
		}
	}

	public ShoppingCartContentAdd(ShoppingCartRef cart, DObjectRef o) {

		_scid = cart.scid();
		_ids = new Vector<String>(1);
		_ids.add(o.id());
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("scid", _scid);
		for (String id : _ids) {
			w.add("id", id);
		}
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.shoppingcart.content.add";
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
