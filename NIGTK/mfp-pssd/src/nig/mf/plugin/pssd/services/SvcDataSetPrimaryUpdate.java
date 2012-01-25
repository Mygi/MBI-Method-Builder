package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.DataSet;
import nig.mf.pssd.plugin.util.DistributedAsset;
import nig.mf.pssd.plugin.util.DistributedAssetUtil;
import nig.mf.pssd.plugin.util.DistributedQuery.ResultAssetType;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcDataSetPrimaryUpdate extends PluginService {
	private Interface _defn;

	public SvcDataSetPrimaryUpdate() throws Throwable {
		_defn = new Interface();
		_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the primary DataSet.", 1, 1));
		_defn.add(new Interface.Element("name",StringType.DEFAULT, "The name of this data set.", 0, 1));
		_defn.add(new Interface.Element("description",StringType.DEFAULT, "An arbitrary description for the data set.", 0, 1));

		_defn.add(new Interface.Element("type",StringType.DEFAULT, "MIME type of the data set if different from the content.", 0, 1));

		Interface.Element se = new Interface.Element("subject",CiteableIdType.DEFAULT,"The identity of the subject from which this data set was acquired.",0,Integer.MAX_VALUE);
		se.add(new Interface.Attribute("state",IntegerType.DEFAULT,"The identity of the state the subject was in at the time of acquisition.",1));
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
		return "om.pssd.dataset.primary.update";
	}

	public String description() {
		return "Updates (merges) an existing locally managed PSSD DataSet that contains data acquired from a subject. The DataSet may have data, or may simply be a container for other DataSets.";
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
		
		// Check a few things...
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
			if (!DistributedAssetUtil.assetExists(executor(), proute, pdist, emid, ResultAssetType.all, false, true, null)) {
				throw new Exception ("The ExMethod object (" + emid + ") cannot be found anywhere in the federation.");
			}
		}

		// FIrst update the component that is common between 'primary' and 'derivation'
		boolean isDerivation = false;
		SvcDataSetDerivationUpdate.updateGenericAndData (executor(), args, dID.getCiteableID(), in,isDerivation);
		
		// Now update the primary data-set relevant bits
		updatePrimary (args,dID.getCiteableID());
	}
	

	/**
	 * Update the meta-data component that is specific to the primary DataSet
	 * 
	 * @param args
	 * @param id
	 * @throws Throwable
	 */
	private void updatePrimary (XmlDoc.Element args, String id) throws Throwable {
		
		// Get old meta-data
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", id);
		dm.add("pdist",0);                 // Force local 	
		XmlDoc.Element r = executor().execute("asset.get", dm.root());	
		XmlDoc.Element oldAcquis = r.element("asset/meta/pssd-acquisition");

		
		// Handle the pssd-acquisition separately. There is a subtlety in MF which
		// means to change an attribute (e.g. the state or step) requires you to
		// change the value as well. The only safe way is to remove and replace.
		XmlDoc.Element newMethod = args.element("method");
		XmlDoc.Element newSubject = args.element("subject");

		if (newMethod!=null || newSubject!=null) {
			
			// First remove the existing pssd-derivation
			dm = new XmlDocMaker("args");
			dm.add("cid",id);
			dm.push("meta", new String[]{"action", "remove"});
			dm.add("pssd-acquisition");
			dm.pop();
			executor().execute("asset.set",dm.root());

			// Now create the new and add in
			dm = new XmlDocMaker("args");
			dm.add("cid",id);
			dm.push("meta", new String[]{"action", "add"});
			dm.push("pssd-acquisition"); 
		
			// Fish out old meta-data
			XmlDoc.Element oldSubject = null;
			XmlDoc.Element oldMethod = null;
			boolean doIt = false;

			if (oldAcquis!=null) {
				oldSubject = oldAcquis.element("subject");
				oldMethod = oldAcquis.element("method");
			}
			
			// Add new or put old back "input". Existing and new are in the same structure :-)
			doIt = addSubject(dm, oldSubject, newSubject);
	
			// ExMethod. If specified, use that. If not specified, could infer from the
			// parent study.  Currently not enabled.
			//SvcDataSetPrimaryCreate.addMethodAndStep(executor(),dm,pid,args);	
			
			// Add new or put back old "method"
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
	
	
	private boolean addSubject (XmlDocMaker dm, XmlDoc.Element oldSubject, XmlDoc.Element newSubject) throws Throwable {
		if (newSubject == null ) {
			// Just add the old Subject back in if it exists
			if (oldSubject != null) {
				dm.add(oldSubject);
				return true;
			}
		} else {
			// Add the new Subject in
			dm.add(newSubject);
			return true;
		}
		return false;
	}		
	
}
