package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.xml.*;
import java.util.*;


public class SvcMethodList extends PluginService {
	private Interface _defn;

	public SvcMethodList() {
		_defn = new Interface();
		_defn.add(new Interface.Element(
				"pdist",
				IntegerType.DEFAULT,
				"Specifies the peer distance for a distributed query. Defaults to infinity in a federated session.  Set to 0 for local only or infinity for all peers (regardless of whether session is federated or not).",
				0, 1));
		_defn.add(new Interface.Element("size", IntegerType.POSITIVE_ONE,
				"Cursor size. Defaults to 100", 0, 1));
		_defn.add(new Interface.Element(
				"forsubject",
				BooleanType.DEFAULT,
				"Set to true to list only the methods for subject. Defaults to false.",
				0, 1));
		// This is a little different to Collection management filters.  We just 
		// want to list local (primary) or replica (generated elsewhere) Methods.
		_defn.add(new Interface.Element(
				"filter-policy", new EnumType(new String[] {"none", "primary", "replica"}), 
				"Specify the policy to select the Methods. Defaults to primary.",
				0, 1));

	}

	public String name() {
		return "om.pssd.method.list";
	}

	public String description() {
		return "Returns a list of the known methods.  Query is distributed in a federation";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {

		String query = "xpath(pssd-object/type)='method'"; // "cid in '" +
															// CiteableIdUtil.citeableIDRoot(executor(),CiteableIdUtil.METHOD_CID_ROOT_NAME)
															// + "'";
		boolean forSubject = args.booleanValue("forsubject", false);
		if (forSubject) {
			query += " and (pssd-method-subject has value or pssd-method-rsubject has value)";
		}
		String filterPolicy = args.stringValue("filter-policy", "primary");

		int size = args.intValue("size", 100);
		String pdist = args.value("pdist");
		//
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where", query);
		dm.add("action", "get-meta");
		dm.add("size", size);
		if (pdist != null) {
			dm.add("pdist", pdist);
		}

		// Distributed query in a federation
		XmlDoc.Element r = executor().execute("asset.query", dm.root());
		Collection<XmlDoc.Element> mes = r.elements("asset/meta");
		if (mes != null) {
			for (XmlDoc.Element me : mes) {
				String cid = me.parent().value("cid");
				String proute = me.parent().value("@proute");
				String rid = me.parent().value("rid");
				String name = me.value("pssd-object/name");
				String description = me.value("pssd-object/description");
				String version = me.stringValue("pssd-method/version", "1.0");
				
				// Filter
				boolean keep =  (filterPolicy.equals("none")) ||
				(filterPolicy.equals("primary") && rid==null) ||
				(filterPolicy.equals("replica") && rid!=null);
				if (keep) {
					if (description != null) {
						w.add("method", new String[] { "proute", proute, "rid",
								rid, "name", name, "description", description, "version", version },
								cid);
					} else {
						w.add("method", new String[] { "name", name, "version", version }, cid);
					}
				}
			}
		}
	}

}
