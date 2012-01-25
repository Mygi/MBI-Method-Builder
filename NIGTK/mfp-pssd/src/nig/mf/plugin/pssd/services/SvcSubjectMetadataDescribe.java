package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.Subject;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcSubjectMetadataDescribe extends PluginService {

	private Interface _defn;

	public SvcSubjectMetadataDescribe() {
		_defn = new Interface();
		//
		Interface.Element me = new Interface.Element("pid",CiteableIdType.DEFAULT, "The identity of the parent (project).", 1, 1);
		me.add(new Interface.Attribute("proute", CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.", 0));
		_defn.add(me);
		//
		_defn.add(new Element("mid", CiteableIdType.DEFAULT,
				"The citeable id of the method (must be one of the methods registered with the parent project)", 0, 1));
	}

	public String name() {
		return "om.pssd.subject.metadata.describe";
	}

	public String description() {
		return "Describes the metadata that can be associated with the creation of a Subject object.  In a federation, fetches the meta-data from the server that manages the Project.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// Set ID and validate
		DistributedAsset dPID = new DistributedAsset(args.element("pid"));
		String pid = dPID.getCiteableID();
		String proute = dPID.getServerRoute();

		Collection<String> mids = getProjectMethodIds(executor(), dPID);
		if (mids == null) {
			throw new Exception("No Methods found in the parent project " + pid + ".");
		}
		if (mids.size() <= 0) {
			throw new Exception("No Methods found in the parent project " + pid + ".");
		}

		String mid = args.value("mid");
		if (mid == null) {
			if (mids.size() > 1) {
				throw new Exception("No mid (citeable id of the Method) is specifed and there are multiple Methods registered with the Project.");
			} else {
				mid = mids.iterator().next();
			}
		} else {
			if (!mids.contains(mid)) {
				throw new Exception(
						"The specified mid (citeable id of the Method) is not a member of the list of Methods registered with the Project. "
								+ pid + ".");
			}
		}

		// Domain metadata from om.pssd.type.metadata.describe.  Depends on the documents registered with the
		// specific object type.  Fetch from server managing Project
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("type", Subject.TYPE);
		if (proute!=null) dm.add("proute", dPID.getServerRoute());
		XmlDoc.Element r1 = executor().execute("om.pssd.type.metadata.describe", dm.root());
		Collection<XmlDoc.Element> mes = r1.elements("metadata");
		if (mes != null) {
			w.push("meta");
			for (XmlDoc.Element me : mes) {
				w.add(me);
			}
			w.pop();
		}

		 // Method metadata from om.pssd.method.subject.metadata.describe. Depends on the details
		// of the Method.  Since Method objects cannot be edited, it could be fetched locally
		// from a replica (if exists) or from the same server as that managing the Project. 
		// For consistency, fetch from server managing Project; our policy says that the
		// server managing the Project must also manage the Methods that it uses.
		dm = new XmlDocMaker("args");
		if (proute!=null) {
			dm.add("id", new String[] {"proute", proute}, mid);
		} else {
			dm.add("id", mid);	
		}
		XmlDoc.Element r2 = executor().execute("om.pssd.method.subject.metadata.describe", dm.root());
		XmlDoc.Element publicMeta = r2.element("method/subject/public");
		if (publicMeta != null) {
			w.add(publicMeta);
		}
		XmlDoc.Element privateMeta = r2.element("method/subject/private");
		if (privateMeta != null) {
			w.add(privateMeta);
		}

	}

	private Collection<String> getProjectMethodIds(ServiceExecutor executor, DistributedAsset dPID) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", new String[]{"proute", dPID.getServerRoute()}, dPID.getCiteableID());
		XmlDoc.Element r = executor().execute("om.pssd.object.describe", dm.root());
		Collection<String> mids = r.values("object/method/id");
		return mids;
	}

}
