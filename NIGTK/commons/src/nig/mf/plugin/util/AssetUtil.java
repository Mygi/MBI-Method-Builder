package nig.mf.plugin.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collection;

import nig.mf.pssd.plugin.util.CiteableIdUtil;

import org.apache.commons.compress.utils.IOUtils;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

public class AssetUtil {

	/**
	 * Gets the asset content from a local asset and save it into the specified
	 * file.
	 * 
	 * @param executor
	 * @param id
	 *            Asset id
	 * @param dstDir
	 * @return
	 * @throws Throwable
	 */
	public static File getContent(ServiceExecutor executor, String id,
			File dstDir) throws Throwable {

		PluginService.Outputs sos = new PluginService.Outputs(1);
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", id);
		doc.add("pdist", 0); // Force local
		XmlDoc.Element r = executor.execute("asset.get", doc.root(), null, sos);

		if (r.element("asset/content") == null) {
			return null;
		}

		String ext = r.value("asset/content/type/@ext");
		if (!dstDir.exists()) {
			throw new FileNotFoundException("The dst directory "
					+ dstDir.getAbsolutePath() + " is not found.");
		}
		File file = new File(dstDir, id + "." + ext);

		PluginService.Output so = sos.output(0);
		InputStream is = so.stream();
		FileOutputStream os = new FileOutputStream(file);
		try {
			IOUtils.copy(is, os);
		} finally {
			os.close();
			is.close();
		}

		return file;

	}

	/**
	 * Gets the asset content from a local asset into a stream
	 * 
	 * @param executor
	 * @param id
	 *            Asset id
	 * @return
	 * @throws Throwable
	 */
	public static InputStream getContentInStream(ServiceExecutor executor,
			String id) throws Throwable {

		PluginService.Outputs sos = new PluginService.Outputs(1);
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", id);
		doc.add("pdist", 0); // Force local
		XmlDoc.Element r = executor.execute("asset.get", doc.root(), null, sos);
		if (r.element("asset/content") == null)
			return null;
		//
		PluginService.Output so = sos.output(0);
		return so.stream();
	}

	/**
	 * Get the sum of the content size recursively for this local citable ID.
	 * 
	 * @param executor
	 * @param id
	 *            citable ID of interest
	 * @return XmlDoc.Element as returned by asset.query :action sum :xpath
	 *         content/size
	 * @throws throwable
	 */
	public static XmlDoc.Element contentSizeSum(ServiceExecutor executor,
			String id) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");

		//
		String query = "(cid starts with '" + id + "' or cid = '" + id + "')";
		dm.add("where", query);
		dm.add("action", "sum");
		dm.add("xpath", "content/size");
		dm.add("pdist", 0); // Force local
		return executor.execute("asset.query", dm.root());
	}

	/**
	 * Returns the asset citeable id for this local id
	 * 
	 * @param id
	 * @param executor
	 * @return
	 * @throws Throwable
	 */
	public static String getCid(ServiceExecutor executor, String id)
	throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", id);
		doc.add("pdist", 0); // Force local
		XmlDoc.Element r = executor
		.execute("asset.get", doc.root());
		return r.value("asset/cid");

	}

	/**
	 * Returns the asset id for this local CID
	 * 
	 * @param cid
	 * @param executor
	 * @return
	 * @throws Throwable
	 */
	public static String getId(ServiceExecutor executor, String cid)
	throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("cid", cid);
		doc.add("pdist", 0); // Force local
		XmlDoc.Element r = executor
		.execute("asset.get", doc.root(), null, null);
		return r.value("asset/@id");

	}

	public static String getModel(ServiceExecutor executor, String id,
			boolean citeable) throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		if (citeable) {
			doc.add("cid", id);
		} else {
			doc.add("id", id);
		}
		doc.add("pdist", 0); // Force local
		XmlDoc.Element r = executor
		.execute("asset.get", doc.root(), null, null);
		return r.value("asset/model");

	}

	/**
	 * Change the parent of the local asset (and its descendants).
	 * 
	 * @param cid
	 *            The citeable id of the asset.
	 * @param parent
	 *            The new citeable id of the new parent asset.
	 * @param recursive
	 * @return
	 * @throws Throwable
	 */
	public static String changeParent(ServiceExecutor executor, String cid,
			String pid, boolean recursive) throws Throwable {

		String proute = null;  // local

		String id = getCid(executor, cid);

		if (CiteableIdUtil.getIdDepth(pid) + 1 != CiteableIdUtil
				.getIdDepth(cid)) {
			throw new Exception("Citeable id depth/length do not match. (cid="
					+ cid + " pid=" + pid + ")");
		}

		String newCid = pid + "." + CiteableIdUtil.getLastSection(cid);
		if (exists(executor, newCid, true)) {
			newCid = CiteableIdUtil.createCid(executor, pid);
		} else {
			if (!nig.mf.pssd.plugin.util.CiteableIdUtil.cidExists(proute, executor, newCid)) {
				CiteableIdUtil.importCid(executor, newCid, 1);
			}
		}

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		dm.add("cid", newCid);
		executor.execute("asset.cid.set", dm.root(), null, null);

		if (recursive) {
			dm = new XmlDocMaker("args");
			dm.add("where", "cid in " + "'" + cid + "'");
			dm.add("size", "infinity");
			dm.add("action", "get-cid");
			dm.add("pdist", 0); // Force local
			XmlDoc.Element r = executor.execute("asset.query", dm.root());
			Collection<String> ccids = r.values("cid");
			if (ccids != null) {
				for (String ccid : ccids) {
					changeParent(executor, ccid, newCid, recursive);
				}
			}
		}

		return newCid;

	}

	public static boolean exists(ServiceExecutor executor, String id,
			boolean citeable) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		if (citeable) {
			dm.add("cid", id);
		} else {
			dm.add("id", id);
		}
		XmlDoc.Element r = executor.execute("asset.exists", dm.root());
		String exists = r.value("exists");
		if (exists != null) {
			if (exists.equals("true")) {
				return true;
			}
		}
		return false;

	}

	public static String getNamespace(ServiceExecutor executor, String id,
			String proute) throws Throwable {

		ServerRoute sr = proute == null ? null : new ServerRoute(proute);
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		XmlDoc.Element r = executor.execute(sr, "asset.get", dm.root());
		return r.value("asset/namespace");
	}

	public static boolean hasContent(ServiceExecutor executor, String id,
			String proute) throws Throwable {

		ServerRoute sr = proute == null ? null : new ServerRoute(proute);
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		XmlDoc.Element r = executor.execute(sr, "asset.get", dm.root());
		if (r.element("asset/content") != null) {
			return true;
		}
		return false;
	}


	/**
	 * Copy documents from one asset to another. All attributes are transferred except 'id' which is
	 * generated afresh in the new asset.
	 * 
	 * @param executor
	 * @param docTypes  The names of the documents; i.e. the document types
	 * @param cidIn
	 * @param cidOut
	 * @throws Throwable
	 */
	public static void copyMetaData (ServiceExecutor executor, Collection<String> docTypes, String cidIn, String cidOut) throws Throwable {

		// Get meta-data in input object
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", cidIn);
		XmlDoc.Element r = executor.execute("asset.get", dm.root());
		if (r==null) return;

		// Iterate through given document types
		for (String docType : docTypes) {

			// If we have this document type, copy it
			XmlDoc.Element docIn = r.element("asset/meta/"+docType);
			if (docIn!=null) {

				// We don't want the 'id' attribute. We want all other attributes
				// such as namespace and tag
				removeAttribute(executor, docIn, "id");

				// Do it
				XmlDocMaker dm2 = new XmlDocMaker("args");
				dm2.add("cid", cidOut);
				dm2.push("meta", new String[]{"action", "add"});
				dm2.add(docIn);
				dm2.pop();	
				executor.execute("asset.set", dm2.root());
			}
		}			
	}
	
	
	/**
	 * Get's the asset meta-data
	 * 
	 * @param executor
	 * @param cid Citable ID
	 * @param id Asset ID (give one of id or cid)
	 * @throws Throwable
	 */
	public static XmlDoc.Element getAsset (ServiceExecutor executor, String cid, String id) throws Throwable {

		// Get meta-data in input object
		XmlDocMaker dm = new XmlDocMaker("args");
		if (cid!=null) {
			dm.add("cid", cid);
		} else if (id!=null) {
			dm.add("id", id);
		} else {
			throw new Exception("One of cid or id must be given");
		}
		
		return executor.execute("asset.get", dm.root());

	}
	
	
	
	
	/**
	 * Remove the given document (i.e. a full instantiation of a document type) from the given asset.
	 * 
	 * @param executor
	 * @param cid asset CID (give cid or id)
	 * @param id asset ID
	 * @param doc 
	 * @throws Throwable
	 */
	public static void removeDocument (ServiceExecutor executor, String cid, String id, XmlDoc.Element doc) throws Throwable {
		

		String[] attString = doc.attributeArray();
		
		// Remove
		XmlDocMaker dm = new XmlDocMaker("args");
		if (cid!=null) {
			dm.add("cid", cid);
		} else {
			if (id!=null) dm.add("id", id);
		}
		dm.push("meta", new String[]{"action", "remove"});
		dm.push(doc.name(), attString);  
		dm.pop();
		dm.pop();
		//
		executor.execute("asset.set", dm.root());
	}
	
	/**
	 * Remove the named atrtibute from the document
	 * 
	 * @param executor
	 * @param doc
	 * @param attributeName
	 * @throws Throwable
	 */
	public static void removeAttribute (ServiceExecutor executor, XmlDoc.Element doc, String attributeName) throws Throwable {
		XmlDoc.Attribute attr = doc.attribute(attributeName);
		if (attr != null) {
			doc.remove(attr);
		}

	}
}
