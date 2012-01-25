package nig.mf.plugin.util;

import java.util.Collection;
import java.util.Iterator;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

/**
 * Class to manipulate generic local (not federated) registry assets. These registry assets can store lists of information such
 * as  project role members, or DICOM servers that have been registered.  They are like dictionaries, but allow a richer 
 * XML structure.
 * 
 * For each registry type, you need to create a document type matching the element type. These document types
 * should be limited to a single element.  In addition, the current implementation requires that the
 * element can have attributes, values and children, but the children cannot themselves have children
 * (will need some recursive XML handling if this is not the case).
 * 
 * @author nebk
 *
 */
public class AssetRegistry {

	private static final String ASSETNAMESPACE = "/pssd";


	/**
	 * Find the local role-member registry asset (singleton).
	 * 
	 * @param executor
	 * @param registryAssetName This is a string which the asset will be named by.  It's up to the caller
	 * to define and manage the name.  Must be unique.  For example, "pssd-role-member", 'pssd-dicom-server"
	 * @return - Asset ID of registry object.  Will be null if does not exist
	 * @throws Throwable
	 */
	public static String findRegistry (ServiceExecutor executor, String registryAssetName) throws Throwable {

		// We only query on the asset name as if all roles have been removed, no Document will
		// be attached to the singleton asset.
		XmlDocMaker dm = new XmlDocMaker("args");
		String query = "name='" + registryAssetName + "'";        
		dm.add("where", query);
		dm.add("pdist", 0);               // Force to local query
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		//
		Collection<XmlDoc.Element> ids = r.elements("id");
		String id = null;
		if (ids==null || ids.size() == 0) {
			// Does not exist
		} else if (ids.size() > 1) {
			// Trouble
			throw new Exception ("There are multiple registry objects of name " + registryAssetName + ". Contact the administrator");
		} else {
			id = r.value("id");
		}
		return id;
	}


	/**
	 * Find the specified (by name) registry asset (singleton); create if does not exist
	 * 
	 * @param executor
	 * @param registryAssetName This is a string which the asset will be named by.  It's up to the caller
	 * to define and manage the name.  Must be unique.  For example, "pssd-role-member", "pssd-dicom-server"
	 * @return - Asset ID of registry object.  Will be null if not created/exists
	 * @throws Throwable
	 */
	public static String findAndCreateRegistry (ServiceExecutor executor, String registryAssetName) throws Throwable {

		// Find if exists
		String id = findRegistry(executor, registryAssetName);

		// Create if needed
		if (id==null) {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm = new XmlDocMaker("args");
			dm.add("namespace", ASSETNAMESPACE);
			dm.add("name", registryAssetName);

			// Put a basic ACL on the objects.  The system:manager needs to have read/write
			// but end users need read
			dm.push("acl");
			dm.add("actor", new String[] {"type", "role"}, "pssd.object.admin");
			dm.add("access", "read-write");
			dm.pop();
			//
			dm.push("acl");
			dm.add("actor", new String[] {"type", "role"}, "pssd.model.user");
			dm.add("access", "read");
			dm.pop();

			// Do it
			XmlDoc.Element r2 = executor.execute("asset.create", dm.root());
			id = r2.value("id");
		}

		// Return id
		return id;
	}


	/**
	 * Find and destroy the specified (by name) asset registry asset.  
	 * 
	 * @param executor
	 * @param registryAssetName This is a string which the asset will be named by.  It's up to the caller
	 * to define and manage the name.  Must be unique.  For example, "role-member", 'dicom-server"
	 * @return - Asset ID of registry object.  Will be null if not created/exists
	 * @throws Throwable
	 */
	public static String  destroyRegistry (ServiceExecutor executor,  String registryAssetName) throws Throwable {

		// Find if exists
		String id = findRegistry(executor, registryAssetName);
		if (id==null) return null;

		// Destroy
		XmlDocMaker dm = new XmlDocMaker("args");
		dm = new XmlDocMaker("args");
		dm.add("id", id);
		executor.execute("asset.destroy", dm.root());
		return id;
	}	

	/**
	 * Add the new item  to the specified registry. 
	 * 
	 * @param id - The asset ID of the registry asset
	 * @param item - The XML item to add- must match the single element specified in the given registry document type 
	 * @param documentType The Document Type  of the specific registry elements you are working with (e.g. pssd-dicom-server-registry)
	 * @throws Throwable
	 */
	public static boolean addItem (ServiceExecutor executor, String id, XmlDoc.Element item, String documentType)  throws Throwable {

		// See if this item is already pre-existing
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		dm.add("pdist",0);                 // Force local 		
		XmlDoc.Element r = executor.execute("asset.get", dm.root());

		// DOn't add if pre-exists
		Collection<XmlDoc.Element> docs = r.elements("asset/meta/"+documentType);
		String docId  = hasItem (executor, docs, item);

		// Add new item
		if (docId==null) {
			dm = new XmlDocMaker("args");
			dm.add("id", id);
			dm.push("meta", new String[] {"action", "add"});
			dm.push(documentType);
			dm.add(item);
			dm.pop();
			dm.pop();
			executor.execute("asset.set", dm.root());
			return true;
		}
		return false;
	}


	/**
	 * Remove the specified item from the registry. 
	 * 
	 * @param id - The asset ID of the registry asset
	 * @param item - The XML item to add- must match the single element specified in the given registry document type 
	 * @param documentType The Document Type  of the specific registry elements you are working with (e.g. pssd-dicom-server-registry
	 * @throws Throwable
	 */
	public static boolean removeItem (ServiceExecutor executor, String id, XmlDoc.Element item, String documentType)  throws Throwable {


		// See if we have this one
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		dm.add("pdist",0);                 // Force local	
		XmlDoc.Element r = executor.execute("asset.get", dm.root());
		if (r==null) return false;

		// Get documents
		Collection<XmlDoc.Element> docs = r.elements("asset/meta/"+documentType);
		if (docs==null) return false;

		// See if pre-exists
		String docId = hasItem (executor, docs, item);

		// Remove if exists
		if (docId!=null) {
			XmlDocMaker dm2 = new XmlDocMaker("args");
			dm2.add("id", id);
			dm2.push("meta", new String[] {"action", "remove"});
			dm2.push(documentType, new String[]{"id", docId});
			dm2.pop();
			dm2.pop();
			executor.execute("asset.set", dm2.root());
			return true;
		}
		return false;
	}	

	/**
	 * List the contents of the registry in the writer
	 * 
	 * @param executor
	 * @param id - Asset ID of registry asset
	 * @param the Xpath to iterate over. E.g. asset/meta/pssd-role-member-registry/role
	 * @param w
	 * @throws Throwable
	 */
	static public void list (ServiceExecutor executor, String id, String xpath, XmlWriter w) throws Throwable {

		// Get the registry asset
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		dm.add("pdist",0);                 // Force local 	
		XmlDoc.Element r = executor.execute("asset.get", dm.root());

		// Iterate over given Xpath
		Collection<XmlDoc.Element> elements = r.elements(xpath);
		if (elements!= null) {
			for (XmlDoc.Element el : elements) {
				w.add(el);
			}
		}
	}



	/**
	 * Find out if the specified item  is a member of the registry
	 * 
	 * @param executor
	 * @param registryAssetName This is a string which the asset will be named by.  It's up to the caller
	 * to define and manage the name.  Must be unique.  For example, "role-member", 'dicom-server"
	 * @param documentType The Document Type  of the specific registry elements you are working with (e.g. pssd-dicom-server-registry)
	 * @param item The item to test
	 * @return
	 * @throws Throwable
	 */
	public static boolean isInRegistry (ServiceExecutor executor,  String registryAssetName, String documentType, XmlDoc.Element item) throws Throwable {

		// Find if exists
		String id = findRegistry(executor, registryAssetName);
		if (id==null) return false;

		// Get the registry asset
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		dm.add("pdist",0);                 // Force local 	
		XmlDoc.Element r = executor.execute("asset.get", dm.root());

		// Find collection and check
		Collection<XmlDoc.Element> docs = r.elements("asset/meta/"+documentType);
		return hasItem (executor, docs, item) != null;
	}


	//
	/**
	 * Looks to see if the given item is already existing as an element of the provided documents
	 * Premised on depth 2 only (too lazy to write fully recursive function)
	 *  XmlDoc.Element with atts and value. Will throw an exception if
	 * this is not the case.
	 * 
	 * E.g.
	 * ae -name
	 *    host
	 *    port
	 *    aet
	 *    
	 *  is as deep as you can go
	 *  
	 */
	static private String hasItem (ServiceExecutor executor, Collection<XmlDoc.Element> docs,  XmlDoc.Element item) throws Throwable {

		if (docs==null) return null;

		// Iterate over documents
		for (XmlDoc.Element doc : docs) {

			// Find all elements. Should be one per document...
			Collection<XmlDoc.Element> elements = doc.elements();  

			for (XmlDoc.Element el : elements)	 {

				// Compare value and attributes at this top level
				boolean match = compareElements (el, item, false);

				// Proceed to the children (one level only)
				if (match) {
					Collection<XmlDoc.Element> children = el.elements();
					Collection<XmlDoc.Element> itemChildren = item.elements();
					if (children==null && itemChildren==null) {
						return doc.value("@id");   // A match
					}
					//
					if (children!=null && itemChildren!=null) {
						if (children.size() == itemChildren.size()) {

							// Compare children
							boolean childMatch = true;
							Iterator<XmlDoc.Element> itemChildIt = itemChildren.iterator();
							for (XmlDoc.Element child : children) {
								XmlDoc.Element itemChild = itemChildIt.next();
								//
								if (child.elements()!=null) {
									// I am too lazy for now to implement a recursive function
									throw new Exception ("The element " + child + " has children and the hasItem function cannot handle this; needs recursive enhancement");
								}
								if (itemChild.elements()!=null) {
									// I am too lazy for now to implement a recursive function
									throw new Exception ("The element " + itemChild + " has children and the hasItem function cannot handle this; needs recursive enhancement");
								}
								//
								if (!compareElements (child, itemChild, true)) {
									childMatch = false;
									break;
								}	
							}
							if (childMatch) return  doc.value("@id");
						}
					}
				}
			}
		}
		return null;
	}


	/**
	 * It is required that both elements are non-null
	 * 
	 * @param oldEl
	 * @param newEl
	 * @return
	 */
	private static boolean compareElements (XmlDoc.Element oldEl, XmlDoc.Element newEl, boolean checkName) throws Throwable {
		if (oldEl==null || newEl==null) throw new Exception("Comparison elements both null");

		// Compare names
		// We don't always want to compare the parent name of the element.  In some circumstances
		// the parent names are different (e.g. role members in the registry have
		// a parent 'role' but when compared with the existing members the parent is 'member'
		if (checkName) {
			String oldName = oldEl.name();
			String newName = newEl.name();
			if (!newName.equals(oldName)) return false;
		}

		// Compare value and attributes
		String[] oldAtt = oldEl.attributeArray();
		String oldVal = oldEl.value();
		//
		String[] newAtt = newEl.attributeArray();
		String newVal = newEl.value();

		// See if values match; handle case of no value
		boolean valuesMatch = (newVal!=null && oldVal!=null && newVal.equals(oldVal)) ||
		(newVal==null && oldVal==null);
		if (!valuesMatch) return false;

		if (newAtt!=null && oldAtt!=null && newAtt.length==oldAtt.length) {
			for (int i =0; i<newAtt.length; i++) {
				if (!oldAtt[i].equals(newAtt[i])) return false;
			}
			return true;
		} else if (newAtt==null && oldAtt==null) {
			// Neither having attributes is a match
			return true;
		} else {
			return false;
		}
	}
}
