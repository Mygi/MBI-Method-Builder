package nig.mf.plugin.pssd;

import arc.mf.plugin.PluginService.Outputs;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public abstract class Asset {

	private String _id;
	private String _proute;
	private String _cid;
	private String _rid;
	private String _vid;
	private int _version = 0;

	protected Asset() {

	}

	protected Asset(XmlDoc.Element ae) throws Throwable {

		parseAssetMeta(ae);
	}

	public String assetId() {

		return _id;
	}

	public String citeableId() {

		return _cid;
	}

	public String proute() {

		return _proute;
	}

	public String rid() {

		return _rid;
	}

	public String vid() {

		return _vid;
	}

	public int version() {

		return _version;
	}

	public boolean isReplica() {

		return _rid != null;
	}

	public boolean isPrimary() {

		return _rid == null;
	}

	public boolean isRemote() {

		return isRemote(_proute);
	}

	public boolean isLocal() {

		return !isRemote(_proute);
	}

	/**
	 * judge if the proute is for a remote asset.
	 * 
	 * @param proute
	 * @return
	 */
	public static boolean isRemote(String proute) {

		if (proute == null) {
			return false;
		}
		if (proute.indexOf(".") == -1) {
			return false;
		}
		return true;
	}

	protected void parseAssetMeta(XmlDoc.Element ae) throws Throwable {

		_id = ae.value("@id");
		_proute = ae.value("@proute");
		_cid = ae.value("cid");
		_rid = ae.value("rid");
		_version = ae.intValue("@version", 0);
		_vid = ae.value("vid");
	}

	protected void saveAssetMeta(XmlWriter w) throws Throwable {

	}

	/**
	 * Calls asset.query service.
	 * 
	 * @param sroute
	 *            Server route.
	 * @param executor
	 * @param args
	 * @return the result XML element.
	 * @throws Throwable
	 */
	public static XmlDoc.Element query(ServiceExecutor executor, ServerRoute sroute, XmlDoc.Element args)
			throws Throwable {

		return executor.execute(sroute, "asset.query", args);
	}

	/**
	 * Calls asset.query service
	 * 
	 * @param executor
	 * @param args
	 * @return the result XML element.
	 * @throws Throwable
	 */
	public static XmlDoc.Element query(ServiceExecutor executor, XmlDoc.Element args) throws Throwable {

		return query(executor, null, args);
	}

	/**
	 * Calls asset.get service.
	 * 
	 * @param sroute
	 * @param executor
	 * @param args
	 * @param outputs
	 * @return the asset XML element.
	 * @throws Throwable
	 */
	public static XmlDoc.Element get(ServiceExecutor executor, ServerRoute sroute, XmlDoc.Element args, Outputs outputs)
			throws Throwable {

		XmlDoc.Element r = executor.execute(sroute, "asset.get", args, null, outputs);
		XmlDoc.Element ae = null;
		if (r != null) {
			ae = r.element("asset");
		}
		return ae;
	}

	public static XmlDoc.Element get(ServiceExecutor executor, ServerRoute sroute, XmlDoc.Element args)
			throws Throwable {

		return get(executor, sroute, args, null);
	}

	public static XmlDoc.Element getByCid(ServiceExecutor executor, ServerRoute sroute, String cid) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", cid);
		return get(executor, sroute, dm.root(), null);
	}

	public static XmlDoc.Element getById(ServiceExecutor executor, ServerRoute sroute, String id) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		return get(executor, sroute, dm.root(), null);
	}

	public static boolean existsByCid(ServiceExecutor executor, ServerRoute sroute, String cid) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", cid);
		XmlDoc.Element r = executor.execute(sroute, "asset.exists", dm.root());
		return r.booleanValue("exists", false);
	}

	public static boolean existsById(ServiceExecutor executor, ServerRoute sroute, String id) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		XmlDoc.Element r = executor.execute(sroute, "asset.exists", dm.root());
		return r.booleanValue("exists", false);
	}

	public static String getIdByCid(ServiceExecutor executor, String cid) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where", "cid='" + cid + "'");
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		return r.value("id");
	}

	public static String getCidById(ServiceExecutor executor, String id) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where", "id=" + id + "");
		dm.add("action", "get-cid");
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		return r.value("cid");
	}

}
