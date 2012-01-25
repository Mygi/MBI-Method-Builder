package nig.mf.plugin.pssd.services;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import nig.mf.plugin.pssd.sc.ShoppingCart;
import nig.mf.plugin.pssd.sc.Status;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.IntegerType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcShoppingCartCleanup extends PluginService {

	private Interface _defn;

	public SvcShoppingCartCleanup() throws Throwable {

		_defn = new Interface();
		Interface.Element e = new Interface.Element("before",
				IntegerType.DEFAULT,
				"The shopping cart finished before the specified time frame.",
				1, 1);
		e.add(new Interface.Attribute("unit",
				new arc.mf.plugin.dtype.EnumType(new String[] { "year",
						"month", "week", "day", "hour", "minute" }),
				"The time unit", 1));
		_defn.add(e);

	}

	public String name() {

		return "om.pssd.shoppingcart.cleanup";
	}

	public String description() {

		return "Clean up the shopping carts finished the specified days ago.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {

		String unit = args.value("before/@unit");
		int n = args.intValue("before");
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		if (unit.equalsIgnoreCase("year")) {
			cal.add(Calendar.YEAR, -n);
		} else if (unit.equalsIgnoreCase("month")) {
			cal.add(Calendar.MONTH, -n);
		} else if (unit.equalsIgnoreCase("day")) {
			cal.add(Calendar.DATE, -n);
		} else if (unit.equalsIgnoreCase("hour")) {
			cal.add(Calendar.HOUR, -n);
		} else if (unit.equalsIgnoreCase("minute")) {
			cal.add(Calendar.MINUTE, -n);
		}
		Date d = cal.getTime();

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("status", Status.aborted);
		dm.add("status", Status.data_ready);
		dm.add("status", Status.rejected);
		dm.add("status", Status.error);
		dm.add("status", Status.withdrawn);
		dm.add("size", "infinity");
		
		XmlDoc.Element r = executor().execute("shopping.cart.describe", dm.root());
		List<XmlDoc.Element> ces = r.elements("cart");
		if(ces!=null){
			for(XmlDoc.Element ce :ces){
				String id = ce.value("@id");
				Date changed = ce.dateValue("status/@changed");
				if(changed.before(d)){
					ShoppingCart.destroy(executor(), id);
				}
			}
		}
	}
}