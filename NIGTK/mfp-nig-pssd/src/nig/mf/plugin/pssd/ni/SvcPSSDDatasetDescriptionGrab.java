package nig.mf.plugin.pssd.ni;

import nig.mf.plugin.util.AssetUtil;
import nig.mf.pssd.plugin.util.PSSDUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.BooleanType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcPSSDDatasetDescriptionGrab extends PluginService {
	private Interface _defn;

	public SvcPSSDDatasetDescriptionGrab() {

		_defn = new Interface();
		_defn.add(new Element("id", AssetType.DEFAULT,
				"The asset id of the (dicom) PSSD dataset", 0, 1));

		_defn.add(new Element("cid", CiteableIdType.DEFAULT,
				"The citeable id of the (dicom) PSSD dataset", 0, 1));

		_defn.add(new Element("overwrite", BooleanType.DEFAULT,
						"Set to ture to overwrite the existing pssd-object/description. Defaults to false.",0,1));

	}

	public String name() {
		return "nig.pssd.dataset.description.grab";
	}

	public String description() {
		return "This service extracts the mf-dicom-series/description meta-data element and inserts it into the pssd-object/description field of the given local PSSD DataSet (DICOM) object. If mf-dicom-series/description is not populated, it is generated it from the DICOM file header (using first the series description (0008,103E) and if that is absent the protocol (0018,1030)";

	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {

		String id = args.value("id");
		String cid = args.value("cid");
		boolean overwrite = args.booleanValue("overwrite",false);

		if (id == null && cid == null) {
			throw new Exception("You need to specify either id or cid.");
		}
		if (id != null && cid != null) {
			throw new Exception("You should not specify both id and cid.");
		}
		if (cid==null) cid = AssetUtil.getCid(executor(), id);
		if (PSSDUtil.isReplica(executor(), cid)) {
			throw new Exception ("This object is a replica. Cannot modify");
		}

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("cid", cid);
		doc.add("pdist", 0);       // Force local
		XmlDoc.Element r1 = executor().execute("asset.get", doc.root(), null,
				null);
		id = r1.value("asset/@id");
		cid = r1.value("asset/cid");
		if (cid == null) {
			throw new Exception("No asset/cid found. Asset(id=" + id
					+ ") is not a valid PSSD asset.");
		}
		if (r1.value("asset/model") == null) {
			throw new Exception("No asset/model found. Asset(id=" + id
					+ ") is not a valid PSSD asset.");
		}
		if (!r1.value("asset/model").equals("om.pssd.dataset")) {
			throw new Exception("Wrong asset/model. Asset(id=" + id
					+ ") is not a valid PSSD dataset asset.");
		}
		if (r1.element("asset/meta/mf-dicom-series") == null) {
			throw new Exception(
					"No asset/meta/mf-dicom-series found. Asset(id=" + id
							+ ") is not a valid DICOM PSSD dataset asset.");
		}
		String pssdDescription = r1.value("asset/meta/pssd-object/description");
		if(pssdDescription!=null&&overwrite==false){
			throw new Exception("There is an existing pssd-object/description element. Set overwrite to true if desired.");
		}

		// Fetch the pre-existing indexed meta-data elements of interest and update them if missing
		// Description
		String mfdsDescription = r1
				.value("asset/meta/mf-dicom-series/description");
		if (mfdsDescription == null) {
			mfdsDescription = grabDicomSeriesDescription(id);         // Falls back on Protocol if needed
		}
		if (mfdsDescription == null) {
			throw new Exception(
					"No asset/meta/mf-dicom-series/description found.");
		}
	
		// Protocol
		String mfdsProtocol = r1.value("asset/meta/mf-dicom-series/protocol");
		if (mfdsProtocol == null) {
			mfdsProtocol = grabDicomSeriesProtocol(id);
		}
		if (mfdsProtocol == null) {
			throw new Exception("No asset/meta/mf-dicom-series/protocol found.");
		}
		
		String newPssdDescription = mfdsDescription;
		updatePssdDescription(id,newPssdDescription);

	}

	void updatePssdDescription(String id, String newPssdDescription) throws Throwable {
		
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", id);
		doc.push("meta");
		doc.push("pssd-object");
			doc.add("description",newPssdDescription);
		doc.pop();
		doc.pop();
		XmlDoc.Element r1 = executor().execute("asset.set", doc.root());
		
		
	}

	String grabDicomSeriesProtocol(String id) throws Throwable {

		// :element -name "protocol" -type "string" -index "true" -min-occurs
		// "0" -max-occurs "1"
		// :description
		// "User defined description of conditions under which the series was performed. Derived from DICOM element (0018,1030)."

		String protocol = null;
		if (getContentStatus(id).equals("online")) {
			// dicom.metadata.get :id $dicomDatasetAssetId
			XmlDocMaker doc = new XmlDocMaker("args");
			doc.add("id", id);
			XmlDoc.Element r1 = executor().execute("dicom.metadata.get", doc.root());
			protocol = r1.value("de[@tag='00181030']/value");
		} else {
			throw new Exception(
					"asset(id="
							+ id
							+ ") content is not online. To run this service you need to make it online first.");
		}
		if(protocol!=null){
			XmlDocMaker doc = new XmlDocMaker("args");
			doc.add("id", id);
			doc.push("meta");
			doc.push("mf-dicom-series",new String[]{"ns","dicom"});
			if(protocol!=null){
				doc.add("protocol",protocol);
			}
			doc.pop();
			doc.pop();
			XmlDoc.Element r1 = executor().execute("asset.set", doc.root());
		}
		return protocol;

	}

	String grabDicomSeriesDescription(String id) throws Throwable {

		// :element -name "description" -type "string" -index "true" -min-occurs
		// "0" -max-occurs "1"
		// :description
		// "Description, if any. Derived from DICOM element (0008,103E), the series description, 
		//            or if no description, synthesized from the protocol name - DICOM element (0018,1030)."

		String description = null;
		if (getContentStatus(id).equals("online")) {
			// dicom.metadata.get :id $dicomDatasetAssetId
			XmlDocMaker doc = new XmlDocMaker("args");
			doc.add("id", id);
			XmlDoc.Element r1 = executor().execute("dicom.metadata.get",
					doc.root(), null, null);
			description = r1.value("de[@tag='0008103E']/value");
			if (description == null) {
				description = r1.value("de[@tag='00181030']/value");
			}
		} else {
			throw new Exception(
					"asset(id="
							+ id
							+ ") content is not online. To run this service you need to make it online first.");
		}
		if(description!=null){
			XmlDocMaker doc = new XmlDocMaker("args");
			doc.add("id", id);
			doc.push("meta");
			doc.push("mf-dicom-series",new String[]{"ns","dicom"});
			if(description!=null){
				doc.add("description",description);
			}
			doc.pop();
			doc.pop();
			XmlDoc.Element r1 = executor().execute("asset.set", doc.root());
		}
		return description;

	}

	
	String getContentStatus(String id) throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", id);
		XmlDoc.Element r1 = executor().execute("asset.content.status",
				doc.root(), null, null);
		return r1.value("asset/state");

	}


}