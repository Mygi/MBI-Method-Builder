package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

import nig.mf.plugin.pssd.method.Method;
import nig.mf.pssd.plugin.util.DistributedAssetUtil;
import nig.mf.pssd.plugin.util.DistributedQuery;

import java.util.*;

public class SvcMethodFind extends PluginService {
	private Interface _defn;

	public SvcMethodFind() throws Throwable {

		_defn = new Interface();
		_defn.add(new Interface.Element("for", new EnumType(new String[] { "subject" }),
				"The type of entity to which this method applies. If not specified, returns all methods.", 0, 1));
		_defn.add(new Interface.Element("study", new DictionaryEnumType("pssd.study.types"), "The type of the study.",
				0, 1));
		_defn.add(new Interface.Element("name", StringType.DEFAULT, "Name of Method looking for", 0, 1));
		_defn.add(new Interface.Element("text", StringType.DEFAULT,
				"Arbitrary literal (exact match) string for query of indexed meta-data text strings.", 0, 1));
		_defn.add(new Interface.Element("method", CiteableIdType.DEFAULT, "Method utilized.", 0, 1));
		_defn.add(new Interface.Element("idx", LongType.POSITIVE_ONE, "Cursor position. Defaults to 1", 0, 1));
		_defn.add(new Interface.Element("size", IntegerType.POSITIVE_ONE, "Cursor size. Defaults to 100", 0, 1));
		_defn.add(new Interface.Element("action", new EnumType(
				new String[] { "list", "summarize", "describe", "expand" }),
				"The type of display. Defaults to 'list'.", 0, 1));
		_defn.add(new Interface.Element("asset-type", new EnumType(new String[] { "primary", "replica", "all" }),
				"Specify type of asset to find. Defaults to all.", 0, 1));
		_defn.add(new Interface.Element(
				"pdist",
				IntegerType.DEFAULT,
				"Specifies the peer distance for a distributed query. Defaults to all servers in a federated session.  Set to 0 for local only or infinity for all peers (regardless of whether session is federated or not).",
				0, 1));
	}

	public String name() {

		return "om.pssd.method.find";
	}

	public String description() {

		return "Returns methods that match the given search criteria.  The query is distributed in a federation.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String study = args.value("study");
		String text = args.value("text");
		String method = args.value("method");
		String target = args.value("for");
		String pdist = args.stringValue("pdist");
		String assetType = args.stringValue("asset-type", "all");
		String name = args.value("name");

		// Setup query
		String query = null;
		if (target != null) {
			if (target.equalsIgnoreCase("subject")) {
				query = "(pssd-method-subject has value or pssd-method-rsubject has value)";
			}
		}

		if (study != null) {
			if (query != null) {
				query += " and ";
			}

			query = "xpath(pssd-method/step/study/type)='" + study + "'";
		}

		if (method != null) {
			String sq = "(xpath(pssd-method/step/method/id)='" + method + "'";
			sq += " or xpath(pssd-method/step/branch/method/id)='" + method + "')";

			if (query == null) {
				query = sq;
			} else {
				query += " and " + sq;
			}
		}

		if (text != null) {
			String sq = "mtext contains literal ('" + text + "')";
			if (query == null) {
				query = sq;
			} else {
				query += " and " + sq;
			}
		}

		if (name != null) {
			String sq = "xpath(pssd-object/name)='" + name + "'";
			if (query == null) {
				query = sq;
			} else {
				query += " and " + sq;
			}
		}

		// Specify Method object
		query += " and xpath(pssd-object/type)='method'";

		// Primary/replica/both (default)
		DistributedQuery.appendResultAssetTypePredicate(query, DistributedQuery.ResultAssetType.instantiate(assetType));

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where", query);
		dm.add("action", "get-meta");
		dm.add("idx", args.longValue("idx", 1));
		dm.add("size", args.intValue("size", 100));
		if (pdist != null)
			dm.add("pdist", pdist);

		// Do the query; distributed in federation
		XmlDoc.Element r = executor().execute("asset.query", dm.root());

		String action = args.stringValue("action", "list");

		if (action.equalsIgnoreCase("list")) {
			list(w, r);
		} else if (action.equalsIgnoreCase("summarize")) {
			summarize(w, r);
		} else if (action.equalsIgnoreCase("describe")) {
			describe(executor(), w, r, false);
		} else if (action.equalsIgnoreCase("expand")) {
			describe(executor(), w, r, true);
		}
	}

	public static void list(XmlWriter w, XmlDoc.Element r) throws Throwable {

		Collection<XmlDoc.Element> ids = r.elements("asset");
		if (ids == null) {
			return;
		}

		for (XmlDoc.Element ide : ids) {
			String proute = ide.value("@proute");
			String id = ide.value("cid");

			w.add("id", new String[] { "proute", proute }, id);
		}
	}

	public static void summarize(XmlWriter w, XmlDoc.Element r) throws Throwable {

		Collection<XmlDoc.Element> aes = r.elements("asset");
		if (aes != null) {
			for (XmlDoc.Element ae : aes) {
				w.push("method", new String[] { "proute", ae.value("@proute"), "id", ae.value("cid") });

				String name = ae.value("meta/pssd-object/name");
				String desc = ae.value("meta/pssd-object/description");

				w.add("name", name);

				if (desc != null) {
					w.add("description", desc);
				}

				w.pop();
			}
		}
	}

	public static void describe(ServiceExecutor executor, XmlWriter w, XmlDoc.Element r, boolean expand)
			throws Throwable {

		Collection<XmlDoc.Element> aes = r.elements("asset");
		if (aes != null) {
			for (XmlDoc.Element ae : aes) {
				describeMethodElement(executor, w, ae, expand);
			}
		}
	}

	public static void describeMethodElement(ServiceExecutor executor, XmlWriter w, XmlDoc.Element ae, boolean expand)
			throws Throwable {

		String mid = ae.value("cid");
		String assetId = ae.value("@id");
		String version = ae.value("@version");
		String proute = ae.value("@proute");

		w.push("method", new String[] { "proute", proute, "id", mid, "asset", assetId, "version", version });

		String name = ae.value("meta/pssd-object/name");
		String description = ae.value("meta/pssd-object/description");

		w.add("name", name);
		if (description != null) {
			w.add("description", description);
		}

		XmlDoc.Element me = ae.element("meta/pssd-method");
		if (me != null) {

			if (expand) {
				String versionMethod = me.value("version");
				if (versionMethod != null)
					w.add("version", versionMethod);
				Method m = new Method(mid, name, description, versionMethod);
				Collection<XmlDoc.Element> authors = me.elements("author");
				if (authors != null) {
					for (XmlDoc.Element author : authors)
						w.add(author);
				}
				m.restoreSteps(me);
				m.convertBranchesToSubSteps(proute, executor);
				m.saveSteps(w);
			} else {
				w.add(me, false);
			}

			// Method for a subject?
			XmlDoc.Element se = ae.element("meta/pssd-method-subject");
			XmlDoc.Element rse = ae.element("meta/pssd-method-rsubject");

			if (se != null || rse != null) {
				w.push("subject");

				if (se != null) {
					w.push("project");
					w.add(se, false);
					w.pop();
				}

				if (rse != null) {
					w.push("rsubject");
					w.add(rse, false);
					w.pop();
				}

				w.pop();
			}
		}

		w.pop();
	}
}
