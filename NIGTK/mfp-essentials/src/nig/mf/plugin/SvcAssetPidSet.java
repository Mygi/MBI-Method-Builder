package nig.mf.plugin;

import nig.mf.plugin.util.AssetUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcAssetPidSet extends PluginService {
	private Interface _defn;

	public SvcAssetPidSet() {

		_defn = new Interface();
		_defn.add(new Element("cid", CiteableIdType.DEFAULT, "The citeable id of the asset.", 1, 1));
		_defn.add(new Element("pid", CiteableIdType.DEFAULT, "The citeable id of the new parent.", 1, 1));
		_defn.add(new Element("recursive", BooleanType.DEFAULT,
				"Change descendant assets recursively. Defaults to true", 0, 1));

	}

	public String name() {
		return "nig.asset.pid.set";
	}

	public String description() {
		return "Change/set the parent of a local asset.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String cid = args.value("cid");
		String pid = args.value("pid");
		Boolean recursive = args.booleanValue("recursive", true);

		String newCid = AssetUtil.changeParent(executor(), cid, pid, recursive);
		if (newCid != null) {
			w.add("cid", new String[] { "old", cid, "recursive", recursive.toString() }, newCid);
		}

	}

}