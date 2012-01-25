package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.Asset;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcObjectLock extends PluginService {

	private Interface _defn;

	public SvcObjectLock() throws Throwable {

		_defn = new Interface();
		addInterface(_defn);
	}

	public static void addInterface(Interface defn) throws Throwable {

		defn.add(new Interface.Element("id", CiteableIdType.DEFAULT, "The identity of the pssd-object.", 1, 1));
		defn.add(new Interface.Element("descend", BooleanType.DEFAULT,
				"Controls whether locks should also be acquired for all descendants. The default is set to true.", 0, 1));
		defn.add(new Interface.Element(
				"timeout",
				IntegerType.DEFAULT,
				"Specifies the length of time in seconds the lock should be held before automatic release. If not specified, defaults to infinite.",
				0, 1));
		defn.add(new Interface.Element("comment", StringType.DEFAULT, "Reason for acquiring the lock.", 0, 1));
	}

	public Access access() {

		return ACCESS_MODIFY;
	}

	public Interface definition() {

		return _defn;
	}

	public String description() {

		return "Locks a pssd-object unless it is locked by another user. Returns the universally unique identifier (UUID) lock for the object. If this object was previously locked by this user, then the same lock identifier is returned. To change the lock type or expiry, use the service 'om.pssd.object.relock'.";
	}

	public String name() {

		return "om.pssd.object.lock";
	}

	public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {

		String id = args.value("id");
		XmlDoc.Element asset = Asset.getByCid(executor(), null, id);
		String assetId = asset.value("@id");
		args.element("id").setValue(assetId);
		XmlDoc.Element r = executor().execute("asset.lock", args);
		XmlDoc.Element uuid = r.element("uuid");
		if (uuid != null) {
			XmlDoc.Element lock = describeAssetLock(executor(), assetId, null, null, null);
			if (lock != null) {
				lock.add(new XmlDoc.Attribute("object-id", id));
				lock.add(new XmlDoc.Attribute("asset-id", assetId));
				lock.remove(lock.element("asset"));
				w.add(lock);
				return;
			}
		}
		throw new Exception("Failed to lock object " + id);
	}

	private static XmlDoc.Element describeAssetLock(ServiceExecutor executor, String assetId, String uuid,
			String domain, String user) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", assetId);
		if (domain != null && user != null) {
			dm.push("user");
			dm.add("domain", domain);
			dm.add("user", user);
			dm.pop();
		}
		if (uuid != null) {
			dm.add("uuid", uuid);
		}
		XmlDoc.Element r = executor.execute("asset.lock.describe", dm.root());
		return r.element("lock");
	}
}
