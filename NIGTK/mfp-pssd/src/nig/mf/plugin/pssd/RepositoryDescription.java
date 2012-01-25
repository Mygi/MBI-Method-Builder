package nig.mf.plugin.pssd;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import nig.mf.plugin.util.DateUtil;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

/**
 * Class to specify some details about this local PSSD repository. For example,
 * the custodian, the rights and a generic description of the held data.
 * 
 * @author nebk
 * 
 */
public class RepositoryDescription {

	private static final String DOC_TYPE = "pssd-repository-description";
	private static final String ASSET_NAME = "pssd-repository-description";
	private static final String ASSET_NAMESPACE = "/pssd";

	/**
	 * Find the local repository description asset (singleton).
	 * 
	 * @param executor
	 * @return - Asset ID of asset. Will be null if does not exist
	 * @throws Throwable
	 */
	public static String findRepositoryDescription(ServiceExecutor executor)
			throws Throwable {

		// We only query on the asset name as if all roles have been removed, no
		// Document will
		// be attached to the singleton asset.
		// String query = "(" + DOCTYPE + " has value and name='" + ASSETNAME +
		// "')";

		XmlDocMaker dm = new XmlDocMaker("args");
		String query = "name='" + ASSET_NAME + "'";
		dm.add("where", query);
		dm.add("pdist", 0); // Force to local query
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		//
		Collection<XmlDoc.Element> ids = r.elements("id");
		String id = null;
		if (ids == null || ids.size() == 0) {
			// Does not exist
		} else if (ids.size() > 1) {
			// Trouble
			throw new Exception(
					"There are multiple repository description objects. Contact the administrator");
		} else {
			id = r.value("id");
		}
		return id;
	}

	/**
	 * Find the repository description asset (singleton); create if does not
	 * exist
	 * 
	 * @param executor
	 * @return - Asset ID of object. Will be null if not created/exists
	 * @throws Throwable
	 */
	public static String findAndCreateRepositoryDescription(
			ServiceExecutor executor) throws Throwable {

		// Find if exists
		String id = findRepositoryDescription(executor);

		// Create if needed
		if (id == null) {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm = new XmlDocMaker("args");
			dm.add("namespace", ASSET_NAMESPACE);
			dm.add("name", ASSET_NAME);

			// Put a basic ACL on the objects. The system:manager needs to be
			// able to
			// manipulate it. End users should have the ability to see it.
			dm.push("acl");
			dm.add("actor", new String[] { "type", "role" },
					"pssd.object.admin");
			dm.add("access", "read-write");
			dm.pop();
			//
			dm.push("acl");
			dm.add("actor", new String[] { "type", "role" }, "pssd.model.user");
			dm.add("access", "read");
			dm.pop();

			// Do it
			XmlDoc.Element r2 = executor.execute("asset.create", dm.root());
			id = r2.value("id");
		}

		// Return id
		return id;
	}

	/**
	 * Find the repository description asset (singleton); create if does not
	 * exist
	 * 
	 * @param executor
	 * @return - Asset ID of registry object. Will be null if not created/exists
	 * @throws Throwable
	 */
	public static void destroyRepositoryDescription(ServiceExecutor executor)
			throws Throwable {

		// Find if exists
		String id = findRepositoryDescription(executor);

		// Destroy
		if (id != null) {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm = new XmlDocMaker("args");
			dm.add("id", id);
			executor.execute("asset.destroy", dm.root());
		}
	}

	/**
	 * Replace the full record with a new one.
	 * 
	 * @param id
	 *            - The asset ID of the repository description asset
	 * @param the
	 *            pssd-repository-description record to add
	 * 
	 * @throws Throwable
	 */
	public static void replaceRecord(ServiceExecutor executor, String id,
			XmlDoc.Element name, XmlDoc.Element custodian,
			XmlDoc.Element location, XmlDoc.Element rights,
			XmlDoc.Element holdings) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		dm.push("meta", new String[] { "action", "replace" });
		dm.push(DOC_TYPE);
		if (name != null)
			dm.add(name);
		if (custodian != null)
			dm.add(custodian);
		if (location != null)
			dm.add(location);
		if (rights != null)
			dm.add(rights);
		if (holdings != null)
			dm.add(holdings);
		//
		dm.pop();
		dm.pop();

		// If the user does not set any elements, we want to overwrite with
		// null.
		executor.execute("asset.set", dm.root());
	}

	/**
	 * 
	 * @param executor
	 * @param useANDSDateFormat  USe YYYY-MN-DD for the date format instead of standard MF DD-MMM-YYYY
	 * @return
	 * @throws Throwable
	 */
	public static XmlDoc.Element getRepositoryDescription(ServiceExecutor executor, Boolean useANDSDateFormat) throws Throwable {

		// Find if exists
		String id = findRepositoryDescription(executor);
		if (id == null) {
			return null;
		}
		XmlDocMaker dm = new XmlDocMaker("args");
		dm = new XmlDocMaker("args");
		dm.add("id", id);
		XmlDoc.Element r = executor.execute("asset.get", dm.root());
		
		// 
		if (useANDSDateFormat && r!=null) {
			XmlDoc.Element date = r.element("asset/meta/pssd-repository-description/data-holdings/start-date");
			if (date!=null) {
				formatDate (date);
			}
		}
		return r.element("asset/meta/pssd-repository-description");
	}
	
	
	/**
	 * Convert date from dd-MM-yyyy to yyyy-MM-dd
	 * 
	 * @param date
	 * @throws Throwable
	 */
	private static void formatDate (XmlDoc.Element date) throws Throwable {
		String in = date.value();
		String out = DateUtil.convertDateString(in, "dd-MMM-yyyy", "yyyy-MM-dd");
		date.setValue(out);
	}

}
