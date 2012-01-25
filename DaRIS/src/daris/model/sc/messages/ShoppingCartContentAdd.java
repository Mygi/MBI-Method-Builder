package daris.model.sc.messages;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.model.object.PSSDObjectRef;
import daris.model.sc.ShoppingCartRef;

public class ShoppingCartContentAdd extends ObjectMessage<Boolean> {
	private String _cartId;

	private Collection<String> _ids;

	private boolean _recursive = true;
	

	public ShoppingCartContentAdd(ShoppingCartRef cart, PSSDObjectRef o,
			boolean recursive) {
		this(cart.id(), o.id(), recursive);
	}

	public ShoppingCartContentAdd(String cartId, String id, boolean recursive) {
		
		this(cartId, (Collection<String>)null, recursive);
		_ids = new Vector<String>();
		_ids.add(id);
	}

	public ShoppingCartContentAdd(ShoppingCartRef cart, Collection<PSSDObjectRef> os, boolean recursive){
		this(cart.id(), (Collection<String>)null, recursive);
		if(os!=null){
			_ids = new Vector<String>(os.size());
			for(PSSDObjectRef o :os){
				_ids.add(o.id());
			}
		}
	}

	
	public ShoppingCartContentAdd(String cartId, Collection<String> ids,
			boolean recursive) {

		_cartId = cartId;
		_ids = ids;
		_recursive = recursive;
	}

	public ShoppingCartContentAdd(String cartId, boolean recursive,
			List<PSSDObjectRef> os) {

		_cartId = cartId;
		_ids = new Vector<String>();
		if (os != null) {
			for (PSSDObjectRef o : os) {
				_ids.add(o.id());
			}
		}
		_recursive = recursive;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("id", _cartId);
		for (String id : _ids) {
			w.add("object-id",
					new String[] { "recursive", Boolean.toString(_recursive) },
					id);
		}
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.shoppingcart.content.add";
	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {

		return true;
	}

	@Override
	protected String objectTypeName() {

		return "shopping-cart-content-item";
	}

	@Override
	protected String idToString() {

		return _cartId;
	}
}
