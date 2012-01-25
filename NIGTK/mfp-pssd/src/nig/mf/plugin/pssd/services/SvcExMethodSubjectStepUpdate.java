package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.method.ExMethod;
import nig.mf.pssd.plugin.util.DistributedAsset;
import nig.mf.pssd.plugin.util.DistributedAssetUtil;

import java.util.List;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

public class SvcExMethodSubjectStepUpdate extends PluginService {
	private Interface _defn;

	public SvcExMethodSubjectStepUpdate() {
		_defn = new Interface();
		SvcExMethodStepUpdate.addInterfaceDefn(_defn);
		
		Interface.Element me = new Interface.Element("ps-meta",XmlDocType.DEFAULT,"Optional metadata for the project Subject - a list of asset documents.",0,1);
		me.setIgnoreDescendants(true);
		_defn.add(me);

		me = new Interface.Element("rs-meta",XmlDocType.DEFAULT,"Optional metadata for the RSubject - a list of asset documents.",0,1);
		me.setIgnoreDescendants(true);
		_defn.add(me);

	}
	
	public String name() {
		return "om.pssd.ex-method.subject.step.update";
	}

	public String description() {
		return "Updates information for a subject-related step in a locally managed ExMethod.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
 
		// Set distributed citeable ID for the ExMethod.  It is local by definition.
		DistributedAsset dEID = new DistributedAsset(args.element("id"));
		
		// Check a few things...
		String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dEID);
		if (type==null) {
			throw new Exception("The asset associated with " + dEID.toString() + " does not exist");
		}
		if ( !type.equals(ExMethod.TYPE) ) {
			throw new Exception("Object " + dEID.getCiteableID() + " [type=" + type + "] is not an " + ExMethod.TYPE);
		}
		if (dEID.isReplica()) {
			throw new Exception ("The supplied ExMethod is a replica and this service cannot modify it.");
		}
	
		// Update Subject/RSubject meta; objects may be remote and generate
		// an excception so we do this first
		String stepPath = args.value("step");
		updateSubject(executor(), dEID, stepPath, args);
				
		// UPdate the local ExMethod
		SvcExMethodStepUpdate.updateExMethod(executor(), dEID.getCiteableID(), stepPath, args);

	}


	/**
	 * Update Subject meta
	 * 
	 * @param executor
	 * @param dEID  the ExMEthod
	 * @param dSID  the parent Subject
	 * @param stepPath
	 * @param args
	 * @throws Throwable
	 */
	protected static void updateSubject(ServiceExecutor executor, DistributedAsset dEID, 
			 String stepPath, XmlDoc.Element args) throws Throwable {
		
		// Find the parent Subject; could be anywhere in the federation
		Boolean readOnly = false;
		DistributedAsset dSID = dEID.getParentSubject(readOnly);
		if (dSID==null) {
			throw new Exception ("Cannot find the parent Subject of the ExMethod");
		}
	
		// The ExMethod must be local as we are allowed to change it.
		String mns = ExMethod.metaNamespace(dEID.getCiteableID(), stepPath);
		
		XmlDoc.Element psMeta = args.element("ps-meta");
		if ( psMeta != null && psMeta.nbElements() > 0 ) {
			if (!dSID.isLocal()) {
				throw new Exception ("The parent Subject " + dSID.getCiteableID() + " for this ExMethod is not " +
						    "managed by the local server and cannot be modified");
			}
			if (dSID.isReplica()) {
				throw new Exception ("The parent Subject " + dSID.getCiteableID() + " for this ExMethod is a replica " +
			    " and cannot be modified by this service");	
			}
			
			// Update the Subject
			setAssetMeta(executor, dSID, mns, psMeta);
		}
		
		XmlDoc.Element rsMeta = args.element("rs-meta");
		if ( rsMeta != null && rsMeta.nbElements() > 0 ) {
			
			// We need to find out which is the r-subject..
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("cid", dSID.getCiteableID());
			dm.add("pdist",0);                 // Force local 					
			XmlDoc.Element r = executor.execute("asset.get",dm.root());
			String rsid = r.value("meta/pssd-subject/r-subject");
			if ( rsid != null ) {
				
				// Find the Primary RSubject object
				DistributedAsset dRSID = DistributedAssetUtil.findPrimaryObject(executor, rsid);
				if (dRSID==null) {
					throw new Exception ("Cannot find the RSubject referred to by the Subject with cid " + dSID.getCiteableID());
				} else {
					if (!dRSID.isLocal()) {
						throw new Exception ("The RSubject " + dRSID.getCiteableID() + " specified by the parent Subject for this ExMethod is not " +
								    "managed by the local server and cannot be modified");
					}
					if (dRSID.isReplica()) {
						throw new Exception ("The RSubject " + dRSID.getCiteableID() + " specified by the parent Subject for this ExMethod is a replica " +
					    " and cannot be modified by this service");	
					}
				}
				
				// Update the RSubject
				setAssetMeta(executor, dRSID, mns, rsMeta);
			}
		}
	}
	
	/**
	 * Set metadata on an asset.  It is the caller's responsibility to ensure
	 * that federation policy is consisent with updating this object.
	 * 
	 * @param executor
	 * @param dID The distributed asset to update
	 * @param mns The metadata namespace.
	 * @param meta The metadata to set.
	 * @throws Throwable
	 */
	private static void setAssetMeta(ServiceExecutor executor, DistributedAsset dID, String mns,XmlDoc.Element meta) throws Throwable {
		// OK, replace all of the namespace attributes on the document
		// elements with the method namespace.
		List<XmlDoc.Element> des = meta.elements();
		for ( int i=0; i < des.size(); i++ ) {
			XmlDoc.Element de = des.get(i);
			XmlDoc.Attribute xa = de.attribute("ns");
			if ( xa == null ) {
				de.add(new XmlDoc.Attribute("ns",mns));
			} else {
				xa.setValue(mns);
			}
		} 
		
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", dID.getCiteableID());
		dm.push("meta",new String[] { "action", "replace" });
		dm.add(meta,false);
		dm.pop();
		
		// Update asset.  
		executor.execute(dID.getServerRouteObject(), "asset.set",dm.root());
		
	}
}
