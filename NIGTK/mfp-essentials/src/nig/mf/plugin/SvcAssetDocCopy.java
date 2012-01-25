package nig.mf.plugin;

import java.util.Collection;
import java.util.Vector;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Attribute;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcAssetDocCopy extends PluginService {

	private Interface _defn;

	public SvcAssetDocCopy() {

		_defn = new Interface();
		Element e = new Element("from", AssetType.DEFAULT, "The asset id that the meta/document is copied from.", 1, 1);
		e.add(new Interface.Attribute("proute", CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.", 0));
		_defn.add(e);
		//
		e = new Element("to", AssetType.DEFAULT, "The asset id that the meta/document is copied to.", 1, 1);
		e.add(new Attribute("ns", StringType.DEFAULT, "Namespace of the document on the target asset. (only used when action is 'add')", 0));
		e.add(new Attribute("tag", StringType.DEFAULT, "Tag of the document on the target asset. (only used when action is 'add')", 0));
		_defn.add(e);
		
		e = new Element("doc", StringType.DEFAULT, "The document to be copied from the source asset.", 1, Integer.MAX_VALUE);
		e.add(new Attribute("tag", StringType.DEFAULT, "Tag of the document.", 0));
		e.add(new Attribute("ns", StringType.DEFAULT, "Namespace of the document.", 0));
		_defn.add(e);

		_defn.add(new Element("action", new EnumType(new String[] { "add", "replace", "merge" }, true),
				"Action to perform when copying meta information. Defaults to add", 0, 1));

	}

	public String name() {

		return "nig.asset.doc.copy";

	}

	public String description() {

		return "This service is used to copy a document from from one asset to another.";

	}

	public Interface definition() {

		return _defn;

	}

	public Access access() {

		return ACCESS_MODIFY;

	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String from = args.value("from");
		String fromRoute = args.value("from/@proute");              // Local if null
		Vector<XmlDoc.Element> docNames = args.elements("doc");
		String to = args.value("to");
		String tag = args.value("to/@tag");
		String ns = args.value("to/@ns");
		String action = args.stringValue("action", "add");

		/*
		 * Retrieve the source asset
		 */
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", from);
		XmlDoc.Element ae = executor().execute(new ServerRoute(fromRoute), "asset.get", dm.root());

		/*
		 * Find the docs from the source asset
		 */
		Vector<XmlDoc.Element> docs = findDocs(ae, docNames);
		if (docs == null) {
			// No docs found.
			return;
		}
		
		/*
		 * Override the namespace and tag attributes if the action is add.
		 */
		if(action.equals("add")){
			setDocAttributes(docs, ns, tag);
		} else {
			setDocAttributes(docs, null, null);
		}
		
		/*
		 * Set the target asset
		 */
		dm = new XmlDocMaker("args");
		dm.add("id", to);
		dm.push("meta", new String[] { "action", action });
		for (XmlDoc.Element doc : docs) {
			dm.add(doc);
		}
		dm.pop();
		executor().execute("asset.set", dm.root());

		/*
		 * Show some information
		 */
		for (XmlDoc.Element doc : docs) {
			w.add("doc", new String[] { "from", from, "to", to }, doc.name());
		}

	}

	private void setDocAttributes(Vector<XmlDoc.Element> docs, String ns, String tag) {
		if (docs == null) {
			return;
		}
		for (XmlDoc.Element doc : docs) {
			if (doc.attribute("ns") != null) {
				doc.remove(doc.attribute("ns"));
			}
			if (doc.attribute("tag") != null) {
				doc.remove(doc.attribute("tag"));
			}
			if (ns != null) {
				doc.add(new XmlDoc.Attribute("ns", ns));
			}
			if (tag != null) {
				doc.add(new XmlDoc.Attribute("tag", tag));
			}
		}
	}

	private Vector<XmlDoc.Element> findDocs(XmlDoc.Element ae, Vector<XmlDoc.Element> docNames) throws Throwable {
		if (ae == null || docNames == null) {
			return null;
		}
		Vector<XmlDoc.Element> rdocs = new Vector<XmlDoc.Element>();
		for (XmlDoc.Element doc : docNames) {
			String docName = doc.value();
			String ns = doc.value("@ns");
			String tag = doc.value("@tag");
			XmlDoc.Element rdoc = findDoc(ae, docName, ns, tag);
			if (rdoc != null) {
				rdocs.add(rdoc);
			}
		}
		if (rdocs.size() > 0) {
			return rdocs;
		}
		return null;
	}

	private XmlDoc.Element findDoc(XmlDoc.Element ae, String docName, String ns, String tag) throws Throwable {
		if (ae == null || docName == null) {
			return null;
		}
		Collection<XmlDoc.Element> docs = ae.elements("asset/meta/" + docName);
		if (docs != null) {
			for (XmlDoc.Element doc : docs) {
				XmlDoc.Attribute nsAttr = doc.attribute("ns");
				XmlDoc.Attribute tagAttr = doc.attribute("tag");

				boolean nsMatch = false;
				if (ns == null) {
					// if (nsAttr == null) {
					nsMatch = true;
					// }
				} else {
					if (nsAttr != null) {
						if (ns.equals(nsAttr.value())) {
							nsMatch = true;
						}
					}
				}
				boolean tagMatch = false;
				if (tag == null) {
					// if (tagAttr == null) {
					tagMatch = true;
					// }
				} else {
					if (tagAttr != null) {
						if (tag.equals(tagAttr.value())) {
							tagMatch = true;
						}
					}
				}
				if (nsMatch && tagMatch) {
					return doc;
				}
			}
		}
		return null;
	}

}