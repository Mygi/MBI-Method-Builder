package nig.mf.plugin.pssd.services;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.federation.Session;
import nig.mf.pssd.CiteableIdUtil;
import nig.mf.pssd.plugin.util.DistributedQuery;
import nig.mf.pssd.plugin.util.DistributedQuery.ResultAssetType;
import nig.mf.pssd.plugin.util.DistributedQuery.ResultFilterPolicy;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.LongType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcCollectionMemberList extends PluginService {
	private Interface _defn;

	public SvcCollectionMemberList() {

		_defn = new Interface();
		_defn.add(new Interface.Element("id", CiteableIdType.DEFAULT,
				"The identity of the (parent) pssd object. If not specified, then returns the root level objects.", 0,
				1));
		_defn.add(new Interface.Element("idx", LongType.POSITIVE_ONE, "Cursor position. Defaults to 1", 0, 1));
		_defn.add(new Interface.Element("size", IntegerType.POSITIVE_ONE, "Cursor size. Defaults to infinity.", 0, 1));
		_defn.add(new Interface.Element("isleaf", BooleanType.DEFAULT,
				"Identify whether each node is a leaf. Defaults to false.", 0, 1));
		_defn.add(new Interface.Element(
				"pdist",
				IntegerType.DEFAULT,
				"Specifies the peer distance for a distributed query. Defaults to infinity in a federated session.  Set to 0 for local only or infinity for all peers (regardless of whether session is federated or not).",
				0, 1));
		_defn.add(new Interface.Element("asset-type", new EnumType(DistributedQuery.ResultAssetType.stringValues()),
				"Specify type of asset to find. Defaults to all.", 0, 1));
		_defn.add(new Interface.Element("filter-policy", new EnumType(new String[] {
				DistributedQuery.ResultFilterPolicy.none.toString(),
				DistributedQuery.ResultFilterPolicy.primary_then_any_replica.toString() }),
				"The policy for filtering collections. Defaults to primary-then-replica", 0, 1));
		_defn.add(new Interface.Element("sort", BooleanType.DEFAULT,
				"Sort into ascending order by last child of CID (default is false)", 0, 1));

	}

	public String name() {

		return "om.pssd.collection.member.list";
	}

	public String description() {

		return "Returns the list of members of the given PSSD object. The object id and name are listed but the detail about the object are not included. In a federated session, members will be found across all peers.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		long t1 = (new Date()).getTime();

		String id = args.stringValue("id");
		boolean isStudy = id == null ? false : CiteableIdUtil.isStudyId(id);
		int idx = args.intValue("idx", 1);
		String size = args.stringValue("size", "infinity");
		boolean isleaf = args.booleanValue("isleaf", false);
		String pdist = args.value("pdist");
		DistributedQuery.ResultAssetType assetType = DistributedQuery.ResultAssetType.instantiate(args.stringValue(
				"asset-type", DistributedQuery.ResultAssetType.all.toString()));
		DistributedQuery.ResultFilterPolicy filterPolicy = DistributedQuery.ResultFilterPolicy.instantiate(args
				.stringValue("filter-policy", ResultFilterPolicy.primary_then_any_replica.toString()));
		boolean sort = args.booleanValue("sort", false);

		String where = id == null ? "xpath(pssd-object/type)='project'" : "cid in '" + id + "'";
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("idx", idx);
		dm.add("size", size);
		if (pdist != null) {
			dm.add("pdist", pdist);
		}

		boolean federated = Session.isFederated(executor());
		if (federated) {
			if (assetType == ResultAssetType.primary) {
				where += "and (rid hasno value) ";
			} else if (assetType == ResultAssetType.replica) {
				where += "and (rid has value)";
			}
			// TODO: change to get-value. Because currently the distributed
			// query failed to return result if action is get-value. Jason will
			// look at it.
			dm.add("action", "get-meta");
		} else {
			dm.add("xpath", new String[] { "ename", "type" }, "meta/pssd-object/type");
			dm.add("xpath", new String[] { "ename", "cid" }, "cid");
			dm.add("xpath", new String[] { "ename", "name" }, "meta/pssd-object/name");
			if (isStudy) {
				// Dataset needs source/type to identify itself as
				// primary/derivation
				dm.add("xpath", new String[] { "ename", "source-type" }, "meta/pssd-dataset/type");
			}
			dm.add("action", "get-value");
		}
		dm.add("where", where);

		XmlDoc.Element re = executor().execute("asset.query", dm.root());

		if (re == null) {
			return;
		}
		List<XmlDoc.Element> aes = re.elements("asset");
		if (aes == null) {
			return;
		}
		if (aes.size() == 0) {
			return;
		}

		// Sort into ascending order is desored
		if (sort) {
			CidComparator ct = new CidComparator();
			Collections.sort(aes, ct);
		}
		//
		if (federated) {
			switch (filterPolicy) {
			case primary_then_any_replica:
				HashMap<String, Boolean> map = new HashMap<String, Boolean>();
				for (XmlDoc.Element ae : aes) {
					String cid = ae.value("cid");
					String rid = ae.value("rid");
					boolean isPrimary = rid == null;
					if (isPrimary) {
						map.put(cid, true);
					} else {
						Boolean v = map.get(cid);
						if (v == null) {
							map.put(cid, false);
						}
					}
				}
				for (XmlDoc.Element ae : aes) {
					String cid = ae.value("cid");
					String rid = ae.value("rid");
					if (rid == null || map.get(cid) == false) {
						String assetId = ae.value("@id");
						String proute = ae.value("@proute");
						String type = ae.value("meta/psst-object/type");
						String name = ae.value("meta/psst-object/name");
						String sourceType = isStudy ? ae.value("meta/pssd-dataset/type") : null;
						write(w, proute, cid, assetId, type, name, sourceType, isleaf);
					}
				}
				break;
			case none:
				for (XmlDoc.Element ae : aes) {
					String cid = ae.value("cid");
					String assetId = ae.value("@id");
					String proute = ae.value("@proute");
					String type = ae.value("meta/psst-object/type");
					String name = ae.value("meta/psst-object/name");
					String sourceType = isStudy ? ae.value("meta/pssd-dataset/type") : null;
					write(w, proute, cid, assetId, type, name, sourceType, isleaf);
				}
				break;
			default:
				throw new Exception("Filter policy: " + filterPolicy.toString() + " is not supported.");
			}
		} else {
			for (XmlDoc.Element ae : aes) {
				String cid = ae.value("cid");
				String assetId = ae.value("@id");
				String proute = ae.value("@proute");
				String type = ae.value("type");
				String name = ae.value("name");
				String sourceType = isStudy ? ae.value("source-type") : null;
				write(w, proute, cid, assetId, type, name, sourceType, isleaf);
			}
		}

		long t2 = (new Date()).getTime();
		System.out.println("Query & Filter & Format: " + (t2 - t1) / 1000);
	}

	private void write(XmlWriter w, String proute, String cid, String assetId, String type, String name,
			String sourceType, boolean checkIsLeaf) throws Throwable {

		w.push("object", new String[] { "type", type });
		if (proute == null) {
			w.add("id", new String[] { "asset", assetId }, cid);
		} else {
			w.add("id", new String[] { "asset", assetId, "proute", proute }, cid);
		}
		if (name != null) {
			w.add("name", name);
		}
		if (sourceType != null) {
			w.push("source");
			w.add("type", sourceType);
			w.pop();
		}
		if (checkIsLeaf) {
			w.add("isleaf", PSSDObject.isLeaf(executor(), cid));
		}
		w.pop();
	}

	/**
	 * This is a bit simple. It just looks at the child cid only So it will not
	 * do the right thing for collections which mix server roots and project
	 * namespaces
	 * 
	 * @author nebk
	 * 
	 */
	public class CidComparator implements Comparator<XmlDoc.Element> {
		public CidComparator() {

			;
		};

		public int compare(XmlDoc.Element a, XmlDoc.Element b) {

			try {
				return compare(a.value("cid"), b.value("cid"));
			} catch (Throwable t) {
				// What to do in here, I don't know
				// I'm not able to throw an exception because the interface
				// doesn't allow it
				System.out.println("error=" + t.getMessage());
				return 0;
			}
		}

		// From Wilson (see daris.client.model.IDUtil)
		// For some reason when I put this in Commons in a static function
		// I get a run-time exception...
		public int compare(String id1, String id2) {

			assert id1 != null && id2 != null;
			if (id1.equals(id2)) {
				return 0;
			}
			String[] parts1 = id1.split("\\.");
			String[] parts2 = id2.split("\\.");
			if (parts1.length < parts2.length) {
				return -1;
			}
			if (parts1.length > parts2.length) {
				return 1;
			}
			for (int i = 0; i < parts1.length; i++) {
				if (!parts1[i].equals(parts2[i])) {
					long n1 = Long.parseLong(parts1[i]);
					long n2 = Long.parseLong(parts2[i]);
					if (n1 < n2) {
						return -1;
					}
					if (n1 > n2) {
						return 1;
					}
				}
			}
			return 0;
		}
	}

}
