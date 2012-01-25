package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.method.ExMethod;
import nig.mf.plugin.pssd.method.ExMethodStepStatus;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlDocWriter;
import arc.xml.XmlWriter;

public class SvcExMethodStepUpdate extends PluginService {
	private Interface _defn;

	public SvcExMethodStepUpdate() {
		_defn = new Interface();
		addInterfaceDefn(_defn);
	}

	public static void addInterfaceDefn(Interface defn) {
		defn.add(new Interface.Element("id",CiteableIdType.DEFAULT,"The identity of the ExMethod object. Must be managed by the local server.",0,1));
		defn.add(new Interface.Element("step",CiteableIdType.DEFAULT,"The step within the method(s).",1,1));
		defn.add(new Interface.Element("state",new EnumType(new String[] { ExMethodStepStatus.STATUS_INCOMPLETE, ExMethodStepStatus.STATUS_WAITING, ExMethodStepStatus.STATUS_COMPLETE, ExMethodStepStatus.STATUS_ABANDONED }),"The current state of execution.",1,1));
		defn.add(new Interface.Element("notes",StringType.DEFAULT,"Notes to associate with the step, if any.",0,1));
	}
	
	public String name() {
		return "om.pssd.ex-method.step.update";
	}

	public String description() {
		return "Updates information for a step in a locally managed ExMethod.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		
		// Set distributed citeable ID for the ExMethod.  
		DistributedAsset dID = new DistributedAsset(args.element("id"));
		
		// Check a few things...
		String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dID);
		if (type==null) {
			throw new Exception("The asset associated with " + dID.toString() + " does not exist");
		}
		if ( !type.equals(ExMethod.TYPE) ) {
			throw new Exception("Object " + dID.getCiteableID() + " [type=" + type + "] is not a " + ExMethod.TYPE);
		}
		if (dID.isReplica()) {
			throw new Exception ("The supplied ExMethod is a replica and this service cannot modify it.");
		}

		// The step
		String stepPath   = args.value("step");

		// Update local object
		updateExMethod(executor(), dID.getCiteableID(), stepPath, args);
	}

	/**
	 * Update the Method-related meta-data in a local ExMethod.   IT is the callers responsibility to 
	 * ensure that the object to be updated is consistent with Federation policy
	 * 
	 * @param executor
	 * @param emid  ExMethod CID
	 * @param sid  STep path
	 * @param args
	 * @throws Throwable
	 */
	public static void updateExMethod(ServiceExecutor executor,String emid,String stepPath,XmlDoc.Element args) throws Throwable {
		String state = args.value("state");
		String notes = args.value("notes");
		
		ExMethodStepStatus status = new ExMethodStepStatus(stepPath,state);
		status.setNotes(notes);
		
		// Update the status of the executing method..
		//
		// TODO - consider making this more granular -- to the asset level.
		//        although this will be hardly noticable.
		synchronized ( ExMethod.LOCK ) {
			XmlDocMaker dm = new XmlDocMaker("args");
			
			dm.add("cid",emid);
			dm.add("pdist",0);                 // Force local 		
			XmlDoc.Element r = executor.execute("asset.get",dm.root());
			
			ExMethod em = new ExMethod();
			em.parseAssetMeta(r.element("asset"));

			em.setStepStatus(status);
			
			dm = new XmlDocMaker("args");
			XmlDocWriter dw = new XmlDocWriter(dm);
			
			dw.add("cid", emid);
			dw.push("meta",new String[] { "action", "replace" });
			em.saveAssetMeta(dw);
			dw.pop();
			
			executor.execute("asset.set",dm.root());
		}
	}
	
}
