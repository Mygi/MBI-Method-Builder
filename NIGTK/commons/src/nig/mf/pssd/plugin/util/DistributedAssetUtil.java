package nig.mf.pssd.plugin.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Vector;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.*;

/**
 * A collection of functions to aid in federation management
 * 
 * @author nebk
 * 
 */
public class DistributedAssetUtil {

	// // Asset type; maybe there will be other types in the future, so 'all'
	// rather than 'both'
	// public enum AssetType { primary, replica, all };
	//
	// // When collections may contain primaries and replicas. The policy filter
	// // states how we filter this collection
	// public enum FilterPolicy { primary_then_any_replica, // Use the primary
	// in preference to any replica
	// primary_then_nearest_replica, // Use the primary in preference to the
	// nearest replica
	// any, // Use the first object that is encountered, primary or replica
	// nearest, // Use the nearest (proute) object that is encountered, primary
	// or replica
	// none }; // Don't filter
	//
	// /**
	// * Convert an asset type string to the enum
	// *
	// * @param assetType One of "primary", "replica", "all"
	// *
	// * @return
	// * @throws Throwable
	// */
	// public static AssetType assetTypeFromString (String assetType) throws
	// Throwable {
	// AssetType tp = null;
	// String t = assetType.toLowerCase();
	// if (t.equals("replica")) {
	// tp = AssetType.replica;
	// } else if (t.equals("primary")) {
	// tp = AssetType.primary;
	// } else if (t.equals("all")) {
	// tp = AssetType.all;
	// } else {
	// throw new Exception ("The asset type " + assetType +
	// " is not supported");
	// }
	// return tp;
	// }
	//
	// /**
	// * Convert a filter policy string to the enum
	// *
	// * @param filterPolicy One of "primary-then-any-replica", "none"
	// *
	// * @return
	// * @throws Throwable
	// */
	// public static FilterPolicy filterPolicyFromString (String filterPolicy)
	// throws Throwable {
	// FilterPolicy p = null;
	// if (filterPolicy.equals("primary-then-any-replica")) {
	// p = FilterPolicy.primary_then_any_replica;
	// } else if (filterPolicy.equals("none")) {
	// p = FilterPolicy.none;
	// } else {
	// throw new Exception ("The filter policy " + filterPolicy +
	// " is not supported");
	// }
	// return p;
	// }
	//
	// /**
	// * Formulate a partial query string to select the given type of asset
	// *
	// * @param assetType
	// * @return
	// */
	// public static String queryString (AssetType assetType) {
	// if (assetType==AssetType.primary) {
	// return " and rid hasno value";
	// } else if (assetType==AssetType.replica) {
	// return " and rid has value";
	// } else {
	// return "";
	// }
	// }

	/**
	 * Ascertain if an asset associated with the given CID and proute exists.
	 * Does a distributed query in a federated session (see proute/pdist details
	 * below). Only objects that the user has permission to see will be
	 * included.
	 * 
	 * @param executor
	 * @param proute
	 *            Server route to execute query on
	 * @param pdist
	 *            If the server route is not given, you can specify the
	 *            distation of the query (infinity will force it over all
	 *            servers). If the proute is given, pdist is set to 0 and the
	 *            query only run on that server.
	 * @param cid
	 *            The CID of interest
	 * @param assetType
	 *            Type of asset, one of "primary", "replica", "both"
	 * @param includeChildren
	 *            If true, will also check if the CID has any children that have
	 *            assets\
	 * @param pssdOnly
	 *            restricts to only PSSD Objects
	 * @param w
	 *            Writer to write result in. Can be null for no writer
	 * @return
	 * @throws Throwable
	 */
	public static boolean assetExists(ServiceExecutor executor, String proute, String pdist, String cid,
			DistributedQuery.ResultAssetType assetType, boolean includeChildren, boolean pssdOnly, XmlWriter w)
			throws Throwable {

		// Do this with a query rather than asset exists so I can make it
		// distributed
		// in a federation and also test for primary/replica objects
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("size", 1);
		dm.add("action", "get-meta");
		//
		if (assetType == DistributedQuery.ResultAssetType.primary) {
			if (includeChildren) {
				dm.add("where", "(cid='" + cid + "' or cid starts with '" + cid + "') and rid hasno value");
			} else {
				dm.add("where", "cid='" + cid + "' and rid hasno value");
			}
		} else if (assetType == DistributedQuery.ResultAssetType.replica) {
			if (includeChildren) {
				dm.add("where", "(cid='" + cid + "' or cid starts with '" + cid + "') and rid has value");
			} else {
				dm.add("where", "cid='" + cid + "' and rid has value");
			}
		} else if (assetType == DistributedQuery.ResultAssetType.all) {
			if (includeChildren) {
				dm.add("where", "cid='" + cid + "' or cid starts with '" + cid + "'");
			} else {
				dm.add("where", "cid='" + cid + "'");
			}
		} else {
			throw new Exception("Unknown asset type = " + assetType);
		}

		// Query
		XmlDoc.Element r = null;
		if (proute != null) {
			if (pdist != null)
				dm.add("pdist", pdist); // Distributed
			r = executor.execute("asset.query", dm.root());
		} else {
			dm.add("pdist", 0); // Local
			r = executor.execute(new ServerRoute(proute), "asset.query", dm.root());
		}

		// Parse
		boolean exists = false;
		if (r.element("asset") != null) {

			// Is it a PSSD object ?
			exists = true;
			if (pssdOnly) {
				String type = r.value("asset/meta/pssd-object/type");
				if (type == null)
					exists = false;
			}
		}
		//
		if (exists) {
			if (w != null) {
				String proute2 = r.value("asset/@proute");
				String rid = r.value("asset/rid");
				w.add("exists", new String[] { "proute", proute2, "id", cid, "rid", rid }, true);
			}
		} else {
			if (w != null)
				w.add("exists", new String[] { "id", cid }, false);
		}
		return exists;
	}

	/**
	 * Find if the given CID has children assets (at any level) that aren't
	 * local to it. Forces the query over all peers whether the session is
	 * federated or not.
	 * 
	 * @param executor
	 * @param proute
	 *            The server route to this CID. Null means local
	 * @param cid
	 *            The CID of interest
	 * @param assetType
	 *            Type of asset, one of "primary", "replica", "both"
	 * @param ptag
	 *            Only look at peers with this tag. If null, look at all peers
	 * @param pdist
	 *            distation for distributed query
	 * @throws Throwable
	 */
	public static boolean hasRemoteChildren(ServiceExecutor executor, String proute, String cid,
			DistributedQuery.ResultAssetType assetType, String ptag, String pdist) throws Throwable {

		// Form query
		XmlDocMaker dm = new XmlDocMaker("args");
		if (pdist != null) {
			dm.add("pdist", pdist);
		} else {
			// Defaults to infinity
			dm.add("pdist", "infinity");
		}
		if (ptag != null)
			dm.add("ptag", ptag);
		String where = "cid starts with '" + cid + "'";
		DistributedQuery.appendResultAssetTypePredicate(where, assetType);
		dm.add("where", where);

		// Parse. All we care about is the server UUID of the asset; we don't
		// care
		// about the path to it.
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		Collection<String> routes = r.values("id/@proute");

		// Because we have forced the query to all peers, all assets will have a
		// non-null proute
		// So no proutes means no assets
		if (routes == null)
			return false;

		// Find the server UUID of the given CID
		String givenUUID = serverUUIDFromProute(proute);

		// Iterate and look for a remote asset. If we find one we are done.
		for (String route : routes) {
			String assetUUID = serverUUIDFromProute(route);
			if (!assetUUID.equals(givenUUID))
				return true;
		}
		return false;
	}

	/**
	 * Find an object, first looking for a primary, second a replica on the
	 * preferred route and third a replica anywhere.
	 * 
	 * @param executor
	 * @param cid
	 * @return
	 * @throws Throwable
	 */
	public static DistributedAsset findObject(ServiceExecutor executor, String cid, String preferredRoute)
			throws Throwable {

		DistributedAsset dID = DistributedAssetUtil.findPrimaryObject(executor, cid);
		if (dID == null)
			dID = DistributedAssetUtil.findReplicaObject(executor, cid, preferredRoute);
		return dID;
	}

	/**
	 * Finds the primary asset that is associated with this CID and proute. The
	 * query is distributed in a federated session.
	 * 
	 * Returns null if none found.
	 * 
	 * @param cid
	 * @return
	 * @throws Throwable
	 */
	public static DistributedAsset findPrimaryObject(ServiceExecutor executor, String cid) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where", "cid='" + cid + "' and rid hasno value");
		dm.add("action", "get-cid");
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		if (r.value("cid") == null)
			return null;
		//
		DistributedAsset t = new DistributedAsset(r.value("cid/@proute"), cid);
		return t;
	}

	/**
	 * Finds replica assets associated with the given CID. A preferred proute
	 * where we would like to find a replica is provided (could enhance to
	 * supply Array of preferred routes). The query is distributed in a
	 * federated session.
	 * 
	 * Returns null if none found.
	 * 
	 * @param cid
	 * @param preferredRoute
	 *            The route to the server we would like to find this replica on.
	 *            null means local
	 * @return
	 * @throws Throwable
	 */
	public static DistributedAsset findReplicaObject(ServiceExecutor executor, String cid, String preferredRoute)
			throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where", "cid='" + cid + "' and rid has value");

		// Switch to get-rid when get new server available
		dm.add("action", "get-meta");

		// First look for the replica on the specified server. The route may be
		// null which is
		// just the local server.
		dm.add("pdist", 0);
		XmlDoc.Element r = executor.execute(new ServerRoute(preferredRoute), "asset.query", dm.root());

		// Extract result if non-null
		if (r.element("asset") != null) {

			// There can only be one replica on any given server so it is safe
			// to extract the values directly.
			DistributedAsset t = new DistributedAsset(preferredRoute, cid);
			t.setReplicaId(r.value("asset/rid"));
			return t;
		} else {

			// We didn't find it in the preferred place so look elsewhere in the
			// federation
			// This could be enhanced to find the closest one (by proute); this
			// is possibly
			// the order they come back in anyway
			dm = new XmlDocMaker("args");
			dm.add("where", "cid='" + cid + "' and rid has value");
			dm.add("action", "get-meta");
			r = executor.execute("asset.query", dm.root());
			if (r.element("asset") == null)
				return null;
			//
			Vector<XmlDoc.Element> els = r.elements("cid");
			for (XmlDoc.Element el : els) {
				String rid = el.value("asset/rid");
				String proute = el.value("asset/@proute");

				// Just return the first one we find
				DistributedAsset t = new DistributedAsset(proute, cid);
				t.setReplicaId(rid);
				return t;
			}
		}
		//
		DistributedAsset t = null;
		return t;
	}

	/**
	 * Find the server at the end of the server route
	 * 
	 * @param proute
	 * @return
	 */
	public static String serverUUIDFromProute(String proute) {

		if (proute == null) {
			return PluginService.serverIdentityAsString();
		} else {
			if (proute.contains(".")) {
				String[] s = proute.split("\\.");
				return s[(s.length) - 1];
			} else {
				return proute;
			}
		}
	}

	/**
	 * Augment a query for the type of asset desired and filter the result for
	 * the kind of policy wanted.
	 * 
	 * @param executor
	 * @param assetType
	 *            Type of asset desired
	 * @param filterPolicy
	 *            How to filter the result
	 * @param query
	 *            MUst be fully set except for the clause that selects the asset
	 *            type which will be set here
	 * @param dm
	 *            Must be fully populated with all arguments required for
	 *            asset.query, except for the "where" which will be filled in
	 *            here from the augmented query
	 * @return
	 * @throws Throwable
	 */
	public static XmlDoc.Element queryAndFilter(ServiceExecutor executor, DistributedQuery.ResultAssetType assetType,
			DistributedQuery.ResultFilterPolicy filterPolicy, String query, XmlDocMaker dm) throws Throwable {

		// TODO:
		// A bug in asset,query prevents it from finding both replicas and
		// primaries
		// So make two queries and join them for now. Later when fixed all this
		// can be replaced
		// by a single query and filter

		XmlDoc.Element where = null;
		// Find the primaries
		XmlDoc.Element rPrimary = null;
		Collection<XmlDoc.Element> cPrimary = null;
		if (assetType == DistributedQuery.ResultAssetType.primary || assetType == DistributedQuery.ResultAssetType.all) {
			String query2 = query + " and rid hasno value";
			where = new XmlDoc.Element("where", query2);
			dm.add(where);
			rPrimary = executor.execute("asset.query", dm.root());
			if (assetType == DistributedQuery.ResultAssetType.primary)
				return rPrimary; // Filter policy not relevant
			//
			if (rPrimary != null)
				cPrimary = rPrimary.elements("asset");
		}

		// Now find the replicas
		XmlDoc.Element rReplica = null;
		Collection<XmlDoc.Element> cReplica = null;
		if (assetType == DistributedQuery.ResultAssetType.replica || assetType == DistributedQuery.ResultAssetType.all) {
			String query2 = query + " and rid has value";
			if (where != null) {
				where.setValue(query2); // We can't copy DM so change the value
										// of the 'where' element by reference
			} else {
				where = new XmlDoc.Element("where", query2);
				dm.add(where);
			}
			rReplica = executor.execute("asset.query", dm.root());
			if (assetType == DistributedQuery.ResultAssetType.replica)
				return rReplica; // Filter policy not relevant
			//
			if (rReplica != null)
				cReplica = rReplica.elements("asset");
		}

		// Now reformat; join primary/replica queries if needed. It does not
		// make sense to apply the filter policy
		// if the Collection is only of a single type (by result or selection).
		if (cReplica == null) {
			return rPrimary;
		} else if (cPrimary == null) {
			return rReplica;
		} else {

			// Join the primaries and replicas
			XmlDoc.Element join = new XmlDoc.Element("result");
			join.addAll(cPrimary);
			join.addAll(cReplica);

			// Enact filter policy if we have a mixed collection
			if (filterPolicy == DistributedQuery.ResultFilterPolicy.primary_then_any_replica) {
				filterCollectionPrimaryThenAnyReplica(executor, join);
			} else if (filterPolicy == DistributedQuery.ResultFilterPolicy.none) {
				return join;
			} else {
				throw new Exception("This Filter Policy is not yet implemented");
			}
			//
			return join;
		}
	}

	/**
	 * Filter the collection of assets as returned by asset.query Return a set
	 * of unique CIDs. If the primary object is found, retain that. If there is
	 * no primary, then retain the first replica found.
	 * 
	 * When/if we add other filter types, this function could be turned into an
	 * base class to supply the basic iteration superstructure and then the
	 * derived classes could supply the algorithm.
	 * 
	 * 
	 * @param executor
	 * @param r
	 *            The only structure we require is asset cid rid
	 * @return
	 * @throws Throwable
	 */
	public static void filterCollectionPrimaryThenAnyReplica(ServiceExecutor executor, XmlDoc.Element r)
			throws Throwable {

		// The map accumulates the elements we already have
		// The key is the CID, the value is the actual element
		HashMap<String, XmlDoc.Element> map = new HashMap<String, XmlDoc.Element>();

		// Elements to delete from the input
		ArrayList<XmlDoc.Element> assetsToRemove = new ArrayList<XmlDoc.Element>();

		// Iterate through the assets. Primary or replica may arrive in any
		// order from any server
		Collection<XmlDoc.Element> assets = r.elements("asset");
		if (assets != null) {
			for (XmlDoc.Element asset : assets) {
				String cid = asset.value("cid");
				String rid = asset.value("rid");

				// If we have this CID already, lets see what kind of asset the
				// new one is
				if (map.containsKey(cid)) {
					if (rid == null) {

						// An extra check could be that the existing element
						// cannot be
						// primary as there should only be one primary in a
						// federation

						// The new element is a primary which we want in
						// preference to replicas
						// Add the existing element we don't want into the
						// removal list
						assetsToRemove.add((XmlDoc.Element) map.get(cid));

						// Now replace map element with new primary
						map.remove(cid);
						map.put(cid, asset);
					} else {
						// We have a replica. The map already contains an entry
						// and
						// that entry may be primary or replica. Unless we want
						// to select
						// objects by distance of their path, there is nothing
						// further
						// to do with this asset apart from mark it for deletion
						assetsToRemove.add(asset);
					}
				} else {

					// We don't have this CID, add it to the list of existing
					// CID/elements
					map.put(cid, asset);
				}
			}
		}

		// Now remove all of the elements we don't want
		if (assetsToRemove.size() > 0) {
			for (XmlDoc.Element el : assetsToRemove) {
				r.removeInstance(el);
			}
		}
	}
}
