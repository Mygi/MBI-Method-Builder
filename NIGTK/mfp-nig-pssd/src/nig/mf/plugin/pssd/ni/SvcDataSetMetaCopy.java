package nig.mf.plugin.pssd.ni;

import java.util.Collection;

import nig.mf.pssd.plugin.util.PSSDUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

// Specialized service used to fix up some meta-data after a PSSD migration
// the PSSD migration service was subsequently improved  so this service
// should not be needed further.

public class SvcDataSetMetaCopy extends PluginService {

	private Interface _defn;

	public SvcDataSetMetaCopy() {
		_defn = new Interface();
		_defn.add(new Element("cid", CiteableIdType.DEFAULT,
				"The citeable id of the parent PSSD object (project, subject etc) to begin searching from.", 1, 1));
	}

	public String name() {
		return "nig.pssd.dataset.meta.copy";
	}

	public String description() {
		return "Copies meta-data on a local PSSD data-set from mf-dicom-series/description to pssd-object/name";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		// Distributed ID for  DataSet. It must be a primary or we are not allowed
		// to modify it
		String cidIn = args.value("cid");
		
		// Validate
		PSSDUtil.isValidDataSet(executor(), cidIn, true);
		if (PSSDUtil.isReplica(executor(), cidIn)) {
			throw new Exception ("The supplied parent Study/DataSet is a replica and this service cannot modify it.");
		}

		// Query to find content
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("where", "xpath(pssd-object/type)='dataset' and " + "(cid starts with " + "'" + cidIn + "'"
				+ " or cid = " + "'" + cidIn + "') and "
				+ "xpath(pssd-object/name) starts with 'Migrated_from_' and mf-dicom-series has value");
		doc.add("size", "infinity");
		doc.add("pdist", 0);      // Force local
		XmlDoc.Element ret = executor().execute("asset.query", doc.root());

		// Get collection of asset IDs
		Collection<String> assets = ret.values("id");
		int nAssets = 0;
		if (assets != null) {

			// Iterate through and get each asset
			for (String id : assets) {
				nAssets++;
				XmlDocMaker doc2 = new XmlDocMaker("args");
				doc2.add("id", id);
				XmlDoc.Element ret2 = executor().execute("asset.get", doc2.root());

				// Get the things we need
				String description = ret2.value("asset/meta/mf-dicom-series/description");
				String cid = ret2.value("asset/cid");

				// Update the PSSD dataset object
				XmlDocMaker doc3 = new XmlDocMaker("args");
				doc3.add("id", cid);
				doc3.add("name", description);
				doc3.add("description", description);
				XmlDoc.Element ret3 = executor().execute("om.pssd.object.update", doc3.root());
			}
		} else {
			// System.out.println("No assets returned by query");
		}
	}
}
