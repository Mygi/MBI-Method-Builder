package daris.client.model.sc;

import com.google.gwt.user.client.Timer;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;
import arc.mf.object.ObjectResolveHandler;

public class ShoppingCartRef extends ObjectRef<ShoppingCart> implements
		Comparable<ShoppingCartRef> {

	private long _scid;
	private String _name;
	private Status _status;

	public ShoppingCartRef(long scid) {

		this(scid, null, null);
	}

	public ShoppingCartRef(long scid, String name, Status status) {

		_scid = scid;
		_name = name;
		_status = status;
	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		w.add("scid", _scid);
	}

	@Override
	protected String resolveServiceName() {

		return "om.pssd.shoppingcart.describe";
	}

	@Override
	protected ShoppingCart instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			XmlElement ce = xe.element("cart");
			if (ce != null) {
				ShoppingCart sc = ShoppingCart.instantiate(ce);
				if (sc != null) {
					_name = sc.name();
					_status = sc.status();
					return sc;
				}
			}
		}
		return null;
	}

	@Override
	public String referentTypeName() {

		return ShoppingCart.TYPE_NAME;
	}

	@Override
	public String idToString() {

		return Long.toString(_scid);
	}

	public long scid() {

		return _scid;
	}

	public String name() {

		return _name;
	}

	public Status status() {

		return _status;
	}

	@Override
	public boolean equals(Object o) {

		if (o != null) {
			if (o instanceof ShoppingCartRef) {
				return _scid == ((ShoppingCartRef) o).scid();
			}
		}
		return false;
	}

	@Override
	public int compareTo(ShoppingCartRef o) {

		if (o == null) {
			return -1;
		}
		if (_scid > o.scid()) {
			return -1;
		} else if (_scid == o.scid()) {
			return 0;
		} else {
			return 1;
		}
	}

	public String toHTML() {

		if (referent() != null) {
			return referent().toHTML();
		} else {
			String html = "<table><thead><tr><th align=\"center\" colspan=\"2\">Shopping-cart</th></tr><thead>";
			html += "<tbody>";
			html += "<tr><td><b>id:</b></td><td>" + _scid + "</td></tr>";
			if (_name != null) {
				html += "<tr><td><b>name:</b></td><td>" + _name + "</td></tr>";
			}
			html += "<tr><td><b>status:</b></td><td>" + _status + "</td></tr>";
			html += "</tbody></table>";
			return html;
		}
	}

	public String summary() {

		return "Shopping Cart (ID: " + _scid + ", Status:" + _status + ")";
	}

	public void statusDescription(final ObjectResolveHandler<String> rh) {

		resolve(new ObjectResolveHandler<ShoppingCart>() {

			@Override
			public void resolved(ShoppingCart cart) {
				DeliveryMethod m = cart.destination().method();
				boolean active = ShoppingCartManager.isActive(cart);
				String desc;
				switch (cart.status()) {
				case editable:
					desc = (active ? "active, " : "") + "ready to use";
					break;
				case await_processing:
					desc = "ordered, await processing";
					break;
				case assigned:
					desc = "assigned";
					break;
				case processing:
					if (DeliveryMethod.deposit.equals(m)) {
						desc = "checking out to: " + cart.destination().name();
					} else {
						desc = "preparing archive";
					}
					break;
				case data_ready:
					if (DeliveryMethod.deposit.equals(m)) {
						desc = "completed, data transfered to "
								+ cart.destination().name();
					} else {
						desc = "archive is ready to download";
					}
					break;
				case fulfilled:
					desc = cart.status().toString();
					break;
				case rejected:
					desc = cart.status().toString();
					break;
				case error:
					desc = "error occured";
					break;
				case withdrawn:
					desc = cart.status().toString();
					break;
				default:
					desc = cart.status().toString();
					break;
				}
				rh.resolved(desc);
			}
		});
	}

	public Timer monitorProgress(int delay, ProgressHandler ph) {
		return ShoppingCart.monitorProgress(scid(), delay, ph);
	}

}
