package daris.model.sc.messages;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.model.sc.ShoppingCartRef;
import daris.model.sc.Status;

public class ShoppingCartDescribe extends ObjectMessage<List<ShoppingCartRef>> {

	private String _id;
	private String _status;

	public ShoppingCartDescribe(String id) {
		this(id, null);
	}

	public ShoppingCartDescribe(Status.Value status) {
		this(null, status);
	}

	public ShoppingCartDescribe(String id, Status.Value status) {
		_id = id;
		if (status != null) {
			_status = status.toString();
		} else {
			_status = null;
		}
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {
		w.add("size", "infinity");
		if (_id != null) {
			w.add("id", _id);
		}
		if (_status != null) {
			w.add("status", _status);
		}
	}

	@Override
	protected String messageServiceName() {
		return "om.pssd.shoppingcart.describe";
	}

	@Override
	protected List<ShoppingCartRef> instantiate(XmlElement xe) throws Throwable {
		List<XmlElement> ces = xe.elements("cart");
		if (ces != null) {
			List<ShoppingCartRef> carts = new Vector<ShoppingCartRef>(
					ces.size());
			for (XmlElement ce : ces) {
				carts.add(new ShoppingCartRef(ce));
			}
			if (!carts.isEmpty()) {
				return carts;
			}
		}
		return null;
	}

	@Override
	protected String objectTypeName() {

		return "shopping-carts";
	}

	@Override
	protected String idToString() {
		return "shopping-carts";
	}

}
