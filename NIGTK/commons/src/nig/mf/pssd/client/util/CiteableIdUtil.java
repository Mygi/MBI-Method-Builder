package nig.mf.pssd.client.util;

import arc.mf.client.ServerClient;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;

public class CiteableIdUtil extends nig.mf.pssd.CiteableIdUtil{

	/**
	 * Get the citeable id root for the projects.
	 * 
	 * @param cxn
	 * @return
	 * @throws Throwable
	 */
	public static String getProjectIdRoot(ServerClient.Connection cxn) throws Throwable {
		XmlStringWriter w = new XmlStringWriter();
		w.add("name", "pssd.project");
		XmlDoc.Element r = cxn.execute("citeable.named.id.create", w.document());
		return r.value("cid");
	}
	
	
	/**
	 * Find out if a CID already exists on the given server
	 * 
	 * @param proute Route to server.  Null means local
	 * @param executor
	 * @param cid
	 * @return
	 * @throws Throwable
	 */
	public static boolean cidExists(ServerClient.Connection cxn, String cid) throws Throwable {

		XmlStringWriter w = new XmlStringWriter();
		w.add("cid", cid);
		XmlDoc.Element r = cxn.execute("citeable.id.exists", w.document());
		String exists = r.value("exists");
		if (exists != null) {
			if (exists.equals("true")) {
				return true;
			}
		}
		return false;

	}


	/**
	 * Import the given CID Into the local server
	 * 
	 * @param executor
	 * @param cid
	 * @param rootDepth
	 * @return
	 * @throws Throwable
	 */
	public static String importCid(ServerClient.Connection cxn, String cid, int rootDepth) throws Throwable {

		XmlStringWriter w = new XmlStringWriter();
		w.add("cid", cid);
		w.add("root-depth", rootDepth);
		XmlDoc.Element r = cxn.execute("citeable.id.import", w.document());
		return r.value("cid");

	}
	
	/**
	 * This function imports a CID and will replace just the first value (the
	 * server) with the server id. This is a way to create a CID as specified.
	 * 
	 * @param executor
	 * @param serverRoute
	 *            the route to the server that manages (allocates this CID tree)
	 * @param The
	 *            citable ID to import
	 * @throws Throwable
	 */
	public static String importCid(ServerClient.Connection cxn, String cid) throws Throwable {

		return importCid(cxn, cid, 1);
	}
}
