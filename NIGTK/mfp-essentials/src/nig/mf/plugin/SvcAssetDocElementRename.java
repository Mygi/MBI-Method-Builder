package nig.mf.plugin;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

/**
 * Specialised function to rename an element in a document type. Both elements must exist. Remove the old one after
 * finishing all renaming.
 * 
 * This service could be extended to operate across Document Types and/or assets
 * 
 * @author Neil Killeen
 * 
 */
public class SvcAssetDocElementRename extends PluginService {
	private Interface _defn;

	public SvcAssetDocElementRename() {

		_defn = new Interface();
		_defn.add(new Element("id", AssetType.DEFAULT, "Asset ID", 1, 1));
		_defn.add(new Element("doc", StringType.DEFAULT, "The Document Type name.", 1, 1));
		_defn.add(new Element("old", StringType.DEFAULT, "The old element name.", 1, 1));
		_defn.add(new Element("new", StringType.DEFAULT, "The new element name.", 1, 1));

	}

	public String name() {
		return "nig.asset.doc.element.rename";
	}

	public String description() {
		return "Specialized service to rename an element in a Document attached to a local asset. Both element names must exist in the Document.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// Get the ID of the specified Subject if any
		String id = args.value("id");
		String docType = args.value("doc");
		String elNameOld = args.value("old");
		String elNameNew = args.value("new");

		// Get the asset meta-data for the give Document Type
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", id);
		doc.add("pdist", 0);        // Force local
		XmlDoc.Element r = executor().execute("asset.get", doc.root());
		XmlDoc.Element docIn = r.element("asset/meta/" + docType);
		String nameSpace = docIn.attribute("ns").value();

		// Modify meta if exists
		if (docIn != null) {

			// Remove old document (replace will only replace 'matching' elements which is dumb...)
			doc = new XmlDocMaker("args");
			doc.add("id", id);
			doc.push("meta", new String[] { "action", "remove" });
			doc.add(docType, new String[] { "ns", nameSpace });
			doc.pop();
			r = executor().execute("asset.set", doc.root());

			// Rename element in document
			docIn.renameElement(elNameOld, elNameNew);

			// Add new document (name space is embodied in docIn)
			doc = new XmlDocMaker("args");
			doc.add("id", id);
			doc.push("meta", new String[] { "action", "add" });
			doc.add(docIn);
			doc.pop();
			r = executor().execute("asset.set", doc.root());
		}

	}
}
