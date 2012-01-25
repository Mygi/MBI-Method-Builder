package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.*;
import nig.mf.plugin.pssd.util.*;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.*;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

public class SvcDataSetPrimaryCreate extends PluginService {
	private Interface _defn;

	public SvcDataSetPrimaryCreate() throws Throwable {
		_defn = new Interface();
		
		Interface.Element me = new Interface.Element("pid",CiteableIdType.DEFAULT, "The identity of the parent (study or dataset).", 1, 1);
		me.add(new Interface.Attribute("proute", CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.", 0));
		_defn.add(me);
		
		_defn.add(new Interface.Element("dataset-number", IntegerType.POSITIVE_ONE,
				"Specifies the DataSet number for the identifier. If not given, the next available DataSet is created. If specified, then there cannot be any other asset/object with this identity assigned. Used for importing DataSets from another system.",
				0, 1));
		_defn.add(new Element("fillin", BooleanType.DEFAULT, "If the dataset-number is not given, fill in the DataSet allocator space (re-use allocated CIDs with no assets), otherwise create the next available CID at the end of the CID pool. Defaults to false; use with care in federated envionment.", 0, 1));
		_defn.add(new Interface.Element("name",StringType.DEFAULT, "The name of this data set.", 1, 1));
		_defn.add(new Interface.Element("description",StringType.DEFAULT, "An arbitrary description for the data set.", 0, 1));

		_defn.add(new Interface.Element("type",StringType.DEFAULT, "MIME type of the data set if different from the content.", 0, 1));

		me = new Interface.Element("subject",XmlDocType.DEFAULT,"Details about the subject for which this acquisition was made.",0,1);
		Interface.Element se = new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the subject. If not specified, defaults to the identity of the parent subject.", 0, 1);
		se.add(new Interface.Attribute("proute", CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.", 0));
		me.add(se);
		me.add(new Interface.Element("state",LongType.POSITIVE,"The state of the subject at the time of acquisition. If not specified, then defaults to current subject state.",0,1));
		_defn.add(me);
		
		me = new Interface.Element("method",XmlDocType.DEFAULT,"Details about the ex-method for which this acquisition was made.",0,1);
		me.add(new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the ex-method. If not specified, defaults to the identity of the parent ex-method.", 0, 1));
		me.add(new Interface.Element("step",CiteableIdType.DEFAULT,"The execution step within the ex-method",1,1));
		_defn.add(me);
		
		me = new Interface.Element("transform",XmlDocType.DEFAULT,"If transformed, then details of the transform.",0,1);
		me.add(new Interface.Element("id",CiteableIdType.DEFAULT,"The identity of an agreed method of transformation.",0,1));
		me.add(new Interface.Element("notes",StringType.DEFAULT,"Arbitrary description of the transformation.",0,1));
		_defn.add(me);
		
		me = new Interface.Element("meta",XmlDocType.DEFAULT,"Optional metadata - a list of asset documents.",0,1);
		me.setIgnoreDescendants(true);

		_defn.add(me);
	}

	public String name() {
		return "om.pssd.dataset.primary.create";
	}

	public String description() {
		return "Creates a primary PSSD DataSet on the local server. The DataSet contains data acquired from a Subject. The DataSet may have data, or may simply be a container for other DataSets or DataObjects.";
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
			throw new Exception("The object " + dPID.toString() + " does not exist");
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
			
		// Create DataSet
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

		dm.push("pssd-dataset");
		dm.add("type","primary");
		dm.pop();
		
		// If the subject was not supplied, default to parent subject
		String sid = args.value("subject/id");
		DistributedAsset dSID = null;
		if ( sid == null ) {
			Boolean readOnly = false;                  // Creation context
			dSID = dPID.getParentSubject(readOnly);
			sid = dSID.getCiteableID();
		} else {
			String proute = args.value("subject/id/@proute");
			dSID = new DistributedAsset(proute, sid);        // We were told where to find it so don't care if replica
		}
		
		String state = args.value("subject/state");
		if ( state == null ) {
			state = currentStateOfSubject(dSID);
		}
		
		dm.push("pssd-acquisition");
		dm.add("subject",new String[] { "state", state },sid);
		
		addMethodAndStep(executor(),dm,dPID,args);
		
		dm.pop();
		
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
		
		// Get the parent project CID; this is required for ACLs on the DataSet
		PSSDUtils.addDataSetACLs(dm, dPID.getParentProjectCID());
		
		// Create on local server
		executor().execute("asset.create",dm.root(),in,null);
	}
	
	/**
	 * 
	 * @param executor
	 * @param dm
	 * @param dPID  Distributed citeable asset for parent Study/DataSet
	 * @param args
	 * @throws Throwable
	 */
	public static void addMethodAndStep(ServiceExecutor executor,XmlDocMaker dm, DistributedAsset dPID,XmlDoc.Element args) throws Throwable {
		
		String mid = null;
		String step = null;
		
		XmlDoc.Element me = args.element("method");
		if ( me == null ) {
			XmlDocMaker am = new XmlDocMaker("args");
			am.add("id",dPID.getCiteableID());
			
			XmlDoc.Element r = executor.execute(dPID.getServerRouteObject(), "om.pssd.object.describe",am.root());
			me = r.element("object/method");
			if ( me != null ) {
				mid = me.value("id");
				step = me.value("step");
			}	
		} else {
			mid = me.value("id");
			step = me.value("step");
		}
		
		if ( mid == null && step == null ) {
			return;
		}
		
		dm.add("method",new String[] { "step", step  },mid);
	}
	
	private String currentStateOfSubject(DistributedAsset dSID) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid",dSID.getCiteableID());
		dm.add("pdist",0);                 // Force local on whatever server it's executed		
		XmlDoc.Element r = executor().execute(dSID.getServerRouteObject(), "asset.get", dm.root());
		return r.value("asset/@version");
	}
}
