package nig.mf.pssd.client.bruker;

import arc.mf.client.ServerClient;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;

public class ExMethod extends BaseObject {

	public static final String OBJECT_TYPE = "ex-method";

	protected ExMethod(String id, String name, String description) {
		super(id, name, description);
	}

	/**
	 * Find the ex-method of the specified subject.
	 * 
	 * Note: it is possible that a subject has multiple ex-methods. But
	 * currently all the data from AMRIF Bruker scanner has only one ex-method.
	 * Therefore only one ex-method is returned.
	 * 
	 * @param cxn
	 * @param pid
	 *            the citeable id of the parent subject
	 * @return
	 * @throws Throwable
	 */
	public static ExMethod find(ServerClient.Connection cxn, String pid) throws Throwable {

		// Find the ex-method of the specified subject
		XmlStringWriter w = new XmlStringWriter();
		w.add("where", "model = 'om.pssd.ex-method' and cid in '" + pid + "'");
		w.add("action", "get-meta");
		XmlDoc.Element r = cxn.execute("asset.query", w.document());
		if (r.elements("asset") == null) {
			return null;
		}
		if (r.elements("asset").size() == 0) {
			return null;
		}
		// Note: it is possible that a subject has multiple ex-methods. But
		// currently all the data from AMRIF Bruker scanner has only one
		// ex-method. Therefore only one ex-method is returned.
		if (r.elements("asset").size() != 1) {
			String errMsg = "More than one ex-methods found for PSSD subject(id=" + pid + ").";
			PSSDUtil.logError(cxn, errMsg);
			throw new Exception(errMsg);
		}

		String id = r.value("asset/cid");
		String name = r.value("asset/meta/pssd-object/name");
		String description = r.value("asset/meta/pssd-object/description");
		return new ExMethod(id, name, description);

	}

	/**
	 * Check if the ex-method exists.
	 * 
	 * @param cxn
	 * @param id
	 *            the citeable id of the ex-method.
	 * @return
	 * @throws Throwable
	 */
	public static boolean exists(ServerClient.Connection cxn, String id) throws Throwable {

		return exists(cxn, id, ExMethod.OBJECT_TYPE);

	}

}
