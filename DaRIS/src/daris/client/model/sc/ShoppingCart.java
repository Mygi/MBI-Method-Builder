package daris.client.model.sc;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import arc.mf.client.util.DateTime;
import arc.mf.client.xml.XmlElement;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.user.client.Timer;

import daris.client.Resource;
import daris.client.model.sc.messages.ShoppingCartProcessingDescribe;
import daris.client.model.transcode.Transcode;
import daris.client.util.ByteUtil;

public class ShoppingCart {

	public static final String ACTIVE_ICON = Resource.INSTANCE.active24()
			.getSafeUri().asString();
	public static final String DEPOSIT_ICON = Resource.INSTANCE.deposit24()
			.getSafeUri().asString();

	public static final String TYPE_NAME = "shoppingcart";

	private String _name;
	private String _description;
	private Status _status;
	private Date _changed;
	private List<Log> _logs;
	private long _scid;
	private String _templateName;
	private Archive _archive;
	private DeliveryDestination _destination;
	private List<Transcode> _transcodes;
	private MetadataOutput _metadataOutput;
	private Layout.Type _layoutType;
	private String _layoutPattern;
	private int _totalNumberOfContentItems;
	private long _totalSizeOfContentItems;
	private Map<String, Integer> _mimeTypeCount;
	private boolean _canModify;
	private boolean _canReEdit;
	private boolean _canWithdraw;
	private boolean _canReprocess;
	private boolean _canDestroy;

	protected ShoppingCart(XmlElement ce) throws Throwable {

		/*
		 * id
		 */
		_scid = ce.longValue("@scid", 0);

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
		_changed = ce.dateValue("status/@changed");

		/*
		 * log
		 */
		_logs = Log.instantiate(ce.elements("log"));

		/*
		 * access
		 */
		_canModify = ce.booleanValue("access/can-modify", false);
		_canReEdit = ce.booleanValue("access/can-re-edit", false);
		_canWithdraw = ce.booleanValue("access/can-withdraw", false);
		_canReprocess = ce.booleanValue("access/can-reprocess", false);
		_canDestroy = ce.booleanValue("access/can-destroy", false);

		/*
		 * archive
		 */
		_archive = new Archive(Archive.Type.instantiate(ce
				.value("archive/type")));
		List<XmlElement> pes = ce.elements("archive/parameter");
		if (pes != null) {
			for (XmlElement pe : pes) {
				_archive.setParameter(pe.value("@name"), pe.value());
			}
		}

		/*
		 * destination
		 */
		_destination = DeliveryDestination.instantiate(ce
				.element("destination"));

		/*
		 * transcodes
		 */
		List<XmlElement> tes = ce.elements("transcode");
		if (tes != null) {
			_transcodes = Transcode.instantiate(tes);
		}

		/*
		 * content summary
		 */

		XmlElement cse = ce.element("content");
		if (cse != null) {
			_totalNumberOfContentItems = cse.intValue("@count", 0);
			_totalSizeOfContentItems = cse.longValue("@size", 0);
			if (_totalNumberOfContentItems > 0) {
				List<XmlElement> mtns = cse.elements("mime-type");
				if (mtns != null) {
					for (XmlElement mtn : mtns) {
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

	public String description() {
		return _description;
	}

	public void setDescription(String description) {
		_description = description;
	}

	public Status status() {

		return _status;
	}

	public Date changed() {
		return _changed;
	}

	public long scid() {

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

	public void addTranscode(Transcode transcode) {
		if (transcode == null) {
			return;
		}
		if (_transcodes == null) {
			_transcodes = new Vector<Transcode>();
		}
		for (Iterator<Transcode> it = _transcodes.iterator(); it.hasNext();) {
			Transcode t = it.next();
			if (t.from().equals(transcode.from())) {
				it.remove();
				break;
			}
		}
		_transcodes.add(transcode);
	}

	public boolean hasTranscodes() {
		if (_transcodes != null) {
			if (!_transcodes.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	public int totalNumberOfContentItems() {

		return _totalNumberOfContentItems;
	}

	public long totalSizeOfContentItems() {

		return _totalSizeOfContentItems;
	}

	public boolean canModify() {
		return _canModify;
	}

	public boolean canReEdit() {
		return _canReEdit;
	}

	public boolean canWithdraw() {
		return _canWithdraw;
	}

	public boolean canReprocess() {
		return _canReprocess;
	}

	public boolean canDestroy() {
		return _canDestroy;
	}

	@Override
	public boolean equals(Object o) {

		if (o != null) {
			if (o instanceof ShoppingCart) {
				return _scid == ((ShoppingCart) o).scid();
			}
		}
		return false;
	}

	public String summaryHTML() {
		boolean isActive = ShoppingCartManager.isActive(this);
		return "<b>Shopping Cart "
				+ _scid
				+ " [Active: "
				+ isActive
				+ "; Status: "
				+ _status.toString()
				+ ", Number of Datasets: "
				+ _totalNumberOfContentItems
				+ ", Total Size: "
				+ ByteUtil.humanReadableByteCount(_totalSizeOfContentItems,
						true) + "]</b>";
	}

	public String toHTML() {

		String html = "<table><thead><tr><th align=\"center\" colspan=\"2\">Shopping-cart</th></tr><thead>";
		html += "<tbody>";
		html += "<tr><td><b>id:</b></td><td>" + _scid + "</td></tr>";
		if (_name != null) {
			html += "<tr><td><b>name:</b></td><td>" + _name + "</td></tr>";
		}
		html += "<tr><td><b>status:</b></td><td>" + _status + "</td></tr>";
		if (_totalNumberOfContentItems > 0) {
			html += "<tr><td><b>content:</b></td><td>"
					+ _totalNumberOfContentItems + "items (size="
					+ _totalSizeOfContentItems + " bytes)";
			if (_mimeTypeCount != null) {
				for (String mimeType : _mimeTypeCount.keySet()) {
					html += "<br/>" + mimeType + ": "
							+ _mimeTypeCount.get(mimeType);
				}
			}
			html += "</td></tr>";
		}
		if (_destination != null) {
			html += "<tr><td><b>destination:</b></td><td>"
					+ _destination.name() + "</td></tr>";
		}
		if (_archive != null) {
			if (!_archive.toString().equals(Archive.Type.none)) {
				html += "<tr><td><b>archive:</b></td><td>type: "
						+ _archive.type().toString() + "</td></tr>";
			}
		}
		if (_transcodes != null) {
			for (Transcode transcode : _transcodes) {
				html += "<tr><td><b>transcode:</b></td><td>from: "
						+ transcode.from() + " to: " + transcode.to()
						+ "</td></tr>";
			}
		}
		if (_logs != null) {
			for (Log log : _logs) {
				html += "<tr><td><b>log:</b></td><td>["
						+ DateTime.dateTimeAsClientString(log.changed())
						+ " status: " + log.status() + "] " + log.message()
						+ "</td></tr>";
			}
		}
		html += "</tbody></table>";
		return html;
	}

	public String statusIcon() {
		String icon = status().icon();
		if (Status.data_ready == status()
				&& destination().method() == DeliveryMethod.deposit) {
			icon = DEPOSIT_ICON;
		}
		if (Status.editable == status() && ShoppingCartManager.isActive(this)) {
			icon = ACTIVE_ICON;
		}
		return icon;
	}

	public Timer monitorProgress(int delay, ProgressHandler ph) {
		return monitorProgress(scid(), delay, ph);
	}

	public static ShoppingCart instantiate(XmlElement ce) throws Throwable {

		return new ShoppingCart(ce);
	}

	public static Timer monitorProgress(final long scid, final int delay,
			final ProgressHandler ph) {
		Timer t = new Timer() {

			@Override
			public void run() {
				progress(this, delay, scid, ph);

			}
		};
		t.schedule(1);
		return t;
	}

	public static void progress(long scid, ProgressHandler ph) {
		progress(null, 0, scid, ph);
	}

	private static void progress(final Timer t, final int delay, long scid,
			final ProgressHandler ph) {
		if (scid <= 0) {
			ph.progress(null);
			return;
		}
		new ShoppingCartProcessingDescribe(scid)
				.send(new ObjectMessageResponse<Progress>() {

					@Override
					public void responded(Progress progress) {
						if (t != null) {
							if (progress == null) {
								t.cancel();
							} else {
								t.schedule(delay);
							}
							ph.progress(progress);
						}
					}
				});
	}
}
