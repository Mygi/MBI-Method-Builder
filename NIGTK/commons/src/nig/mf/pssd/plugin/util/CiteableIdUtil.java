package nig.mf.pssd.plugin.util;

import java.util.Collection;

import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

public class CiteableIdUtil extends nig.mf.pssd.CiteableIdUtil {
	

	/**
	 * Returns the named root identifier. Will create if non-existent or return
	 * existing
	 * 
	 * @param executor
	 * @param proute
	 *            Route to remote server. If null use local
	 * @param name
	 *            If null, returns the server citeable ID root
	 * @return
	 * @throws Throwable
	 */
	public static String citeableIDRoot(ServiceExecutor executor, String proute, String name) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("name", name);
		//
		XmlDoc.Element r = null;
		if (name == null) {
			r = executor.execute(new ServerRoute(proute), "citeable.root.get",
					dm.root());
		} else {
			r = executor.execute(new ServerRoute(proute),
					"citeable.named.id.create", dm.root());
		}
		return r.value("cid");
	}
	
	/**
	 * Return the CID root for creating projects under
	 * 
	 * @param executor
	 * @param proute
	 *            Route to remote server. If null use local
	 * @return
	 * @throws Throwable
	 */
	public static String projectIDRoot(ServiceExecutor executor, String proute)
			throws Throwable {

		return citeableIDRoot(executor, proute, nig.mf.pssd.CiteableIdUtil.PROJECT_ID_ROOT_NAME);
	}

	/**
	 * Find the root CID for creating Method objects
	 * 
	 * @param executor
	 * @param proute
	 *            route to remote server. If null use local
	 * @return
	 * @throws Throwable
	 */
	public static String methodIDRoot(ServiceExecutor executor, String proute)
			throws Throwable {

		return citeableIDRoot(executor, proute, nig.mf.pssd.CiteableIdUtil.METHOD_ID_ROOT_NAME);
	}
	
	
	/**
	 * Create the next CID under the given root
	 * 
	 * @param executor
	 * @param pid
	 * @return
	 * @throws Throwable
	 */
	public static String createCid(ServiceExecutor executor, String pid) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("pid", pid);
		XmlDoc.Element r = executor.execute("citeable.id.create", dm.root());
		return r.value("cid");

	}

	/*
	 * Generates the next CID under the RSubject root.
	 * 
	 * @param proute Route to server. If null use local
	 */
	public static String generateRSubjectID(ServiceExecutor executor,
			String proute) throws Throwable {

		String parent = citeableIDRoot(executor, proute, nig.mf.pssd.CiteableIdUtil.RSUBJECT_ID_ROOT_NAME);
		return generateCiteableID(executor, parent);
	}


	/**
	 * Generates the next CID under the Method root
	 * 
	 * @param executor
	 * @param proute
	 *            Route to server. If null use local
	 * @return
	 * @throws Throwable
	 */
	public static String generateMethodID(ServiceExecutor executor,
			String proute) throws Throwable {

		String parent = citeableIDRoot(executor, proute, nig.mf.pssd.CiteableIdUtil.METHOD_ID_ROOT_NAME);
		return generateCiteableID(executor, parent);
	}

	
	
	
	/**
	 * Generates the next unallocated child under the given parent CID. OPerates
	 * in a federated environment so that the correct server, that manages the
	 * CID fetches the next allocation
	 * 
	 * @param executor
	 * @param pid
	 *            The parent CID to get the next child under
	 * @return
	 * @throws Throwable
	 */
	public static String generateCiteableID(ServiceExecutor executor, String pid)
			throws Throwable {

		// In federated session, will find correct server to supply next child
		// CID
		// Could use :pdist infinity to force to all peers whether session
		// federated or not.
		// But an exception is probably the right action
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("pid", pid);
		XmlDoc.Element r = executor.execute("citeable.id.create", dm.root());
		return r.value("cid");
	}

	
	/**
	 * Find out if a CID already exists on the given server
	 * 
	 * @param proute Route to server.  Null means local
	 * @param executor
	 * @param cid
	 * @return
	 * @throws Throwable
	 */
	public static boolean cidExists(String proute, ServiceExecutor executor, String cid) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", cid);
		XmlDoc.Element r = executor.execute(new ServerRoute(proute), "citeable.id.exists", dm.root());
		String exists = r.value("exists");
		if (exists != null) {
			if (exists.equals("true")) {
				return true;
			}
		}
		return false;

	}

	/**
	 * Find out if a Distributed CID already exists on the given server
	 * 
	 * @param executor
	 * @param cid
	 *            Distributed CID
	 * @return
	 * @throws Throwable
	 */
	public static boolean cidExists(ServiceExecutor executor,
			DistributedAsset dCid) throws Throwable {
		return cidExists(dCid.getServerRoute(), executor, dCid.getCiteableID());
	}
	
	/**
	 * Import the given CID Into the local server
	 * 
	 * @param executor
	 * @param cid
	 * @param rootDepth
	 * @return
	 * @throws Throwable
	 */
	public static String importCid(ServiceExecutor executor, String cid, int rootDepth) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", cid);
		dm.add("root-depth", rootDepth);
		XmlDoc.Element r = executor.execute("citeable.id.import", dm.root());
		return r.value("cid");

	}
	
	/**
	 * This function imports a CID and will replace just the first value (the
	 * server) with the server id. This is a way to create a CID as specified.
	 * 
	 * @param executor
	 * @param serverRoute
	 *            the route to the server that manages (allocates this CID tree)
	 * @param The
	 *            citable ID to import
	 * @throws Throwable
	 */
	public static void importCid(ServiceExecutor executor, String serverRoute,
			String cid) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", cid);
		dm.add("root-depth", 1);
		// TODO: should this really run on a remote server ? This would seem
		// to import on a remote host.
		executor.execute(new ServerRoute(serverRoute), "citeable.id.import", dm.root());
	}
	
	/**
	 * Destroy a CID. This means it cannot be re-used
	 * 
	 * @param executor
	 * @param cid
	 * @throws Throwable
	 */
	public static void destroyCID(String proute, ServiceExecutor executor, String cid)
			throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("cid", cid);
		XmlDoc.Element r1 = executor.execute(new ServerRoute(proute), "citeable.id.exists", doc.root());
		if (r1.value("exists").equals("true")) {
			doc = new XmlDocMaker("args");
			doc.add("cid", cid);
			XmlDoc.Element r2 = executor.execute("citeable.id.destroy",
					doc.root());
		}

	}

	
	/**
	 * Convert asset ID to asset CID (if it has one)
	 * 
	 * @param executor
	 * @param id
	 * @return
	 * @throws Throwable
	 */
	public static String idToCid(ServiceExecutor executor, String id)
			throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		dm.add("pdist", 0); // Force local
		XmlDoc.Element r = executor.execute("asset.get", dm.root());
		return r.value("asset/cid");
	}

	/**
	 * Convert asset CID to asset ID
	 * 
	 * @param executor
	 * @param cid
	 * @return
	 * @throws Throwable
	 */
	public static String cidToId(ServiceExecutor executor, String cid)
			throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", cid);
		dm.add("pdist", 0); // Force local
		XmlDoc.Element r = executor.execute("asset.get", dm.root());
		return r.value("asset/@id");
	}

	
	/**
	 * Given a PSSD CID, return the CID of the parent Project Must be of depth > 3
	 * 
	 * @param cid
	 * @return
	 */
	public static String getProjectCID(String cid) {

		int depth = nig.mf.pssd.CiteableIdUtil.getIdDepth(cid);
		if (depth < nig.mf.pssd.CiteableIdUtil.PROJECT_ID_DEPTH || 
				depth > nig.mf.pssd.CiteableIdUtil.DATA_OBJECT_ID_DEPTH) {
			return null;
		}
		int diff = depth - nig.mf.pssd.CiteableIdUtil.PROJECT_ID_DEPTH;
		return nig.mf.pssd.CiteableIdUtil.getParentId(cid, diff);
	}

	/**
	 * Given a PSSD CID, return the CID of the parent Subject Must be of depth > 4
	 * 
	 * @param cid
	 * @return
	 */
	public static String getSubjectCID(String cid) {

		int depth = nig.mf.pssd.CiteableIdUtil.getIdDepth(cid);
		if (depth < nig.mf.pssd.CiteableIdUtil.SUBJECT_ID_DEPTH || 
				depth > nig.mf.pssd.CiteableIdUtil.DATA_OBJECT_ID_DEPTH) {
			return null;
		}
		int diff = depth - nig.mf.pssd.CiteableIdUtil.SUBJECT_ID_DEPTH;
		return nig.mf.pssd.CiteableIdUtil.getParentId(cid, diff);
	}

	


	/**
	 * Generate the next CID child under the given parent CID.
	 * 
	 * If the child number is specified, generate the required CID; this
	 * pre-specified CID must be unallocated or allocated but naked (no
	 * associated assets).
	 * 
	 * If the child number is not specified, generate the next CID and if
	 * desired, fill in the allocator space. The returned CID is allocated and
	 * ready for use
	 * 
	 * @param executor
	 * @param dPID
	 *            The Distributed CID for the parent.
	 * @param childNUmber
	 *            The child number of the object to use if possible. -1 to just
	 *            make a new CID under pcid
	 * @param fillIn
	 *            If childNumber is not given, then if fillIn is true, will look
	 *            for the next available CID either an already allocated naked
	 *            CID or create the next one. This fills in the allocator space.
	 * @return
	 * @throws Throwable
	 */
	public static String generateCiteableID(ServiceExecutor executor,
			DistributedAsset dPID, long childNumber, boolean fillIn)
			throws Throwable {

		String cid = null;
		if (childNumber == -1) {

			// User does not specify the child number
			if (fillIn) {

				// Find the first already allocated naked CID or unallocated
				// CID. Whichever comes first
				long startChild = 1;
				cid = getFirstAvailableCid(executor, dPID, startChild);
			} else {

				// Just create the next CID under this parent. No asset checks
				// are required.
				cid = generateCiteableID(executor, dPID.getCiteableID());
			}
		} else {

			// Generate the CID for the given child number if we can
			cid = generateCiteableID(executor, dPID, childNumber);
		}
		//
		return cid;
	}


	/**
	 * This function gets the first *unallocated* or *naked* (allocated cid with
	 * no asset) child of the parent CID starting with a given number. It will
	 * fill in unallocated 'holes' in CID allocations. The returned CID is
	 * allocated so it is available for use.
	 * 
	 * Currently throws an exception if it fails to find a CID after 1000 tries
	 * 
	 * @param executor
	 * @param parent
	 *            distributed CID.
	 * @param id
	 *            number to start with. So if parent=1.5 and id=2 we begin
	 *            looking from 1.5.2 .. 1.5.N
	 * @return The cid
	 * @throws Throwable
	 */
	public static String getFirstAvailableCid(ServiceExecutor executor,
			DistributedAsset dPID, long id) throws Throwable {

		if (id <= 0)
			throw new Exception("First CID child number must be positive");

		// Find the server managing the CID.
		String managingUUID = dPID.getManagingServerUUID();

		while (true) {
			// Make desired CID string
			String cid = dPID.getCiteableID() + "." + id;

			// We check for the existence of the CID on the server that manages
			// it.
			DistributedAsset dID = new DistributedAsset(managingUUID, cid);
			if (cidExists(executor, dID)) {

				// The CID is already created; now we must see if an asset
				// exists with
				// this CID exists anywhere in the federation.
				if (!anyAssetTypeExistsAnywhere(executor, cid))
					return cid;
			} else {
				// Import (allocate) this CID on the managing server
				importCid(executor, managingUUID, cid);
				return cid;
			}

			// This number was an allocated CID with an asset. Try again
			id++;

			// Remove when secure
			if (id == 1000) {
				throw new Exception(
						"CID child finder appears to be in an infinite loop");
			}
		}
	}



	

	// Private functions

	/**
	 * Generate the next CID child under the given parent CID. If the child
	 * number is specified generate the required CID; this pre-specified CID
	 * must be unallocated or allocated and naked (no associated assets). The
	 * returned CID is allocated and ready for use
	 * 
	 * @param executor
	 * @param dPID
	 *            The distributed CID for the parent
	 * @param childNUmber
	 *            The child number of the object to use if possible. -1 to just
	 *            make a new CID under pcid
	 * @return
	 * @throws Throwable
	 */
	private static String generateCiteableID(ServiceExecutor executor,
			DistributedAsset dPID, long childNumber) throws Throwable {

		// No child given, just make the next CID
		if (childNumber == -1) {
			return generateCiteableID(executor, dPID.getCiteableID());
		}

		// Check this CID, if it exists, has no associated assets
		String cid = dPID.getCiteableID() + "." + childNumber;
		if (anyAssetTypeExistsAnywhere(executor, cid)) {
			throw new Exception("Cannot reuse cid " + cid
					+ " as it has an existing asset.");
		}

		// Import CID (either new or pre-existing is fine) with the server that
		// manages it
		String managingUUID = dPID.getManagingServerUUID();
		importCid(executor, managingUUID, cid);
		//
		return cid;
	}

	/**
	 * Specialized service used when re-using CIDs. Finds any asset (primary or
	 * replica) on any peer with the given CID.
	 * 
	 * A risk here is that a primary asset already exists with the given CID but
	 * that because the federation is not activated, we won't know.
	 * 
	 * Must use asset.exists because it can see any asset, regardless of user
	 * permissions
	 * 
	 * 
	 * @param executor
	 * @param cid
	 * @return
	 * @throws Throwable
	 */
	private static boolean anyAssetTypeExistsAnywhere(ServiceExecutor executor,
			String cid) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", cid);
		ServerRoute serverRoute = new ServerRoute(ServerRoute.EXECUTE_DISTRIBUTED_ALL);
		// XmlDoc.Element r = executor.execute(serverRoute, "asset.exists",
		// dm.root()); // TODO: Restore when ServerRoute bug fixed
		XmlDoc.Element r = executor.execute("asset.exists", dm.root());

		// Collection vals = r.booleanValues("peer/exists"); // Restore when bug
		// fixed
		Collection<Boolean> vals = r.booleanValues("exists");

		if (vals == null) return false;
		for (Boolean val :vals) {
			if (val) return true;
		}
		return false;
	}



}
