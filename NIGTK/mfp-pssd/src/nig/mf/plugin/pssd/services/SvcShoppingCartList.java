package nig.mf.plugin.pssd.services;

import java.util.List;

import nig.mf.plugin.pssd.sc.Status;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcShoppingCartList extends PluginService {

	private Interface _defn;

	public SvcShoppingCartList() throws Throwable {

		_defn = new Interface();
		_defn.add(new Interface.Element("idx", IntegerType.DEFAULT,
				"The starting position of the result set. Defaults to 1.", 0, 1));
		_defn.add(new Interface.Element("size", IntegerType.DEFAULT,
				"The number of entries to retrieve. Defaults to 100.", 0, 1));
		_defn.add(new Interface.Element("status", new EnumType(Status.stringValues()),
				"If set, only cart matching the specified status will be returned.", 0, 1));
	}

	public String name() {

		return "om.pssd.shoppingcart.list";
	}

	public String description() {

		return "List the shopping-cart owned by the user.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String idx = args.value("idx");
		String size = args.value("size");
		String status = args.value("status");
		XmlDocMaker dm = new XmlDocMaker("args");
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
		XmlDoc.Element r = executor().execute("shopping.cart.list", dm.root());
		List<XmlDoc.Element> ces = r.elements("cart");
		if (ces != null) {
			for (XmlDoc.Element ce : ces) {
				String name = ce.value("@name");
				if (name != null) {
					w.add("cart",
							new String[] { "scid", ce.value("@id"), "name", name, "status",
									ce.value("@status") });
				} else {
					w.add("cart", new String[] { "scid", ce.value("@id"), "status", ce.value("@status") });
				}

			}
		}
		XmlDoc.Element cursorElement = r.element("cursor");
		if ((idx != null || size != null) && cursorElement != null) {
			w.add(cursorElement);
		}
		XmlDoc.Element sizeElement = r.element("size");
		if ((idx != null || size != null) && sizeElement != null) {
			w.add(sizeElement);
		}
	}
}
