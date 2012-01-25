package nig.mf.plugin.pssd.services;

import java.util.Collection;
import java.util.List;

import nig.iio.dicom.DicomElements;
import nig.mf.plugin.pssd.Study;
import nig.mf.plugin.pssd.method.Method;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.DictionaryEnumType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcMethodCreate extends PluginService {
	private Interface _defn;

	public SvcMethodCreate() throws Throwable {

		_defn = new Interface();
		addInterface(_defn);
	}

	public static Interface.Element metadataInterfaceDefn(String desc) throws Throwable {

		Interface.Element mde = new Interface.Element("metadata", XmlDocType.DEFAULT, desc, 0, Integer.MAX_VALUE);
		Interface.Element de = new Interface.Element("definition", StringType.DEFAULT,
				"The name of a metadata type that must/can be applied.", 1, 1);
		de.add(new Interface.Attribute("requirement", new EnumType(new String[] { "mandatory", "optional" }),
				"Defines whether the metadata is mandatory or optional. Defaults to mandatory.", 0));
		mde.add(de);

		Interface.Element ve = new Interface.Element(
				"value",
				XmlDocType.DEFAULT,
				"The value of fragments of the definition. These are sub-elements that would be supplied to the document fragment when creating an asset. Constant values are specified by enclosing the value in the function 'constant()'. E.g. 'constant(123)'. Where 'constant' is not specified, the value is considered the default.",
				0, 1);
		ve.setIgnoreDescendants(true);
		mde.add(ve);

		return mde;
	}

	public static void addInterface(Interface defn) throws Throwable {

		defn.add(new Interface.Element("namespace", StringType.DEFAULT,
				"The namespace in which to create this method.", 1, 1));
		defn.add(new Interface.Element("name", StringType.DEFAULT, "The name of the method.", 0, 1));
		defn.add(new Interface.Element("description", StringType.DEFAULT, "Arbitrary description for the method.", 0, 1));
		defn.add(new Interface.Element("author", CiteableIdType.DEFAULT, "Reference to the author of this method.", 0,
				1));
		defn.add(new Interface.Element ("version", StringType.DEFAULT, "Version identifying Method object structure, defaults to 1.1", 0, 1));

		Interface.Element se = new Interface.Element(
				"step",
				XmlDocType.DEFAULT,
				"Step in the process. Only one of types must be specified - [subject, method, branch]. The step may result in a study.",
				0, Integer.MAX_VALUE);
		se.add(new Interface.Element("name", StringType.DEFAULT, "Arbitrary name for the step.", 1, 1));
		se.add(new Interface.Element("description", StringType.DEFAULT, "Arbitrary description for the step.", 0, 1));

		Interface.Element sue = new Interface.Element("subject", XmlDocType.DEFAULT,
				"Subject specific metadata for a state.", 0, Integer.MAX_VALUE);
		sue.add(new Interface.Attribute("part", new EnumType(new String[] { "p", "r" }),
				"The target subject component. 'p' is the project subject. 'r' is the real subject. Defaults to 'p'.",
				0));

		Interface.Element mde = metadataInterfaceDefn("Subject specific metadata representing state for the subject.");
		sue.add(mde);
		se.add(sue);

		Interface.Element ste = new Interface.Element("study", XmlDocType.DEFAULT,
				"Study, if any, generated from this step.", 0, 1);
		ste.add(new Interface.Element("type", new DictionaryEnumType(Study.TYPE_DICTIONARY), "The type of the study.",
				1, 1));
		Interface.Element md2 = new Interface.Element("dicom",XmlDocType.DEFAULT,
				"The DICOM restrictions on this Study type.",0,1);
		md2.add(new Interface.Element("modality", new DictionaryEnumType(DicomElements.DICOM_MODALITY_DICTIONARY), "The DICOM modality allowed for this Study type. If not specified, any modality is allowed.", 0, Integer.MAX_VALUE));
		ste.add(md2);
		ste.add(mde);
		se.add(ste);

		Interface.Element me = new Interface.Element("method", XmlDocType.DEFAULT,
				"Utilise another pre-existing method that is primary on the local server.", 0, 1);
		me.add(new Interface.Element("id", CiteableIdType.DEFAULT, "The identity of the method.", 1, 1));

		se.add(me);

		Interface.Element be = new Interface.Element("branch", XmlDocType.DEFAULT,
				"Parallel or selective branch to another pre-existing method that is primary on the local server.", 0,
				1);
		be.add(new Interface.Attribute("type", new EnumType(new String[] { "or", "and" }),
				"The evaluation method of the branch.", 1));
		me = new Interface.Element("method", XmlDocType.DEFAULT, "Method.", 1, Integer.MAX_VALUE);
		me.add(new Interface.Element("id", CiteableIdType.DEFAULT, "The identity of the method.", 1, 1));
		be.add(me);
		se.add(be);

		defn.add(se);
	}

	public String name() {

		return "om.pssd.method.create";
	}

	public String description() {

		return "Creates a subject independent research method on the local server.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {		
		// Because we allow editing and creating via the same interface definition (really they should
		// be separated) we must test that the name is specified in this creation service
		XmlDoc.Element name = args.element("name");
		if (name==null) {
			throw new Exception ("You must specify the name element as you are creating the Method");
		}
	
		Boolean replace = null;   // irrelevant to creation
		execute(executor(), null, args, w, replace);
	}

	/**
	 * 
	 * @param executor
	 * @param id
	 * @param args
	 * @param w
	 * @param replace  Only relevant to pre-existing Methods (and may be null for creation) if false, and if the Method  pre-exists (id!=null), then the meta-data are merged. 
	 * @throws Throwable
	 */
	public static void execute(ServiceExecutor executor, String id, XmlDoc.Element args, XmlWriter w, Boolean replace) throws Throwable {

		// Make the object locally. We also require all subMethods to be on the
		// local server.
		String proute = null;

		// Get any Method IDs and validate them to be primary (they are local by
		// definition; no proute)
		validateMethod(executor, args.value("step/method/id"));
		validateMethod(executor, args.value("step/branch/method/id"));

		//
		String ns = args.value("namespace");
		String name = args.value("name");
		String description = args.value("description");
		String author = args.value("author");

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("model", Method.MODEL);

		if (id == null) {
			dm.add("namespace", ns);
			dm.push("meta");
		} else {
			if (replace) {
				dm.push("meta", new String[] { "action", "replace" });
			} else {
				dm.push("meta", new String[] { "action", "merge" });
			}
		}

		dm.push("pssd-object");
		dm.add("type", "method");
		if (name!=null) dm.add("name", name);

		if (description != null) {
			dm.add("description", description);
		}
		dm.pop();

		dm.push("pssd-method");


		if (author != null) {
			dm.add("author", author);
		}
		
		// Version 1.1 Added :dicom element to :study steps
		// First version 1.0 implied by not having version element
		String version = args.stringValue("version", "1.1");
		dm.add("version", version);

		// Add the step and auto-generate the step id when creating or updating
		// Find the  pre-existing number of steps and use this to generate the 
		// step id.  Because the id is auto-generated, you can't edit an existing
		// step, just add them or fully replace them
		int sid = 1;
		if (replace!=null && !replace) sid = findPreExistingSteps(executor, id) + 1;
		List<XmlDoc.Element> ses = args.elements("step");
		if (ses != null) {
			for (XmlDoc.Element se : ses) {
				dm.push("step", new String[] { "id", String.valueOf(sid++) });
				dm.add(se, false);
				dm.pop();
			}
		}
		dm.pop();
		

		// OK, if there is subject information, then add..
		XmlDoc.Element se = args.element("subject/project");
		if (se != null) {
			dm.push("pssd-method-subject");
			dm.add(se, false);
			dm.pop();
		}

		se = args.element("subject/rsubject");
		if (se != null) {
			dm.push("pssd-method-rsubject");
			dm.add(se, false);
			dm.pop();
		}

		dm.pop();

		if (id == null) {
			id = nig.mf.pssd.plugin.util.CiteableIdUtil.methodIDRoot(executor, proute);

			dm.add("pcid", id);
			dm.add("action", "get-cid");
			XmlDoc.Element r = executor.execute("asset.create", dm.root());
			id = r.value("cid");

			w.add("id", id);
		} else {
			dm.add("cid", id);
			executor.execute("asset.set", dm.root());
		}
	}

	// Private functions
	private static void validateMethod(ServiceExecutor executor, String methodId) throws Throwable {

		if (methodId != null) {
			DistributedAsset dMID = new DistributedAsset(null, methodId);
			String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor, dMID);
			if (!type.equals(Method.TYPE)) {
				throw new Exception("Object " + dMID.getCiteableID() + " [type=" + type + "] is not a " + Method.TYPE);
			}
			if (dMID.isReplica()) {
				throw new Exception("The supplied Method " + methodId
						+ " is a replica and this service cannot utilise it.");
			}
		}
	}
	
	
	private static int findPreExistingSteps (ServiceExecutor executor, String id) throws Throwable {
		if (id==null) return 0;
		//
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", id);
		XmlDoc.Element r = executor.execute("asset.get", dm.root());
		Collection<XmlDoc.Element> steps = r.elements("asset/meta/pssd-method/step");
		if (steps==null) return 0;	
		return steps.size();
	}

}
