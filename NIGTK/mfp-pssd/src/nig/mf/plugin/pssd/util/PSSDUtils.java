package nig.mf.plugin.pssd.util;

import nig.mf.plugin.pssd.*;
import nig.mf.pssd.plugin.util.DistributedAsset;
import nig.mf.pssd.plugin.util.DistributedQuery;

import java.util.Collection;

import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.*;

public class PSSDUtils {

	public static final String OBJECT_ADMIN_ROLE_NAME = "pssd.object.admin";
	public static final String OBJECT_GUEST_ROLE_NAME = "pssd.object.guest";
	public static final String SUBJECT_ADMIN_ROLE_NAME = "pssd.r-subject.admin";
	public static final String SUBJECT_GUEST_ROLE_NAME = "pssd.r-subject.guest";

	/**
	 * FInd the citable IDs of the children of the given object
	 * 
	 * @param executor
	 * @param id
	 *            Citable ID of PSSD object. If null, use repository root.
	 * @param pdist
	 *            Federation distation. Set to "0" for local objects
	 * @param filter
	 *            -policy The policy for filtering the results. If null,
	 *            defaults to 'primary-or-any-replica'
	 * @return
	 * @throws Throwable
	 */
	public static Collection<String> children(ServiceExecutor executor, String id, String pdist,
			DistributedQuery.ResultAssetType assetType, DistributedQuery.ResultFilterPolicy filterPolicy)
			throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		if (id != null)
			dm.add("id", id);
		dm.add("size", "infinity");
		if (pdist != null) {
			dm.add("pdist", pdist);
		}
		if (assetType != null) {
			dm.add("asset-type", assetType.toString());
		}
		if (filterPolicy != null) {
			dm.add("filter-policy", filterPolicy.toString());
		}
		XmlDoc.Element r = executor.execute("om.pssd.collection.member.list", dm.root());
		if (r == null)
			return null;
		return r.values("object/id");
	}

	public static void setObjectMeta(XmlDocMaker dm, String type, String name, String description) throws Throwable {

		dm.push("pssd-object");

		if (type != null) {
			dm.add("type", type);
		}
		if (name != null) {
			dm.add("name", name);
		}

		if (description != null) {
			dm.add("description", description);
		}
		dm.pop();

	}

	public static void setObjectOptionalMeta(XmlDocMaker dm, XmlDoc.Element me, String ns) throws Throwable {

		// If there are "meta" elements, then copy them through..
		if (me != null) {
			Collection<XmlDoc.Element> mes = me.elements();
			if (mes != null) {
				for (XmlDoc.Element se : mes) {

					if (ns != null && se.attribute("ns") == null) {
						se.add(new XmlDoc.Attribute("ns", ns));
					}

					if (se.attribute("tag") == null) {
						se.add(new XmlDoc.Attribute("tag", "pssd.meta"));
					}

					dm.add(se);
				}
			}
		}
	}

	/**
	 * Add the ACLs to the asset.create meta-data that allow Project
	 * team-members basic access to the Project data
	 * 
	 * @param dm
	 *            XML document which will be handed as argument to asset.create
	 * @param cid
	 *            ID of Project object
	 * @throws Throwable
	 */
	public static void addProjectACLs(XmlDocMaker dm, String cid) throws Throwable {

		addACL(dm, OBJECT_ADMIN_ROLE_NAME, "read-write");
		addACL(dm, OBJECT_GUEST_ROLE_NAME, "read", "none");
		addACL(dm, Project.projectAdministratorRoleName(cid), "read-write");
		addACL(dm, Project.subjectAdministratorRoleName(cid), "read");
		addACL(dm, Project.memberRoleName(cid), "read");
		addACL(dm, Project.guestRoleName(cid), "read");
	}

	/**
	 * Add ACLs to the asset.create meta-data that allow project team-members
	 * basic access to the Subject data
	 * 
	 * @param dm
	 *            XML document which will be handed as argument to asset.create
	 * @param cid
	 *            ID of parent Project object
	 * @throws Throwable
	 */
	public static void addSubjectACLs(XmlDocMaker dm, String cid) throws Throwable {

		addACL(dm, OBJECT_ADMIN_ROLE_NAME, "read-write");
		addACL(dm, OBJECT_GUEST_ROLE_NAME, "read", "none");
		addACL(dm, Project.projectAdministratorRoleName(cid), "read-write");
		addACL(dm, Project.subjectAdministratorRoleName(cid), "read-write");
		addACL(dm, Project.memberRoleName(cid), "read");
		addACL(dm, Project.guestRoleName(cid), "read");
	}

	/**
	 * Add ACLs to the asset.create meta-data that allow project team-members
	 * basic access to the ExMethod data
	 * 
	 * @param dm
	 *            XML document which will be handed as argument to asset.create
	 * @param cid
	 *            ID of parent Project object
	 * @throws Throwable
	 */
	public static void addExMethodACLs(XmlDocMaker dm, String cid) throws Throwable {

		addProjectACLs(dm, cid);
	}

	/**
	 * Add ACLs to the asset.create meta-data that allow project team-members
	 * basic access to the Study data
	 * 
	 * @param dm
	 *            XML document which will be handed as argument to asset.create
	 * @param cid
	 *            ID of parent Project object
	 * @throws Throwable
	 */
	public static void addStudyACLs(XmlDocMaker dm, String cid) throws Throwable {

		addSubjectACLs(dm, cid);
	}

	/**
	 * Add ACLs to the asset.create meta-data that allow project team-members
	 * basic access to the DataSet data
	 * 
	 * @param dm
	 *            XML document which will be handed as argument to asset.create
	 * @param cid
	 *            ID of parent Project object
	 * @throws Throwable
	 */
	public static void addDataSetACLs(XmlDocMaker dm, String cid) throws Throwable {

		addSubjectACLs(dm, cid);
	}

	/**
	 * Add ACLs to the asset.create meta-data that allow project team-members
	 * basic access to the DataObject data
	 * 
	 * @param dm
	 *            XML document which will be handed as argument to asset.create
	 * @param cid
	 *            ID of parent Project object
	 * @throws Throwable
	 */
	public static void addDataObjectACLs(XmlDocMaker dm, String cid) throws Throwable {

		addSubjectACLs(dm, cid);
	}

	/**
	 * Add ACLs to the asset.create meta-data that allow project team-members
	 * basic access to the RSubject data
	 * 
	 * @param dm
	 *            XML document which will be handed as argument to asset.create
	 * @param cid
	 *            ID of the RSubject object
	 * @throws Throwable
	 */
	public static void addRSubjectACLs(XmlDocMaker dm, String cid) throws Throwable {

		addACL(dm, SUBJECT_ADMIN_ROLE_NAME, "read-write");
		addACL(dm, SUBJECT_GUEST_ROLE_NAME, "read");
		addACL(dm, OBJECT_ADMIN_ROLE_NAME, "read-write");
		addACL(dm, OBJECT_GUEST_ROLE_NAME, "read", "none");
		addACL(dm, RSubject.administratorRoleName(cid), "read-write");
		addACL(dm, RSubject.stateRoleName(cid), "read-write");
		addACL(dm, RSubject.guestRoleName(cid), "read");
	}

	public static void addIdentityACLs(XmlDocMaker dm, String cid) throws Throwable {

		PSSDUtils.addACL(dm, RSubject.administratorRoleName(cid), "read-write");
	}

	public static void addACL(XmlDocMaker dm, String role, String ma) throws Throwable {

		addACL(dm, role, ma, null);
	}

	public static void addACL(XmlDocMaker dm, String role, String ma, String ca) throws Throwable {

		dm.push("acl");
		dm.add("propagate", true);
		dm.add("actor", new String[] { "type", "role" }, role);
		if (ca == null) {
			dm.add("access", ma);
		} else {
			dm.add("metadata", ma);
			dm.add("content", ca);
		}

		dm.pop();
	}

	/**
	 * Create specified role. If the role pre-exists, ignore silently.
	 * 
	 * @param executor
	 * @param name
	 * @throws Throwable
	 */
	public static void createRole(ServiceExecutor executor, String name) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("role", name);
		dm.add("ifexists", "ignore");

		executor.execute("authorization.role.create", dm.root());
	}

	/**
	 * Check the given role exists. Exception if not.
	 * 
	 * @param role
	 *            - the role
	 * @param throwIt
	 *            - if true throw an exception if it does not exist
	 * @return - true if exists, false if does not
	 * @throws Throwable
	 */
	public static boolean checkRoleExists(ServiceExecutor executor, String role, boolean throwIt) throws Throwable {

		if (role != null) {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("role", role);

			XmlDoc.Element r = executor.execute("authorization.role.exists", dm.root());
			if (!r.booleanValue("exists")) {
				if (throwIt)
					throw new Exception("Specified role '" + role + "' does not exist");
				return false;
			}
		}
		return true;
	}

	/**
	 * Grants a specific role to the caller.
	 * 
	 * @param executor
	 * @param role
	 * @throws Throwable
	 */
	public static void grantRoleToSelf(ServiceExecutor executor, String role) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("role", new String[] { "type", "role" }, role);

		executor.execute("actor.self.grant", dm.root());
	}

	/**
	 * Grants a specific role to the given role.
	 * 
	 * @param executor
	 * @param arole
	 * @param role
	 * @throws Throwable
	 */
	public static void grantRoleToRole(ServiceExecutor executor, String arole, String role) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("type", "role");
		dm.add("name", arole);
		dm.add("role", new String[] { "type", "role" }, role);

		executor.execute("actor.grant", dm.root());
	}

	/**
	 * Returns the namespace of the given distributed asset.
	 * 
	 * @param executor
	 * @param cid
	 * @return
	 * @throws Throwable
	 */
	public static String namespace(ServiceExecutor executor, DistributedAsset dCID) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", dCID.getCiteableID());

		XmlDoc.Element r = executor.execute(dCID.getServerRouteObject(), "asset.namespace.get", dm.root());
		return r.value("namespace");
	}

	/**
	 * Returns the CID of the parent of a specified number of levels higher
	 * 
	 * @param cid
	 * @param levels
	 *            The number of levels above.
	 * @return
	 * @throws Throwable
	 */
	public static String identityOfParent(String cid, int levels) throws Throwable {

		return nig.mf.pssd.CiteableIdUtil.getParentId(cid, levels);
	}

	/**
	 * Adds a document tag attribute to the sub-documents of the given node.
	 * 
	 * @param me
	 * @param tag
	 */
	public static void setMetaNamespace(XmlDoc.Element me, String ns) {

		Collection<XmlDoc.Element> eles = me.elements();
		if (eles == null) {
			return;
		}

		for (XmlDoc.Element se : eles) {
			if (se.attribute("ns") == null) {
				se.add(new XmlDoc.Attribute("ns", ns));
			}

		}

	}

	/**
	 * Return the UUID of the server
	 * 
	 * @param executor
	 * @param proute
	 *            The route to the server of interest. Null for local.
	 * 
	 * @return
	 * @throws Throwable
	 */
	public static String serverUUID(ServiceExecutor executor, String proute) throws Throwable {

		XmlDoc.Element r = executor.execute(new ServerRoute(proute), "server.uuid");
		return r.value("uuid");
	}

	/**
	 * Revoke all ACLs on a given asset
	 * 
	 * @param executor
	 * @param id
	 *            asset id or citeable id
	 * @param citeable
	 * @throws Throwable
	 */
	public static void revokeAllACLs(ServiceExecutor executor, String id, boolean citeable) throws Throwable {

		String assetId = null;
		if (citeable) {
			assetId = nig.mf.pssd.plugin.util.CiteableIdUtil.cidToId(executor, id);
		} else {
			assetId = id;
		}

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", assetId);
		XmlDoc.Element r = executor.execute("asset.acl.describe", doc.root());
		Collection<XmlDoc.Element> acls = r.elements("asset/acl");

		if (acls == null) {
			return;
		}

		for (XmlDoc.Element acl : acls) {
			String actor = acl.value("actor");
			String actorType = acl.value("actor/@type");
			doc = new XmlDocMaker("args");
			doc.push("acl");
			doc.add("id", assetId);
			doc.add("actor", new String[] { "type", actorType }, actor);
			doc.pop();
			executor.execute("asset.acl.revoke", doc.root());
		}
	}

	public static void grantProjectACLsToAsset(ServiceExecutor executor, String cid) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		addProjectACLsToAsset(executor, dm, cid);
		executor.execute("asset.acl.grant", dm.root(), null, null);
	}

	public static void addProjectACLsToAsset(ServiceExecutor executor, XmlDocMaker dm, String cid) throws Throwable {

		// depth of a project cid is 3. for example 1.5.1
		int projectCidDepth = 3;
		int cidDepth = nig.mf.pssd.CiteableIdUtil.getIdDepth(cid);
		if (cidDepth < projectCidDepth) {
			throw new Exception("Invalid citeable ID. Depth should be greater or equal to 3.");
		}
		String id = nig.mf.pssd.plugin.util.CiteableIdUtil.cidToId(executor, cid);

		String projectCid = nig.mf.pssd.CiteableIdUtil.getParentId(cid, cidDepth - projectCidDepth);
		PSSDUtils.addACLToAsset(dm, PSSDUtils.OBJECT_ADMIN_ROLE_NAME, "read-write", null, id);
		PSSDUtils.addACLToAsset(dm, PSSDUtils.OBJECT_GUEST_ROLE_NAME, "read", "none", id);
		PSSDUtils.addACLToAsset(dm, Project.projectAdministratorRoleName(projectCid), "read-write", null, id);
		if (cidDepth == projectCidDepth) {
			PSSDUtils.addACLToAsset(dm, Project.subjectAdministratorRoleName(projectCid), "read", null, id);
		} else {
			PSSDUtils.addACLToAsset(dm, Project.subjectAdministratorRoleName(projectCid), "read-write", null, id);
		}
		PSSDUtils.addACLToAsset(dm, Project.memberRoleName(projectCid), "read", null, id);
		PSSDUtils.addACLToAsset(dm, Project.guestRoleName(projectCid), "read", null, id);

	}

	public static void addACLToAsset(XmlDocMaker dm, String role, String ma, String ca, String assetId)
			throws Throwable {

		dm.push("acl");
		dm.add("id", assetId);
		dm.add("propagate", true);
		dm.add("actor", new String[] { "type", "role" }, role);
		if (ca == null) {
			dm.add("access", ma);
		} else {
			dm.add("metadata", ma);
			dm.add("content", ca);
		}
		dm.pop();
	}

	/**
	 * Removes all invalid ACLs on the local server that might have been left
	 * dangling because an actor (e.g. a role) was removed
	 * 
	 * @param executor
	 * @throws Throwable
	 */
	public static void removeInvalidACLs(ServiceExecutor executor) throws Throwable {

		// Find the danglers
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where", "acl actor invalid");
		dm.add("size", "infinity");
		XmlDoc.Element r1 = executor.execute("asset.query", dm.root());
		Collection<String> ids = r1.values("id");
		if (ids == null)
			return;

		// Delete danglers (have to do it this way as can't pipe query if
		// session is federated
		for (String id : ids) {
			dm = new XmlDocMaker("args");
			dm.add("id", id);
			executor.execute("asset.acl.invalid.remove", dm.root());
		}
	}

	/**
	 * Destroy the specified role on the local server
	 * 
	 * @param executor
	 * @param role
	 * @throws Throwable
	 */
	public static void destroyRole(ServiceExecutor executor, String role) throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("role", role);
		XmlDoc.Element r = executor.execute("authorization.role.exists", doc.root());
		if (r.value("exists").equals("true")) {
			doc = new XmlDocMaker("args");
			doc.add("role", role);
			executor.execute("authorization.role.destroy", doc.root());
		}

	}

	/**
	 * Find out if this asset has a "template" element.
	 * 
	 * @param executor
	 * @param cid
	 * @return
	 * @throws Throwable
	 */
	public static boolean objectHasTemplate(ServiceExecutor executor, String cid) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", cid);
		dm.add("pdist", 0); // Force local
		XmlDoc.Element asset = executor.execute("asset.get", dm.root());
		XmlDoc.Element templ = asset.element("asset/template");
		return templ != null;
	}
}
