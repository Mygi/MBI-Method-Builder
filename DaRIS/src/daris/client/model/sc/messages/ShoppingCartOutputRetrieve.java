package daris.client.model.sc.messages;

import java.util.List;

import arc.mf.client.Output;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.i18n.client.DateTimeFormat;

import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartRef;

public class ShoppingCartOutputRetrieve extends ObjectMessage<Boolean> {

	private ShoppingCartRef _cart;

	public ShoppingCartOutputRetrieve(ShoppingCartRef cart) {

		_cart = cart;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("scid", _cart.scid());
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.shoppingcart.output.retrieve";
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

		return Long.toString(_cart.scid());
	}

	@Override
	protected int numberOfOutputs() {

		return 1;
	}

	@Override
	protected void process(Boolean o, final List<Output> outputs) {

		if (o) {
			if (outputs != null) {
				_cart.resolve(new ObjectResolveHandler<ShoppingCart>() {

					@Override
					public void resolved(ShoppingCart cart) {

						if (cart != null) {
							for (Output output : outputs) {

								String ext = cart.archive().type().extension();
								String filename = "DARIS_SC_" + cart.scid();
								if (cart.name() != null) {
									filename += "_" + cart.name();
								}
								filename += "_"+DateTimeFormat.getFormat(
										"yyyy.MM.dd_HHmmss").format(cart.changed());
								filename += (ext != null ? ("." + ext) : "");
								output.download(filename);
							}
						}
					}
				});

			}
		}
	}
}
