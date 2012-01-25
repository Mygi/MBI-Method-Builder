package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.*;
import nig.mf.plugin.pssd.util.*;
import nig.mf.pssd.plugin.util.DistributedAsset;

import java.util.Collection;

import arc.mf.plugin.*;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

public class SvcDataSetDerivationCreate extends PluginService {
	private Interface _defn;

	public SvcDataSetDerivationCreate() throws Throwable {
		_defn = new Interface();
		
		Interface.Element me = new Interface.Element("pid",CiteableIdType.DEFAULT, "The identity of the parent (study or dataset).", 1, 1);
		me.add(new Interface.Attribute("proute", CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.", 0));
		_defn.add(me);

		_defn.add(new Interface.Element("dataset-number", IntegerType.POSITIVE_ONE,
				"Specifies the DataSet number for the identifier. If not given, the next available DataSet is created. If specified, then there cannot be any other asset/object with this identity assigned. Used for importing DataSets from another system.",
				0, 1));
		_defn.add(new Element("fillin", BooleanType.DEFAULT, "If the dataset-number is not given, fill in the DataSet allocator space (re-use allocated CIDs with no assets), otherwise create the next available CID at the end of the CID pool. Defaults to false; use with care in federated envionment.", 0, 1));
		_defn.add(new Interface.Element("name",StringType.DEFAULT, "The name of this data set.", 0, 1));
		_defn.add(new Interface.Element("description",StringType.DEFAULT, "An arbitrary description for the data set.", 0, 1));

		_defn.add(new Interface.Element("type",StringType.DEFAULT, "MIME type of the data set if different from the content.", 0, 1));

		me = new Interface.Element("input",CiteableIdType.DEFAULT,"Input data set(s) from which the derivation was made, if available.",0,Integer.MAX_VALUE);
		me.add(new Interface.Attribute("vid",StringType.DEFAULT,"The value identifier for the data set.",1));
		_defn.add(me);

		me = new Interface.Element("method",XmlDocType.DEFAULT,"Details about the ex-method for which this acquisition was made.",0,1);
		me.add(new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the ex-method. If not specified, defaults to the identity of the parent ex-method.", 0, 1));
		me.add(new Interface.Element("step",CiteableIdType.DEFAULT,"The execution step within the ex-method",1,1));
		_defn.add(me);

		me = new Interface.Element("transform",XmlDocType.DEFAULT,"If transformed, then details of the transform, if known.",0,1);
		me.add(new Interface.Element("id",CiteableIdType.DEFAULT,"The identity of an agreed method of transformation.",0,1));
		me.add(new Interface.Element("notes",StringType.DEFAULT,"Arbitrary description of the transformation.",0,1));
		_defn.add(me);

		me = new Interface.Element("meta",XmlDocType.DEFAULT,"Optional metadata - a list of asset documents.",0,1);
		me.setIgnoreDescendants(true);

		_defn.add(me);
	}

	public String name() {
		return "om.pssd.dataset.derivation.create";
	}

	public String description() {
		return "Creates a derivation PSSD DataSet on the local server. The DataSet contains data acquired from a Subject. The DataSet may have data, or may simply be a container for other DataSets or DataObjects.";
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
	
		// Distributed ID for parent Study or DataSet. It must be a primary or we are not allowed
		// to create children under it.
		DistributedAsset dPID = new DistributedAsset (args.element("pid"));
				
		// Validate
		String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dPID);
		if (type==null) {
			throw new Exception("The asset associated with " + dPID.toString() + " does not exist");
		}
		if ( !type.equals(Study.TYPE)  && !type.equals(DataSet.TYPE)) {
			throw new Exception("Object " + dPID.getCiteableID() + " [type=" + type + "] is neither a " + Study.TYPE + " nor a " + DataSet.TYPE);
		}
		if (dPID.isReplica()) {
			throw new Exception ("The supplied parent Study/DataSet is a replica and this service cannot create its child");
		}
	
		// If the user does not give project-number,  we may want to fill in 
		// any holes in the allocator space for Projects as sometimes we use 
		// large numbers for 'service' activities. 
		long datasetNumber = args.longValue("dataset-number", -1);
        boolean fillIn = args.booleanValue("fillin", false);

		// Generate CID, filling in allocator space if desired
		String cid = nig.mf.pssd.plugin.util.CiteableIdUtil.generateCiteableID(executor(), dPID, datasetNumber, fillIn);

		createDataSetAsset(args,dPID,cid,in);
		w.add("id",cid);
	}
	
	private void createDataSetAsset(XmlDoc.Element args, DistributedAsset dPID, String cid, Inputs in) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		
		dm.add("cid",cid);
		dm.add("namespace",PSSDUtils.namespace(executor(), dPID));
		dm.add("model",DataSet.MODEL);
		
		String type = args.value("type");
		if ( type != null ) {
			dm.add("type",type);
		}
		
		dm.push("meta");
		PSSDUtils.setObjectMeta(dm, DataSet.TYPE, args.value("name"), args.value("description"));
		PSSDUtils.setObjectOptionalMeta(dm, args.element("meta"),"om.pssd.dataset");
		
		// Data set..
		dm.push("pssd-dataset");
		dm.add("type","derivation");
		dm.pop();
		
		dm.push("pssd-derivation");

		// Inputs ..
		Collection<XmlDoc.Element> des = args.elements("input");
		if ( des != null ) {
			for (XmlDoc.Element de : des) {
				dm.add("input",new String[] { "vid", de.value("@vid") },de.value());
			}
		}
		
		// Method. If specified, use that. If not specified, infer from the
		// parent study.
		SvcDataSetPrimaryCreate.addMethodAndStep(executor(),dm,dPID,args);
		
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
		
		// Get the project identifier..this is required for ACLs on the DataSet
		PSSDUtils.addDataSetACLs(dm, dPID.getParentProjectCID());
		
		// Create asset on the local server
		executor().execute("asset.create",dm.root(),in,null);
	}
	
}
