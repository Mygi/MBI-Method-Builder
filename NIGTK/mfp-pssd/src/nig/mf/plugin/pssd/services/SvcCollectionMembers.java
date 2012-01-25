package nig.mf.plugin.pssd.services;

import java.util.Date;

import nig.mf.pssd.plugin.util.DistributedAssetUtil;
import nig.mf.pssd.plugin.util.DistributedQuery;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.LongType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcCollectionMembers extends PluginService {
	private Interface _defn;

	public SvcCollectionMembers() {

		_defn = new Interface();
		_defn.add(new Interface.Element("id", CiteableIdType.DEFAULT,
				"The identity of the (parent) pssd object. If not specified, then returns the root level objects.", 0,
				1));
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
		_defn.add(new Interface.Element("text", StringType.DEFAULT, "Arbitrary search text for free text query.", 0, 1));
		_defn.add(new Interface.Element("idx", LongType.POSITIVE_ONE, "Cursor position. Defaults to 1", 0, 1));
		_defn.add(new Interface.Element("size", IntegerType.POSITIVE_ONE, "Cursor size. Defaults to 100", 0, 1));
		_defn.add(new Interface.Element("isleaf", BooleanType.DEFAULT,
				"Identify whether each node is a leaf. Defaults to false.", 0, 1));
		_defn.add(new Interface.Element(
				"foredit",
				BooleanType.DEFAULT,
				"Indicates whether the object may be edited. If true, then a description of the structure of the data is returned. Defaults to 'false'.",
				0, 1));
	}

	public String name() {

		return "om.pssd.collection.members";
	}

	public String description() {

		return "Returns the members of the given PSSD object. In a federated session, members will be found across all peers.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		long t1 = (new Date()).getTime();

		String pid = args.stringValue("id");
		String pdist = args.stringValue("pdist");
		//
		String atype = args.stringValue("asset-type", "all");
		DistributedQuery.ResultAssetType assetType = DistributedQuery.ResultAssetType.instantiate(atype);
		String policy = args.stringValue("filter-policy",
				DistributedQuery.ResultFilterPolicy.primary_then_any_replica.toString());
		DistributedQuery.ResultFilterPolicy filterPolicy = DistributedQuery.ResultFilterPolicy.instantiate(policy);
		//
		String text = args.value("text");
		boolean isleaf = args.booleanValue("isleaf", false);
		boolean forEdit = args.booleanValue("foredit", false);
		int size = args.intValue("size", 100);

		// Prepare query
		String query;
		if (pid == null) {
			query = "xpath(pssd-object/type)='project'";
			// cid in named id '" + CiteableIdUtil.PROJECT_CID_ROOT_NAME + "'";
		} else {
			query = "cid in '" + pid + "'";
		}

		if (text != null) {
			query += " and text contains '" + text + "'";
		}

		XmlDocMaker dm = new XmlDocMaker("args");
		if (forEdit) {
			dm.add("action", "get-template-meta");
		} else {
			dm.add("action", "get-meta");
		}

		dm.add("idx", args.longValue("idx", 1));
		dm.add("size", size);
		if (pdist != null) {
			dm.add("pdist", pdist);
		}

		dm.add("get-related-meta", "true");
		dm.add("related-type", "attachment");

		// Do distributed query, handling asset type and filter policy

		XmlDoc.Element r = DistributedAssetUtil.queryAndFilter(executor(), assetType, filterPolicy, query, dm);
		long t2 = (new Date()).getTime();
		System.out.println("Query & Filter: " + (t2 - t1) / 1000);
		// Reformat for PSSD
		SvcObjectFind.addPssdObjects(executor(), w, r, isleaf, forEdit);
		long t3 = (new Date()).getTime();
		System.out.println("Format: " + (t3 - t2) / 1000);
	}
}
