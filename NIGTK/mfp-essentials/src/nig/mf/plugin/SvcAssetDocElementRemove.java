package nig.mf.plugin;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

/**
 * Specialised function to remove an element in a document.
 * 
 * @author Neil Killeen
 * 
 */
public class SvcAssetDocElementRemove extends PluginService {
	private Interface _defn;

	public SvcAssetDocElementRemove() {

		_defn = new Interface();
		_defn.add(new Element("id", AssetType.DEFAULT, "Asset ID", 1, 1));
		_defn.add(new Element("doc", StringType.DEFAULT, "The Document Type name.", 1, 1));
		_defn.add(new Element("element", StringType.DEFAULT, "The meta-data element name to remove.", 1, 1));

	}

	public String name() {
		return "nig.asset.doc.element.remove";
	}

	public String description() {
		return "Specialized service to remove an element in a Document from a local asset. .";
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
		String elName = args.value("element");

		// Get the asset meta-data for the given Document Type
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", id);
		doc.add("pdist", 0);        // Force local
		XmlDoc.Element r = executor().execute("asset.get", doc.root());
		XmlDoc.Element docIn = r.element("asset/meta/" + docType);

		// If the Document exists, modify it
		if (docIn != null) {

			// Find the desired element in the Document and remove it
			XmlDoc.Element elIn = docIn.element(elName);
			if (elIn != null) {
				docIn.remove(elIn);

				// Replace the Document
				doc = new XmlDocMaker("args");
				doc.add("id", id);
				doc.push("meta", new String[] { "action", "replace" });
				doc.add(docIn);
				doc.pop();
				r = executor().execute("asset.set", doc.root());
			}
		}
	}
}
