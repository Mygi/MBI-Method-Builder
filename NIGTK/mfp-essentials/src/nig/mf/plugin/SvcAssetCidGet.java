package nig.mf.plugin;

import nig.mf.plugin.util.AssetUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.AssetType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcAssetCidGet extends PluginService {

	private Interface _defn;

	public SvcAssetCidGet() {

		_defn = new Interface();
		_defn.add(new Element("id", AssetType.DEFAULT, "The asset id.", 1, 1));

	}

	public String name() {

		return "nig.asset.cid.get";

	}

	public String description() {

		return "Get the cid of the local asset.";

	}

	public Interface definition() {

		return _defn;

	}

	public Access access() {

		return ACCESS_ACCESS;

	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String id = args.value("id");
		String cid = AssetUtil.getCid(executor(), id);
		if (cid != null) {
			w.add("cid", cid);
		}

	}

}