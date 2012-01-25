package nig.mf.plugin.pssd.services;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.Study;
import nig.mf.plugin.pssd.Subject;
import nig.mf.plugin.pssd.method.ExMethod;
import nig.mf.pssd.plugin.util.DistributedAsset;
import nig.mf.pssd.plugin.util.DistributedQuery.ResultAssetType;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcStudyRename extends PluginService {
	private static final int ELEMENT_MAX_OCCURS = 3;
	private Interface _defn;

	public SvcStudyRename() throws Throwable {
		_defn = new Interface();
		_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT,
						"The citeable ID of the parent object. It can be a Project, a Subject, an ExMethod or a Study.",
						1, 1));
		Interface.Element e = new Interface.Element("element",StringType.DEFAULT,
				"The XPATH to the document element/attribute to be included in the new study name.",
				1, ELEMENT_MAX_OCCURS);
		e.add(new Interface.Attribute("idx",IntegerType.POSITIVE_ONE,
						"Index number of the element if multiple elements are specified. It is not requried if single element is specified. Starts from 1.",
						0));
		e.add(new Interface.Attribute("prefix", StringType.DEFAULT,"Prefix to the element value.", 0));
		e.add(new Interface.Attribute("postfix", StringType.DEFAULT,"Postfix to the element value.", 0));
		_defn.add(e);
		_defn.add(new Interface.Element("pdist",IntegerType.DEFAULT,"Specifies the peer distance when looking for remote children. Defaults to infinity.  Set to 0 for local only.",0,1));
		_defn.add(new Interface.Element("ptag", StringType.DEFAULT, "When looking for remote children, only query peers with this ptag. If none, query all peers.", 0, 1));
	}

	public String name() {
		return "om.pssd.study.rename";
	}

	public String description() {
		return "Rename all of the local studies (under the parent object) with the specified element/attribute value, which is the query result of the specified XPATH expression.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {

		// SOme validation.
		DistributedAsset rootId = new DistributedAsset(args.element("id"));
		String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), rootId);
		if (type==null) {
			throw new Exception("The asset associated with " + rootId.toString() + " does not exist");
		}
		if (!type.equals(Study.TYPE) && !type.equals(ExMethod.TYPE) && 
				!type.equals(Project.TYPE) && !type.equals(Subject.TYPE)) {
			throw new Exception(
					"Invalid root citeable id. It has to be a Project, Subject, Study, or an ExMethod.");
		}
		if (rootId.isReplica()) {
			throw new Exception ("The supplied object is a replica and this service cannot rename it or its children.");
		}
		
		// This could easily be relaxed as we are going to rename local children.  But
		// this is our policy at the moment
		String ptag = args.stringValue("ptag");
		String pdist = args.stringValue("pdist", "infinity");
		if (rootId.hasRemoteChildren(ResultAssetType.primary, ptag, pdist)) {
			throw new Exception ("The supplied object has primary children on remote servers in the federation; cannot rename children.");
		}
				
		// Find all Studies belonging to the given parent. Can't do this with an om.* service...
		String rootCID = rootId.getCiteableID();
		Vector<XmlDoc.Element> ses = sortElements(args.elements("element"));
		String queryString = "(cid starts with '" + rootCID + "' or cid = '"
				+ rootCID + "') and xpath(pssd-object/type) = 'study' ";
		for (int i = 0; i < ses.size(); i++) {
			XmlDoc.Element e = ses.get(i);
			queryString += "and xpath(" + e.value() + ") has value ";
		}
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where", queryString);
		dm.add("size", "infinity");
		dm.add("pdist", 0);             // Force to local
		XmlDoc.Element r = executor().execute("asset.query", dm.root());
		Collection<String> ids = r.values("id");
		
		// Iterate over Studies and rename them
		if (ids != null) {
			for (String id : ids) {
				
				// Get object meta-data
				XmlDocMaker dm2 = new XmlDocMaker("args");
				dm2.add("id", id);
				dm2.add("pdist",0);                 // Force local 	
				XmlDoc.Element r2 = executor().execute("asset.get", dm2.root());
				String cid = r2.value("asset/cid");
				
				// Generate name
				String name = createName(ses, r2.element("asset/meta"));
				
				// Set new name
				XmlDocMaker dm3 = new XmlDocMaker("args");
				dm3.add("id", cid);
				dm3.add("name", name);
				executor().execute("om.pssd.study.update", dm3.root());
				w.add("name", new String[]{"id", cid}, name);
			}
		}

	}

	private Vector<XmlDoc.Element> sortElements(Collection<XmlDoc.Element> es)
			throws Throwable {
		Vector<XmlDoc.Element> ses = new Vector<XmlDoc.Element>();
		HashMap<Integer, XmlDoc.Element> map = new HashMap<Integer, XmlDoc.Element>();
		for (XmlDoc.Element e : es) {
			int idx = Integer.parseInt(e.attribute("idx").value());
			map.put(new Integer(idx), e);
		}
		Object[] indexes = map.keySet().toArray();
		Arrays.sort(indexes);
		for (int i = 0; i < indexes.length; i++) {
			Integer idx = (Integer) indexes[i];
			ses.add(map.get(idx));
		}
		return ses;
	}

	private String createName(Vector<XmlDoc.Element> ses, XmlDoc.Element meta) throws Throwable {
		String name = "";
		for(int i=0;i<ses.size();i++){
			XmlDoc.Element e = ses.get(i);
			String prefix = e.value("@prefix");
			String postfix = e.value("@postfix");
			String value = meta.value(e.value());
			if(prefix!=null){
				name += prefix;
			}
			if(value!=null){
				name += value;
			}
			if(postfix!=null){
				name += postfix;
			}
		}
		return name;
	}
}
