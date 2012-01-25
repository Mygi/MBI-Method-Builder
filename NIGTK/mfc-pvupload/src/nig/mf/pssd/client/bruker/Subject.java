package nig.mf.pssd.client.bruker;

import java.util.Collection;
import java.util.Vector;

import arc.mf.client.ServerClient;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;

public class Subject extends BaseObject {

	public static final String OBJECT_TYPE = "subject";

	protected Subject(String id, String name, String description) {
		super(id, name, description);
	}

	/**
	 * Check if the subject exists.
	 * 
	 * @param cxn
	 * @param id
	 *            the citeable id of the subject.
	 * @return
	 * @throws Throwable
	 */
	public static boolean exists(ServerClient.Connection cxn, String id) throws Throwable {

		return exists(cxn, id, Subject.OBJECT_TYPE);

	}

	/**
	 * Returns the ex-method in the subject
	 * 
	 * @param cxn
	 * @param id
	 *            the citeable id of the subject
	 * @return
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	public static Vector<ExMethod> getExMethods(ServerClient.Connection cxn, String id) throws Throwable {

		// Find the ex-method of the subject
		XmlStringWriter w = new XmlStringWriter();
		w.add("where", "model = 'om.pssd.ex-method' and cid in '" + id + "'");
		w.add("action", "get-meta");
		XmlDoc.Element r = cxn.execute("asset.query", w.document());
		Collection<XmlDoc.Element> assets = r.elements("asset");
		if (assets == null) {
			return null;
		}
		Vector<ExMethod> ems = new Vector<ExMethod>();
		for (XmlDoc.Element asset : assets) {
			String emId = asset.value("cid");
			String emName = asset.value("meta/pssd-object/name");
			String emDescription = asset.value("meta/pssd-object/description");
			ems.add(new ExMethod(emId, emName, emDescription));
		}
		return ems;

	}

}
