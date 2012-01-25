package nig.mf.plugin.pssd.util;

import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

public class PSSDObjectUtil {

	/**
	 * Returns the namespace of the specified PSSD object.
	 * 
	 * @param executor
	 *            the service executor
	 * 
	 * @param sroute
	 *            the server route
	 * 
	 * @param id
	 *            the citeable id of the object
	 * @return
	 * @throws Throwable
	 */
	public static String namespaceOf(ServiceExecutor executor,
			ServerRoute sroute, String id) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", id);
		XmlDoc.Element r = executor.execute(sroute, "asset.get", dm.root());
		return r.value("asset/namespace");
	}

	/**
	 * Returns the namespace of the specified PSSD object.
	 * 
	 * @param executor
	 *            the service executor
	 * @param id
	 *            the citeable id of the object
	 * @return
	 * @throws Throwable
	 */
	public static String namespaceOf(ServiceExecutor executor, String id)
			throws Throwable {
		return namespaceOf(executor, null, id);
	}
	
}
