package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.*;
import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

public class SvcStudyTypeDestroy extends PluginService {
	private Interface _defn;

	public SvcStudyTypeDestroy() {
		_defn = new Interface();
		_defn.add(new Interface.Element("type",StringType.DEFAULT, "The (unique) type name of this study.", 1, 1));
	}

	public String name() {
		return "om.pssd.study.type.destroy";
	}

	public String description() {
		return "Removes a type of study on the local server.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		String type = args.value("type");
		
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("dictionary",Study.TYPE_DICTIONARY);
		dm.add("term",type);
		
		executor().execute("dictionary.entry.remove",dm.root());
	}
	
}
