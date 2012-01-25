package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;
import nig.mf.plugin.pssd.ModelUser;
import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.method.*;
import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.plugin.util.AssetUtil;
import nig.mf.pssd.plugin.util.DistributedAsset;



public class SvcExMethodReplaceMethod extends PluginService {
	private Interface _defn;

	public SvcExMethodReplaceMethod() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT,"The identity of the ExMethod (managed by the local server).",1,1));
		_defn.add(new Interface.Element("mid",CiteableIdType.DEFAULT,"The identity of the new Method (managed by the local server) to replace the old. Can be the same as the existing Method (so if the Method has changed, this will update the ExMethod).",1,1));
	}

	public String name() {
		return "om.pssd.ex-method.method.replace";
	}

	public String description() {
		return "Specialized service function to replace the Method in an ExMethod; replaces the pssd-method document and pssd-exmethod/method components.  Must be done in conjuction with changes to the related Project and Subjects.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {


		// Set distributed citeable ID for the local ExMethod.  
		DistributedAsset eID = new DistributedAsset(args.element("id"));
		
		// Find primary parent Project 
		Boolean readOnly = false;
		DistributedAsset dPID = eID.getParentProject(readOnly);
		if (dPID==null) {
			throw new Exception ("Cannot find primary Project parent of the given ExMethod");
		}

		// Modifier must have administrator role locally
		Boolean isAdmin = (ModelUser.hasRole(null, executor(), Project.projectAdministratorRoleName(dPID.getCiteableID())) ||
				           ModelUser.hasRole(null, executor(), PSSDUtils.OBJECT_ADMIN_ROLE_NAME));
		if (!isAdmin) {
			throw new Exception ("User not authorised: requires '" + Project.projectAdministratorRoleName(dPID.getCiteableID()) +
					             "' or '" + PSSDUtils.OBJECT_ADMIN_ROLE_NAME + " role");
		}

		// Check a few things...
		String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), eID);
		if (type==null) {
			throw new Exception("The asset associated with CID " + eID.toString() + " does not exist");
		}
		if ( !type.equals(ExMethod.TYPE) ) {
			throw new Exception("Object " + eID.getCiteableID() + " [type=" + type + "] is not a " + ExMethod.TYPE);
		}
		if (eID.isReplica()) {
			throw new Exception ("The supplied ExMethod is a replica and this service cannot modify it.");
		}

		// NOw the Method
		DistributedAsset mID = new DistributedAsset(args.element("mid"));

		// Check a few things...
		type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), mID);
		if (type==null) {
			throw new Exception("The asset associated with CID " + mID.toString() + " does not exist");
		}
		if ( !type.equals(Method.TYPE) ) {
			throw new Exception("Object " + eID.getCiteableID() + " [type=" + type + "] is not a " + Method.TYPE);
		}
		if (mID.isReplica()) {
			throw new Exception ("The supplied Method is a replica; this service only consumes primary Methods.");
		}
		

		// Do the work
		replace (executor(), eID.getCiteableID(), mID);
	}


	private void replace (ServiceExecutor executor, String eID, DistributedAsset mID) throws Throwable {
		// Get ExMethod XML
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", eID);
		XmlDoc.Element asset = executor().execute("asset.get", dm.root());

		// Remove pssd-method
		XmlDoc.Element pssdMethodOld = asset.element("asset/meta/pssd-method");
		if (pssdMethodOld!=null) {

			// Remove pssd-method from asset
			// Exception here is ok.
			AssetUtil.removeDocument(executor(), eID, null, pssdMethodOld);
		}

		// Instantiate new Method
		Method m = Method.lookup(executor, mID);

		// Take a copy, and fully instantiate the method to which we are referring.
		// The server route is passed down requiring all sub-Methods
		// to be managed on the same server
		m.convertBranchesToSubSteps(mID.getServerRoute(), executor);

		// Save pssd-method meta-data
		dm = new XmlDocMaker("args");
		dm.add("cid", eID);
		dm.push("meta", new String[] {"action", "replace"});
		dm.push("pssd-method");
		if (m.version()!=null) dm.add("version", m.version());
		if ( m.numberOfSteps() > 0 ) {
			XmlDocWriter w = new XmlDocWriter(dm);
			m.saveSteps(w);
		}
		dm.pop();

		// pssd-ex-method/method)
		dm.push("pssd-ex-method");                 // There are no attribute (ns or tag)
		dm.push("method");
		dm.add("id", mID.getCiteableID());
		dm.add("name", m.name());                  // mandatory
		dm.add("description", m.description());    // mandatory
		dm.pop();
		dm.pop();
		//
		dm.pop();       // meta

		// Update asset
		executor.execute("asset.set", dm.root());
	}
}
