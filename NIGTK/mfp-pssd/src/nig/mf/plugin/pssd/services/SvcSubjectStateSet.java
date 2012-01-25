package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.*;
import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;
import nig.mf.plugin.pssd.method.*;
import nig.mf.pssd.plugin.util.DistributedAsset;

/**
 * This service does not appear to be utilised within our system.  It has similar functionality
 * to  om.pssd.ex-method.subject.step.update which is utilised.

 * @author Jason
 *
 */
public class SvcSubjectStateSet extends PluginService {
	private Interface _defn;

	public SvcSubjectStateSet() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the Subject.", 1, 1));
		
		_defn.add(new Interface.Element("ns",StringType.DEFAULT,"The namespace of the metadata. Must be specified if 'method' is not specified.",0,1));
		
		Interface.Element me = new Interface.Element("method",CiteableIdType.DEFAULT, "The identity of the ExMethod that is updating the state.", 0, 1);
		me.add(new Interface.Attribute("step",CiteableIdType.DEFAULT,"The step within the method that resulted in this metadata.",1));
		_defn.add(me);

		/*
		Interface.Element we = new Interface.Element("workflow",XmlDocType.DEFAULT,"Workflow information.",0,1);
		we.add(new Interface.Element("status",new EnumType(new String[] { "incomplete", "waiting", "complete", "abandoned" }),"The status of the workflow",1,1));
		we.add(new Interface.Element("notes",StringType.DEFAULT,"Arbitrary workflow notes.",0,1));
		_defn.add(we);
	*/
		
		me = new Interface.Element("meta",XmlDocType.DEFAULT,"Optional metadata - a list of asset documents.",0,1);
		me.setIgnoreDescendants(true);

		_defn.add(me);
	}

	public String name() {
		return "om.pssd.subject.state.set";
	}

	public String description() {
		return "Set or updates an existing state for the subject.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		
		// Set distributed citeable ID for the local SUbject
		DistributedAsset dSID = new DistributedAsset(args.element("id"));
		
		// Check a few things...
		String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dSID);
		if (type==null) {
			throw new Exception("The asset associated with " + dSID.toString() + " does not exist");
		}
		if ( !type.equals(Subject.TYPE) ) {
			throw new Exception("Object " + dSID.getCiteableID() + " [type=" + type + "] is not a " + Subject.TYPE);
		}
		if (dSID.isReplica()) {
			throw new Exception ("The supplied Subject is a replica and this service cannot update its state.");
		}
 			
		// TODO -- must be a member of the group.
		
		String ns;
		String action = "replace";
		
		String mid = args.value("method");
		if ( mid == null ) {
			ns = args.value("ns");
			if ( ns == null ) {
				throw new Exception("Namespace (ns) or method must be specified.");
			}
		} else {
			ns = ExMethod.metaNamespace(mid,args.value("method/@step"));
		}
		
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid",dSID.getCiteableID());
		dm.push("meta",new String[] { "action", action });
		dm.add(args.element("meta"),false);
		dm.pop();
		
		executor().execute("asset.set",dm.root());
		
	}
	

	
	
	private static void checkMetaIsValid(ServiceExecutor executor,XmlDoc.Element me) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.push("document");
		dm.add(me,false);
		dm.pop();

		executor.execute("asset.doc.type.validate",dm.root());
	}
	
}
