package nig.mf.plugin.pssd;

import java.util.Collection;
import java.util.List;

import nig.mf.plugin.pssd.services.SvcObjectDescribe;
import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.pssd.plugin.util.DistributedAsset;
import nig.mf.pssd.plugin.util.DistributedQuery;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class RSubject {
	public static final String TYPE = "r-subject";

	public static final String TYPE_PRIVATE = "r-subject-private";

	public static final String MODEL = "om.pssd.r-subject";

	public static String administratorRoleName(String cid) {

		return "pssd.subject.admin." + cid;
	}

	public static String stateRoleName(String cid) {

		return "pssd.subject.state." + cid;
	}

	public static String guestRoleName(String cid) {

		return "pssd.subject.guest." + cid;
	}

	/**
	 * Returns true if the distributed CID is for an RSubject object
	 */
	public static boolean isObjectRSubject(ServiceExecutor executor, DistributedAsset dCID) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", dCID.getCiteableID());
		XmlDoc.Element r = executor.execute(dCID.getServerRouteObject(), "om.pssd.object.type", dm.root());
		if (r == null)
			return false;
		//
		String type = r.value("type");
		if (type.equals(TYPE))
			return true;
		return false;
	}

	/**
	 * Check if there are any Subjects that use this R-Subject. The Subjects
	 * found will only exist on the same server as the RSubject
	 */
	public static boolean hasRelatedSubjects(ServiceExecutor executor, DistributedAsset dCID) throws Throwable {

		if (isObjectRSubject(executor, dCID)) {
			XmlDocMaker doc = new XmlDocMaker("args");
			doc.add("where", " xpath(pssd-subject/r-subject)='" + dCID.getCiteableID() + "'");
			XmlDoc.Element r1 = executor.execute(dCID.getServerRouteObject(), "asset.query", doc.root());
			if (r1.values("id") != null)
				return true;
		} else {
			throw new Exception("CID " + dCID.getCiteableID() + " is not an RSubject object");
		}
		return false;
	}

	/**
	 * Returns descriptions of the RSubjects associated with the given Project
	 * It will only find RSubjects on the same server as the given Project.
	 * 
	 * @param executor
	 * @param dPID
	 *            Project of interest
	 * @param assetType
	 *            Asset type, primary, replica, all
	 * @return
	 * @throws Throwable
	 */
	public static void findProjectRelatedRSubjects(ServiceExecutor executor, DistributedAsset dPID,
			DistributedQuery.ResultAssetType assetType, XmlWriter w) throws Throwable {

		// FInd the Subjects
		XmlDocMaker doc = new XmlDocMaker("args");
		//
		String query = "cid in '" + dPID.getCiteableID() + "'";
		DistributedQuery.appendResultAssetTypePredicate(query, assetType);
		//
		doc.add("where", "cid in '" + dPID.getCiteableID() + "'");
		doc.add("size", "infinity");
		doc.add("action", "get-meta");
		doc.add("pdist", 0);
		XmlDoc.Element r = executor.execute(dPID.getServerRouteObject(), "asset.query", doc.root());

		// Find any r-subjects
		String proute = dPID.getServerRoute();
		Boolean isLeaf = false;
		Boolean forEdit = false;
		Collection<String> rcids = r.values("asset/meta/pssd-subject/r-subject");
		if (rcids != null) {
			for (String id : rcids) {
				SvcObjectDescribe.describeObject(executor, id, proute, null, isLeaf, forEdit, w);
			}
		}

	}

	/**
	 * Destroy all roles associated with the specified RSubject. You should only
	 * do this if the RSubject asset has been destroyed but this is not checked
	 * here. There is no check that the CID is associated with an RSubject
	 * object (as the assets should already be destroyed). It is the callers
	 * responsibility to make these checks
	 * 
	 * @param executor
	 * @param cid
	 *            Project CID
	 * @throws Throwable
	 */

	public static void destroyRoles(ServiceExecutor executor, String cid) throws Throwable {

		PSSDUtils.destroyRole(executor, guestRoleName(cid));
		PSSDUtils.destroyRole(executor, administratorRoleName(cid));
		PSSDUtils.destroyRole(executor, stateRoleName(cid));
	}

	/**
	 * Locates R-Subjects based on some search criteria. The criteria might be
	 * used for "fuzzy" matching.
	 * 
	 * @param executor
	 * @param assetType
	 *            primary/replica/all
	 * @param pdist
	 *            Distation for distributed query. 0 means local. null means
	 *            distribute in federated session and infinity forces to all
	 *            peers regardless of whether the session is federated or not
	 * @param imeta
	 * @return
	 * @throws Throwable
	 */
	public static XmlDoc.Element find(ServiceExecutor executor, DistributedQuery.ResultAssetType assetType,
			String pdist, XmlDoc.Element imeta, boolean forEdit) throws Throwable {

		// OK, the identity meta may be complete, and/or partial. We need to do
		// a fuzzy match
		// on the values (later). That is, rank by number of hits..
		XmlDocMaker dm = new XmlDocMaker("args");

		if (forEdit) {
			dm.add("action", "get-template-meta");
		} else {
			dm.add("action", "get-meta");
		}

		String query = null;

		if (imeta != null) {

			List<XmlDoc.Element> eles = imeta.elements();
			if (eles == null) {
				return null;
			}

			for (int i = 0; i < eles.size(); i++) {
				XmlDoc.Element me = eles.get(i);

				// String xp = xpath(me);
				// String value = value(me);
				//
				// String sq = "xpath(" + xp + ") as string = ignore-case('" +
				// value + "')";
				// if ( query == null ) {
				// query = sq;
				// } else {
				// query += " and " + sq;
				// }

				Collection<XmlDoc.Element> mes = me.elements();
				for (XmlDoc.Element ee : mes) {
					String xp = me.name() + "/" + xpath(ee);
					String value = value(ee);
					String sq = "xpath(" + xp + ") as string = ignore-case('" + value + "')";
					if (query == null) {
						query = sq;
					} else {
						query += " and " + sq;
					}
				}
			}
		} else {
			query = "xpath(pssd-object/type)='r-subject'";
		}

		// Primary/replica/all
		DistributedQuery.appendResultAssetTypePredicate(query, assetType);

		dm.add("where", query);
		dm.add("size", "infinity");
		if (pdist != null) {
			dm.add("pdist", pdist);
		}

		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		return r;

	}

	private static String xpath(XmlDoc.Element me) throws Throwable {

		String xp = me.name();

		if (me.nbElements() > 0) {
			XmlDoc.Element se = me.elementAt(0);
			xp += "/" + xpath(se);
		}

		return xp;
	}

	private static String value(XmlDoc.Element me) throws Throwable {

		if (me.nbElements() > 0) {
			XmlDoc.Element se = me.elementAt(0);
			return value(se);
		}

		return me.value();
	}
}
