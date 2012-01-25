package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.sc.ShoppingCart;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.LongType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcShoppingCartDestroy extends PluginService {

	private Interface _defn;

	public SvcShoppingCartDestroy() throws Throwable {

		_defn = new Interface();
		_defn.add(new Interface.Element("scid", LongType.DEFAULT, "The id of the shopping cart.", 1, Integer.MAX_VALUE));
	}

	public String name() {

		return "om.pssd.shoppingcart.destroy";
	}

	public String description() {

		return "Destroy the specified shopping cart.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		Collection<String> scids = args.values("scid");
		if (scids != null) {
			ShoppingCart.destroy(executor(), scids);
		}
	}
}
