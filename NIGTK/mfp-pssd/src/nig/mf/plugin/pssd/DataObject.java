package nig.mf.plugin.pssd;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

public class DataObject {
	public static final String TYPE = "data-object";
	
	public static final int DEPTH = 5;
	
	public static final String MODEL = "om.pssd.data-object";
	
	/**
	 * Returns true if the CID is for a DataObject object on the local server
	 */
	public static boolean isObjectDataObject (ServiceExecutor executor, String cid) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id",cid);	
		XmlDoc.Element r = executor.execute("om.pssd.object.type",dm.root());	
		String type = r.value("type");
		if (type.equals(TYPE)) return true;
		return false;
	}

}
