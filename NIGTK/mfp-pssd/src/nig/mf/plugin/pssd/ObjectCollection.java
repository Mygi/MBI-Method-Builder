package nig.mf.plugin.pssd;

import java.util.List;

import nig.mf.pssd.CiteableIdUtil;
import nig.mf.pssd.plugin.util.DistributedQuery;
import nig.mf.pssd.plugin.util.DistributedQuery.ResultAssetType;
import nig.mf.pssd.plugin.util.DistributedQuery.ResultFilterPolicy;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class ObjectCollection {
	/**
	 * 
	 * @param executor
	 * @param id
	 * @param filterText
	 * @param pdist
	 * @param assetType
	 * @param filterPolicy
	 * @param size
	 * @param idx
	 * @param foredit
	 * @param brief
	 * @param w
	 * @throws Throwable
	 */
	public static void members(ServiceExecutor executor, String id, String filterText, int pdist,
			DistributedQuery.ResultAssetType assetType, DistributedQuery.ResultFilterPolicy filterPolicy, int size,
			int idx, boolean foredit, boolean brief, XmlWriter w) throws Throwable {

		/*
		 * compose the query string
		 */
		String where = (id == null ? "( xpath(pssd-object/type)='project' )" : "( cid in '" + id + "' )");
		where += (filterText == null ? "" : " and ( text contains '" + filterText + "')");
		switch (assetType) {
		case primary:
			where += " and ( rid hasno value )";
			break;
		case replica:
			where += " and ( rid has value )";
			break;
		default:
			break;
		}

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("count", true);
		dm.add("pdist", pdist);
		dm.add("size", size);
		dm.add("idx", idx);
		dm.add("where", where);
		dm.add("action", foredit ? "get-template-meta" : (brief ? "get-value" : "get-meta"));

		if (pdist == 0) {
			// local query
			if (filterPolicy != ResultFilterPolicy.none) {
				throw new Exception("Result filter policy: " + filterPolicy.toString()
						+ " is not supported in local query.");
			}
			if (brief) {
				dm.add("xpath", new String[] { "ename", "type" }, "meta/pssd-object/type");
				dm.add("xpath", new String[] { "ename", "cid" }, "cid");
				dm.add("xpath", new String[] { "ename", "name" }, "meta/pssd-object/name");
				if (CiteableIdUtil.isStudyId(id)) {
					// Dataset needs source/type to identify itself as
					// primary/derivation
					dm.add("xpath", new String[] { "ename", "source-type" }, "meta/pssd-dataset/type");
				}
			}
		} else {
			// distributed query
			if (assetType == ResultAssetType.all) {
				throw new Exception("Mediaflux BUG: distributed query cannot return both primary and replicas.");
			}
			if (filterPolicy != ResultFilterPolicy.primary_then_any_replica) {
				throw new Exception("Result filter policy: " + filterPolicy.toString()
						+ " is not supported in local query.");
			}
		}

		
		/*
		 * do query
		 */
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		List<XmlDoc.Element> aes = r.elements("asset");
		
		/*
		 * format results
		 */
		if (aes != null) {
			if (pdist == 0) {
//				listMembers(w, aes);
			} else {
				if (filterPolicy == ResultFilterPolicy.primary_then_any_replica) {
					// filter()
				}
//				describeMembers(w, aes, foredit, brief);
			}
		}
		/*
		 * cursor element
		 */
		XmlDoc.Element cursor = r.element("cursor");
		if (cursor != null) {
			w.add(cursor, true);
		}
	}
}
