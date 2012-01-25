package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

import nig.mf.plugin.pssd.*;
import nig.mf.plugin.pssd.method.*;

import java.util.*;

public class SvcTypeMetadataDescribe extends PluginService {
	private Interface _defn;

	public SvcTypeMetadataDescribe() {
		_defn = new Interface();
		_defn.add(new Interface.Element("proute", CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages this meta-data.  If not supplied, then the meta-data will be fetched from the local server.", 0, 1));
		_defn.add(new Interface.Element("type",new EnumType(new String[] { Project.TYPE, Subject.TYPE, ExMethod.TYPE, Study.TYPE, DataSet.TYPE, DataObject.TYPE, RSubject.TYPE, RSubject.TYPE_PRIVATE }), "The type to retrieve definitions of metadata.", 1, 1));
	}

	public String name() {
		return "om.pssd.type.metadata.describe";
	}

	public String description() {
		return "Describes the metadata that can be associated with a specified type of object.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		String type = args.stringValue("type");
		String proute = args.stringValue("proute");
		
		String mn = Metadata.modelNameForType(type);
		
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("name",mn);

		// Fetch the meta-data from the specified server.
		XmlDoc.Element r = executor().execute(new ServerRoute(proute), "asset.model.metadata.describe",dm.root());
		Collection<XmlDoc.Element> mes = r.elements("model/metadata");
		if ( mes != null ) {
			for (XmlDoc.Element me : mes) {
				w.add(me);
			}
		}
	}
}
