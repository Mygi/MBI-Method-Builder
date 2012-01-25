package nig.mf.plugin;

import nig.mf.plugin.util.AssetUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

/**
 * 
 * @author Wilson Liu
 *
 */
public class SvcAssetIdGet extends PluginService {
	private Interface _defn;

	public SvcAssetIdGet() {

		_defn = new Interface();
		_defn.add(new Element("cid", CiteableIdType.DEFAULT, "the citable id of the asset.", 1, 1));

	}

	public String name() {
		return "nig.asset.id.get";
	}

	public String description() {
		return "returns the id of the local asset.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String cid = args.value("cid");
		String id = AssetUtil.getId(executor(), cid);
		w.add("id", id);

	}

}