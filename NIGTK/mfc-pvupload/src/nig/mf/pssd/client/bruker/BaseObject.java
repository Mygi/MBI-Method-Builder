package nig.mf.pssd.client.bruker;

import arc.mf.client.ServerClient;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;

public abstract class BaseObject {

	String _id;        // Citable ID
	String _name;
	String _description;

	protected BaseObject(String id, String name, String description) {
		_id = id;
		_name = name;
		_description = description;
	}

	public String id() {
		return _id;
	}

	public String name() {
		return _name;
	}

	public String description() {
		return _description;
	}

	/**
	 * Check if the object exists.
	 * 
	 * @param cxn
	 * @param id
	 * @param type
	 *            the type of the object, e.g. project, subject, ex-method,
	 *            study, dataset or data-object.
	 * @return
	 * @throws Throwable
	 */
	protected static boolean exists(ServerClient.Connection cxn, String id, String type) throws Throwable {

		XmlStringWriter w = new XmlStringWriter();
		w.add("id", id);
		XmlDoc.Element r = cxn.execute("om.pssd.object.exists", w.document());
		if (r.value("exists").equalsIgnoreCase("false")) {
			return false;
		}
		if (type == null) {
			return true;
		}

		// Validate type
		r = cxn.execute("om.pssd.object.type", w.document());
		if (r.value("type").equalsIgnoreCase(type)) {
			return true;
		} else {
			return false;
		}

	}

}
