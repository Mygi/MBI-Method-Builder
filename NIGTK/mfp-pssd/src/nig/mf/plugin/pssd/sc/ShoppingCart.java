package nig.mf.plugin.pssd.sc;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import nig.mf.plugin.pssd.Asset;
import arc.mf.plugin.PluginService.Outputs;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class ShoppingCart {

	private String _name;
	private String _description;
	private Status _status;
	private String _changed;
	private List<Log> _logs;
	private Access _access;
	private String _scid;
	private String _templateName;
	private Archive _archive;
	private DeliveryDestination _destination;
	private List<Transcode> _transcodes;
	private MetadataOutput _metadataOutput;
	private Layout.Type _layoutType;
	private String _layoutPattern;
	private int _totalNumberOfContentItems;
	private long _totalSizeOfContentItems;
	private String _sizeUnits;
	private Map<String, Integer> _mimeTypeCount;

	protected ShoppingCart(ServiceExecutor executor, XmlDoc.Element ce) throws Throwable {

		/*
		 * id
		 */
		_scid = ce.value("@id");

		/*
		 * name
		 */
		_name = ce.value("name");

		/*
		 * description
		 */
		_description = ce.value("description");

		/*
		 * template
		 */
		_templateName = ce.value("template");

		/*
		 * status
		 */
		_status = Status.instantiate(ce.value("status"));

		/*
		 * changed
		 */
		_changed = ce.value("status/@changed");

		/*
		 * logs
		 */
		List<XmlDoc.Element> les = ce.elements("log");
		if (les != null) {
			_logs = new Vector<Log>(les.size());
			for (XmlDoc.Element le : les) {
				_logs.add(new Log(le));
			}
		}

		/*
		 * access
		 */
		XmlDoc.Element ae = ce.element("access");
		if (ae != null) {
			_access = new Access(ae);
		}

		/*
		 * archive
		 */
		_archive = new Archive(Archive.Type.instantiate(ce.value("packaging")));
		List<XmlDoc.Element> pes = ce.elements("packaging/parameter");
		if (pes != null) {
			for (XmlDoc.Element pe : pes) {
				_archive.setParameter(pe.value("@name"), pe.value());
			}
		}

		/*
		 * destination
		 */
		DeliveryMethod deliveryMethod = DeliveryMethod.instantiate(ce.value("delivery-method"));
		if (deliveryMethod.equals(DeliveryMethod.deposit)) {
			_destination = DeliveryDestination.getDestinationForCurrentUserByRootUrl(executor,
					ce.value("delivery-method/delivery-destination"));
		} else {
			_destination = DeliveryDestination.BROWSER;
		}

		/*
		 * transcodes
		 */
		List<XmlDoc.Element> tes = ce.elements("data-transformation/transcode");
		if (tes != null) {
			_transcodes = Transcode.instantiate(tes);
		}

		/*
		 * content summary
		 */
		XmlDoc.Element contentStatisticsElement = ce.element("content-statistics");
		if (contentStatisticsElement != null) {
			_totalNumberOfContentItems = contentStatisticsElement.intValue("item-count", 0);
			_totalSizeOfContentItems = contentStatisticsElement.longValue("item-size", 0);
			_sizeUnits = contentStatisticsElement.value("item-size/@units");
			if (_totalNumberOfContentItems > 0) {
				List<XmlDoc.Element> mtns = contentStatisticsElement.elements("content-mimetype/name");
				if (mtns != null) {
					for (XmlDoc.Element mtn : mtns) {
						setMimeTypeCount(mtn.value(), mtn.intValue("@count", 0));
					}
				}
			}
		}

		/*
		 * layout & layout pattern
		 */
		_layoutType = Layout.Type.instantiate(ce.value("layout"));
		_layoutPattern = ce.value("layout/layout-pattern");

	}

	public void setMimeTypeCount(String mimeType, int count) {

		if (_mimeTypeCount == null) {
			_mimeTypeCount = new HashMap<String, Integer>();
		}
		_mimeTypeCount.put(mimeType, count);
	}

	public String name() {

		return _name;
	}

	public void setName(String name) {

		_name = name;
	}

	public Status status() {

		return _status;
	}

	public String scid() {

		return _scid;
	}

	public List<Log> logs() {

		return _logs;
	}

	public String templateName() {

		return _templateName;
	}

	public Archive archive() {

		return _archive;
	}

	public void setArchive(Archive archive) {

		_archive = archive;
	}

	public DeliveryDestination destination() {

		return _destination;
	}

	public void setDestination(DeliveryDestination destination) {

		_destination = destination;
	}

	public MetadataOutput medatadataOutput() {

		return _metadataOutput;
	}

	public void setMetadataOutput(MetadataOutput mo) {

		_metadataOutput = mo;
	}

	public Layout.Type layoutType() {

		return _layoutType;
	}

	public String layoutPattern() {

		return _layoutPattern;
	}

	public List<Transcode> transcodes() {

		return _transcodes;
	}

	public void setTranscodes(List<Transcode> transcodes) {

		_transcodes = transcodes;
	}

	public void describe(XmlWriter w) throws Throwable {

		w.push("cart", new String[] { "scid", _scid });
		/*
		 * name
		 */
		if (_name != null) {
			w.add("name", _name);
		}
		/*
		 * description
		 */
		if (_description != null) {
			w.add("description", _description);
		}
		/*
		 * template
		 */
		w.add("template", _templateName);
		/*
		 * status
		 */
		w.add("status", new String[] { "changed", _changed }, _status.toString());
		/*
		 * log
		 */
		if (_logs != null) {
			for (Log log : _logs) {
				log.describe(w);
			}
		}
		/*
		 * access
		 */
		if (_access != null) {
			_access.describe(w);
		}
		/*
		 * archive
		 */
		if (_archive != null) {
			w.push("archive");
			w.add("type", _archive.type().toString());
			if (_archive.hasParameters()) {
				for (String pn : _archive.getParameterNames()) {
					w.add("parameter", new String[] { "name", pn }, _archive.getParameterValue(pn));
				}
			}
			w.pop();
		}
		/*
		 * destination
		 */
		if (_destination != null) {
			if (_destination.method.equals(DeliveryMethod.download)) {
				w.add("destination", new String[] { "method", _destination.method.toString() }, _destination.name);
			} else {
				w.add("destination",
						new String[] { "method", _destination.method.toString(), "url", _destination.url },
						_destination.name);
			}
		}
		/*
		 * transcodes
		 */
		if (_transcodes != null) {
			for (Transcode transcode : _transcodes) {
				w.push("transcode");
				w.add("from", transcode.from);
				w.add("to", transcode.to);
				w.pop();
			}
		}
		/*
		 * layout
		 */
		w.push("layout");
		w.add("type", _layoutType.toString());
		if (_layoutPattern != null) {
			w.add("pattern", _layoutPattern);
		}
		w.pop();
		/*
		 * content
		 */
		if (_mimeTypeCount == null) {
			w.add("content",
					new String[] { "count", Integer.toString(_totalNumberOfContentItems), "size",
							Long.toString(_totalSizeOfContentItems) });
		} else {
			w.push("content",
					new String[] { "count", Integer.toString(_totalNumberOfContentItems), "size",
							Long.toString(_totalSizeOfContentItems) });
			for (String mimeType : _mimeTypeCount.keySet()) {
				w.add("mime-type", new String[] { "count", _mimeTypeCount.get(mimeType).toString() }, mimeType);
			}
			w.pop();
		}
		w.pop();
	}

	public int totalNumberOfContentItems() {

		return _totalNumberOfContentItems;
	}

	public long totalSizeOfContentItems() {

		return _totalSizeOfContentItems;
	}

	public String sizeUnits() {

		return _sizeUnits;
	}

	public void commitChanges(ServiceExecutor executor, boolean validate) throws Throwable {

		if (validate) {
			if (_destination == null) {
				throw new Exception("Null delivery destination.");
			}
			if (_archive == null) {
				throw new Exception("Null archive settings.");
			}
			if (_destination.method.equals(DeliveryMethod.deposit) && !_archive.type().equals(Archive.Type.none)) {
				throw new Exception("If method is deposit, archive type must be none.");
			}
			if (_destination.method.equals(DeliveryMethod.download) && _archive.type().equals(Archive.Type.none)) {
				throw new Exception("If method is download, archive type must not beFs none.");
			}
		}
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("sid", _scid);
		if (_name != null) {
			dm.add("name", _name);
		}
		dm.add("layout", _layoutType.toString());
		dm.add("layout-pattern", _layoutPattern);
		if (_transcodes != null) {
			if (_transcodes.size() > 0) {
				dm.push("data-transformation");
				for (Transcode transcode : _transcodes) {
					dm.push("transform");
					dm.add("from", transcode.from);
					dm.add("to", transcode.to);
					dm.pop();
				}
				dm.pop();
			}
		}
		if (_destination != null) {
			dm.add("delivery", _destination.method.toString());
			if (_destination.method.equals(DeliveryMethod.deposit)) {
				dm.add("delivery-destination", _destination.url);
			}
		}
		if (_archive != null) {
			dm.push("packaging");
			dm.add("package-method", _archive.type().toString());
			if (_archive.hasParameters()) {
				for (String parameter : _archive.getParameterNames()) {
					dm.add("parameter", new String[] { "name", parameter }, _archive.getParameterValue(parameter));
				}
			}
			dm.pop();
		}
		if (_metadataOutput != null) {
			dm.add("metadata-output", _metadataOutput);
		}
		executor.execute("shopping.cart.modify", dm.root());
	}

	public void refreshContentItems(ServiceExecutor executor) throws Throwable {

		refreshContentItems(executor, _scid);
	}

	public static ShoppingCart instantiate(ServiceExecutor executor, XmlDoc.Element ce) throws Throwable {

		return new ShoppingCart(executor, ce);
	}

	public static ShoppingCart get(ServiceExecutor executor, String scid) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("sid", scid);
		XmlDoc.Element r = executor.execute("shopping.cart.describe", dm.root());
		XmlDoc.Element ce = r.element("cart");
		return instantiate(executor, ce);
	}

	public static String create(ServiceExecutor executor, String name, DeliveryDestination destination,
			Archive archive, MetadataOutput metadataOutput) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("template", Template.NAME);
		XmlDoc.Element r = executor.execute("shopping.cart.create", dm.root());
		String sid = r.value("id");
		if (destination != null || archive != null || metadataOutput != null) {
			update(executor, sid, name, null, destination, archive, metadataOutput, false);
		}
		return sid;
	}

	public static void update(ServiceExecutor executor, String scid, String name, List<Transcode> transcodes,
			DeliveryDestination destination, Archive archive, MetadataOutput metadataOutput, boolean validate)
			throws Throwable {

		ShoppingCart sc = ShoppingCart.get(executor, scid);
		if (name != null) {
			sc.setName(name);
		}
		if (transcodes != null) {
			sc.setTranscodes(transcodes);
		}
		if (destination != null) {
			sc.setDestination(destination);
		}
		if (archive != null) {
			sc.setArchive(archive);
		}
		if (metadataOutput != null) {
			sc.setMetadataOutput(metadataOutput);
		}
		sc.commitChanges(executor, validate);
	}

	public static void destroy(ServiceExecutor executor, Collection<String> scids) throws Throwable {

		if (scids != null) {
			for (String scid : scids) {
				destroy(executor, scid);
			}
		}
	}

	public static void destroy(ServiceExecutor executor, String scid) throws Throwable {

		executor.execute("shopping.cart.destroy", "<args><sid>" + scid + "</sid></args>", null, null);
	}

	public static Collection<String> getCartsForCurrentUser(ServiceExecutor executor) throws Throwable {

		return getCartsForCurrentUser(executor, null);
	}

	public static Collection<String> getCartsForCurrentUser(ServiceExecutor executor, Status status) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("size", "infinity");
		if (status != null) {
			dm.add("status", status.toString());
		}
		XmlDoc.Element r = executor.execute("shopping.cart.list", dm.root());
		return r.values("cart/@id");
	}

	public static void refreshContentItems(ServiceExecutor executor, String scid) {

		try {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("sid", scid);
			executor.execute("shopping.cart.content.refresh", dm.root());
		} catch (Throwable e) {
			// failed to refresh. It is fine.
		}
	}

	public static void retrieveOutput(ServiceExecutor executor, String cartId, Outputs outputs) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("sid", cartId);
		executor.execute("shopping.cart.output.retrieve", dm.root(), null, outputs);
	}

	public static void addContentItems(ServiceExecutor executor, String scid, Collection<String> cids, boolean recursive)
			throws Throwable {

		if (cids != null) {
			for (String cid : cids) {
				addContentItem(executor, scid, cid, recursive);
			}
		}
	}

	public static void addContentItem(ServiceExecutor executor, String scid, String cid, boolean recursive)
			throws Throwable {

		// TODO: remove has content.
		String where = "(asset has content) and ( cid = '" + cid + "'";
		if (recursive) {
			where += " or cid starts with '" + cid + "' ";
		}
		where += " )";
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("sid", scid);
		dm.add("where", where);
		executor.execute("shopping.cart.content.add", dm.root());
	}

	public static void describeContentItems(ServiceExecutor executor, List<XmlDoc.Element> es, XmlWriter w)
			throws Throwable {

		if (es != null) {
			List<ContentItem> items = ContentItem.instantiate(executor, es);
			if (items != null) {
				for (ContentItem item : items) {
					item.describe(w);
				}
			}
		}
	}

	public static void clearContentItems(ServiceExecutor executor, String scid) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("sid", scid);
		dm.add("size", "infinity");
		dm.add("count", true);
		XmlDoc.Element r = executor.execute("shopping.cart.content.list", dm.root());
		Collection<String> assetIds = r.values("cart/item/asset/@id");
		removeContentItems(executor, scid, assetIds);
	}

	public static void removeContentItems(ServiceExecutor executor, String scid, Collection<String> assetIds)
			throws Throwable {

		if (assetIds != null) {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("sid", scid);
			for (String assetId : assetIds) {
				dm.add("id", assetId);
			}
			executor.execute("shopping.cart.content.remove", dm.root());
		}
	}

	public static void removeContentItems(ServiceExecutor executor, String scid, Collection<String> cids,
			boolean recursive) throws Throwable {

		if (cids == null) {
			return;
		}
		if (recursive) {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("sid", scid);
			dm.add("size", "infinity");
			XmlDoc.Element r = executor.execute("shopping.cart.content.list", dm.root());
			Collection<String> assetIds = r.values("cart/item/asset/@id");
			if (assetIds == null) {
				return;
			}
			Map<String, String> ccids = new HashMap<String, String>(assetIds.size());
			for (String assetId : assetIds) {
				ccids.put(assetId, Asset.getCidById(executor, assetId));
			}
			dm = new XmlDocMaker("args");
			dm.add("sid", scid);
			int count = 0;
			for (String assetId : ccids.keySet()) {
				String ccid = ccids.get(assetId);
				if (ccid != null) {
					for (String cid : cids) {
						if (ccid.startsWith(cid)) {
							dm.add("id", assetId);
							count++;
							break;
						}
					}
				}
			}
			if (count > 0) {
				executor.execute("shopping.cart.content.remove", dm.root());
			}
		} else {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("sid", scid);
			for (String cid : cids) {
				String assetId = Asset.getIdByCid(executor, cid);
				dm.add("id", assetId);
			}
			executor.execute("shopping.cart.content.remove", dm.root());
		}
	}
}
