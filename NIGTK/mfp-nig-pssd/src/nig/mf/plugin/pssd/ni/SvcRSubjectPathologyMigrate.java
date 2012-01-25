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
 * R-Subject:hfi.pssd.animal.disease/disease Once the migration is complete, the human-mri-simple Method will only
 * create this meta-data in the latter location and this function will be retired It can serve as a template for other
 * migrations in the future
 * 
 * @author nebk
 * 
 */
public class SvcRSubjectPathologyMigrate extends PluginService {
	private Interface _defn;

	public SvcRSubjectPathologyMigrate() {

		_defn = new Interface();
		_defn.add(new Element("cid", CiteableIdType.DEFAULT,
						"The citeable asset id of the local R-Subject to be migrated. If not set, all R-Subjects will be found and migrated.",
						0, 1));
	}

	public String name() {
		return "nig.pssd.r-subject.pathology.migrate";
	}

	public String description() {
		return "Specialized service to migrate R-Subject:hfi.pssd/human.subject/pathology to Subject:hfi.pssd.animal.disease";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// Get the ID of the specified R-Subject if any
		String cid = args.value("cid");
		if (PSSDUtil.isReplica(executor(), cid)) {
			throw new Exception ("The given RSubject is a replica. Cannot modify");
		}


		// Construct a List of R-Subject CIDs to loop over
		Collection<String> rSubjects = null;
		if (cid == null) {
			// Find all R-Subjects
			XmlDocMaker doc = new XmlDocMaker("args");
			doc.add("pdist", 0);            // Force local
			XmlDoc.Element r1 = executor().execute("om.pssd.r-subject.find", doc.root());
			rSubjects = r1.values("object/id");
		} else {
			// Get this asset and verify it is an R-Subject; exception if not
			PSSDUtil.isValidRSubject(executor(), cid);
			rSubjects = new Vector<String>();
			rSubjects.add(cid);
		}

		// Iterate over all R-Subjects
		w.push("R-Subjects");
		for (String rCid : rSubjects) {
			XmlDocMaker doc = new XmlDocMaker("args");
			doc.add("cid", rCid);
			doc.add("pdist", 0);        // FOrce local
			XmlDoc.Element r2 = executor().execute("asset.get", doc.root());
			String pathStr = r2.value("asset/meta/hfi.pssd.animal.subject/pathology");

			// If pathology exists, transfer to all Subjects associated with this R-Subject
			if (pathStr != null) {
				w.add("R-Subject", rCid);
				w.push("Subjects");

				// Find all of the Subjects associated with this R-Subject
				doc = new XmlDocMaker("args");
				doc.add("where", "xpath(pssd-subject/r-subject)='" + rCid + "'");
				doc.add("size", "infinity");
				doc.add("pdist", 0);        // FOrce local
				XmlDoc.Element r3 = executor().execute("asset.query", doc.root());
				Collection<String> subjects = r3.values("id");

				// Iterate over all Subjects if any
				if (subjects != null) {
					for (String sId : subjects) {
						w.add("Subject", AssetUtil.getCid(executor(), sId));

						// Transfer the Pathology information from the
						// R-Subject/hfi.pssd.animal.subject/pathology ->
						// Subject/hfi.pssd.animal.disease/disease
						doc = new XmlDocMaker("args");
						doc.add("id", sId);
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
						XmlDoc.Element r4 = executor().execute("asset.set", doc.root());
					}
				}
				w.pop();

				// Remove Pathology element from animal.subject meta
				XmlDoc.Element asMeta = r2.element("asset/meta/hfi.pssd.animal.subject");
				XmlDoc.Element pMeta = r2.element("asset/meta/hfi.pssd.animal.subject/pathology");
				asMeta.remove(pMeta);

				// And overwrite (the namespace is as provided in-situ in asMeta)
				doc = new XmlDocMaker("args");
				doc.add("cid", rCid);
				doc.push("meta", new String[] { "action", "replace" });
				doc.add(asMeta);
				doc.pop();
				XmlDoc.Element r5 = executor().execute("asset.set", doc.root());
			}
		}
		w.pop();
	}
}
