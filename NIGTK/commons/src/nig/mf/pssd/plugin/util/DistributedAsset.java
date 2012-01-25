package nig.mf.pssd.plugin.util;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginThread;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

/**
 * Class to wrap a citeable id and server route, and, if the CID is associated
 * with an asset, its asset ID and replica ID.
 * 
 * In a non-federated environment, the server route is null. In a federated
 * environment, the server route is non-null even for the local server.
 * 
 * This class uses a lazy assignment approach so that private members are only
 * discovered/computed when actually needed by the caller.
 * 
 * 
 * @author nebk
 * 
 */
public class DistributedAsset {

	private ServiceExecutor _thread = null; // The thread executing this class
	//
	private String _cid = null; // The citeable ID of interest
	// Server route. In a federation, the server route will be non-null for a
	// local object
	// following a query.
	private String _route = null; // Server route to CID/assett
	//
	private String _localServerUUID = null; // The UUID of the server executing
											// this thread (the local server).
	private String _supplyingServerUUID = null; // The UUID of the server
												// supplying (i.e. the server at
												// the end of the server route)
												// this CID/asset.
	//
	private Boolean _isLocal = null; // True if the last server in the route for
										// this CID/asset is the local
										// (executing) server.
	private Boolean _hasAsset = null; // Does this CID actually have an asset
										// associated with it; null if not yet
										// checked.
	private String _assetId = null; // Asset ID; null if not set or no asset
	private String _assetVid = null; // Asset VID; null if not set or no asset
										// or asset but no vid
	private String _assetRid = null; // Replica ID; null if not set or no asset
										// or asset but no rid
	//
	private DistributedAsset _parentProject = null; // The parent project of
													// this asset (if possible)
	Boolean _parentProjectContext = null;
	private DistributedAsset _parentSubject = null; // The parent subject of
													// this asset (if possible)
	Boolean _parentSubjectContext = null;

	/**
	 * Constructor
	 * 
	 * @param route
	 *            null means local in unfederated environment. Else must have
	 *            value
	 * @param cid
	 *            The citeable ID. Must be of depth 2 (e.g. Project root) or
	 *            more.
	 * @throws Throwable
	 */
	public DistributedAsset(String route, String cid) throws Throwable {

		// CIDs include the Project root (depth 2)
		if (nig.mf.pssd.CiteableIdUtil.getIdDepth(cid) < (nig.mf.pssd.CiteableIdUtil.PROJECT_ID_DEPTH - 1)) {
			throw new Exception("The CID " + cid + " must be at least of depth = "
					+ (nig.mf.pssd.CiteableIdUtil.PROJECT_ID_DEPTH - 1));
		}
		_cid = cid;
		_route = route;
		_thread = PluginThread.serviceExecutor();
	}

	/**
	 * Convenience constructor for standard service interface argument :pid
	 * -proute
	 * 
	 * @param cid
	 *            The citeable ID as XML with value (the cid) and attribute
	 *            'proute' the route The citeable ID must be of depth 2 (e.g.
	 *            Project root) or more.
	 * @throws Throwable
	 */
	public DistributedAsset(XmlDoc.Element cid) throws Throwable {

		if (cid == null)
			return;
		//
		_cid = cid.value();

		// CIDs include the Project root (depth 2)
		if (nig.mf.pssd.CiteableIdUtil.getIdDepth(_cid) < (nig.mf.pssd.CiteableIdUtil.PROJECT_ID_DEPTH - 1)) {
			throw new Exception("The CID " + cid + " must be at least of depth = "
					+ (nig.mf.pssd.CiteableIdUtil.PROJECT_ID_DEPTH - 1));
		}
		//
		_route = cid.value("@proute");
		_thread = PluginThread.serviceExecutor();
	}

	/**
	 * Find the parent project of this object. Returns self if a Project (as we
	 * don't always know what kind of object belongs to the DCA). CIDs higher
	 * than Project have already been rejected in the DCI constructor.
	 * 
	 * Returns null if parent project cannot be found
	 * 
	 * @param readOnly
	 *            This means that the context for finding this parent is a
	 *            read-only (viewing) context. Therefore, the parent project can
	 *            be a primary or a replica. If the context is read/write (e.g.
	 *            editing/creating an asset) then the parent project must be
	 *            primary as we are not allowed to create children under
	 *            replicas.
	 * @return
	 * @throws Throwable
	 */
	public DistributedAsset getParentProject(Boolean readOnly) throws Throwable {

		// If cached return it.
		if (_parentProjectContext != null && _parentProjectContext == readOnly && _parentProject != null) {
			return _parentProject;
		}

		// Return self if is a Project (of course this is not a strict test as
		// other CID trees could
		// have the same Depth).
		int depth = nig.mf.pssd.CiteableIdUtil.getIdDepth(_cid);
		if (depth == nig.mf.pssd.CiteableIdUtil.PROJECT_ID_DEPTH) {
			_parentProject = this;
			return _parentProject;
		}

		// Find the project parent.
		String pid = nig.mf.pssd.plugin.util.CiteableIdUtil.getProjectCID(_cid);
		_parentProject = findObject(_thread, _route, pid, readOnly);

		// Set new context if found
		if (_parentProject != null) {
			_parentProjectContext = readOnly;
		}
		//
		return _parentProject;
	}

	/**
	 * Get the citeable ID of the parent project. This is quite different from
	 * finding the parent project itself (function getParentProject) which
	 * includes knowledge of the server on which the project resides
	 * 
	 * @return
	 */
	public String getParentProjectCID() {

		return nig.mf.pssd.plugin.util.CiteableIdUtil.getProjectCID(_cid);
	}

	/**
	 * Find the parent Subject of this object. Returns self if CID represents a
	 * Subject. CIDs higher than Project have already been rejected in the DCI
	 * constructor. Exception if object is a Project
	 * 
	 * Exception if parent subject cannot be found
	 * 
	 * @param readOnly
	 *            This means that the context for finding this parent is a
	 *            read-only (viewing) context. Therefore, the parent project can
	 *            be a primary or a replica. If the context is read/write (e.g.
	 *            editing/creating an asset) then the parent project must be
	 *            primary as we are not allowed to create children under
	 *            replicas.
	 * @return
	 * @throws Throwable
	 */
	public DistributedAsset getParentSubject(Boolean readOnly) throws Throwable {

		// If cached return it.
		if (_parentSubjectContext != null && _parentSubjectContext == readOnly && _parentSubject != null) {
			return _parentSubject;
		}

		// Return self if is a Subject (of course this is not a strict test as
		// other CID trees could
		// have the same Depth).
		int depth = nig.mf.pssd.CiteableIdUtil.getIdDepth(_cid);
		if (depth == nig.mf.pssd.CiteableIdUtil.SUBJECT_ID_DEPTH) {
			_parentSubject = this;
			return _parentSubject;
		} else if (depth < nig.mf.pssd.CiteableIdUtil.SUBJECT_ID_DEPTH) {
			throw new Exception("The object " + _cid + " is a Project so you cannot find its parent Subject");
		}

		// Find the subject parent.
		String sid = nig.mf.pssd.plugin.util.CiteableIdUtil.getSubjectCID(_cid);
		_parentSubject = findObject(_thread, _route, sid, readOnly);

		// Set new context if found
		if (_parentSubject != null) {
			_parentSubjectContext = readOnly;
		}
		//
		return _parentSubject;
	}

	/**
	 * Get the citeable ID of the parent subject. This is quite different from
	 * finding the parent subject itself (function getParentProject) which
	 * includes knowledge of the server on which the subject resides
	 * 
	 * @return
	 */
	public String getParentSubjectCID() {

		return nig.mf.pssd.plugin.util.CiteableIdUtil.getSubjectCID(_cid);
	}

	/**
	 * Return server route to the citeable ID as a String
	 * 
	 * @return Server route in the form <uuid _local><uuid remote 1>.<uuid
	 *         remote 2>.<uuid remote N>
	 */
	public String getServerRoute() {

		return _route;
	}

	/**
	 * Return the server route to the citeable ID wrapped as ServerRoute object
	 * Returns null if server route is null
	 * 
	 * @return
	 */
	public ServerRoute getServerRouteObject() throws Throwable {

		if (_route == null)
			return null;
		return new ServerRoute(_route);
	}

	/**
	 * Return the citeable ID
	 * 
	 * @return
	 */
	public String getCiteableID() {

		return _cid;
	}

	/**
	 * Get asset meta-data. If there is no asset with this CID and proute we
	 * will get an exception
	 * 
	 * @return
	 */
	public XmlDoc.Element getAsset() throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", _cid);
		return _thread.execute(new ServerRoute(_route), "asset.get", dm.root());
	}

	/**
	 * If the CID has an associated asset, return its asset ID. If there is no
	 * asset, an exception will occur. Use function hasAsset to check
	 * 
	 * @return
	 * @throws Throwable
	 */
	public String getAssetID() throws Throwable {

		setIDs();
		if (!_hasAsset) {
			throw new Exception("The citeable ID " + _cid + " does not have an associated asset");
		}
		return _assetId;
	}

	/**
	 * If the CID has an associated asset, return its asset VID. If there is no
	 * asset, an exception will occur. Use function hasAsset to check
	 * 
	 * @return
	 * @throws Throwable
	 */
	public String getAssetVID() throws Throwable {

		setIDs();
		if (!_hasAsset) {
			throw new Exception("The citeable ID " + _cid + " does not have an associated asset");
		}
		return _assetVid;
	}

	/**
	 * Get the server UUID that actually manages this CID (as distinct from the
	 * server supplying it via the server route). This server is the one that
	 * can allocate new child CIDs under the CID root.
	 * 
	 * @return
	 * @throws Throwable
	 */
	public String getManagingServerUUID() throws Throwable {

		// This class caches the values in static memory
		return ManagingCIDServer.serverUUID(_cid);
	}

	/**
	 * Get the server, as specified by the server route, that is supplying this
	 * CID. This is just the UUID of the last server in the server route. Note
	 * that this may not be the same server that actually manages the CID (see
	 * getManagingServer). If the server route is null, this means a local
	 * object and the UUID of the executing server will be returned
	 * 
	 * @return
	 */
	public String getSupplyingServerUUID() throws Throwable {

		setSupplyingServerUUID();
		return _supplyingServerUUID;
	}

	/**
	 * Is the CID associated with an asset (primary or replica) ?
	 * 
	 * @return
	 * @throws Throwable
	 */
	public Boolean hasAsset() throws Throwable {

		setHasAsset();
		return _hasAsset;
	}

	/**
	 * Does this object have any children on remote servers in a federation.
	 * Query is forced over all peers regardless of whether the session is
	 * 'federated' or not.
	 * 
	 * @param asset
	 *            type (primary, replica, all)
	 * @param ptag
	 *            Only look at peers with this tag. If null, look at all peers
	 * @param pdist
	 *            distation for distributed query
	 * @return
	 * @throws Throwable
	 */
	public Boolean hasRemoteChildren(DistributedQuery.ResultAssetType assetType, String ptag, String pdist)
			throws Throwable {

		return DistributedAssetUtil.hasRemoteChildren(_thread, _route, _cid, assetType, ptag, pdist);
	}

	/**
	 * Is the CID, as specified by the server route, local to the executing
	 * server?
	 * 
	 * @return
	 */
	public Boolean isLocal() throws Throwable {

		// Lazy assignment
		if (_isLocal == null) {

			// By definition, if the asset has a null route it is local to the
			// executing server
			if (_route != null) {
				setLocalServerUUID();
				setSupplyingServerUUID();
				_isLocal = _localServerUUID.equals(_supplyingServerUUID);
			} else {
				_isLocal = true;
			}
		}
		//
		return _isLocal;
	}

	/**
	 * Is the object a replica ? Caller should check hasAsset first. Exception
	 * if no asset associated with the CID.
	 * 
	 * @return
	 * @throws Throwable
	 * 
	 */
	public Boolean isReplica() throws Throwable {

		setIDs();
		if (!_hasAsset) {
			throw new Exception("The citeable ID " + _cid + " does not have an associated asset");
		}
		return _assetRid != null;
	}

	/**
	 * Print out the server route and CID
	 * 
	 * @param s
	 *            A text string to label the output
	 */
	public void print(String s) {

		System.out.println(s + toString());
	}

	/**
	 * Set the replica ID (<server uuid>.<asset id>) for this object if known.
	 * Otherwise it can be computed via function isReplica
	 * 
	 * @param rid
	 *            If null, then this will set the internal state to indicate
	 *            that this object is not a replica
	 */

	public void setReplicaId(String rid) throws Throwable {

		if (rid == null) {
			_assetRid = null;
		} else {
			_assetRid = rid;
		}
		setIDs(); // Won't overwrite rid
	}

	/**
	 * String representation of asset
	 * 
	 */
	public String toString() {

		return "route/cid/id/rid = " + _route + "/" + _cid + "/" + _assetId + "/" + _assetRid;
	}

	// Private functions

	/**
	 * Set if asset (primary or replica) exists
	 * 
	 * @throws Throwable
	 */
	private void setHasAsset() throws Throwable {

		if (_hasAsset == null) {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("cid", _cid);
			XmlDoc.Element r = _thread.execute(new ServerRoute(_route), "asset.exists", dm.root());
			_hasAsset = r.booleanValue("exists");
		}
	}

	/**
	 * Find if CID has an asset and if so set the asset IDs. If asset rid
	 * already set via function setReplicaId does not overwrite
	 * 
	 * 
	 * @throws Throwable
	 */
	private void setIDs() throws Throwable {

		setHasAsset();
		if (_hasAsset) {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("cid", _cid);
			dm.add("pdist", 0); // Force local on whatever server it's executed
			XmlDoc.Element r = _thread.execute(new ServerRoute(_route), "asset.get", dm.root()); // Exception
																									// if
																									// does
																									// not
																									// exist

			// rid may have been set manually
			if (_assetRid == null)
				_assetRid = r.value("asset/rid");
			_assetId = r.value("asset/@id");
			_assetVid = r.value("asset/@vid");
		} else {
			_assetRid = null;
			_assetId = null;
			_assetVid = null;
		}
	}

	/**
	 * Find the server UUID at the end of this server route.
	 * 
	 */
	private void setSupplyingServerUUID() throws Throwable {

		if (_route == null) {

			// Null means the local executing server. Fill it in explicitly
			// as in a federation, the route will be equal to the local server
			// UUID
			setLocalServerUUID();
			_supplyingServerUUID = _localServerUUID;
		} else {

			// server routes are of the form <server 1 UUID>.<server 2
			// UUID>...<server N UUID>
			// The last one is the one we want
			_supplyingServerUUID = DistributedAssetUtil.serverUUIDFromProute(_route);
		}
	}

	/**
	 * Set the local server UUID
	 * 
	 * @return
	 * @throws Throwable
	 */
	private void setLocalServerUUID() throws Throwable {

		if (_localServerUUID == null) {
			_localServerUUID = PluginService.serverIdentityAsString();
		}
	}

	/**
	 * FInd the asset with the given CID
	 * 
	 * @param preferredRoute
	 *            when looking for replicas, this is the first place we'd like
	 *            to find it
	 * @param cid
	 *            CID of the object to find
	 * @param readOnly
	 *            If true, can find primary or replica, else primary only
	 * @return
	 * @throws Throwable
	 */
	private DistributedAsset findObject(ServiceExecutor thread, String preferredRoute, String cid, Boolean readOnly)
			throws Throwable {

		if (readOnly) {
			// Primary and then replica
			DistributedAsset dID = DistributedAssetUtil.findPrimaryObject(thread, cid);
			if (dID == null) {
				return DistributedAssetUtil.findReplicaObject(thread, cid, preferredRoute);
			} else {
				return dID;
			}
		} else {
			if (isReplica()) {
				// We probably shouldn't be looking for the parent of a replica
				// in a read/write environment.
				// So this should is an exception... I think...
				// _parentProject = DistributedAssetUtil.findReplicaObject
				// (_thread, pid, preferredRoute);
				throw new Exception("Looking for the parent of a replica in a read/write context is not allowed");
			} else {
				return DistributedAssetUtil.findPrimaryObject(_thread, cid);
			}
		}
	}
}
