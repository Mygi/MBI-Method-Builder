package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.*;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.*;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.*;
import arc.xml.*;


/**
 * This service does not appear to be utilised within our system.  The document type pssd-state
 * has never been defined/created in our system.  So looks like a path JL abandoned.
 * 
 * @author Jason
 *
 */
public class SvcSubjectStateCreate extends PluginService {
	private Interface _defn;

	public SvcSubjectStateCreate() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the Subject.", 1, 1));
		
		Interface.Element me = new Interface.Element("method",CiteableIdType.DEFAULT, "The identity of the Method.", 0, 1);
		me.add(new Interface.Attribute("step",IntegerType.POSITIVE_ONE,"The step within the method that resulted in this Study.",1));
		_defn.add(me);
		//
		Interface.Element we = new Interface.Element("workflow",XmlDocType.DEFAULT,"Workflow information.",0,1);
		we.add(new Interface.Element("status",new EnumType(new String[] { "incomplete", "waiting", "complete", "abandoned" }),"The status of the workflow",1,1));
		we.add(new Interface.Element("notes",StringType.DEFAULT,"Arbitrary workflow notes.",0,1));
		_defn.add(we);
		
		me = new Element("meta",XmlDocType.DEFAULT,"Optional metadata - a list of asset documents.",0,1);
		me.setIgnoreDescendants(true);

		_defn.add(me);
	}

	public String name() {
		return "om.pssd.subject.state.create";
	}

	public String description() {
		return "Creates a (new) state for the specific Subject on the local server.";
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
			throw new Exception ("The supplied Subject is a replica and this service cannot modify it.");
		}
 	
		// Set state
		createState (executor(), dSID.getCiteableID(), args);
	}

	private static void createState (ServiceExecutor executor, String id, XmlDoc.Element args) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", id);
		dm.add("pdist",0);                 // Force local 	
		XmlDoc.Element r = executor.execute("asset.get",dm.root());
		XmlDoc.Element ses = r.element("asset/meta/pssd-state");
		if ( ses == null ) {
			ses = new XmlDoc.Element("pssd-state");
		}

		int nid = ses.count("state") + 1;

		// 
		XmlDoc.Element se = new XmlDoc.Element("state");
		se.add(new XmlDoc.Attribute("id",String.valueOf(nid)));
		ses.add(se);

		XmlDoc.Element me = args.element("method");
		if ( me != null ) {
			se.add(me);
		}

		XmlDoc.Element we = args.element("workflow");
		if ( we != null ) {
			se.add(we);
		}

		me = args.element("meta");
		if ( me != null ) {
			// Check the meta is indeed conformant to the prescribed definitions..
			checkMetaIsValid(executor,me);
			se.add(me);
		}

		dm.push("meta");
		dm.add(ses);
		dm.pop();

		// Do it
		executor.execute("asset.set",dm.root());
		}
	
	private static void checkMetaIsValid(ServiceExecutor executor,XmlDoc.Element me) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.push("document");
		dm.add(me,false);
		dm.pop();

		executor.execute("asset.doc.type.validate",dm.root());
	}
	
}
