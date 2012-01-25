package nig.mf.plugin;

import java.util.ArrayList;
import java.util.Collection;

import nig.mf.plugin.util.AssetUtil;
import nig.mf.pssd.plugin.util.CiteableIdUtil;
import arc.mf.plugin.Exec;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcAssetDMFPut extends PluginService {

	public static final String CMD_DMPUT = "dmput";

	private Interface _defn;

	public SvcAssetDMFPut() {

		_defn = new Interface();
		_defn.add(new Interface.Element("id", AssetType.DEFAULT, "The id of the asset.", 0, 1));
		_defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citeable id of the asset.", 0, 1));
		_defn.add(new Interface.Element("async", BooleanType.DEFAULT,
				"Set to true to perform asynchronous operation. Defaults to false, which is synchronous.", 0, 1));
		_defn.add(new Interface.Element("recursive", BooleanType.DEFAULT,
				"Set to true to include the citeable descendant assets. Defaults to false.", 0, 1));

	}

	public String name() {
		return "nig.asset.dmput";
	}

	public String description() {
		return "Make the contents of the specified local object and its descendants offline if they are stored in a SGI DMF system.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		boolean async = args.booleanValue("async", false);
		boolean recursive = args.booleanValue("recursive", false);

		String id = args.value("id");
		String cid = args.value("cid");
		if (id == null && cid == null) {
			throw new Exception("Neither id nor cid is specified.");
		}
		if (cid == null) {
			cid = AssetUtil.getCid(executor(), id);
		}
		if (cid != null) {
			if (CiteableIdUtil.getIdDepth(cid) < CiteableIdUtil.PROJECT_ID_DEPTH) {
				throw new Exception("Depth of citeable id must be at least " + CiteableIdUtil.PROJECT_ID_DEPTH
						+ "(Project ID depth). ");
			}
		}

		if (cid == null) {
			if (async) {
				XmlDocMaker doc = new XmlDocMaker("args");
				doc.add("id", id);
				doc.add("action", "move");
				doc.add("destination", "offline");
				executor().execute("asset.content.migrate", doc.root());
			} else {
				XmlDocMaker doc = new XmlDocMaker("args");
				doc.add("id", id);
				doc.add("pdist", 0);        // Force local
				XmlDoc.Element r = executor().execute("asset.get", doc.root());
				String url = r.value("asset/content/url");
				if (url != null) {
					Exec.exec(new String[] { CMD_DMPUT, url });
				}
			}
		} else {
			String query;
			if (recursive) {
				query = "(cid starts with '" + cid + "' or cid = '" + cid + "')";
			} else {
				query = "(cid = '" + cid + "')";
			}
			if (async) {
				XmlDocMaker doc = new XmlDocMaker("args");
				doc.add("where", query);
				doc.add("size", "infinity");
				doc.add("action", "pipe");
				doc.push("service", new String[] { "name", "asset.content.migrate" });
				doc.add("action", "move");
				doc.add("destination", "offline");
				doc.pop();
				doc.add("pdist", 0);        // Force local
				executor().execute("asset.query", doc.root());
			} else {
				ArrayList<String[]> commandList = new ArrayList<String[]>();
				int size = 100;
				int idx = 1;
				boolean complete = false;
				while (!complete) {
					XmlDocMaker doc = new XmlDocMaker("args");
					doc.add("where", query);
					doc.add("action", "get-meta");
					doc.add("size", size);
					doc.add("idx", idx);
					doc.add("pdist", 0);        // Force local
					XmlDoc.Element r = executor().execute("asset.query", doc.root());
					Collection<String> urls = r.values("asset/content/url");
					if (urls != null) {
						ArrayList<String> command = new ArrayList<String>();
						command.add("dmput");
						command.add("-r");
						for (String url : urls) {
							if (url.startsWith("file:")) {
								String file = url.substring(5);
								command.add(file);
							}
						}
						commandList.add((String[]) command.toArray(new String[command.size()]));
					}
					if (r.value("cursor/total/@complete").equals("true")) {
						complete = true;
					}
					idx += size;
				}
				for (String[] command : commandList) {
					Exec.exec(command);
				}
			}
		}
	}
}
