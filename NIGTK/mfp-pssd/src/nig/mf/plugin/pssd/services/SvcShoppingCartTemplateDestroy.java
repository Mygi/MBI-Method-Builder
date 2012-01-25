package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.sc.Template;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.BooleanType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcShoppingCartTemplateDestroy extends PluginService {

	private Interface _defn;

	public SvcShoppingCartTemplateDestroy() throws Throwable {

		_defn = new Interface();
		_defn.add(new Interface.Element(
				"force",
				BooleanType.DEFAULT,
				"set to true to destroy all shopping carts that still use this template. Defaults to false.",
				0, 1));
	}

	public String name() {

		return "om.pssd.shoppingcart.template.destroy";
	}

	public String description() {

		return "Destroy the shopping cart template for PSSD data model.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_ADMINISTER;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {

		boolean force = args.booleanValue("force", false);
		Template.destroy(executor(), Template.NAME, force);
	}

}