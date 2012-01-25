package nig.mf.plugin.pssd;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

public class DataSet {
	public static final String TYPE = "dataset";
	
	public static final int DEPTH = 4;
	
	public static final String MODEL = "om.pssd.dataset";

	/**
	 * Returns true if the CID is for a DataSet object on the local server
	 */
	public static boolean isObjectDataSet (ServiceExecutor executor, String cid) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id",cid);	
		XmlDoc.Element r = executor.execute("om.pssd.object.type",dm.root());	
		String type = r.value("type");
		if (type.equals(TYPE)) return true;
		return false;
	}
	
	/**
	 * Update the ExMethod information (only) on a DataSet on the local server.  Finds out whether the DataSet
	 * is derivation or primary and calls appropriate service.
	 * 
	 * @param id The CID of the DataSet
	 * @param method  The CID of the Method (if null no change)
	 * @param step The path of the Step in the Method (if null no change)
	 */
	public static void updateExMethodMeta (ServiceExecutor executor, String id, String method, String step) throws Throwable {
		if (id==null) return;
		if (method==null && step==null) return;
		//
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		XmlDoc.Element r = executor.execute("om.pssd.object.describe",dm.root());	
		if (r==null) return;
		
		// Setup meta-data
		dm = new XmlDocMaker("args");
		dm.add("id", id);
		dm.push("method");
		if (method!=null) dm.add("id", method);
		if (step!=null) dm.add("step", step);
		dm.pop();
		
		// Set Method meta in DataSet
		String dsType = r.value("object/source/type");
		if (dsType.equals("primary")) {
			executor.execute("om.pssd.dataset.primary.update",dm.root());	
		} else if (dsType.equals("derivation")) {
			executor.execute("om.pssd.dataset.derivation.update",dm.root());	
		} else {
			throw new Exception("Unknown DataSet has value type=" + dsType);
		}

	}
	
}
