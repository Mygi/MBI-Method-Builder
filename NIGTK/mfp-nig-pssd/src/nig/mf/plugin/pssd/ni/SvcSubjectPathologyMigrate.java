package nig.mf.plugin.pssd.ni;

import java.util.Vector;
import java.util.Collection;

import nig.mf.plugin.util.AssetUtil;
import nig.mf.pssd.plugin.util.PSSDUtil;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

/**
 * Specialized function to migrate the Pathology meta-data element from Subject:hfi.pssd.animal.subject/pathology to
 * Subject:hfi.pssd.animal.disease/disease Once the migration is complete, the animal-mri-simple Method will only create
 * this meta-data in the latter location and this function will be retired It can serve as a template for other
 * migrations in the future
 * 
 * @author nebk
 * 
 */
public class SvcSubjectPathologyMigrate extends PluginService {
	private Interface _defn;

	public SvcSubjectPathologyMigrate() {

		_defn = new Interface();
		_defn.add(new Element("cid", CiteableIdType.DEFAULT,
						"The citeable asset id of the local Subject to be migrated. If not set, Subjects will be found and migrated.",
						0, 1));
	}

	public String name() {
		return "nig.pssd.subject.pathology.migrate";
	}

	public String description() {
		return "Specialized service to migrate Subject:hfi.pssd/human.subject/pathology to Subject:hfi.pssd.animal.disease";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// Get the ID of the specified Subject if any
		String cid = args.value("cid");

		// Construct a List of Subject asset IDs to loop over
		Collection<String> subjects = null;
		if (cid == null) {
			// Find all Subjects
			XmlDocMaker doc = new XmlDocMaker("args");
			doc.add("where", "model='om.pssd.subject'");
			doc.add("size", "infinity");
			doc.add("pdist", 0);          // Force local
			XmlDoc.Element r1 = executor().execute("asset.query", doc.root());
			subjects = r1.values("id");
		} else {
			// Get this asset and verify it is a Subject; exception if not
			PSSDUtil.isValidSubject(executor(), cid, true);
			subjects = new Vector<String>();
			subjects.add(AssetUtil.getId(executor(), cid));
		}

		// Iterate over all Subjects
		for (String id : subjects) {

			// Get the Subject Pathology meta-data
			if (PSSDUtil.isReplica(executor(), cid)) {
				w.add("subject", "The given Subject '" + id + "' is a replica. Cannot modify it.");
			} else {

				//
				XmlDocMaker doc = new XmlDocMaker("args");
				doc.add("id", id);
				doc.add("pdist", 0);       // Force local
				XmlDoc.Element r2 = executor().execute("asset.get", doc.root());
				String pathStr = r2.value("asset/meta/hfi.pssd.animal.subject/pathology");

				// If pathology exists, transfer to all Subjects associated with this R-Subject
				if (pathStr != null) {
					w.add("Subject", AssetUtil.getCid(executor(), id));

					// Transfer the Pathology information from hfi.pssd.animal.subject/pathology to
					// Subject/hfi.pssd.animal.disease
					doc = new XmlDocMaker("args");
					doc.add("id", id);
					doc.push("meta");
					doc.push("hfi.pssd.animal.disease", new String[] { "ns", "pssd.public" });
					doc.add("disease", pathStr);
					doc.push("disease-state");
					doc.push("time");
					doc.add("time-point", 0); // Assume all are baseline
					doc.pop();
					doc.pop();
					doc.pop();
					doc.pop();
					executor().execute("asset.set", doc.root());

					// Remove Pathology element from hfi.pssd.animal.subject meta
					XmlDoc.Element asMeta = r2.element("asset/meta/hfi.pssd.animal.subject");
					XmlDoc.Element pMeta = r2.element("asset/meta/hfi.pssd.animal.subject/pathology");
					asMeta.remove(pMeta);

					// And overwrite (the namespace is as provided in-situ in asMeta)
					doc = new XmlDocMaker("args");
					doc.add("id", id);
					doc.push("meta", new String[] { "action", "replace" });
					doc.add(asMeta);
					doc.pop();
					executor().execute("asset.set", doc.root());
				}
			}
		}
	}
}
