package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.RSubject;
import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.pssd.CiteableIdUtil;
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

public class SvcObjectDestroy extends PluginService {
	private Interface _defn;

	public SvcObjectDestroy() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT,"The identity of the local object.",1,1));
		_defn.add(new Interface.Element("destroyCID", BooleanType.DEFAULT,
				"Destroy the CID as as well as the asset (defaults to false). Not destroying the CID allows it to be reused",
				0, 1));
		_defn.add(new Interface.Element("check-remote-children", BooleanType.DEFAULT, "Check if object has children on remote peers (defaults to false). If so, cannot destroy.", 0, 1));
		_defn.add(new Interface.Element("ptag", StringType.DEFAULT, "When checking for remote children, only query peers with this ptag. If none, query all peers.", 0, 1));
		_defn.add(new Interface.Element("pdist",IntegerType.DEFAULT,"Specifies the peer distance when looking for remote children. Defaults to infinity.  Set to 0 for local only.",0,1));
		_defn.add(new Interface.Element("destroy", BooleanType.DEFAULT, "If the object you are destroying is a Project, you must set this to true. Default is false.", 0, 1));
	}

	public String name() {
		return "om.pssd.object.destroy";
	}

	public String description() {
		return "Destroys the local object (and children) on the local server. When check-remote-children is true, if the object has children on another server in a federation it will not destroy.  If the object is a Project or an RSubject, then all associated object-specific roles and invalid (dangling) ACLs (associated with these roles) are also destroyed. If the object is an R-Subject, it will not be destroyed if it is inuse with existing local Subjects.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		
		// Destroy service must be run locally (or via a peer command)
		String proute = null;   // local
		String id   = args.value("id");
		DistributedAsset dID = new DistributedAsset(proute, id);
		String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dID);
		if (type==null) {
			throw new Exception("The asset associated with " + dID.toString() + " does not exist");
		}
		String ptag = args.stringValue("ptag");
		String pdist = args.stringValue("pdist", "infinity");
		Boolean destroy = args.booleanValue("destroy", false);
		
		// TRy not to destroy the repository :-)
		int depth = CiteableIdUtil.getIdDepth(id);
		if (depth < CiteableIdUtil.PROJECT_ID_DEPTH) {
			throw new Exception ("You are attempting to destroy objects at a level higher than Projects (e.g. the entire repository). This is not allowed");
		}
		
		// See if have children somewhere in federation. If it does, you can't destroy this
		// object without first destroying them.  Perhaps this is not a good policy
		// and it should just destroy all remote assets too...
		//
		// If the object is a replica, its children must be replica.  If the object
		// is primary, its children could be primary or replicas.
		Boolean checkRemote = args.booleanValue("check-remote-children", false);
		if (checkRemote) {
			ResultAssetType assetType = ResultAssetType.all;
			if (dID.isReplica()) assetType = ResultAssetType.replica;

			// The following code is a problem with system:manager which does not have
			// full federation rights. Fix this ?
			if (dID.hasRemoteChildren(assetType, ptag, pdist)) {
				if (dID.isReplica()) {
					throw new Exception ("CID " + id + " has replica children on other servers in the federation. Cannot destroy.");

				} else {
					throw new Exception ("CID " + id + " has (primary/replica) children on other servers in the federation. Cannot destroy.");
				}      		
			}
		}
		
        // 
		boolean destroyCID = false;
		if (args.value("destroyCID") != null) {
			if (args.value("destroyCID").equals("true")) destroyCID = true;
		}
		
		// Project and RSubject objects are special.  Find out if we have one
		boolean isRSubject = false;
		boolean isProject = Project.isObjectProject(executor(), dID);
		if (!isProject) isRSubject = RSubject.isObjectRSubject(executor(), dID);
		if (isProject && !destroy) {
			throw new Exception ("You must set argument 'destroy' to true to destroy a whole Project tree.");
		}
		

		// Abandon if RSubject is related to SUbjects (must be local)
		if (isRSubject) {
			if (RSubject.hasRelatedSubjects(executor(), dID)) {
				throw new Exception ("The RSubject " + id + " has related Subjects; wil not destroy");
			}
		}
		
		// Destroy the assets
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid",id);
		dm.add("members", true);
		executor().execute("asset.destroy",dm.root());
		

		// Destroy all local roles associated with the object and clean up any dangling ACLs
		if (isProject) {
			Project.destroyRoles(executor(), id);
			PSSDUtils.removeInvalidACLs(executor());
		} else if (isRSubject) {
			RSubject.destroyRoles(executor(), id);
			PSSDUtils.removeInvalidACLs(executor());
		}
		
		// Destroy CID as well if desired
		if (destroyCID) {
			nig.mf.pssd.plugin.util.CiteableIdUtil.destroyCID (proute, executor(), id);
		}
	}
	
}
