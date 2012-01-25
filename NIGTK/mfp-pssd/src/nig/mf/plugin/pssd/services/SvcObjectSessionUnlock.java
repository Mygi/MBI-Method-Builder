package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.Asset;
import nig.mf.plugin.pssd.PSSDObject;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcObjectSessionUnlock extends PluginService {

	private Interface _defn;

	public SvcObjectSessionUnlock() throws Throwable {

		_defn = new Interface();
		addInterface(_defn);
	}

	public static void addInterface(Interface defn) throws Throwable {

		defn.add(new Interface.Element("id", CiteableIdType.DEFAULT, "The identity of the pssd-object.", 1, 1));
	}

	public Access access() {

		return ACCESS_MODIFY;
	}

	public Interface definition() {

		return _defn;
	}

	public String description() {

		return "Removes the exclusive and transient lock held by the caller's session on the given pssd-ojbect.";
	}

	public String name() {

		return "om.pssd.object.session.unlock";
	}

	public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {

		String id = args.value("id");
		if (!PSSDObject.hasSessionLock(executor(), null, id)) {
			// Not locked. Return.
			return;
		}
		XmlDoc.Element asset = Asset.getByCid(executor(), null, id);
		String assetId = asset.value("@id");
		args.element("id").setValue(assetId);
		executor().execute("asset.session.unlock", args);
	}
}