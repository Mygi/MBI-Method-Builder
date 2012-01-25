package nig.mf.pssd.client.bruker;

import java.util.Collection;

import nig.mf.MimeTypes;

import arc.mf.client.ServerClient;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;

public class DerivationDataSet extends DataSet {

	boolean _hasPrimary = false;

	protected DerivationDataSet(String id, String name, String description, String seriesUID, String seriesID,
			boolean hasPrimary) {
		super(id, name, description, seriesUID, seriesID);
		_hasPrimary = hasPrimary;
	}

	public boolean hasPrimary() {
		return _hasPrimary;
	}

	/**
	 * Find a DICOM derivation data set.
	 * 
	 * @param cxn
	 * @param pid
	 *            the citeable id of the parent (study) asset
	 * @param seriesID
	 * @return
	 * @throws Throwable
	 */
	public static DerivationDataSet find(ServerClient.Connection cxn, String pid, String seriesUID, String seriesID)
			throws Throwable {
		if (!((pid != null && seriesID != null) || seriesUID != null)) {
			String errMsg = "Insufficient conditions provided when querying for DICOM Derivation DataSet. ";
			PSSDUtil.logError(cxn, errMsg);
			throw new Exception(errMsg);
		}
		String where = "model = 'om.pssd.dataset'";
		if (seriesUID != null) {
			where += " and xpath(mf-dicom-series/uid) = '" + seriesUID + "'";
		}
		if (pid != null && seriesID != null) {
			where += " and ( cid starts with '" + pid + "' and xpath(mf-dicom-series/id) = '" + seriesID + "' )";
		}
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
			String errMsg = "Found multiple PSSD derivation datasets (in one study) with the same series id. "
					+ "They could be duplicate derivation datasets."
					+ " Please ask administrator to resolve this problem.";
			PSSDUtil.logError(cxn, errMsg);
			throw new Exception(errMsg);
		}
		String id = r.value("asset/cid");
		String name = r.value("asset/meta/pssd-object/name");
		String description = r.value("asset/meta/pssd-object/description");
		if (seriesUID == null) {
			seriesUID = r.value("asset/meta/mf-dicom-series/uid");
		}
		if (seriesID == null) {
			seriesID = r.value("asset/meta/mf-dicom-series/id");
		}
		// Check if it has primary data set (bruker series)
		boolean hasPrimary = false;
		if (r.elements("asset/meta/pssd-derivation/input") != null) {
			hasPrimary = true;
		}

		// Check if it has primary data set which is a bruker series.
		// Collection inputs = r.values("asset/meta/pssd-derivation/input");
		// for (Iterator it = inputs.iterator(); it.hasNext();) {
		// String input = (String) it.next();
		// w = new XmlStringWriter();
		// w.add("cid", input);
		// XmlDoc.Element r3 = cxn.execute("asset.get", w.document());
		// String type = r3.value("asset/type");
		// if (type != null) {
		// if (type.equalsIgnoreCase(MimeTypes.BRUKER_SERIES)) {
		// hasPrimary = true;
		// break;
		// }
		// }
		// }

		return new DerivationDataSet(id, name, description, seriesUID, seriesID, hasPrimary);

	}

	/**
	 * Set the primary data set of a derivation data set.
	 * 
	 * @param cxn
	 * @param derivationId
	 * @param primaryId
	 * @throws Throwable
	 */
	public static void setPrimary(ServerClient.Connection cxn, String derivationId, String primaryId) throws Throwable {

		XmlStringWriter w = new XmlStringWriter();
		w.add("cid", primaryId);
		XmlDoc.Element r1 = cxn.execute("asset.get", w.document());
		String primaryVID = r1.value("asset/@vid");

		w = new XmlStringWriter();
		w.add("id", derivationId);
		w.add("input", new String[] { "vid", primaryVID }, primaryId);
		cxn.execute("om.pssd.dataset.derivation.update", w.document());

	}

	/**
	 * 
	 * Check if the specified derivation data set has (bruker) primary data set.
	 * 
	 * @param cxn
	 * @param derivationId
	 * @return
	 * @throws Throwable
	 */
	public static boolean hasBrukerPrimary(ServerClient.Connection cxn, String derivationId) throws Throwable {

		XmlStringWriter w = new XmlStringWriter();
		w.add("cid", derivationId);
		XmlDoc.Element r1 = cxn.execute("asset.get", w.document());

		Collection<String> inputs = r1.values("asset/meta/pssd-derivation/input");
		if (inputs == null) {
			return false;
		}
		boolean hasBrukerPrimary = false;
		for (String input : inputs) {
			w = new XmlStringWriter();
			w.add("cid", input);
			XmlDoc.Element r2 = cxn.execute("asset.get", w.document());
			String type = r2.value("asset/type");
			if (type != null) {
				if (type.equalsIgnoreCase(MimeTypes.BRUKER_SERIES)) {
					hasBrukerPrimary = true;
					break;
				}
			}
		}
		return hasBrukerPrimary;

	}

}
