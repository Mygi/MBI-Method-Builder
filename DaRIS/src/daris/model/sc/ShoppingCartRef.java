package daris.model.sc;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import arc.mf.client.util.Action;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectMessage;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectRef;
import arc.mf.object.ObjectResolveHandler;
import daris.model.object.PSSDObjectRef;
import daris.model.sc.archive.Archive;
import daris.model.sc.messages.ShoppingCartContentAdd;
import daris.model.sc.messages.ShoppingCartContentRemove;
import daris.model.sc.messages.ShoppingCartDestroy;
import daris.model.sc.messages.ShoppingCartOrder;
import daris.model.sc.messages.ShoppingCartOutputRetrieve;
import daris.model.sc.messages.ShoppingCartUpdate;
import daris.model.transcode.Transcode;

public class ShoppingCartRef extends ObjectRef<ShoppingCart> {

	private String _id;

	private String _name;

	private Status _status;

	private String _template;

	private boolean _selfServiced;

	private String _orderId;

	private Content _content;

	private Map<String, Transcode> _transcodes;

	private Destination _destination;

	private Layout _layout;

	private Archive _archive = Archive.DEFAULT;

	private MetadataOutput _metadataOutput = MetadataOutput.DEFAULT;

	private ContentItemSetRef _contentItems;

	public ShoppingCartRef() {

	}

	public ShoppingCartRef(String id) {

		_id = id;
	}

	public ShoppingCartRef(XmlElement ce) {

		parse(ce);
	}

	private void parse(XmlElement ce) {

		/*
		 * id
		 */
		_id = ce.value("@id");

		/*
		 * name
		 */
		_name = ce.value("name");

		/*
		 * status
		 */
		XmlElement statusElement = ce.element("status");
		if (statusElement != null) {
			_status = new Status(statusElement);
		}
		/*
		 * template
		 */
		_template = ce.value("template");
		try {
			_selfServiced = ce.booleanValue("self-serviced", true);
		} catch (Throwable t) {
			throw new AssertionError(t.getMessage());
		}
		/*
		 * order
		 */
		_orderId = ce.value("order");
		/*
		 * content
		 */
		XmlElement contentStatisticsElement = ce.element("content");
		if (contentStatisticsElement != null) {
			_content = new Content(contentStatisticsElement);
		} else {
			_content = null;
		}
		/*
		 * transcode
		 */
		List<XmlElement> transcodeElements = ce.elements("transcode");
		if (transcodeElements != null) {
			for (XmlElement transcodeElement : transcodeElements) {
				addTranscode(transcodeElement.value("@from"),
						transcodeElement.value("@to"));
			}
		} else {
			clearTranscodes();
		}
		/*
		 * delivery
		 */
		XmlElement destinationElement = ce.element("destination");
		if (destinationElement != null) {
			_destination = new Destination(destinationElement);
		} else {
			throw new AssertionError("No delivery element found.");
		}
		/*
		 * package
		 */
		XmlElement archiveElement = ce.element("archive");
		if (archiveElement != null) {
			_archive = Archive.instantiate(archiveElement);
		} else {
			throw new AssertionError("No package element found.");
		}
		/*
		 * layout
		 */
		XmlElement layoutElement = ce.element("layout");
		if (layoutElement != null) {
			_layout = new Layout(layoutElement);
		} else {
			throw new AssertionError("No layout element found.");
		}
		/*
		 * metadata-output
		 */
		String metadataOutput = ce.value("metadata-output");
		if (metadataOutput != null) {
			_metadataOutput = MetadataOutput.instantiate(metadataOutput);
		} else {
			_metadataOutput = MetadataOutput.none;
		}
	}

	public void addTranscode(String from, String to) {

		addTranscode(new Transcode(from, to));
	}

	public void addTranscode(Transcode transcode) {

		if (_transcodes == null) {
			_transcodes = new HashMap<String, Transcode>();
		}
		_transcodes.put(transcode.from(), transcode);
	}

	public void removeTranscode(String from) {

		if (_transcodes != null) {
			_transcodes.remove(from);
		}
	}

	private void clearTranscodes() {

		if (_transcodes != null) {
			_transcodes.clear();
		}
		_transcodes = null;
	}

	public String id() {

		return _id;
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

	public String template() {

		return _template;
	}

	public String orderId() {

		return _orderId;
	}

	public boolean selfServiced() {

		return _selfServiced;
	}

	public Content content() {

		return _content;
	}

	public Collection<Transcode> transcodes() {

		if (_transcodes != null) {
			return _transcodes.values();
		}
		return null;
	}

	public Destination destination() {

		return _destination;
	}

	public void setDestination(Destination destination) {

		_destination = destination;
	}

	public Archive archive() {

		return _archive;
	}

	public void setArchiveType(Archive.Type archiveType) {
		if (_archive != null) {
			if (_archive.type().equals(archiveType)) {
				return;
			}
		}
		_archive = Archive.create(archiveType);
	}

	public Layout layout() {

		return _layout;
	}

	public void setLayout(Layout layout) {

		_layout = layout;
	}

	public MetadataOutput metadataOutput() {

		return _metadataOutput;
	}

	public void setMetadataOutput(MetadataOutput metadataOutput) {

		_metadataOutput = metadataOutput;
	}

	@Override
	public boolean equals(Object o) {

		if (o instanceof ShoppingCartRef) {
			return _id.equals(((ShoppingCartRef) o).id());
		}
		return false;
	}

	@Override
	public int hashCode() {

		return _id.hashCode();
	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		w.add("id", _id);
	}

	@Override
	protected String resolveServiceName() {

		return "om.pssd.shoppingcart.describe";
	}

	@Override
	protected ShoppingCart instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			XmlElement ce = xe.element("cart");
			if (ce != null) {
				parse(ce);
				return new ShoppingCart(ce);
			}
		}
		return null;
	}

	@Override
	public String referentTypeName() {

		return "shopping-cart";
	}

	@Override
	public String idToString() {

		return _id;
	}

	public void refresh(final Action action) {

		reset();
		resolve(new ObjectResolveHandler<ShoppingCart>() {

			@Override
			public void resolved(ShoppingCart o) {

				if (action != null) {
					action.execute();
				}
			}
		});
	}

	public void refresh() {

		ShoppingCartManager.instance().refresh(this);
	}

	/**
	 * Commit the local changes.
	 * 
	 * @param action
	 *            post action.
	 */
	public void commitChanges(final Action action) {

		ObjectMessage<Boolean> msg = new ShoppingCartUpdate(this);
		msg.send(new ObjectMessageResponse<Boolean>() {

			@Override
			public void responded(Boolean r) {

				if (r) {
					ShoppingCartRef.this.refresh(action);
				}
			}
		});
	}

	public void commitChanges() {

		ShoppingCartManager.instance().commitChanges(this);
	}

	/**
	 * Destroy the shopping cart.
	 * 
	 * @param action
	 *            post action.
	 */
	public void destroy(Action action) {

		ObjectMessage<Boolean> msg = new ShoppingCartDestroy(this);
		sendMessage(msg, action);
	}

	public void destroy() {

		ShoppingCartManager.instance().destroy(this);
	}

	public void contentItems(ObjectResolveHandler<List<ContentItem>> rh,
			boolean refresh) {

		if (_id == null) {
			if (rh != null) {
				rh.resolved(null);
			}
		}
		if (_contentItems == null) {
			_contentItems = new ContentItemSetRef(_id);
		}
		if (refresh) {
			_contentItems.reset();
		}
		_contentItems.resolve(rh);
	}

	public void addContentItem(PSSDObjectRef o, boolean recursive, Action action) {

		ShoppingCartContentAdd msg = new ShoppingCartContentAdd(this.id(),
				o.id(), recursive);
		sendMessage(msg, action);
	}

	public void addContentItem(PSSDObjectRef o, boolean recursive) {

		ShoppingCartManager.instance().addContentItem(this, o, recursive);
	}

	public void addContentItems(Collection<PSSDObjectRef> os,
			boolean recursive, Action action) {

		ShoppingCartContentAdd msg = new ShoppingCartContentAdd(this, os,
				recursive);
		sendMessage(msg, action);
	}

	public void addContentItems(Collection<PSSDObjectRef> os, boolean recursive) {
		ShoppingCartManager.instance().addContentItems(this, os, recursive);
	}

	public void removeContentItem(PSSDObjectRef o, boolean recursive,
			Action action) {

		ShoppingCartContentRemove msg = new ShoppingCartContentRemove(this, o,
				recursive);
		sendMessage(msg, action);
	}

	public void removeContentItem(PSSDObjectRef o, boolean recursive) {

		ShoppingCartManager.instance().removeContentItem(this, o, recursive);
	}

	public void removeContentItems(Collection<PSSDObjectRef> os,
			boolean recursive, Action action) {

		ShoppingCartContentRemove msg = new ShoppingCartContentRemove(this, os,
				recursive);
		sendMessage(msg, action);
	}

	public void removeContentItems(Collection<PSSDObjectRef> os,
			boolean recursive) {

		ShoppingCartManager.instance().removeContentItems(this, os, recursive);
	}

	public void removeContentItem(ContentItem item, Action action) {

		ShoppingCartContentRemove msg = new ShoppingCartContentRemove(this,
				item.id(), false);
		sendMessage(msg, action);
	}

	public void removeContentItem(ContentItem item) {
		ShoppingCartManager.instance().removeContentItem(this, item);
	}

	public void removeContentItems(Collection<ContentItem> items, Action action) {

		Collection<String> ids = null;
		if (items != null) {
			ids = new Vector<String>();
			for (ContentItem item : items) {
				ids.add(item.id());
			}
		}
		ShoppingCartContentRemove msg = new ShoppingCartContentRemove(id(),
				ids, false);
		sendMessage(msg, action);
	}

	public void removeContentItems(Collection<ContentItem> items) {
		ShoppingCartManager.instance().removeContentItems(this, items);
	}

	public void clearContentItems(Action action) {

		ShoppingCartContentRemove msg = new ShoppingCartContentRemove(this,
				true);
		sendMessage(msg, action);
	}

	public void clearContentItems() {
		ShoppingCartManager.instance().clearContentItems(this);
	}

	private void sendMessage(ObjectMessage<Boolean> msg, final Action action) {

		msg.send(new ObjectMessageResponse<Boolean>() {

			@Override
			public void responded(Boolean r) {

				if (r) {
					if (action != null) {
						action.execute();
					}
				}
			}
		});
	}

	public void order(final Action action) {

		if (_status.value() != Status.Value.editable) {
			return;
		}
		contentItems(new ObjectResolveHandler<List<ContentItem>>() {

			@Override
			public void resolved(List<ContentItem> o) {

				if (o != null) {
					if (o.size() > 0) {
						ObjectMessage<Boolean> msg = new ShoppingCartOrder(
								ShoppingCartRef.this);
						sendMessage(msg, action);
					}
				}
			}
		}, false);
	}

	public void order() {

		ShoppingCartManager.instance().order(this);
	}

	public void download(Action action) {

		if (status().value() != Status.Value.data_ready) {
			return;
		}
		if (destination().type() != Destination.Type.download) {
			return;
		}
		ObjectMessage<Boolean> msg = new ShoppingCartOutputRetrieve(this);
		sendMessage(msg, action);
	}

	public void download() {

		ShoppingCartManager.instance().download(this);
	}
}
