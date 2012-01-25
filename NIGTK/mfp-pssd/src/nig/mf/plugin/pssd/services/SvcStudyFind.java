package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.Study;
import nig.mf.pssd.plugin.util.DistributedQuery;
import nig.mf.pssd.plugin.util.DistributedQuery.ResultAssetType;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.DictionaryEnumType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.LongType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcStudyFind extends PluginService {
	private Interface _defn;

	public SvcStudyFind() throws Throwable {

		_defn = new Interface();
		_defn.add(new Interface.Element("type", new DictionaryEnumType("pssd.study.types"), "The type of the study.",
				0, 1));
		_defn.add(new Interface.Element("text", StringType.DEFAULT, "Arbitrary search text for free text query.", 0, 1));
		_defn.add(new Interface.Element("method", CiteableIdType.DEFAULT, "Method utilized.", 0, 1));
		_defn.add(new Interface.Element("idx", LongType.POSITIVE_ONE, "Cursor position. Defaults to 1", 0, 1));
		_defn.add(new Interface.Element("size", IntegerType.POSITIVE_ONE, "Cursor size. Defaults to 100", 0, 1));
		_defn.add(new Interface.Element("action", new EnumType(new String[] { "list", "describe" }),
				"The type of display. Defaults to 'list'.", 0, 1));
		_defn.add(new Interface.Element(
				"foredit",
				BooleanType.DEFAULT,
				"Indicates whether the object may be edited. If true, then a description of the structure of the data is returned. Defaults to 'false'.",
				0, 1));
		_defn.add(new Interface.Element("asset-type", new EnumType(new String[] { "primary", "replica", "all" }),
				"Specify type of asset to find. Defaults to all.", 0, 1));
		_defn.add(new Interface.Element(
				"pdist",
				IntegerType.DEFAULT,
				"Specifies the peer distance for a distributed query. Defaults to all servers in a federated session.  Set to 0 for local only or infinity for all peers (regardless of whether session is federated or not).",
				0, 1));
	}

	public String name() {

		return "om.pssd.study.find";
	}

	public String description() {

		return "Returns Studies that match the given search criteria. It does a distributed query in a federation.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String type = args.value("type");
		String text = args.value("text");
		String method = args.value("method");
		boolean forEdit = args.booleanValue("foredit", false);
		String pdist = args.value("pdist");
		String assetType = args.stringValue("asset-type", "all");

		// Set up query.
		String query = null;

		if (type != null) {
			query = "xpath(pssd-study/type)='" + type + "'";
		}

		if (method != null) {
			String sq = "xpath(pssd-study/method)='" + method + "'";
			if (query == null) {
				query = sq;
			} else {
				query += " and " + sq;
			}
		}

		if (text != null) {
			String sq = "mtext contains '" + text + "'";
			if (query == null) {
				query = "xpath(pssd-object/type)='" + Study.TYPE + " and " + sq;
			} else {
				query += " and " + sq;
			}
		}

		if (query == null) {
			query = "xpath(pssd-object/type)='study'";
		}

		// Type of asset to find; primary/replica/both (default)
		DistributedQuery.appendResultAssetTypePredicate(query, ResultAssetType.instantiate(assetType));

		// Set up service call.
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where", query);
		dm.add("action", "get-meta");
		dm.add("idx", args.longValue("idx", 1));
		dm.add("size", args.intValue("size", 100));

		dm.add("get-related-meta", "true");
		dm.add("related-type", "attachment");
		if (pdist != null)
			dm.add("pdist", pdist);

		// Query is distributed in federation
		XmlDoc.Element r = executor().execute("asset.query", dm.root());

		String action = args.stringValue("action", "list");

		// String si = ( callerIsAPeer() ) ? serverIdentityAsString() : null;

		if (action.equalsIgnoreCase("list")) {
			list(w, r);
		} else if (action.equalsIgnoreCase("describe")) {
			describe(executor(), w, r, forEdit);
		}

	}

	public static void list(XmlWriter w, XmlDoc.Element r) throws Throwable {

		Collection<XmlDoc.Element> assets = r.elements("asset");
		if (assets == null)
			return;
		for (XmlDoc.Element el : assets) {
			String cid = el.value("cid");
			String proute = el.value("@proute");
			w.add("id", new String[] { "proute", proute }, cid);
		}
	}

	public static void describe(ServiceExecutor executor, XmlWriter w, XmlDoc.Element r, boolean forEdit)
			throws Throwable {

		SvcObjectFind.addPssdObjects(executor, w, r, false, forEdit);
	}

}
