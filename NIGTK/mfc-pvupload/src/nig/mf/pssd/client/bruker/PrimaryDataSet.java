package nig.mf.pssd.client.bruker;

import java.io.File;
import java.util.Collection;

import nig.mf.MimeTypes;
import arc.mf.client.ServerClient;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;

public class PrimaryDataSet extends DataSet {

	private boolean _hasDerivation = false;
	private String _protocol = null;
	private String _acqTime = null;

	protected PrimaryDataSet(String id, String name, String description, String seriesUID, String seriesID,
			String protocol, String acqTime, boolean hasDerivation) {
		super(id, name, description, seriesUID, seriesID);
		_hasDerivation = hasDerivation;
		_protocol = protocol;
		_acqTime = acqTime;
	}

	/**
	 * Check if the primary data set has an derivation data set.
	 * 
	 * @return
	 */
	public boolean hasDerivation() {
		return _hasDerivation;
	}

	/**
	 * Returns the acquisition time.
	 * 
	 * @return
	 */
	public String acqTime() {
		return _acqTime;
	}

	/**
	 * Returns the acquisition protocols.
	 * 
	 * @return
	 */
	public String protocol() {
		return _protocol;
	}

	/**
	 * Create a Bruker primary data set.
	 * 
	 * @param cxn
	 * @param isImage true for Image series false for fid file
	 * @param pid
	 * @param name
	 * @param description
	 * @param seriesUID
	 * @param seriesID
	 * @param f
	 * @return
	 * @throws Throwable
	 */
	public static PrimaryDataSet create(ServerClient.Connection cxn, boolean isImage, String pid, String name, String description,
			String seriesUID, String seriesID, String protocol, String acqTime, File f) throws Throwable {

		ServerClient.Input in = ServerClient.createInputFromURL("file:" + f.getAbsolutePath());
		String id;
		try {
			// Clear the source -- set it to something more useful.
			in.setSource("ParaVisionUpload (by " + System.getProperty("user.name") + ")");
			XmlStringWriter w = new XmlStringWriter();
			w.add("pid", pid);
			w.add("name", name);
			w.add("description", description);
			if (isImage) {
				w.add("type", MimeTypes.BRUKER_SERIES);
			} else {
				w.add("type", MimeTypes.BRUKER_FID);
			}
			w.add("fillin", "true");         // Fill in DataSet child IDs
			w.push("meta");
			w.push("hfi-bruker-series", new String[] { "ns", PSSDUtil.BRUKER_META_NAMESPACE });
			if (seriesUID != null) {
				w.add("uid", seriesUID);
			}
			if (seriesID != null) {
				w.add("id", seriesID);
			}
			if (protocol != null) {
				w.add("protocol", protocol);
			}
			if (acqTime != null) {
				w.add("acqTime", acqTime);
			}
			w.pop();
			w.pop();
			XmlDoc.Element r = cxn.execute("om.pssd.dataset.primary.create", w.document(), in, null);
			id = r.value("id");
		} finally {
			in.close();
		}
		if (id == null) {
			String errMsg = "failed to create bruker PSSD primary dataset. (study=" + pid + ", seriesUID=" + seriesUID
					+ ", seriesID=" + seriesID + ").";
			PSSDUtil.logError(cxn, errMsg);
			throw new Exception(errMsg);
		}
		return new PrimaryDataSet(id, name, description, seriesUID, seriesID, protocol, acqTime, false);

	}

	/**
	 * Update an Bruker primary data set.
	 * 
	 * @param cxn
	 * @param id
	 * @param name
	 * @param description
	 * @param seriesUID
	 * @param seriesID
	 * @param f
	 * @return
	 * @throws Throwable
	 */
	public static PrimaryDataSet update(ServerClient.Connection cxn, String id, String name, String description,
			String seriesUID, String seriesID, String protocol, String acqTime, File f) throws Throwable {

		ServerClient.Input in = ServerClient.createInputFromURL("file:" + f.getAbsolutePath());
		try {
			// Clear the source -- set it to something more useful.
			in.setSource("ParaVisionLoad (by " + System.getProperty("user.name") + ")");

			XmlStringWriter w = new XmlStringWriter();
			w.add("id", id);
			if (name != null) {
				w.add("name", name);
			}
			if (description != null) {
				w.add("description", description);
			}
			w.add("type", MimeTypes.BRUKER_SERIES);

			w.push("meta");
			w.push("hfi-bruker-series", new String[] { "ns", PSSDUtil.BRUKER_META_NAMESPACE });
			if (seriesID != null) {
				w.add("id", seriesID);
			}
			if (seriesUID != null) {
				w.add("uid", seriesUID);
			}
			if (protocol != null) {
				w.add("protocol", protocol);
			}
			if (acqTime != null) {
				w.add("acqTime", acqTime);
			}
			w.pop();
			w.pop();

			cxn.execute("om.pssd.dataset.primary.update", w.document(), in, null);

		} finally {
			in.close();
		}
		return new PrimaryDataSet(id, name, description, seriesUID, seriesID, protocol, acqTime, false);

	}

	/**
	 * Find the Bruker primary data set by series UID and seriesID
	 * 
	 * @param cxn
	 * @param studyCID
	 * @param seriesUID
	 * @return
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	public static PrimaryDataSet find(ServerClient.Connection cxn, String studyCID, String seriesUID, String seriesID)
			throws Throwable {

		if (!((studyCID != null && seriesID != null) || seriesUID != null)) {
			String errMsg = "Insufficient conditions provided when querying for DICOM Derivation DataSet. ";
			PSSDUtil.logError(cxn, errMsg);
			throw new Exception(errMsg);
		}
		String where = "model = 'om.pssd.dataset'";
		if (seriesUID != null) {
			where += " and xpath(hfi-bruker-series/uid) = '" + seriesUID + "'";
		}
		
		// Restrict the query to the desired CID tree
		if (studyCID != null && seriesID != null) {
			where += " and ( cid starts with '" + studyCID + "' and xpath(hfi-bruker-series/id) = '" + seriesID + "' )";
		}

		// Query to find the bruker primary data set.
		XmlStringWriter w = new XmlStringWriter();
		w.add("where", where);
		w.add("action", "get-meta");
		XmlDoc.Element r = cxn.execute("asset.query", w.document());
		Collection<XmlDoc.Element> assets = r.elements("asset");
		if (assets == null) {
			return null;
		}
		if (assets.size() == 0) {
			return null;
		}
		if (assets.size() != 1) {
			String errMsg = "Found multiple bruker PSSD series/datasets with the same series UID. "
					+ "They could be duplicate series." + " Please ask administrator to resolve this problem.";
			PSSDUtil.logError(cxn, errMsg);
			throw new Exception(errMsg);
		}
		String id = r.value("asset/cid");
		String name = r.value("asset/meta/pssd-object/name");
		String description = r.value("asset/meta/pssd-object/description");
		String protocol = r.value("asset/meta/hfi-bruker-series/protocol");
		String acqTime = r.value("asset/meta/hfi-bruker-series/acqTime");

		// Query to check if the found primary dataset has derivation
		w = new XmlStringWriter();
		w.add("where", "model = 'om.pssd.dataset' and xpath(pssd-derivation/input) = '" + id + "'");
		r = cxn.execute("asset.query", w.document());
		boolean hasDerivation = false;
		if (r.elements("id") != null) {
			hasDerivation = true;
		}
		return new PrimaryDataSet(id, name, description, seriesUID, seriesID, protocol, acqTime, hasDerivation);

	}

	/**
	 * Check if the specified primary data set has a Dicom derivation data set.
	 * 
	 * @param cxn
	 * @param primaryId
	 * @return
	 * @throws Throwable
	 */
	public static boolean hasDicomDerivation(ServerClient.Connection cxn, String primaryId) throws Throwable {

		XmlStringWriter w = new XmlStringWriter();
		w.add("where", "type='" + MimeTypes.DICOM_SERIES + "'" + " and model = 'om.pssd.dataset' "
				+ " and xpath(pssd-derivation/input) = '" + primaryId + "'");
		XmlDoc.Element r = cxn.execute("asset.query", w.document());
		if (r.value("id") != null) {
			return true;
		} else {
			return false;
		}

	}

}
