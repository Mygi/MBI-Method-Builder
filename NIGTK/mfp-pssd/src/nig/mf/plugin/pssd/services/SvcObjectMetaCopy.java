package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.util.AssetUtil;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;


public class SvcObjectMetaCopy extends PluginService {

	private Interface _defn;

	public SvcObjectMetaCopy() {
		_defn = new Interface();
		_defn.add(new Element("from", CiteableIdType.DEFAULT,
				"The citeable id of the source PSSD object", 1, 1));
		_defn.add(new Element("to", CiteableIdType.DEFAULT,
				"The citeable id of the destination PSSD object", 1, 1));
		_defn.add(new Element("type", StringType.DEFAULT, "The name of the document (i.e. the Document Type) to copy", 1,Integer.MAX_VALUE));
	}

	public String name() {
		return "om.pssd.object.metadata.copy";
	}

	public String description() {
		return "Copies a meta-data document (including attributes) from one object to another.  No checks are made about consistent object types or whether the document pre-exists.  It just copies it.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		// Distributed ID for  DataSet. It must be a primary or we are not allowed
		// to modify it
		String cidIn = args.value("from");
		String cidOut = args.value("to");
		Collection<String> docTypes = args.values("type");
	
		// Do it
		AssetUtil.copyMetaData(executor(), docTypes, cidIn, cidOut);		
	}
}
