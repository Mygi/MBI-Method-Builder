package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.Asset;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcObjectUnlock extends PluginService {

	private Interface _defn;

	public SvcObjectUnlock() throws Throwable {

		_defn = new Interface();
		addInterface(_defn);
	}

	public static void addInterface(Interface defn) throws Throwable {

		defn.add(new Interface.Element("id", CiteableIdType.DEFAULT,
				"The identity of the pssd-object.", 1, 1));
		defn.add(new Interface.Element(
				"descend",
				BooleanType.DEFAULT,
				"Controls whether locks should be released for all descendants. The default is set to true.",
				0, 1));
		defn.add(new Interface.Element("uuid", StringType.DEFAULT,
				"The univerally unique identifier for the lock.", 0, 1));
	}

	public Access access() {

		return ACCESS_MODIFY;
	}

	public Interface definition() {

		return _defn;
	}

	public String description() {

		return "Unlocks a pssd-object provided you are the owner, or have permission to unlock locks held by others.";
	}

	public String name() {

		return "om.pssd.object.unlock";
	}

	public void execute(Element args, Inputs inputs, Outputs outputs,
			XmlWriter w) throws Throwable {

		String id = args.value("id");
		XmlDoc.Element asset = Asset.getByCid(executor(), null, id);
		String assetId = asset.value("@id");
		args.element("id").setValue(assetId);
		executor().execute("asset.unlock", args);
	}

}