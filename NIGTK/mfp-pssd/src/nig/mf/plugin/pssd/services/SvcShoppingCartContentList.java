package nig.mf.plugin.pssd.services;

import java.util.List;

import nig.mf.plugin.pssd.sc.ShoppingCart;
import nig.mf.plugin.pssd.sc.Status;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.LongType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcShoppingCartContentList extends PluginService {

	private Interface _defn;

	public SvcShoppingCartContentList() throws Throwable {

		_defn = new Interface();
		_defn.add(new Interface.Element("scid", LongType.DEFAULT, "The shopping cart id.", 1, 1));
		_defn.add(new Interface.Element("idx", IntegerType.DEFAULT,
				"The starting position of the result set. Defaults to 1.", 0, 1));
		_defn.add(new Interface.Element("size", IntegerType.DEFAULT,
				"The number of content items to retrieve. Defaults to infinity.", 0, 1));

	}

	public String name() {

		return "om.pssd.shoppingcart.content.list";
	}

	public String description() {

		return "List the contents in the specified shopping cart.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String scid = args.value("scid");
		String idx = args.value("idx");
		String size = args.value("size");
		/*
		 * refresh the contents if the status is editable.
		 */
		ShoppingCart sc = ShoppingCart.get(executor(), scid);
		if (sc.status().equals(Status.editable)) {
			sc.refreshContentItems(executor());
		}
		
		/*
		 * 
		 */
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("sid", scid);
		if (idx != null) {
			dm.add("idx", idx);
		}
		if (size != null) {
			dm.add("size", size);
		} else {
			dm.add("size", "infinity");
		}
		dm.add("count", true);
		XmlDoc.Element r = executor().execute("shopping.cart.content.list", dm.root());
		XmlDoc.Element cursorElement = r.element("cart/cursor");
		XmlDoc.Element sizeElement = r.element("cart/size");
		List<XmlDoc.Element> aes = r.elements("cart/item/asset");
		if (aes != null) {
			ShoppingCart.describeContentItems(executor(), aes, w);
		}
		if ((idx != null || size != null) && cursorElement != null) {
			w.add(cursorElement);
		}
		if ((idx != null || size != null) && sizeElement != null) {
			w.add(sizeElement);
		}
	}
}
