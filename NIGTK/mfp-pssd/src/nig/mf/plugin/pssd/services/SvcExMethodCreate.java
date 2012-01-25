package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.Subject;
import nig.mf.plugin.pssd.method.ExMethod;
import nig.mf.pssd.plugin.util.DistributedAsset;
import nig.mf.pssd.plugin.util.DistributedAssetUtil;
import nig.mf.pssd.plugin.util.DistributedQuery.ResultAssetType;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.IntegerType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcExMethodCreate extends PluginService {
	private Interface _defn;

	public SvcExMethodCreate() {
		_defn = new Interface();
		Interface.Element me = new Interface.Element("sid",CiteableIdType.DEFAULT,"The identity of the parent subject.",1,1);
		me.add(new Interface.Attribute("proute", CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages this citable ID. If not supplied, then the object will be assumed to be local.", 0));
		_defn.add(me);
		//
		_defn.add(new Interface.Element("exmethod-number", IntegerType.POSITIVE_ONE,
				"Specifies the ExMethod number for the identifier. If not given, the next available ExMethod is created under the parent Subject. If specified, then there cannot be any other asset/object with this citable ID assigned. Used for importing objects from another system.",
				0, 1));
		_defn.add(new Element("fillin", BooleanType.DEFAULT, "If the exmethod-number is not given, fill in the ExMethod allocator space (re-use allocated CIDs with no assets), otherwise create the next available CID at the end of the CID pool. Defaults to false; use with care in federated envionment.", 0, 1));
		_defn.add(new Interface.Element("mid",CiteableIdType.DEFAULT,"The identity of the Method to instantiate.  Must be managed by the same server as the parent Project (not Subject) object.",1,1));
	}

	public String name() {
		return "om.pssd.ex-method.create";
	}

	public String description() {
		return "Creates a PSSD ExMethod (instantiation of a Method for execution) object on the local server.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		
		// Set distributed Subject asset. It must be a primary or we are not allowed
		// to create children under it.
		DistributedAsset dSID = new DistributedAsset(args.element("sid"));
		
		// Validate
		String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dSID);
		if (type==null) {
			throw new Exception("The object " + dSID.toString() + " does not exist");
		}
		if ( !type.equals(Subject.TYPE) ) {
			throw new Exception("Object " + dSID.getCiteableID() + " [type=" + type + "] is not a " + Subject.TYPE);
		}
		if (dSID.isReplica()) {
			throw new Exception ("The supplied parent Subject is a replica and this service cannot create its child");
		}
		
		// Find the parent Project; must be primary for object creation processes (could be relaxed)
		// as long as the parent Subject is primary
		DistributedAsset dPID = dSID.getParentProject(false);
		if (dPID==null) {
			throw new Exception ("Cannot find primary Project parent of the given Subject");
		}
	
		// Set the Method; it must be managed by the same server as the parent Project
		String mid = args.value("mid");
		if (!DistributedAssetUtil.assetExists(executor(), dPID.getServerRoute(), null, mid, ResultAssetType.primary, false, true, null)) {
			throw new Exception ("The Method object (" + mid + ") must be managed by the same server as the Project object");
		}	
		DistributedAsset dMID = new DistributedAsset(dPID.getServerRoute(), mid);	
		//
		long exMethodNumber = args.longValue("exmethod-number", -1);
		boolean fillIn = args.booleanValue("fillIn", false);

		// Create the ExMethod on the local server
		String id = create(executor(), dSID, exMethodNumber, dMID, fillIn);
		w.add("id",id);
	}
	
	
	public static String create(ServiceExecutor executor, DistributedAsset dSID, long exMethodNumber, 
			DistributedAsset dMID, boolean fillIn) throws Throwable {
		return ExMethod.create(executor, dSID, exMethodNumber, dMID, fillIn);
	}
	
}
