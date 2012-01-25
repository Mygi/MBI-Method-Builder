package nig.mf.plugin;

import java.util.Collection;

import nig.mf.pssd.plugin.util.DistributedQuery;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcAssetDocElementReplace extends PluginService {

	private Interface _defn;

	public SvcAssetDocElementReplace() {

		_defn = new Interface();
		_defn.add(new Element("cid", CiteableIdType.DEFAULT,
				"The citable ID of the asset of interest. Defaults to all assets", 0, 1));
		_defn.add(new Element("type", StringType.DEFAULT, "The document type of interest. E.g.  'hfi.pssd.ethics'.", 1,
				1));
		_defn.add(new Element("path", StringType.DEFAULT,
				"The path of the element within the document type to modify. E.g. 'ethics-id'.", 1, 1));
		_defn.add(new Element(
				"attribute",
				StringType.DEFAULT,
				"The attribute of the given element to replace. E.g. 'type'. If not supplied, then the value will be replaced.",
				0, 1));
		_defn.add(new Element("old-value", StringType.DEFAULT,
				"The old value of the element (attribute or value).  Must be paired with a 'new-value'.", 1,
				Integer.MAX_VALUE));
		_defn.add(new Element("new-value", StringType.DEFAULT,
				"The new value of the element (attribute or value).  Must be paired with an 'old value'.", 1,
				Integer.MAX_VALUE));
		_defn.add(new Interface.Element(
				"asset-type",
				new EnumType(new String[] { "primary", "replica", "all" }),
				"Specify type of asset to find. Defaults to 'primary' as generally replicas should be handled by a re-replication process.",
				0, 1));
	}

	public String name() {

		return "nig.asset.doc.element.replace";
	}

	public String description() {

		return "Replaces the value of a specified document meta-data element attribute or value in all relevant local assets. The old and new must be paired "
				+ "lists.  The intent is to replace the values of dictionary values (often used as attributes and sometimes values) when those dictionary values have changed.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_ADMINISTER;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String cid = args.value("cid");
		String docType = args.value("type");
		String elementPath = args.value("path");
		String attName = args.value("attribute");
		Collection<String> oldValues = args.values("old-value");
		Collection<String> newValues = args.values("new-value");
		if (oldValues.size() != newValues.size()) {
			throw new Exception("The old-value and new-value lists must be the same length");
		}
		String tp = args.stringValue("asset-type", DistributedQuery.ResultAssetType.primary.toString());
		DistributedQuery.ResultAssetType assetType = DistributedQuery.ResultAssetType.instantiate(tp);
		//
		replace(executor(), cid, assetType, docType, elementPath, attName, oldValues, newValues, w);
	}

	private void replace(ServiceExecutor executor, String cid, DistributedQuery.ResultAssetType assetType,
			String docType, String elementPath, String attName, Collection<String> oldValues,
			Collection<String> newValues, XmlWriter w) throws Throwable {

		// Find all assets for this xpath
		XmlDocMaker dm = new XmlDocMaker("args");
		String query = "xpath(" + docType + ") has value";
		DistributedQuery.appendResultAssetTypePredicate(query, assetType); // Select
																			// asset
																			// type
		if (cid != null) {
			query += " and cid='" + cid + "'";
		}
		dm.add("where", query);
		dm.add("size", "infinity");
		dm.add("pdist", 0); // Local assets only
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		if (r == null) {
			w.add("number-of-assets", 0);
			return;
		}

		// Iterate over resultant assets
		Collection<String> ids = r.values("id");
		int n = 0;
		if (ids != null) {
			for (String id : ids) {
				if (replace(executor, id, docType, elementPath, attName, oldValues, newValues)) {
					w.add("asset-id", id);
					n++;
				}
			}
		}
		w.add("number-of-assets", n);
	}

	private boolean replace(ServiceExecutor executor, String id, String docType, String path, String attName,
			Collection<String> oldValues, Collection<String> newValues) throws Throwable {

		// Get asset meta-data
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		XmlDoc.Element r = executor.execute("asset.get", dm.root());
		if (r == null)
			return false;
		XmlDoc.Element r2 = r.element("asset/meta");
		if (r2 == null)
			return false;

		// Get documents of given type
		Collection<XmlDoc.Element> docs = r2.elements(docType);
		if (docs == null)
			return false;

		// Prepare for adding new
		dm = new XmlDocMaker("args");
		dm.add("id", id);
		dm.push("meta", new String[] { "action", "add" });

		// Prepare for removing old
		XmlDocMaker dmR = new XmlDocMaker("args");
		dmR.add("id", id);
		dmR.push("meta", new String[] { "action", "remove" });

		// Convert collection to array for convenient access
		String[] newValuesA = (String[]) newValues.toArray(new String[newValues.size()]);

		// Iterate over documents
		Boolean some = false;
		for (XmlDoc.Element doc : docs) {

			// Get specific element needed and iterate over number of
			// occurrences
			Collection<XmlDoc.Element> els = doc.elements(path);
			if (els != null) {
				for (XmlDoc.Element el : els) {

					// Replace attribute or value
					if (attName != null) {

						// Replace the attribute
						XmlDoc.Attribute att = el.attribute(attName);
						if (att != null) {
							String attValue = att.value();

							// See if we have a new value for this old one.
							// Replace it (by reference) if so
							int i = 0;
							for (String oldValue : oldValues) {
								if (oldValue.equals(attValue)) {
									String newValue = newValuesA[i];
									att.setValue(newValue);
									some = true;
								}
								i++;
							}
						}
					} else {
						// Replace the value
						String value = el.value();

						// See if we have a new value for this old one.
						// Replace it (by reference) if so
						if (value != null) {
							int i = 0;
							for (String oldValue : oldValues) {
								if (oldValue.equals(value)) {
									String newValue = newValuesA[i];
									el.setValue(newValue);
									some = true;
								}
								i++;
							}
						}
					}
				}

				// We found something to replace in this document
				if (some) {

					// Prepare for removal of existing document
					String[] attList = doc.attributeArray();
					if (attList != null && attList.length > 0) {
						dmR.add(docType, attList);
					}

					// Prepare for adding of new
					XmlDoc.Attribute t = doc.attribute("id");
					doc.remove(t); // The new document will get a new id
					dm.add(doc);
				}
			}
		}

		// Pop up stack
		dm.pop();
		dmR.pop();

		if (some) {
			// Remove existing documents (only safe way to replace a document is
			// to remove it first)
			executor.execute("asset.set", dmR.root());

			// Add new documents
			executor.execute("asset.set", dm.root());
		}

		return some;
	}
}
