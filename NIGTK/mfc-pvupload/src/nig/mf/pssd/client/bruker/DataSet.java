package nig.mf.pssd.client.bruker;

import arc.mf.client.ServerClient;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;

public abstract class DataSet extends BaseObject {

	public static final String OBJECT_TYPE = "dataset";
	
	private String _seriesUID;
	private String _seriesID;

	protected DataSet(String id, String name, String description, String seriesUID, String seriesID) {
		super(id, name, description);
		_seriesUID = seriesUID;
		_seriesID = seriesID;
	}

	public String seriesUID() {
		return _seriesUID;
	}

	public String seriesID() {
		return _seriesID;
	}

	/**
	 * Check if the two specified data sets are primary-derivation pair.
	 * 
	 * @param cxn
	 * @param primaryId
	 * @param derivationId
	 * @return
	 * @throws Throwable
	 */
	public static boolean isPrimaryDerivationPair(ServerClient.Connection cxn, String primaryId, String derivationId)
			throws Throwable {

		boolean isPrimaryDerivationPair = false;
		XmlStringWriter w = new XmlStringWriter();
		w.add("where", "cid = '" + derivationId + "' and xpath(pssd-derivation/input) = '" + primaryId + "'");
		XmlDoc.Element r = cxn.execute("asset.query", w.document());
		if (r.elements("id") != null) {
			isPrimaryDerivationPair = true;
		}
		return isPrimaryDerivationPair;

	}

}
