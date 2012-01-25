package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.user.UserCredential;
import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.pssd.plugin.util.DistributedAsset;
import nig.mf.pssd.plugin.util.DistributedAssetUtil;
import nig.mf.pssd.plugin.util.DistributedQuery.ResultAssetType;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

/**
 * This service is a little flawed in that some PSSD objects have embedded CIDs
 * (e.g. in Study and DataSet objects) in meta-data This service does not detect
 * these. I have retired it until such time as it is enhanced to accommodate
 * this.
 * 
 * @author nebk
 * 
 */
public class SvcProjectSetCid extends PluginService {
	private Interface _defn;

	public SvcProjectSetCid() {

		_defn = new Interface();
		_defn.add(new Element("from", CiteableIdType.DEFAULT, "The current citable ID of the Project.", 1, 1));
		_defn.add(new Element("to", CiteableIdType.DEFAULT, "The new citable ID of the Project.", 1, 1));
	}

	public String name() {

		return "om.pssd.project.cid.set";
	}

	public String description() {

		return "Recursively set the citeable id of a project. It is required that the new project CID already exists (i.e. it is being re-used)  but that there are no assets associated with it or its children.  The service attempts to preserve the child numbers of objects. E.g. if the first input subject is 1.5.10.3 it's new CID would be 1.5.20.3";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String proute = null;

		// Validate project CIDs
		String pidFrom = args.value("from");
		DistributedAsset dPidFrom = new DistributedAsset(proute, pidFrom);

		String pidTo = args.value("to");
		DistributedAsset dPidTo = new DistributedAsset(proute, pidTo);
		//
		if (pidFrom.equals(pidTo)) {
			throw new Exception("The source and destination citable IDs are identical");
		}
		if (!Project.isObjectProject(executor(), dPidFrom)) {
			throw new Exception("The source object is not a Project");
		}
		if (!nig.mf.pssd.plugin.util.CiteableIdUtil.cidExists(executor(), dPidTo)) {
			throw new Exception("'to' cid " + pidTo + " does not exist");

		}
		if (nig.mf.pssd.CiteableIdUtil.getIdDepth(pidTo) != 3) {
			throw new Exception("to' CID must have depth 3");
		}
		if (!DistributedAssetUtil.assetExists(executor(), dPidTo.getServerRoute(), null, dPidTo.getCiteableID(),
				ResultAssetType.all, false, true, null)) {
			throw new Exception(
					"The destination CID has an associated asset or its children have an asset. It must be a naked CID.");
		}

		// Do it
		set(executor(), pidFrom, pidTo, w);
	}

	private void set(ServiceExecutor executor, String pidFrom, String pidTo, XmlWriter w) throws Throwable {

		// Find any children of the input object before we change its CID
		// which would cause the collection.members service to find nothing
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", pidFrom);
		dm.add("size", "infinity");
		XmlDoc.Element r = executor().execute("om.pssd.collection.members", dm.root());
		Collection<String> descendants = r.values("object/id");

		// Set output Project CID
		String idFrom = nig.mf.pssd.plugin.util.CiteableIdUtil.cidToId(executor(), pidFrom);
		dm = new XmlDocMaker("args");
		dm.add("id", idFrom);
		dm.add("cid", pidTo);
		executor().execute("asset.cid.set", dm.root());

		// Revoke existing ACLs on the Project object
		PSSDUtils.revokeAllACLs(executor, pidTo, true);

		// Create the output Project roles
		Project.createProjectRoles(executor, pidTo);

		// Grant new ACLs on the project object
		PSSDUtils.grantProjectACLsToAsset(executor, pidTo);

		// Iterate recursively through any children objects and move them to
		// their parent
		if (descendants != null) {
			for (String cidIn : descendants) {
				// Get asset ID of child and recursively move it
				PSSDObject.move(executor, cidIn, pidTo, true);
			}
		}

		// Destroy the input Project roles, which will also remove them from
		// users
		// We must defer this step until now as the roles must exist for the
		// removal
		// of ACLs on children objects
		Project.destroyRoles(executor, pidFrom);

		// Finally, parse the project and grant member/data-use roles to the
		// project members
		setMemberRoles(executor, pidTo);
	}

	private void setMemberRoles(ServiceExecutor executor, String pidTo) throws Throwable {

		// Find the members and their roles
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", pidTo);
		XmlDoc.Element r = executor.execute("om.pssd.project.members.list", dm.root());

		// Members
		Collection<XmlDoc.Element> members = r.elements("member");
		if (members != null) {
			for (XmlDoc.Element m : members) {
				String auth = m.value("@authority");
				String protocol = m.value("@protocol");

				String role = m.attribute("role").value();

				// Grant the hierarchical team role to the user-member
				// The roles have already been created.
				UserCredential userCred = new UserCredential(auth, protocol, m.value("@domain"), m.value("@user"));
				Project.grantProjectRole(executor, pidTo, userCred, role, false);

				// Now set the "data-use". For admins, this will be null
				XmlDoc.Attribute dataUse = m.attribute("data-use");
				if (dataUse != null) {
					Project.grantProjectRole(executor, pidTo, userCred, dataUse.value(), false);
				}
			}
		}

		// Role-members
		Collection<XmlDoc.Element> roleMembers = r.elements("role-member");
		if (roleMembers != null) {
			for (XmlDoc.Element m : roleMembers) {
				String member = m.attribute("member").value();
				String role = m.attribute("role").value();

				// Grant the hierarchical team role to the user-member
				// The roles have already been created.
				Project.grantProjectRole(executor, pidTo, member, role, false);

				// Now set the "data-use". For admins, this will be null
				XmlDoc.Attribute dataUse = m.attribute("data-use");
				if (dataUse != null) {
					Project.grantProjectRole(executor, pidTo, member, dataUse.value(), false);
				}
			}
		}
	}

}