package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.DataSet;
import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.pssd.plugin.util.DistributedAsset;
import nig.mf.pssd.plugin.util.DistributedAssetUtil;
import nig.mf.pssd.plugin.util.DistributedQuery;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcDataSetDerivationUpdate extends PluginService {
	private Interface _defn;

	public SvcDataSetDerivationUpdate() throws Throwable {
		_defn = new Interface();
		_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the derivation DataSet. Must be managed by the local server.", 1, 1));
		_defn.add(new Interface.Element("name",StringType.DEFAULT, "The name of this data set.", 0, 1));
		_defn.add(new Interface.Element("description",StringType.DEFAULT, "An arbitrary description for the data set.", 0, 1));

		_defn.add(new Interface.Element("type",StringType.DEFAULT, "MIME type of the data set if different from the content.", 0, 1));

		Interface.Element se = new Interface.Element("input",CiteableIdType.DEFAULT,"Input data set(s) from which the derivation was made, if available.",0,Integer.MAX_VALUE);
		se.add(new Interface.Attribute("vid",StringType.DEFAULT,"The value identifier for the data set.",1));
		_defn.add(se);

		Interface.Element me = new Interface.Element("method",XmlDocType.DEFAULT,"Details about the ExMethod for which this acquisition was made.",0,1);
		me.add(new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the ExMethod. If not specified, defaults to the identity of the parent ExMethod.", 0, 1));
		me.add(new Interface.Element("step",CiteableIdType.DEFAULT,"The execution step within the ExMethod",1,1));
		_defn.add(me);

		Interface.Element te = new Interface.Element("transform",XmlDocType.DEFAULT,"If transformed, then details of the transform, if known.",0,1);
		te.add(new Interface.Element("id",CiteableIdType.DEFAULT,"The identity of an agreed method of transformation.",0,1));
		te.add(new Interface.Element("notes",StringType.DEFAULT,"Arbitrary description of the transformation.",0,1));
		_defn.add(te);

		me = new Interface.Element("meta",XmlDocType.DEFAULT,"Optional metadata - a list of asset documents.",0,1);
		me.setIgnoreDescendants(true);

		_defn.add(me);
	}

	public String name() {
		return "om.pssd.dataset.derivation.update";
	}

	public String description() {
		return "Updates (merges) an existing locally-managed PSSD DataSet that contains data acquired from a subject. The DataSet may have data, or may simply be a container for other DataSets.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public int minNumberOfInputs() {
		return 0;
	}

	public int maxNumberOfInputs() {
		return 1;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// Set distributed citeable ID for the DataSet.  The DataSet is local by definition
		DistributedAsset dID = new DistributedAsset(args.element("id"));

		String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dID);
		if (type==null) {
			throw new Exception("The asset associated with " + dID.toString() + " does not exist");
		}
		if ( !type.equals(DataSet.TYPE) ) {
			throw new Exception("Object " + dID.getCiteableID() + " [type=" + type + "] is not a " + DataSet.TYPE);
		}
		if (dID.isReplica()) {
			throw new Exception ("The supplied DataSet is a replica and this service cannot modify it.");
		}

		// The DataSet holds information describing the ExMethod and step path that it was
		// created with.  Because it only refers to a CID, we don't have to try and check
		// where the actual ExMethod is sourced from. We can check that it exists
		// somewhere in the federation.  We do this now before partial writes are done.
		String emid = args.value("method/id");  
		if (emid!=null) {			
			String proute = null;
			String pdist = "infinity";
			if (!DistributedAssetUtil.assetExists(executor(), proute, pdist, emid, DistributedQuery.ResultAssetType.all, false, true, null)) {
				throw new Exception ("The ExMethod object (" + emid + ") cannot be found anywhere in the federation.");
			}
		}

		// First update the component that is common between 'primary' and 'derivation'
		boolean isDerivation = true;
		updateGenericAndData (executor(), args, dID.getCiteableID(), in, isDerivation);

		// Now update the derived data-set bits
		updateDerivation (args, dID.getCiteableID());
	}



	/**
	 * Update the meta-data that is common between derivation and primary DataSets.  This function
	 * is reused by SvcDatSetPrimaryUpdate.  It is the  callers responsibility to have already
	 * checked that the Method is (embedded in args) consistent with Federation policy
	 * 
	 * @param executor
	 * @param args
	 * @param id
	 * @param in
	 * @param isDerivation
	 * @throws Throwable
	 */
	static public void updateGenericAndData (ServiceExecutor executor, XmlDoc.Element args, String id, Inputs in, boolean isDerivation) throws Throwable {

		// Prepare new meta
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid",id);

		String type = args.value("type");
		if ( type != null ) {
			dm.add("type",type);
		}

		dm.push("meta");
		PSSDUtils.setObjectMeta(dm, DataSet.TYPE, args.value("name"), args.value("description"));
		PSSDUtils.setObjectOptionalMeta(dm, args.element("meta"),"om.pssd.dataset");

		// Data set type.
		dm.push("pssd-dataset");
		if (isDerivation) {
			dm.add("type","derivation");
		} else {
			dm.add("type","primary");
		}
		dm.pop();

		// Transformation..
		XmlDoc.Element te = args.element("transform");

		if ( te != null ) {
			String tid = te.value("id");
			String notes = te.value("notes");

			dm.push("pssd-transform");
			if ( tid != null ) {
				dm.add("id",tid);
			}

			if ( notes != null ) {
				dm.add("notes",notes);
			}

			dm.pop();
		}

		dm.pop(); 	
		dm.add("if",new String[] { "part", "content" },"changed");

		// Update meta-and data
		executor.execute("asset.set",dm.root(),in,null);
	}


	private void updateDerivation (XmlDoc.Element args, String id) throws Throwable {

		// Get old meta-data
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", id);
		dm.add("pdist",0);      // Force local
		XmlDoc.Element r = executor().execute("asset.get", dm.root());	
		XmlDoc.Element oldDeriv = r.element("asset/meta/pssd-derivation");


		// Handle the pssd-derivation separately. There is a subtlety in MF which
		// means to change an attribute (e.g. the state or step) requires you to
		// change the value as well. The only safe way is to remove and replace.
		XmlDoc.Element newMethod = args.element("method");
		Collection<XmlDoc.Element> newInputs = args.elements("input");

		if (newMethod!=null || newInputs!=null) {

			// First remove the existing pssd-derivation
			dm = new XmlDocMaker("args");
			dm.add("cid",id);
			dm.push("meta", new String[]{"action", "remove"});
			dm.add("pssd-derivation");
			dm.pop();
			executor().execute("asset.set",dm.root());

			// Now create the new and add in
			dm = new XmlDocMaker("args");
			dm.add("cid",id);
			dm.push("meta", new String[]{"action", "add"});
			dm.push("pssd-derivation"); 

			// Fish out old meta-data
			Collection<XmlDoc.Element> oldInputs = null;
			XmlDoc.Element oldMethod = null;
			boolean doIt = false;

			if (oldDeriv!=null) {
				oldInputs = oldDeriv.elements("input");
				oldMethod = oldDeriv.element("method");
			}

			// Add new or put old back "input". Existing and new are in the same structure :-)
			doIt = addInputs(dm, oldInputs, newInputs);

			// ExMethod. If specified, use that. If not specified, could infer from the
			// parent study.  Currently not enabled.
			//SvcDataSetDerivationCreate.addMethodAndStep(executor(),dm,pid,args);	

			// Add new or put back  old "method"
			// Existing and new are not in the same structure :-(
			String newMethodID = null;
			String newMethodStep = null;
			if (newMethod!=null) {
				newMethodID = newMethod.value("id");
				newMethodStep = newMethod.value("step");
			}
			if (newMethodID!=null && newMethodStep!=null) {
				dm.add("method", new String[]{"step", newMethodStep}, newMethodID);
				doIt = true;
			} else {
				if (oldMethod!=null) {
					dm.add(oldMethod);
					doIt = true;
				}
			}
			dm.pop();
			dm.pop();

			// Replace meta-data. At the end of all this, this is the equivalent to a 'merge'
			// but one that handles the attributes
			if (doIt) executor().execute("asset.set",dm.root());
		}
	}


	private boolean addInputs (XmlDocMaker dm, Collection<XmlDoc.Element> oldInputs, 
			Collection<XmlDoc.Element> newInputs) throws Throwable {
		if (newInputs == null ) {
			// Just add the old Inputs back in if they exist
			if (oldInputs != null) {
				for (XmlDoc.Element de : oldInputs) {
					dm.add(de);
				}
				return true;
			}
		} else {
			// Add the new inputs in
			for (XmlDoc.Element de : newInputs) {
				dm.add(de);
			}
			return true;
		}
		return false;
	}		

}
