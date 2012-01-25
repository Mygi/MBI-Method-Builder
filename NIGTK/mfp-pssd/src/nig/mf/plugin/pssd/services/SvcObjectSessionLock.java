package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.Asset;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcObjectSessionLock extends PluginService {

	private Interface _defn;

	public SvcObjectSessionLock() throws Throwable {

		_defn = new Interface();
		addInterface(_defn);
	}

	public static void addInterface(Interface defn) throws Throwable {

		defn.add(new Interface.Element("id", CiteableIdType.DEFAULT,
				"The identity of the pssd-object.", 1, 1));
		defn.add(new Interface.Element(
				"action",
				new EnumType(new String[] { "wait", "status", "exception" }),
				"Specifies the lock action. 'wait' will wait until released by others. 'status' will attempt to lock and indicate whether or not the lock was obtained. 'exception' will throw an exception if the lock cannot be gained. Defaults to 'wait'.",
				0, 1));
		defn.add(new Interface.Element(
				"timeout",
				IntegerType.DEFAULT,
				"Specifies the length of time in seconds the lock should be held before automatic release. If not specified, defaults to infinite.",
				0, 1));
		defn.add(new Interface.Element("comment", StringType.DEFAULT,
				"Reason for acquiring the lock.", 0, 1));
	}

	public Access access() {

		return ACCESS_MODIFY;
	}

	public Interface definition() {

		return _defn;
	}

	public String description() {

		return "Creates an exclusive and transient lock that belongs to the caller's session for the given pssd-object.";
	}

	public String name() {

		return "om.pssd.object.session.lock";
	}

	public void execute(Element args, Inputs inputs, Outputs outputs,
			XmlWriter w) throws Throwable {

		String id = args.value("id");
		XmlDoc.Element asset = Asset.getByCid(executor(), null, id);
		String assetId = asset.value("@id");
		args.element("id").setValue(assetId);
		XmlDoc.Element r = executor().execute("asset.session.lock", args);
		XmlDoc.Element locked = r.element("locked");
		if (locked != null) {
			String expiry = locked.value("@expiry");
			if (expiry != null) {
				w.push("lock", new String[] { "type", "transient", "object-id",
						id, "asset-id", assetId });
				w.add("expiry", expiry);
				w.pop();
			} else {
				w.add("lock", new String[] { "type", "transient", "object-id",
						id, "asset-id", assetId });
			}
		}
	}
}