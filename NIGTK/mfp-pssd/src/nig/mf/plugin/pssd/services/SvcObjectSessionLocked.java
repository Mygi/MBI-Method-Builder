package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.PSSDObject;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcObjectSessionLocked extends PluginService {

	private Interface _defn;

	public SvcObjectSessionLocked() throws Throwable {

		_defn = new Interface();
		addInterface(_defn);
	}

	public static void addInterface(Interface defn) throws Throwable {

		defn.add(new Interface.Element("id", CiteableIdType.DEFAULT, "The identity of the pssd-object.", 1, 1));
	}

	public Access access() {

		return ACCESS_ACCESS;
	}

	public Interface definition() {

		return _defn;
	}

	public String description() {

		return "Checks if the object has session lock applied.";
	}

	public String name() {

		return "om.pssd.object.session.locked";
	}

	public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {

		String id = args.value("id");
		boolean locked = PSSDObject.hasSessionLock(executor(), null, id);
		w.add("locked", new String[] { "id", id }, locked);
	}

}