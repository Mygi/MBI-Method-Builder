package nig.mf.plugin;

import java.util.Vector;

import nig.mf.plugin.util.AssetUtil;
import nig.mf.plugin.util.XMLUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

/**
 * Service to match and replace the value of metadata elements.  Operates only on meta-data
 * under the parent "meta". Matching can be exact or contains.
 * 
 * Service could be enhanced to 
 *    1) take multiple pairs of old/new
 *    2) operate on attributes as well
 * 
 * 
 * @author Neil Killeen
 * 
 */
public class SvcAssetMetaStringReplace extends PluginService {
	private Interface _defn;

	public SvcAssetMetaStringReplace() {
		_defn = new Interface();
		_defn.add(new Element("id", AssetType.DEFAULT, "Asset ID (give only id or cid)", 0, 1));
		_defn.add(new Element("cid", CiteableIdType.DEFAULT, "Asset CID (give only id or cid)", 0, 1));
		_defn.add(new Element("old", StringType.DEFAULT, "The old value of the string to replace.", 1, 1));
		_defn.add(new Element("new", StringType.DEFAULT, "The new value of the string.", 1, 1));
		_defn.add(new Element("exact", BooleanType.DEFAULT, "Exact match for the String (true), or contains the String (false) and replaces just the contained section.", 0,1));
	
	}

	public String name() {
		return "nig.asset.doc.string.replace";
	}

	public String description() {
		return "Replace all ocurrences of a given String within all (except system generated) element values (not attributes) of documents held under the 'meta' parent element of a local asset.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// Get the ID of the specified object
		String id = args.stringValue("id");
		String cid = args.stringValue("cid");
		if (id==null && cid==null) {
			throw new Exception("You must specify either the id or cid");
		}
		if (id!=null && cid!=null) {
			throw new Exception("You cannot specify both the id and cid");
		}
		if (cid!=null) id = AssetUtil.getId(executor(), cid);
		//
		String oldValue = args.value("old");
		String newValue = args.value("new");
		//
		Boolean exact = args.booleanValue("exact", false);

		// Get the asset meta-data for the give Document Type
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		dm.add("pdist", 0);        // Force local
		XmlDoc.Element r = executor().execute("asset.get", dm.root());
		XmlDoc.Element doc = r.element("asset/meta");
		if (doc==null) return;
        
		// Generate new metadata
		dm = new XmlDocMaker("args");
		dm.add("id", id);
		dm.push("meta", new String[]{"action", "merge"});

		// There are some that are system generated and cannot be changed. 
		// Filter these out assuming they are all top-level under "meta"
		Vector<XmlDoc.Element>els = doc.elements(); 
		boolean replace = false;
		for (XmlDoc.Element el : els) {
			if (keepElement(el)) {
				if (XMLUtil.replaceString (el, oldValue, newValue, exact)) {
					dm.add(el);
					replace = true;
				}
			}
		}
		dm.pop();
		if (replace) executor().execute("asset.set", dm.root());
	}

	
	/**
	 * We are not allowed to use asset.set on certain system generated
	 * documents.  Filter these out.
	 * 
	 * @param el The element of interest
	 * @return
	 */
	private boolean keepElement (XmlDoc.Element el) {
		String[] dropNames = {"mf-revision-history"};
		//
		for (int i=0; i<dropNames.length; i++) {
			if (el.name().equals(dropNames[i])) return false;
		}
		return true;
	}
}
