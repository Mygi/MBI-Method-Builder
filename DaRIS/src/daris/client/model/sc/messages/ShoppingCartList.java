package daris.client.model.sc.messages;

import java.util.Collections;
import java.util.List;
import java.util.Vector;


import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.sc.ShoppingCartRef;
import daris.client.model.sc.Status;

public class ShoppingCartList extends ObjectMessage<List<ShoppingCartRef>> {

	private Status _status;

	public ShoppingCartList(Status status) {

		_status = status;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("size", "infinity");
		if (_status != null) {
			w.add("status", _status.toString());
		}
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.shoppingcart.list";
	}

	@Override
	protected List<ShoppingCartRef> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			List<XmlElement> ces = xe.elements("cart");
			if (ces != null) {
				List<ShoppingCartRef> cs = new Vector<ShoppingCartRef>(ces.size());
				for (XmlElement ce : ces) {
					cs.add(new ShoppingCartRef(ce.longValue("@scid"), ce.value("@name"), Status.instantiate(ce
							.value("@status"))));
				}
				if (!cs.isEmpty()) {
					Collections.sort(cs);
					return cs;
				}
			}
		}
		return null;
	}

	@Override
	protected String objectTypeName() {

		return null;
	}

	@Override
	protected String idToString() {

		return null;
	}

}
