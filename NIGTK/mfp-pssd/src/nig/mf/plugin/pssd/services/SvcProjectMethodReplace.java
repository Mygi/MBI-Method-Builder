package nig.mf.plugin.pssd.services;

import java.util.Collection;


import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;
import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.pssd.plugin.util.DistributedAsset;
import nig.mf.pssd.plugin.util.DistributedQuery;


/**
 * Highly specialised service function utilised when the Method structure changed to include
 * DICOM modality and when Study types were changed.
 * 
 * For the given Project CID
 * Finds all Methods registered
 * Updates the  ExMethod objects with a new instantiation of the Method
 * Updates the Study object with the new Study type, if it has changed.
 * 
 * 
 * @author nebk
 *
 */
public class SvcProjectMethodReplace extends PluginService {
	private Interface _defn;

	public SvcProjectMethodReplace() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT,"The identity of the Project (managed by the local server). Defaults to all Projects.",0,1));
	}

	public String name() {
		return "om.pssd.project.method.replace";
	}

	public String description() {
		return "Specialized management service to replace the Method in a Project and relevant children objects (Subject & ExMethod). Assumes: " +
		" 1) The Method, referred to by the Project by its CID, has been updated with new internals, 2) The new Method differs from the old only in "+
		" that the dicom/modality structure has been added to the Method and that it may have additional appended steps (not currently referenced), "+
		" 3) The study types in the Method may have changed.  However, there is a 1 to 1 correspondence between" +
		" the steps in the old and new Methods so the types in existing Studies can be updated.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String id = args.value("id");
		if (id==null) {
			
			// FInd all Primary projects.  Replicas must be fixed where they are primaries !
			Collection<String> projects = PSSDUtils.children(executor(), null, "0",DistributedQuery.ResultAssetType.primary, DistributedQuery.ResultFilterPolicy.none);
			if (projects==null) return;
			//
			for (String project : projects) {
				updateProject (executor(), project, w);
			}
		} else {

			// Set distributed citeable ID for the local Project
			DistributedAsset pID = new DistributedAsset(args.element("id"));

			// Check a few things...
			String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), pID);
			if (type==null) {
				throw new Exception("The asset associated with CID " + pID.toString() + " does not exist");
			}
			if ( !type.equals(Project.TYPE) ) {
				throw new Exception("Object " + pID.getCiteableID() + " [type=" + type + "] is not a " + Project.TYPE);
			}
			if (pID.isReplica()) {
				throw new Exception ("The supplied Project is a replica and this service cannot modify it.");
			}

			// Do the work
			updateProject (executor(), pID.getCiteableID(), w);
		}
	}


	private void updateProject (ServiceExecutor executor, String pID, XmlWriter w) throws Throwable {

		// The Project object itself does not require updating
		System.out.println("Updating Project " + pID);
		
		// Iterate over local and primary subjects
		Collection<String> children = PSSDUtils.children(executor, pID, "0", 
				DistributedQuery.ResultAssetType.primary, DistributedQuery.ResultFilterPolicy.none);
		if (children==null) return;
		for (String child : children) {	
			updateSubject (executor(), child);
		}
	}



	private void updateSubject (ServiceExecutor executor, String id) throws Throwable {

		// Get the method used to create this subject. The SUbject itself does not require
		// updating.
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		XmlDoc.Element r = executor.execute("om.pssd.object.describe", dm.root());
		String mid = r.value("object/method/id");

		// DOn't want to throw exceptions once we are going.
		if (mid==null) return;                

		// Iterate over local and primary ExMethods
		String pdist = "0";
		Collection<String> children = PSSDUtils.children(executor, id, pdist, 
				DistributedQuery.ResultAssetType.primary, DistributedQuery.ResultFilterPolicy.none);
		if (children==null) return;
		for (String child : children) {
			updateExMethod (executor, child, mid); 
		}
	}


	private void updateExMethod (ServiceExecutor executor, String id, String mid) throws Throwable {

		// Update ExMethod object
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		dm.add("mid", mid);
		executor.execute("om.pssd.ex-method.method.replace", dm.root());

		// Study types may need to be updated
		String pdist = "0";
		Collection<String> children = PSSDUtils.children(executor, id, pdist, 
				DistributedQuery.ResultAssetType.primary, DistributedQuery.ResultFilterPolicy.none);
		if (children==null) return;
		for (String child : children) {
			updateStudy (executor, id, child); 
		}
	}

	private void updateStudy (ServiceExecutor executor, String exId, String studyId) throws Throwable {

		// Fetch the existing Study type and step
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", studyId);
		XmlDoc.Element r = executor.execute("om.pssd.object.describe", dm.root());
		String oldType = r.value("object/type");
		XmlDoc.Element method  = r.element("object/method");
		
		// I have seen some Projects with no method element.  I don't know why.
		// If it's not there, it should be.  All we can do is 
		// Get the Method from the parent ExMethod (passed in as insurance)
		// Assume step 1. This is the only way we can get
		// the new Study Type as well. At least we know the ExMethod ID.
		String step = "1";
		if (method!=null) {
			step = method.value("step");
		} else {
			System.out.println("updateStudy: no 'method' element for study " + studyId + ". Assuming step=1");
		}

		// Fetch the new Study type from the ExMethod which has already been replaced.
		// om.pssd.ex-method.step.describe
		dm = new XmlDocMaker("args");
		dm.add("id", exId);
		dm.add("step", step);
		XmlDoc.Element r2 = executor.execute("om.pssd.ex-method.step.describe", dm.root());
		String newType = r2.value("ex-method/step/study/type");
		if (oldType.equals(newType)) return;

		// Update the STudy
		dm = new XmlDocMaker("args");
		dm.add("id", studyId);
		dm.add("type", newType);
		
		// If we were missing the method information, add it
		if (method==null) {
			dm.push("method");
			dm.add("id", exId);          // It's the ExMethod we want
			dm.add("step", step);
		}
		executor.execute("om.pssd.study.update", dm.root());
		
		// DataSets don't hold any Method specific meta-data
	}


// The following code exists from the initial version of this service that could
// replace a Method with one of a new CID.  Leave it here in case we want to 
// resurrect
/*
	private String[]  updateProjectObject (ServiceExecutor executor, String pID, XmlWriter w) throws Throwable {

		// Find map from old to new Method CIDs utilised by this project
		// I don't use a HashMap because it reorders the entries
		w.push("project");
		w.add("id", pID);
		//
		String[]  cidMap = makeCIDMap (executor(), pID);
		if (cidMap==null) return null;
		//
		for (int i=0; i<cidMap.length; i+=2) {
		   w.add("method-id", new String[] {"old", cidMap[i], "new", cidMap[i+1]});
		}
		w.pop();

		// Get existing and merge
		XmlDoc.Element r = AssetUtil.getAsset(executor, pID, null);
		XmlDoc.Element meta = r.element("asset/meta/pssd-project");
		Collection<XmlDoc.Element> methods = meta.elements("method");      // Already checked ok in makeCIDMap
		for (XmlDoc.Element method : methods) {
			String cidOld = method.value("id");
			String cidNew = findID (cidMap, cidOld);
			XmlDoc.Element id = method.element("id");
			id.setValue(cidNew);                       // Set new Method ID
		}

		//
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", pID);
		dm.push("meta", new String[]{"action", "merge"});
		AssetUtil.removeAttribute(executor, meta, "id");
		dm.add(meta);
		dm.pop();
		executor.execute("asset.set", dm.root());
		//
		return cidMap;
	}


	private String[] makeCIDMap (ServiceExecutor executor, String pID) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", pID);
		XmlDoc.Element r = executor.execute("om.pssd.object.describe", dm.root());
		if (r==null) return null;
		//
		Collection<XmlDoc.Element> methods = r.elements("object/method");
		if (methods==null) return null;

		// Iterate over methods and make array of pair, old/new
		int n = (methods.size()) * 2;
		String[] map = new String[n];
		int i = 0;
		for (XmlDoc.Element method : methods) {
			String cidOld = method.value("id");

			// Dereference and get meta-data for this existing Method
			XmlDoc.Element methodMeta = getMethodMeta (executor, cidOld);
			String name = methodMeta.value("method/name");

			// Find Methods of this name.  There should be exactly 2; the old and the new
			XmlDocMaker dm2 = new XmlDocMaker("args");
			dm2.add("text", name);
			dm2.add("for", "subject");
			XmlDoc.Element r2 = executor.execute("om.pssd.method.find", dm2.root());
			if (r2==null) throw new Exception ("Could not locate Method of name '" + name + "'");
			Collection<String> cids = r2.values("id");
			if (cids.size()!=2) {
				throw new Exception("Expected exactly 2 Methods of name " + name);
			}
			String cidNew = null;
			boolean foundOld = false;
			for (String cid : cids) {
				if (cid.equals(cidOld)) {
					foundOld = true;
				} else {
					cidNew = cid;
				}	
			}
			if (!foundOld) {
				throw new Exception("Could not find the expected Method for the existing Method cid " + cidOld);
			}
			map[i] = cidOld;
			map[i+1] = cidNew;
			i +=2;
		}
		return map;	
	}


	private String findID (String[] cidMap, String midOld) {
		int n = cidMap.length;
		for (int i=0; i<n; i+=2) {
			if (cidMap[i].equals(midOld)) return cidMap[i+1];	
		}
		return null;
	}
	private XmlDoc.Element getMethodMeta (ServiceExecutor executor, String id) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		return executor.execute("om.pssd.method.describe", dm.root());
	}
	*/
}
