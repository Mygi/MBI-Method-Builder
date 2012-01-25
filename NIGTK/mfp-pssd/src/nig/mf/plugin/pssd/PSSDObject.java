package nig.mf.plugin.pssd;

import java.util.Collection;

import nig.mf.plugin.pssd.method.ExMethod;
import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.pssd.plugin.util.DistributedAsset;
import nig.mf.pssd.plugin.util.DistributedAssetUtil;
import nig.mf.pssd.plugin.util.DistributedQuery;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class PSSDObject extends Asset {

	private String _type;
	private String _name;
	private String _description;

	public PSSDObject() {

	}

	protected PSSDObject(XmlDoc.Element ae) throws Throwable {

		super(ae);
	}

	public PSSDObject(String type, String name, String description) {

		_type = type;
		_name = name;
		_description = description;
	}

	public String id() {

		return citeableId();
	}

	public String type() {

		return _type;
	}

	public String name() {

		return _name;
	}

	public String description() {

		return _description;
	}

	@Override
	protected void parseAssetMeta(XmlDoc.Element ae) throws Throwable {

		super.parseAssetMeta(ae);
		XmlDoc.Element poe = ae.element("meta/pssd-object");
		if (poe != null) {
			_type = poe.value("type");
			_name = poe.value("name");
			_description = poe.value("description");
		}
	}

	@Override
	protected void saveAssetMeta(XmlWriter w) throws Throwable {

		w.push("pssd-object");
		w.add("type", type());
		w.add("name", name());
		if (description() != null) {
			w.add("description", description());
		}
		w.pop();
	}

	/**
	 * Generic function to recursively move a PSSD object (Subject, ExMethod,
	 * Study, DataSet and DataObject) and all of its children to a new parent
	 * object. It does this by re-setting CIDs and modifying ACLs. We do not
	 * allow Projects to be moved as this brings in a whole can of 'role' worms.
	 * Updates meta-data on objects where possible. Read the code for the
	 * details.
	 * 
	 * It is up to the caller to make sure the use of this function is
	 * consistent with Federation policy
	 * 
	 * @param executor
	 * @param id
	 *            The citeable ID of the PSSD object to move
	 * @param pid
	 *            The citeable ID of the destination parent object. This parent
	 *            must have an asset (i.e. it's not just a CID)
	 * @param preserve
	 *            means try to preserve child numbers in the destination tree
	 * @return the CID of the moved object
	 * @throws Throwable
	 */
	public static String move(ServiceExecutor executor, String id, String pid, boolean preserve) throws Throwable {

		// Always set proute to null. We don't want to move assets that are
		// remote to the execution server
		String proute = null;

		// Validate input
		String objectCidIn = id;
		String parentCidOut = pid;
		int depthIn = nig.mf.pssd.CiteableIdUtil.getIdDepth(objectCidIn);
		int depthOut = nig.mf.pssd.CiteableIdUtil.getIdDepth(parentCidOut);

		// Check object types
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", objectCidIn);
		XmlDoc.Element r = executor.execute("om.pssd.object.type", dm.root());
		String objectType = r.value("type");
		if (!(objectType.equals(Subject.TYPE) || objectType.equals(ExMethod.TYPE) || objectType.equals(Study.TYPE)
				|| objectType.equals(DataSet.TYPE) || objectType.equals(DataObject.TYPE))) {
			throw new Exception("The object to move must be one of Subject, ExMethod, Study, DataSet or DataObject.");
		}

		boolean includeChildren = false;
		String pdist = null;
		if (!DistributedAssetUtil.assetExists(executor, null, pdist, parentCidOut,
				DistributedQuery.ResultAssetType.primary, includeChildren, true, null)) {
			throw new Exception("The destination parent CID does not have an asset associated with it");
		}

		//
		dm = new XmlDocMaker("args");
		dm.add("id", parentCidOut);
		r = executor.execute("om.pssd.object.type", dm.root());
		String typeParentOut = r.value("type");
		if (!(typeParentOut.equals(Project.TYPE) || typeParentOut.equals(Subject.TYPE)
				|| typeParentOut.equals(ExMethod.TYPE) || typeParentOut.equals(Study.TYPE) || typeParentOut
					.equals(DataSet.TYPE))) {
			throw new Exception(
					"The destination parent object must be one of Project, Subject, ExMethod, Study or DataSet.");
		}

		// Validate relative depth of input and output
		if (depthOut + 1 != depthIn) {
			throw new Exception("The destination object CID must have depth one greater than input object");
		}

		// Generate new output CID for input object. If desired,
		String objectCidOut = null;

		if (preserve) {
			// try to preserve the child number of the object we are moving
			// E.g. if 1.5.10.2 is the first child, move it to to 1.5.20.2 not
			// 1.5.20.1
			String t = nig.mf.pssd.CiteableIdUtil.getLastSection(objectCidIn);
			long startId = Integer.parseInt(t);
			objectCidOut = nig.mf.pssd.plugin.util.CiteableIdUtil.getFirstAvailableCid(executor, new DistributedAsset(
					proute, parentCidOut), startId);
		} else {

			// Just get the next CID under the parent
			objectCidOut = nig.mf.pssd.plugin.util.CiteableIdUtil.generateCiteableID(executor, parentCidOut);
		}

		// Parent project CIDs
		String projectCidIn = nig.mf.pssd.plugin.util.CiteableIdUtil.getProjectCID(objectCidIn);
		String projectCidOut = nig.mf.pssd.plugin.util.CiteableIdUtil.getProjectCID(parentCidOut);
		//
		boolean sameProject = (projectCidIn.equals(projectCidOut));

		// Find any children of the input object before we change its CID
		// which would cause the collection.members service to find nothing
		dm = new XmlDocMaker("args");
		dm.add("id", objectCidIn);
		dm.add("size", "infinity");
		XmlDoc.Element r2 = executor.execute("om.pssd.collection.members", dm.root());
		Collection<String> descendants = r2.values("object/id");

		// Now set the CID on the object from the old to the new tree
		String objectAssetIdIn = nig.mf.pssd.plugin.util.CiteableIdUtil.cidToId(executor, objectCidIn);
		dm = new XmlDocMaker("args");
		dm.add("id", objectAssetIdIn);
		dm.add("cid", objectCidOut);
		executor.execute("asset.cid.set", dm.root());

		// Fix up ACLs if tree has different Project root
		// Because we only move objects from the Subject and down, their
		// are no role issues to deal with. The destination Project
		// defines the roles and users which have those roles.
		if (!sameProject) {
			// Revoke all old ACLs on the object
			PSSDUtils.revokeAllACLs(executor, objectAssetIdIn, false);

			// Grant new ACLs on the object based on its new CID
			PSSDUtils.grantProjectACLsToAsset(executor, objectCidOut);
		}

		// Some objects have meta-data on them that reflect their parent and
		// must be
		// updated to reflect that.
		if (objectType.equals(Study.TYPE)) {

			// The Study holds both the ExMethod CID and the Step in the Method
			// that was used to create it. We can't know which step in the
			// new ExMethod is the right one; the caller must fix the Step if
			// they want to. However, we do at least know
			// the ExMethod and we can update that and leave the Step the same.
			// This assumes that the new ExMethod is the correct ExMethod to
			// put in the Study (in principle a Study may be the child of an
			// ExMethod that did not create it).
			Study.update(executor, objectCidOut, null, null, null, parentCidOut, null, null);

			// Studies also hold template meta-data that reflect the parent
			// ExMethod (in the namespace)
			// and we can't edit that either. Until we have control over the
			// template meta-data, we can't do anything here. We leave it to
			// the calling service (e.g. SvcStudyMOve) to throw an exception
			// under these conditions
			// as we don't want an exception in the middle of a recursive move

		} else if (objectType.equals(DataSet.TYPE)) {

			// Get Parent Study ExMethod/Step meta-data
			dm = new XmlDocMaker("args");
			dm.add("id", parentCidOut);
			XmlDoc.Element studyMeta = executor.execute("om.pssd.object.describe", dm.root());
			XmlDoc.Element studyMethod = studyMeta.element("object/method");

			// Update the DataSet's ExMethod and Step meta-data according to
			// that
			// of the new parent Study. We don't currently write any template
			// information
			// to DataSets so this is a safe thing to do.
			if (studyMethod != null) {
				DataSet.updateExMethodMeta(executor, objectCidOut, studyMethod.value("id"), studyMethod.value("step"));
			}
		}

		// Iterate through any children objects
		if (descendants != null) {
			for (String cidIn : descendants) {
				// Get asset ID of child and recursively move it
				move(executor, cidIn, objectCidOut, preserve);
			}
		}

		return objectCidOut;

	}

	public static boolean isLeaf(ServiceExecutor executor, String id) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		String where = "cid in '" + id + "'";
		dm.add("where", where);
		dm.add("size", "1");

		XmlDoc.Element r = Asset.query(executor, dm.root());
		if (r.count("id") == 0) {
			return true;
		} else {
			return false;
		}
	}

	public static String type(ServiceExecutor executor, String id) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		String where = "cid='" + id + "'";
		dm.add("where", where);
		dm.add("size", "1");
		dm.add("action", "get-value");
		dm.add("xpath", new String[] { "ename", "type" }, "meta/pssd-object/type");
		XmlDoc.Element r = Asset.query(executor, dm.root());
		return r.value("asset/type");
	}

	public static PSSDObject get(ServiceExecutor executor, ServerRoute sroute, String id) throws Throwable {

		XmlDoc.Element ae = Asset.getByCid(executor, sroute, id);
		if (ae == null) {
			return null;
		}
		return new PSSDObject(ae);
	}

	public static PSSDObject find(ServiceExecutor executor, String proute, String id) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("action", "get-meta");
		dm.add("size", "infinity");
		dm.add("cid", id);
		XmlDoc.Element r = query(executor, dm.root());
		XmlDoc.Element ae = null;
		if (!isRemote(proute)) {
			ae = r.element("asset");
		} else {
			ae = r.element("asset[@proute=" + proute + "]");
		}
		if (ae == null) {
			return null;
		}
		return new PSSDObject(ae);
	}

	public static boolean exists(ServiceExecutor executor, String proute, String id) throws Throwable {

		return existsByCid(executor, proute == null ? null : new ServerRoute(proute), id);
	}

	public static int countDicomDataSets(ServiceExecutor executor, String proute, String id) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("action", "count");
		if (id != null) {
			dm.add("where", "(( cid starts with '" + id + "') or ( cid = '" + id
					+ "')) and ( model = 'om.pssd.dataset' ) and ( mf-dicom-series has value )");
		} else {
			dm.add("where", "( model = 'om.pssd.dataset' ) and ( mf-dicom-series has value )");
		}
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		return r.intValue("value", 0);
	}

	public static boolean hasSessionLock(ServiceExecutor executor, ServerRoute sroute, String id) throws Throwable {

		XmlDoc.Element r = Asset.getByCid(executor, sroute, id);
		return r.element("lock[@type='transient']") != null;
	}

	public static boolean hasLock(ServiceExecutor executor, ServerRoute sroute, String id) throws Throwable {

		XmlDoc.Element r = Asset.getByCid(executor, sroute, id);
		return r.element("lock[@type='persistent']") != null;
	}

	public static int countDataSets(ServiceExecutor executor, String proute, String id) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("action", "count");
		if (id != null) {
			dm.add("where", "(( cid starts with '" + id + "') or ( cid = '" + id
					+ "')) and ( model = 'om.pssd.dataset' ) and ( asset has content )");
		} else {
			dm.add("where", "( model = 'om.pssd.dataset' ) and ( asset has content )");
		}
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		return r.intValue("value", 0);
	}

}
