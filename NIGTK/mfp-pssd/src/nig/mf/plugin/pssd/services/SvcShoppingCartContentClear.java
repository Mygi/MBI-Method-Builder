package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.sc.ShoppingCart;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.LongType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcShoppingCartContentClear extends PluginService {

	private Interface _defn;

	public SvcShoppingCartContentClear() throws Throwable {

		_defn = new Interface();
		_defn.add(new Interface.Element("scid", LongType.DEFAULT, "The shopping cart id.", 1, 1));

	}

	public String name() {

		return "om.pssd.shoppingcart.content.clear";
	}

	public String description() {

		return "Clear the contents of the specified shopping cart.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String scid = args.value("scid");
		ShoppingCart.clearContentItems(executor(), scid);
	}
}
