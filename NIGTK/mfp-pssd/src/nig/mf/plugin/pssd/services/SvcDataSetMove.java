package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.DataSet;
import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.Study;
import nig.mf.pssd.plugin.util.DistributedAsset;
import nig.mf.pssd.plugin.util.DistributedQuery.ResultAssetType;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcDataSetMove extends PluginService {
	private Interface _defn;

	public SvcDataSetMove() throws Throwable {
		_defn = new Interface();
		_defn.add(new Interface.Element("id", CiteableIdType.DEFAULT,
				"The citeable ID of the DataSet (which must be managed by the local server) to move.", 1, 1));
		_defn.add(new Interface.Element("pid", CiteableIdType.DEFAULT,
				"The citeable ID of the Study object (which must be managed by the local server) that will be the new parent.",
				1, 1));
		_defn.add(new Interface.Element("preserve", BooleanType.DEFAULT, "Try to preserve child CID numbers (defaults to false).", 0, 1));
		_defn.add(new Interface.Element("pdist",IntegerType.DEFAULT,"Specifies the peer distance when looking for remote children. Defaults to infinity.  Set to 0 for local only.",0,1));
		_defn.add(new Interface.Element("ptag", StringType.DEFAULT, "When looking for remote children, only query peers with this ptag. If none, query all peers.", 0, 1));
	}

	public String name() {
		return "om.pssd.dataset.move";
	}

	public String description() {
		return "Move a DataSet (and children) from one Study to another on the local server. If the object to move has children on a remote peer in a federation, the service will generate an exception.  The relevant ExMethod-oriented meta-data on the DataSets will be updated to reflect the new parent Study. New CIDs will be allocated as needed";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		
		// Set distributed citeable ID for the DataSet to move.  The DataSet is local by definition
		DistributedAsset dDSID = new DistributedAsset(args.element("id"));
		
		// Check a few things...
		String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dDSID);
		if (type==null) {
			throw new Exception("The asset associated with " + dDSID.toString() + " does not exist");
		}
		if ( !type.equals(DataSet.TYPE) ) {
			throw new Exception("Object " + dDSID.getCiteableID() + " [type=" + type + "] is not a " + DataSet.TYPE);
		}
		if (dDSID.isReplica()) {
			throw new Exception ("The supplied DataSet is a replica and this service cannot move it.");
		}

		// We don't want the DataSet we are moving to have children on a remote server. This
		// restriction could be removed, but for now we take a conservative approach.
		String ptag = args.stringValue("ptag");
		String pdist = args.stringValue("pdist", "infinity");
		if (dDSID.hasRemoteChildren(ResultAssetType.primary, ptag, pdist)) {
			throw new Exception ("The supplied DataSet has primary children on remote servers in the federation; cannot move it.");
		}
		
		
		// Now the parent Study. 	
		String id = args.value("id");
		if(!DataSet.isObjectDataSet(executor(),id)){
			throw new Exception("Asset(cid=" + id + ") is not a valid DataSet.");
		}
		String sid = args.value("pid");
		if(!Study.isObjectStudy(executor(),sid)){
			throw new Exception("Asset(cid=" + sid + ") is not a valid Study.");
		}
		boolean preserve = args.booleanValue("preserve");
		
		// MOve DataSet and update ExMethod/Step meta-data
		String newCid = PSSDObject.move(executor(), id, sid, preserve);
		w.add("id", newCid);
	}


}
