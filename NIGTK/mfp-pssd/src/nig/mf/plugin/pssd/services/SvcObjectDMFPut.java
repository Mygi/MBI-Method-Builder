package nig.mf.plugin.pssd.services;

import java.util.ArrayList;
import java.util.Collection;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

public class SvcObjectDMFPut extends PluginService {
	private Interface _defn;

	public SvcObjectDMFPut() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id", CiteableIdType.DEFAULT,
				"The identity of the root object.", 1, 1));
		_defn
				.add(new Interface.Element(
						"async",
						BooleanType.DEFAULT,
						"Set to true to perform asynchronous operation. Defaults to false, which is synchronous.",
						0, 1));
	}

	public String name() {
		return "om.pssd.dmf.put";
	}

	public String description() {
		return "Make the contents of the specified object and its descendants offline if they are stored in a SGI DMF system. Only operates on the local server.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {

		String id = args.value("id");
		if (nig.mf.pssd.CiteableIdUtil.getIdDepth(id) < 
				nig.mf.pssd.CiteableIdUtil.PROJECT_ID_DEPTH) {
			throw new Exception("Depth of citeable id must be at least "
					+nig.mf.pssd.CiteableIdUtil.PROJECT_ID_DEPTH + "(Project ID depth). ");
		}

		boolean async = false;
		if (args.value("async") != null) {
			if (args.value("async").equals("true")) {
				async = true;
			}
		}

		if (async) {
			XmlDocMaker doc = new XmlDocMaker("args");
			doc.add("where", "(cid starts with '" + id
					+ "' or cid = '" + id + "')");
			doc.add("size", "infinity");
			doc.add("pdist", 0);                 // FOrce to local server
			doc.add("action", "pipe");				
			doc.push("service",
					new String[] { "name", "asset.content.migrate" });
			doc.add("action", "move");
			doc.add("destination", "offline");
			doc.pop();
			executor().execute("asset.query", doc.root(), null, null);
		} else {
			ArrayList<String[]> commandList = new ArrayList<String[]>();
			int size = 100;
			int idx = 1;
			boolean complete = false;
			while (!complete) {
				XmlDocMaker doc = new XmlDocMaker("args");
				doc.add("where", "(cid starts with '" + id
						+ "' or cid = '" + id + "')");
				doc.add("action", "get-meta");
				doc.add("size", size);
				doc.add("pdist", 0);                 // FOrce to local server
				doc.add("idx", idx);
				XmlDoc.Element r = executor().execute("asset.query",
						doc.root(), null, null);
				Collection<String> urls = r.values("asset/content/url");
				if (urls != null) {
					ArrayList<String> command = new ArrayList<String>();
					command.add("dmput");
					command.add("-r");
					for (String url : urls) {
						if(url.startsWith("file:")){
							String file = url.substring(5);
							command.add(file); 
						}
					}
					commandList.add((String[])command.toArray(new String[command.size()]));
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
