package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.Study;
import nig.mf.plugin.pssd.method.ExMethod;
import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.pssd.plugin.util.DistributedAsset;
import nig.mf.pssd.plugin.util.DistributedQuery.ResultAssetType;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcStudyMove extends PluginService {
	private Interface _defn;

	public SvcStudyMove() throws Throwable {
		_defn = new Interface();
		_defn.add(new Interface.Element("id", CiteableIdType.DEFAULT, "The citeable ID of the study. Must be local to the executing server.", 1, 1));
		_defn.add(new Interface.Element("pid", CiteableIdType.DEFAULT,
				"The citeable ID of the ExMethod/Study object that will be the new parent. If an ExMethod, the Study is moved to it.  If a Study, the DataSets of the input Study will be moved to this Study.",
				1, 1));
		_defn.add(new Interface.Element("step",CiteableIdType.DEFAULT,"If the new parent is an ExMethod, this gives the step within that ExMethod that results in this study. This can only be updated if the Study has no template information.",0,1));
		_defn.add(new Interface.Element("preserve", BooleanType.DEFAULT, "Try to preserve child CID numbers (defaults to false).", 0, 1));
		_defn.add(new Interface.Element("destroy-old-study", BooleanType.DEFAULT, "If true (default), and the new parent is a Study, deletes the old, now empty Study.", 0, 1));
		_defn.add(new Interface.Element("pdist",IntegerType.DEFAULT,"Specifies the peer distance when looking for remote children. Defaults to infinity.  Set to 0 for local only.",0,1));
		_defn.add(new Interface.Element("ptag", StringType.DEFAULT, "When looking for remote children, only query peers with this ptag. If none, query all peers.", 0, 1));
	}

	public String name() {
		return "om.pssd.study.move";
	}

	public String description() {
		return "Move a Study (and children) to a new parent ExMethod or Study (discarding input Study).  If the parent is an ExMethod, it is assumed that the Study method-specific meta-data should be updated with this ExMethod.  New CIDs will be allocated as needed.  Can only move objects on the local server; if the object has children on remote peers, an exception will occur.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {
		
		// Set distributed citeable ID for the local (by definition) Study to move. 
		DistributedAsset dSID = new DistributedAsset(args.element("id"));

		// Check a few things...
		String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dSID);
		if (type==null) {
			throw new Exception("The asset associated with " + dSID.toString() + " does not exist");
		}
		if (!type.equals(Study.TYPE) ) {
			throw new Exception("Object " + dSID.getCiteableID() + " [type=" + type + "] is not an " + Study.TYPE);
		}
		if (dSID.isReplica()) {
			throw new Exception ("The supplied Study is a replica and this service cannot move it.");
		}

		// We don't want the Study we are moving to have children on a remote server. This
		// restriction could be removed, but for now we take a conservative approach.
		// We only look for primaries, because otherwise replicas on other hosts will get in the way
		String ptag = args.stringValue("ptag");
		String pdist = args.stringValue("pdist", "infinity");
		if (dSID.hasRemoteChildren(ResultAssetType.primary, ptag, pdist)) {
			throw new Exception ("The supplied Study has primary children on remote servers in the federation; cannot move it.");
		}
		
		// Now the  local parent ExMethod/Study
		String pid = args.value("pid");
		boolean isStudy = Study.isObjectStudy(executor(), pid);
		boolean isExMethod = ExMethod.isObjectExMethod(executor(),pid);
		if (!isStudy && !isExMethod) {
			throw new Exception("Asset(cid=" + pid + ") is neither a valid ExMethod nor a Study.");
		}
		String step = args.value("step");
		boolean preserve = args.booleanValue("preserve");
		boolean destroyOldStudy = args.booleanValue("destroy-old-study", true);
		//
		if (isExMethod) {
			
			// If the Study we are moving has :template information we can't safely
			// move it to a new ExMethod. This is because the :template information
			// uses a namespace that reflects the parent ExMethod and the Method step
			// and we can't currently edit it.
			if (PSSDUtils.objectHasTemplate(executor(), dSID.getCiteableID())) {
				throw new Exception ("This Study cannot be moved to a new parent ExMethod because the Study has template information which cannot be modified to reflect the new ExMethod");
			}
					
			// Move Study to ExMethod
			String newCid = PSSDObject.move(executor(), dSID.getCiteableID(), pid, preserve);
			w.add("id", newCid);
			
			// The move function will update the ExMethod on the Study meta-data (Pssd-study).
			// However, it cannot know the step for the new ExMethod (so leaves unchanged).  Set that here.
			if (step!=null) Study.update(executor(), newCid, null, null, null, null, step, null);
		} else {
			
			// Find DataSets of Study
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("id", dSID.getCiteableID());
			XmlDoc.Element fromDataSets = executor().execute("om.pssd.collection.members", dm.root());
			if (fromDataSets==null) return;
            Collection<String> dsIDs = fromDataSets.values("object/id");
            if (dsIDs==null) return;
            
            // Iterate and move DataSets to new Study
            for(String id :dsIDs){
            	// Move DataSet to new Study and update Method meta-data on DataSet to reflect new Study
            	PSSDObject.move(executor(), id, pid, preserve);
            }
           
            // Delete old (now childless) Study if required
            if (destroyOldStudy) {
            	dm = new XmlDocMaker("args");
            	dm.add("id", dSID.getCiteableID());
            	executor().execute("om.pssd.object.destroy", dm.root());
            }
          
            //
            w.add("id", pid);
		}
	}

}
