package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.sc.ShoppingCart;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.LongType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcShoppingCartOutputRetrieve extends PluginService {

	private Interface _defn;

	public SvcShoppingCartOutputRetrieve() throws Throwable {

		_defn = new Interface();
		_defn.add(new Interface.Element("scid", LongType.DEFAULT, "The id of the shopping cart.", 1, 1));
	}

	public String name() {

		return "om.pssd.shoppingcart.output.retrieve";
	}

	public String description() {

		return "Retrieve the shopping cart to the specified output.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_ACCESS;
	}

	public int minNumberOfOutputs() {

		return 1;
	}

	public int maxNumberOfOutputs() {

		return 1;
	}

	public void execute(XmlDoc.Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {

		ShoppingCart.retrieveOutput(executor(), args.value("scid"), outputs);
	}
}
