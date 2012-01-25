package daris.model.sc.messages;

import java.util.Collection;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.model.object.PSSDObjectRef;
import daris.model.sc.ShoppingCartRef;

public class ShoppingCartContentRemove extends ObjectMessage<Boolean> {

	private String _cartId;

	private Collection<String> _ids = null;

	private boolean _recursive = true;

	private boolean _clear = false;

	/**
	 * Clear all content items if set clear argument to true; Do nothing if
	 * clear is false.
	 * 
	 * @param cart
	 *            The shopping cart.
	 * @param clear
	 */
	public ShoppingCartContentRemove(ShoppingCartRef cart, boolean clear) {

		this(cart.id(), clear);
	}

	/**
	 * Clear all content items if set clear argument to true; Do nothing if
	 * clear is false.
	 * 
	 * @param cartId
	 *            The shopping cart id.
	 * @param clear
	 */
	public ShoppingCartContentRemove(String cartId, boolean clear) {

		_cartId = cartId;
		_ids = null;
		_clear = clear;
	}


	public ShoppingCartContentRemove(ShoppingCartRef cart, PSSDObjectRef o, boolean recursive) {

		this(cart, o.id(), recursive);
	}
	
	public ShoppingCartContentRemove(ShoppingCartRef cart, String id, boolean recursive){
		
		this(cart.id(), (Collection<String>)null, recursive);
		_ids = new Vector<String>();
		_ids.add(id);
	}

	public ShoppingCartContentRemove(ShoppingCartRef cart, Collection<PSSDObjectRef> os,
			boolean recursive) {

		this(cart.id(), (Collection<String>)null, recursive);
		if(os!=null){
			_ids = new Vector<String>(os.size());
			for(PSSDObjectRef o :os){
				_ids.add(o.id());
			}
		}
	}

	public ShoppingCartContentRemove(String cartId, Collection<String> ids,
			boolean recursive) {

		_cartId = cartId;
		_ids = ids;
		_recursive = recursive;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("id", _cartId);
		if (_ids != null) {
			for (String id : _ids) {
				w.add("object-id",
						new String[] { "recursive",
								Boolean.toString(_recursive) }, id);
			}
		}
		if (_clear == true) {
			w.add("clear", _clear);
		}

	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.shoppingcart.content.remove";
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
