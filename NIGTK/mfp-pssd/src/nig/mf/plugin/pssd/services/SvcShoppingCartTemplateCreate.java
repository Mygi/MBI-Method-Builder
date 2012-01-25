package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.sc.Template;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.BooleanType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcShoppingCartTemplateCreate extends PluginService {

	private Interface _defn;

	public SvcShoppingCartTemplateCreate() throws Throwable {

		_defn = new Interface();
		_defn.add(new Interface.Element(
				"replace",
				BooleanType.DEFAULT,
				"Sets to true to destroy and re-create the template if the template named pssd already exists. Defaults to false.",
				0, 1));
	}

	public String name() {

		return "om.pssd.shoppingcart.template.create";
	}

	public String description() {

		return "Create the shopping cart template for PSSD data model.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_ADMINISTER;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {
		boolean replace = args.booleanValue("replace", false);
		String id = Template.getIdByName(executor(), Template.NAME);
		if(id!=null){
			if(replace){
				Template.destroy(executor(), Template.NAME, true);
				id = Template.create(executor(), Template.NAME);
			}
		} else {
			id = Template.create(executor(), Template.NAME);
		}
		w.add("tid",new String[]{"name", Template.NAME}, id);
	}

}