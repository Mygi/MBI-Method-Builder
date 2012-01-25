package nig.mf.pssd.plugin.util;

import nig.mf.plugin.util.AssetUtil;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

public class PSSDUtil {

	public static final String PROJECT_MODEL = "om.pssd.project";
	public static final String SUBJECT_MODEL = "om.pssd.subject";
	public static final String EX_METHOD_MODEL = "om.pssd.ex-method";
	public static final String STUDY_MODEL = "om.pssd.study";
	public static final String DATASET_MODEL = "om.pssd.dataset";
	public static final String DATA_OBJECT_MODEL = "om.pssd.dataobject";
	public static final String R_SUBJECT_MODEL = "om.pssd.r-subject";
	public static final String METHOD_MODEL = "om.pssd.method";

	public static boolean isValidProject(ServiceExecutor executor, String cid, boolean throwIt) throws Throwable {

		String model = AssetUtil.getModel(executor, cid, true);
		if (model == null) {
			throw new Exception("No asset/model found. Asset(cid=" + cid + ") is not a valid PSSD object.");
		}
		if (!model.equals(PROJECT_MODEL)) {
			if (throwIt) {
				throw new Exception("Wrong asset/model. Asset(cid=" + cid + ", model=" + model
						+ ") is not a valid PSSD project asset.");
			} else {
				return false;
			}
		} else {
			return true;
		}

	}

	public static boolean isValidSubject(ServiceExecutor executor, String cid, boolean throwIt) throws Throwable {

		String model = AssetUtil.getModel(executor, cid, true);
		if (model == null) {
			throw new Exception("No asset/model found. Asset(cid=" + cid + ") is not a valid PSSD object.");
		}
		if (!model.equals(SUBJECT_MODEL)) {
			if (throwIt) {
				throw new Exception("Wrong asset/model. Asset(cid=" + cid + ", model=" + model
						+ ") is not a valid PSSD subject asset.");
			} else {
				return false;
			}
		} else {
			return true;
		}

	}

	public static boolean isValidExMethod(ServiceExecutor executor, String cid, boolean throwIt) throws Throwable {

		String model = AssetUtil.getModel(executor, cid, true);
		if (model == null) {
			throw new Exception("No asset/model found. Asset(cid=" + cid + ") is not a valid PSSD object.");
		}
		if (!model.equals(EX_METHOD_MODEL)) {
			if (throwIt) {
				throw new Exception("Wrong asset/model. Asset(cid=" + cid + ", model=" + model
						+ ") is not a valid PSSD ex-method asset.");
			} else {
				return false;
			}
		} else {
			return true;
		}

	}

	public static boolean isValidStudy(ServiceExecutor executor, String cid, boolean throwIt) throws Throwable {

		String model = AssetUtil.getModel(executor, cid, true);
		if (model == null) {
			throw new Exception("No asset/model found. Asset(cid=" + cid + ") is not a valid PSSD object.");
		}
		if (!model.equals(STUDY_MODEL)) {
			if (throwIt) {
				throw new Exception("Wrong asset/model. Asset(cid=" + cid + ", model=" + model
						+ ") is not a valid PSSD study asset.");
			} else {
				return false;
			}
		} else {
			return true;
		}

	}

	public static boolean isValidDataSet(ServiceExecutor executor, String cid, boolean throwIt) throws Throwable {

		String model = AssetUtil.getModel(executor, cid, true);
		if (model == null) {
			throw new Exception("No asset/model found. Asset(cid=" + cid + ") is not a valid PSSD object.");
		}
		if (!model.equals(DATASET_MODEL)) {
			if (throwIt) {
				throw new Exception("Wrong asset/model. Asset(cid=" + cid + ", model=" + model
						+ ") is not a valid PSSD dataset asset.");
			} else {
				return false;
			}
		} else {
			return true;
		}

	}

	public static boolean isValidDataObject(ServiceExecutor executor, String cid) throws Throwable {

		String model = AssetUtil.getModel(executor, cid, true);
		if (model == null) {
			throw new Exception("No asset/model found. Asset(cid=" + cid + ") is not a valid PSSD object.");
		}
		if (!model.equals(DATA_OBJECT_MODEL)) {
			throw new Exception("Wrong asset/model. Asset(cid=" + cid + ", model=" + model
					+ ") is not a valid PSSD data-object asset.");
		} else {
			return true;
		}

	}

	public static boolean isValidMethod(ServiceExecutor executor, String cid) throws Throwable {

		String model = AssetUtil.getModel(executor, cid, true);
		if (model == null) {
			throw new Exception("No asset/model found. Asset(cid=" + cid + ") is not a valid PSSD object.");
		}
		if (!model.equals(METHOD_MODEL)) {
			throw new Exception("Wrong asset/model. Asset(cid=" + cid + ", model=" + model
					+ ") is not a valid PSSD method asset.");
		} else {
			return true;
		}

	}

	public static boolean isValidRSubject(ServiceExecutor executor, String cid) throws Throwable {

		String model = AssetUtil.getModel(executor, cid, true);
		if (model == null) {
			throw new Exception("No asset/model found. Asset(cid=" + cid + ") is not a valid PSSD object.");
		}
		if (!model.equals(R_SUBJECT_MODEL)) {
			throw new Exception("Wrong asset/model. Asset(cid=" + cid + ", model=" + model
					+ ") is not a valid PSSD r-subject asset.");
		} else {
			return true;
		}

	}
	
	/**
	 * Is the asset associated with this CID a replica ?
	 * 
	 * @param executor
	 * @param cid
	 * @return
	 * @throws Throwable
	 */	
	public static Boolean isReplica (ServiceExecutor executor, String cid) throws Throwable {
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", cid);
		XmlDoc.Element r = executor.execute("om.pssd.object.is.replica", doc.root());
		return r.booleanValue("replica");
	}

	/**
	 * Does this CID have children (primary or replica) on remote peers.
	 * 
	 * @param executor
	 * @param cid
	 * @return
	 * @throws Throwable
	 */
	public static Boolean hasRemoteChildren (ServiceExecutor executor, String cid) throws Throwable {
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", cid);
		doc.add("asset-type", "all");
		XmlDoc.Element r = executor.execute("om.pssd.object.has.remote.children", doc.root());
		return r.booleanValue("remote-children");
	}

	
	/**
	 * Check the given Document Type exists. Exception if not.
	 * 
	 * @param role - the role
	 * @param throwIt - if true throw an exception if it does not exist
	 * @return - true if exists, false if does not
	 * @throws Throwable
	 */
	public static boolean checkDocTypeExists(ServiceExecutor executor, String docType) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("type", docType);
			
		XmlDoc.Element r = executor.execute("asset.doc.type.exists", dm.root());
		return r.booleanValue("exists");
	}
	
	/**
	 * Returns the object type for the given object.
	 * 
	 * @param executor
	 * @param dCID DIstributed CID
	 * @return  Returns null if the asset (primary or replica) does not exist.
	 * @throws Throwable
	 */
	public static String typeOf(ServiceExecutor executor, DistributedAsset dCID)
			throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", dCID.getCiteableID());
		
		ServerRoute route = dCID.getServerRouteObject();
		XmlDoc.Element r = executor.execute(route, "asset.exists", dm.root());
		if (!r.booleanValue("exists")) return null;

		dm.add("pdist",0);                 // Force local on whatever server it's executed		
		r = executor.execute(route, "asset.get", dm.root());
		return r.stringValue("asset/meta/pssd-object/type", "unknown");
	}

	/**
	 * Returns the object type for the given local object.
	 * 
	 * @param executor
	 * @param cid  Distributed citeable ID
	 * @return  Returns null if the asset does not exist.
	 * @throws Throwable
	 */

	public static String typeOf(ServiceExecutor executor, String cid) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", cid);
		
		XmlDoc.Element r = executor.execute("asset.exists", dm.root());
		if (!r.booleanValue("exists")) return null;

		dm.add("pdist",0);                 // Force local 		
		r = executor.execute("asset.get", dm.root());
		return r.stringValue("asset/meta/pssd-object/type", "unknown");
	}


	
	
}
