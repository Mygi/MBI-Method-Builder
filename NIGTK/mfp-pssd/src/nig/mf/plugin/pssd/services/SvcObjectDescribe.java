package nig.mf.plugin.pssd.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcObjectDescribe extends PluginService {
	private Interface _defn;

	public SvcObjectDescribe() {

		_defn = new Interface();
		Interface.Element me = new Interface.Element("id",
				CiteableIdType.DEFAULT, "The identity of the pssd object.", 1,
				1);
		me.add(new Interface.Attribute(
				"proute",
				CiteableIdType.DEFAULT,
				"In a federation, must specify the route to the peer that manages this citable ID.  If not supplied, the object is assumed to be local.",
				0));
		_defn.add(me);
		_defn.add(new Interface.Element("isleaf", BooleanType.DEFAULT,
				"Identify whether each node is a leaf. Defaults to false.", 0,
				1));
		_defn.add(new Interface.Element(
				"foredit",
				BooleanType.DEFAULT,
				"Indicates whether the object may be edited. If true, then a description of the structure of the data is returned. Defaults to 'false'.",
				0, 1));

		me = new Interface.Element(
				"lock",
				BooleanType.DEFAULT,
				"If set to true, creates a write lock on the asset before retrieving. By default, is false. If set, the version must be 0 or not set.",
				0, 1);
		me.add(new Interface.Attribute(
				"descend",
				BooleanType.DEFAULT,
				"Controls whether locks should also be acquired for all descendants. The default is set to true.",
				0));
		me.add(new Interface.Attribute(
				"timeout",
				IntegerType.DEFAULT,
				"Specifies the length of time in seconds the lock should be held before automatic release. If not specified, defaults to infinite.",
				0));
		me.add(new Interface.Attribute(
				"type",
				new EnumType(new String[] { "persistent", "transient" }),
				"Indicates the type of lock to acquire. 'persistent' locks are associated with the user and last beyond server restarts. 'transient' locks are associated with the session and are released by session termination. The default is set to 'persistent'.",
				0));
		_defn.add(me);

	}

	public String name() {

		return "om.pssd.object.describe";
	}

	public String description() {

		return "Describes objects that match the given search arguments.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {

		// Get object ID
		String id = args.value("id");
		String proute = args.value("id/@proute");

		boolean isleaf = args.booleanValue("isleaf", false);
		boolean forEdit = args.booleanValue("foredit", false);

		describeObject(executor(), id, proute, args.element("lock"), isleaf,
				forEdit, w);
	}

	public static void describeObject(ServiceExecutor executor, String id,
			String proute, XmlDoc.Element lock, boolean isLeaf,
			boolean forEdit, XmlWriter w) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", id);
		dm.add("get-related-meta", "true");
		dm.add("related-type", "attachment");
		if(lock!=null){
			dm.add(lock, true);
		}

		if (forEdit) {
			dm.add("format", "template");
			dm.add("template-if-generated-by", "user");
		}
		dm.add("pdist", 0); // Force local on whatever server it's executed

		// Get the object and format for PSSD
		XmlDoc.Element r = executor.execute(new ServerRoute(proute),
				"asset.get", dm.root());
		SvcObjectFind.addPssdObjects(executor, w, r, isLeaf, forEdit);
	}
}
