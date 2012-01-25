package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.*;

import java.util.*;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

public class SvcTypeMetadataSet extends PluginService {
	private Interface _defn;

	public SvcTypeMetadataSet() {
		_defn = new Interface();
		_defn.add(new Interface.Element("type",new EnumType(new String[] { Project.TYPE, Subject.TYPE, Study.TYPE, DataSet.TYPE, DataObject.TYPE, RSubject.TYPE, RSubject.TYPE_PRIVATE }), "The type to add definitions of metadata.", 1, 1));
		
		Interface.Element de = new Interface.Element("mtype",StringType.DEFAULT, "The type of the metadata to associate with this object type.", 1, Integer.MAX_VALUE);
		de.add(new Interface.Attribute("requirement",new EnumType(new String[] { "mandatory", "optional" }), "Is this metadata required by the object or is is optional. Defaults to optional.", 0));
		_defn.add(de);
		//
		Interface.Element de2 = new Interface.Element("append", BooleanType.DEFAULT, "Append (rather than replace) the metadata association. Defaults to False (replace).  If appending, new associations that pre-exist will not be added again.", 0, 1);

		_defn.add(de2);
	}

	public String name() {
		return "om.pssd.type.metadata.set";
	}

	public String description() {
		return "Associates existing metadata types with the specified object type on the local server.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		String type = args.stringValue("type");
		Boolean append = args.booleanValue("append", false);
		
		// Fetch old associations if desired
		XmlDoc.Element oldMeta = null;
		if (append) {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("type", type);
			oldMeta = executor().execute("om.pssd.type.metadata.describe", dm.root());
		}
		
		// Prepare container
		XmlDocMaker dm = new XmlDocMaker("args");	
		String modelName = Metadata.modelNameForType(type);
		dm.add("name",modelName);
		
		// Create, if not found.
		dm.add("create","true");
		//
		dm.push("entity",new String[] { "root", "true", "type", type });
		dm.add("member", "*");		
		dm.push("template",new String[] { "ns", modelName });


		// Add old if appending
		boolean some = false;
		if (append && oldMeta != null) {
			Collection<XmlDoc.Element> mes = oldMeta.elements("metadata");
			if (mes != null) {
				some = true;
				for (XmlDoc.Element el : mes) {
					String docType = el.attribute("type").value();
					String requirement = el.attribute("requirement").value();
					addRecord (dm, docType, requirement);	
				}
			}			
		}

		
		// Add new
		Collection<XmlDoc.Element> mes = args.elements("mtype");
		if ( mes != null ) {	
			some = true;
			for (XmlDoc.Element me : mes) {			
				String mtype = me.value();
				String req = me.stringValue("@requirement","optional");
				
				// Don't add if appending and this one pre-exists
				boolean addIt = true;
				if (oldMeta!=null) {
					String tt = "metadata[@type='"+mtype+"']";
					XmlDoc.Element t = oldMeta.element(tt);
					if (t!=null) addIt = false;
				}
				if (addIt) addRecord(dm, mtype, req);
			}
		}

		dm.pop();
		dm.pop();
		
		// Do it
		if (some) executor().execute("asset.model.update",dm.root());
		
	

		// Remove existing tags before we add (new) ones.
		/*
		SvcTypeMetadataRemove.removeTagsForType(executor(),type,mtype);
		
		String tag = Metadata.tagForType(type);
		String reqTag = Metadata.requirementTagForType(type,req);
		
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("type",mtype);
		dm.add("tag",tag);
		dm.add("tag",reqTag);

		executor().execute("asset.doc.type.tag.add",dm.root());
		*/
	}
	
	private void addRecord (XmlDocMaker dm, String mtype, String req) throws Throwable {
		dm.push("metadata");
		dm.add("definition",new String[] { "requirement", req },mtype);
		dm.pop();
	}
}
