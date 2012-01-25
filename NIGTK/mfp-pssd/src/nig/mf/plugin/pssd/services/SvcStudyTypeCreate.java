package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.*;
import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

public class SvcStudyTypeCreate extends PluginService {
	private Interface _defn;

	public SvcStudyTypeCreate() {
		_defn = new Interface();
		_defn.add(new Interface.Element("type",StringType.DEFAULT, "The (unique) type name of this study.", 1, 1));
		_defn.add(new Interface.Element("description",StringType.DEFAULT, "An arbitrary description for the type of study.", 0, 1));
	}

	public String name() {
		return "om.pssd.study.type.create";
	}

	public String description() {
		return "Registers a type of study with the local server. All studies are of a particular type.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		String type = args.value("type");
		String desc = args.value("description");
		
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("dictionary",Study.TYPE_DICTIONARY);
		dm.add("term",type);
		if ( desc != null ) {
			dm.add("definition",desc);
		}
		
		executor().execute("dictionary.entry.add",dm.root());
	}
	
}
