package daris.model.sc.messages;

import java.util.List;

import arc.mf.client.Output;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.model.sc.ShoppingCartRef;

public class ShoppingCartOutputRetrieve extends ObjectMessage<Boolean> {

	private ShoppingCartRef _cart;

	public ShoppingCartOutputRetrieve(ShoppingCartRef cart) {

		_cart = cart;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("id", _cart.id());
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.shoppingcart.output.retrieve";
	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {

		return true;
	}

	@Override
	protected String objectTypeName() {

		return "shopping-cart";
	}

	@Override
	protected String idToString() {

		return _cart.id();
	}

	@Override
	protected int numberOfOutputs() {

		return 1;
	}

	@Override
	protected void process(Boolean o, List<Output> outputs) {

		if (o) {
			if (outputs != null) {
				for (Output output : outputs) {
					String ext = _cart.archive().extension();
					if (ext.equals("compressed-tar")) {
						ext = "tar.gz";
					}
					if (ext.equals("iso9660")) {
						ext = "iso";
					}
					String filename = _cart.id();
					if(_cart.name()!=null){
						filename += "_" + _cart.name();
					}
					filename += "." + ext;
					output.download(filename);
				}
			}
		}
	}
}
