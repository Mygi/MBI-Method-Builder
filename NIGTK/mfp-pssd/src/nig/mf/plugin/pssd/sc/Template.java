package nig.mf.plugin.pssd.sc;

import java.util.List;
import java.util.Vector;

import nig.mf.plugin.pssd.Sink;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

public class Template {

	public static final String NAME = "pssd";

	public static String create(ServiceExecutor executor, String name) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("name", name);
		dm.add("self-serviced", true);
		dm.add("layout", Layout.DEFAULT_TYPE.toString());
		dm.add("layout-pattern", Layout.DEFAULT_PATTERN);
		List<String> sinkRootUrls = Sink.getSinkRootUrls(executor);
		if (sinkRootUrls != null) {
			if (!sinkRootUrls.isEmpty()) {
				String rootUrl = sinkRootUrls.get(0);
				dm.add("delivery-method", DeliveryMethod.deposit.toString());
				dm.add("delivery-destination", rootUrl);
				if (sinkRootUrls.size() > 1) {
					dm.push("alt-delivery-destinations");
					for (int i = 1; i < sinkRootUrls.size(); i++) {
						dm.add("directory", sinkRootUrls.get(i));
					}
					dm.pop();
				}
			}
		} else {
			dm.add("delivery-method", DeliveryMethod.download.toString());
		}
		XmlDoc.Element r = executor.execute("shopping.cart.template.create", dm.root());
		return r.value("id");
	}

	public static void destroy(ServiceExecutor executor, String tplName, boolean force) throws Throwable {

		List<String> scids = getCartsByTemplate(executor, tplName);
		if (scids != null && force == true) {
			ShoppingCart.destroy(executor, scids);
		}
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("name", tplName);
		executor.execute("shopping.cart.template.destroy", dm.root());

	}

	public static boolean isTemplateInUse(ServiceExecutor executor, String name) throws Throwable {

		return getCartsByTemplate(executor, name) != null;
	}

	public static List<String> getCartsByTemplate(ServiceExecutor executor, String name) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("size", "infinity");
		dm.add("list-all", true);
		XmlDoc.Element r = executor.execute("shopping.cart.describe", dm.root());
		List<XmlDoc.Element> sces = r.elements("cart");
		if (sces != null) {
			if (!sces.isEmpty()) {
				List<String> scids = new Vector<String>();
				for (XmlDoc.Element sce : sces) {
					String template = sce.value("template");
					if (name.equals(template)) {
						scids.add(sce.value("@id"));
					}
				}
				if (!scids.isEmpty()) {
					return scids;
				}
			}
		}
		return null;
	}

	public static boolean exists(ServiceExecutor executor, String tplName) throws Throwable {

		XmlDoc.Element r = executor.execute("shopping.cart.template.describe");
		return r.element("shopping-cart-template[@name='" + tplName + "']") != null;
	}

	public static String getIdByName(ServiceExecutor executor, String tplName) throws Throwable {

		XmlDoc.Element r = executor.execute("shopping.cart.template.describe");
		XmlDoc.Element te = r.element("shopping-cart-template[@name='" + tplName + "']");
		if (te != null) {
			return te.value("@id");
		}
		return null;
	}

}
