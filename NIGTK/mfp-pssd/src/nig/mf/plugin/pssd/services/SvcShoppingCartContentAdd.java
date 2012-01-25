package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.sc.ShoppingCart;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.LongType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcShoppingCartContentAdd extends PluginService {

	private Interface _defn;

	public SvcShoppingCartContentAdd() throws Throwable {

		_defn = new Interface();
		_defn.add(new Interface.Element("scid", LongType.DEFAULT, "The shopping cart id.", 1, 1));
		_defn.add(new Interface.Element("id", CiteableIdType.DEFAULT, "The citable id of the object to be added.", 1,
				Integer.MAX_VALUE));
		_defn.add(new Interface.Element("recursive", BooleanType.DEFAULT,
				"Should the descendants of the object(s) be included? Defaults to true.", 0, 1));
	}

	public String name() {

		return "om.pssd.shoppingcart.content.add";
	}

	public String description() {

		return "Add objects into the shopping cart.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String scid = args.value("scid");
		boolean recursive = args.booleanValue("recursive", true);
		Collection<String> cids = args.values("id");
		ShoppingCart.addContentItems(executor(), scid, cids, recursive);
	}
}
