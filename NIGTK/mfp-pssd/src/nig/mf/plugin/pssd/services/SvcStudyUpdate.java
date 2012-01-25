package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.*;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.*;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

public class SvcStudyUpdate extends PluginService {
	private Interface _defn;

	public SvcStudyUpdate() throws Throwable {
		_defn = new Interface();
		_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the Study (managed by the local server).", 1, 1));
		_defn.add(new Interface.Element("type",new DictionaryEnumType(Study.TYPE_DICTIONARY), "The type of the study (merged with old). If not specified, then method must be specified.", 0, 1));
		
		Interface.Element me = new Interface.Element("method",XmlDocType.DEFAULT,"Details about the ex-method for which this acquisition was made.",0,1);
		me.add(new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the ex-method.", 0, 1));
		me.add(new Interface.Element("step",CiteableIdType.DEFAULT,"The execution step within the ex-method",0,1));
		_defn.add(me);	

	    _defn.add(new Interface.Element("name",StringType.DEFAULT, "The name of this study (merged with old)", 0, 1));
		_defn.add(new Interface.Element("description",StringType.DEFAULT, "An arbitrary description for the study.(merged with old)", 0, 1));		
		//
		me = new Element("meta",XmlDocType.DEFAULT,"Optional metadata - a list of asset documents. If the metadata belongs to a method, then it must have an 'ns' attribute which corresponds to the 'ExMethod CID_Step ID' (e.g. 1.1.1.1_1.1).",0,1);
		me.add(new Interface.Attribute("action",new EnumType(new String[] { "add", "merge", "replace", "remove" }),"Action to perform when modifying meta information. Defaults to 'replace'. 'add' means append, 'merge' means combine, 'replace' means to replace matching, 'remove' means to remove the specified document elements.",0));
        me.setIgnoreDescendants(true);
		_defn.add(me);
	}

	public String name() {
		return "om.pssd.study.update";
	}

	public String description() {
		return "Updates a PSSD study on the local server.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		
		// Set distributed citeable ID for the local Study
		String id = args.value("id");		
		DistributedAsset dSID = new DistributedAsset(null, id);
		
		// Check a few things...
		String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dSID);
		if (type==null) {
			throw new Exception("The asset associated with " + dSID.toString() + " does not exist");
		}
		if ( !type.equals(Study.TYPE) ) {
			throw new Exception("Object " + dSID.getCiteableID() + " [type=" + type + "] is not a " + Study.TYPE);
		}
		if (dSID.isReplica()) {
			throw new Exception ("The supplied Study is a replica and this service cannot modify it.");
		}
		
		// TODO -- check have team member role..
	
		String name = args.value("name");
		String studyType = args.value("type");
		String description = args.value("description");	
		String step = args.value("method/step");
		String exMethod = args.value("method/id");
		
		// Update local Study object. 
		Study.update(executor(), id, studyType, name, description, exMethod, step, args.element("meta"));
	}
	
	
}
