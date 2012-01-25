package nig.mf.plugin.pssd.services;

import java.util.List;

import nig.mf.plugin.pssd.sc.ShoppingCart;
import nig.mf.plugin.pssd.sc.Status;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.LongType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcShoppingCartDescribe extends PluginService {

	private Interface _defn;

	public SvcShoppingCartDescribe() throws Throwable {

		_defn = new Interface();
		_defn.add(new Interface.Element("scid", LongType.DEFAULT,
				"The shopping cart to describe.", 0, 1));
		_defn.add(new Interface.Element("idx", IntegerType.DEFAULT,
				"The starting position of the result set. Defaults to 1.", 0, 1));
		_defn.add(new Interface.Element("size", IntegerType.DEFAULT,
				"The number of entries to retrieve. Defaults to 100.", 0, 1));
		_defn.add(new Interface.Element(
				"status",
				new EnumType(Status.stringValues()),
				"If set, only cart matching the specified status will be returned.",
				0, 1));
	}

	public String name() {

		return "om.pssd.shoppingcart.describe";
	}

	public String description() {

		return "Return the details of the specified carts by id or status. If neither of id nor status is specified, all the carts owned by the user are described.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {

		String scid = args.value("scid");
		String idx = args.value("idx");
		String size = args.value("size");
		String status = args.value("status");
		XmlDocMaker dm = new XmlDocMaker("args");
		if (scid != null) {
			dm.add("sid", scid);
		}
		if (idx != null) {
			dm.add("idx", idx);
		}
		if (size != null) {
			dm.add("size", size);
		}
		if (status != null) {
			dm.add("status", status);
		}
		dm.add("count", true);
		XmlDoc.Element r = executor().execute("shopping.cart.describe",
				dm.root());
		List<XmlDoc.Element> ces = r.elements("cart");
		if (ces != null) {
			for (XmlDoc.Element ce : ces) {
				ShoppingCart cart = ShoppingCart.instantiate(executor(), ce);
				cart.describe(w);
			}
			w.add(r.element("cursor"));
		}
	}
}
