package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;
import nig.mf.plugin.pssd.*;

import java.util.*;

public class SvcStudyTypeDescribe extends PluginService {
	private Interface _defn;

	public SvcStudyTypeDescribe() {
		_defn = new Interface();
		_defn.add(new Interface.Element("type",StringType.DEFAULT, "The (unique) type name of this study. If not specified, then all types described.", 0, 1));
	}

	public String name() {
		return "om.pssd.study.type.describe";
	}

	public String description() {
		return "Describes the types of studies that can be created on the local server.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		String type = args.value("type");
		
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("dictionary",Study.TYPE_DICTIONARY);
		
		XmlDoc.Element r;
		if ( type == null ) {
			r = executor().execute("dictionary.entries.describe",dm.root());
		} else {
			dm.add("term",type);
			r = executor().execute("dictionary.entry.describe",dm.root());
		}
		
		Collection<XmlDoc.Element> ees = r.elements("entry");
		if ( ees != null ) {
			for (XmlDoc.Element ee : ees) {
				type = ee.value("term");
				String desc = ee.value("definition");
				
				w.push("type");
				w.add("name",type);
				if ( desc != null ) {
					w.add("description",desc);
				}
				w.pop();
			}
		}
	}
	
}
