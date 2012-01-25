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

public class SvcPSSDDatasetNameGrab extends PluginService {
	private Interface _defn;

	public SvcPSSDDatasetNameGrab() {

		_defn = new Interface();
		_defn.add(new Element("id", AssetType.DEFAULT,
				"The asset ID of the local PSSD dataset (holding DICOM data)", 0, 1));

		_defn.add(new Element("cid", CiteableIdType.DEFAULT,
				"The citeable ID of the local PSSD dataset (holding DICOM data)", 0, 1));

		_defn.add(new Element("overwrite", BooleanType.DEFAULT,
						"Set to true to overwrite the existing pssd-object/name. Defaults to false.",
						0, 1));
		_defn.add(new Element("bruker-fudge", BooleanType.DEFAULT,
				"Fudge: Get the Bruker protocol from the local DataSet name and set hfi-bruker-series/protocol with it. Then set the name with the id and protocol. Defaults to false", 0, 1));


	}

	public String name() {
		return "nig.pssd.dataset.name.grab";
	}

	public String description() {
		return "This service extracts mf-dicom-series/description(0008,103E)and mf-dicom-series/protocol(0018,1030) out of the DICOM meta-data and insert into pssd-object/name field of the PSSD(dicom) dataset assets. It will try to use {mf-dicom-series/protocol}_{mf-dicom-series/description} first. If they are not available it will then re-populate them from the dicom content header.  For Bruker DataSets, it sets the name from the bit-shifted id and the protocol.";
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
		boolean overwrite = args.booleanValue("overwrite", false);
		boolean brukerFudge = args.booleanValue("bruker-fudge", false);

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
		doc.add("pdist", 0);    // Force local
		XmlDoc.Element r1 = executor().execute("asset.get", doc.root());
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
		//
		Boolean isDicom = null;
		if (r1.element("asset/meta/mf-dicom-series") != null) {
			isDicom = true;
		} else if (r1.element("asset/meta/hfi-bruker-series") != null) {
			isDicom = false;
		} else {
			throw new Exception(
					"Asset(id=" + id
							+ ") is neither a valid DICOM nor Bruker PSSD dataset asset.");
		}
		String pssdName = r1.value("asset/meta/pssd-object/name");

		// Handle DICOM and Bruker separately
		String newPssdName = null;
		if (isDicom) {
			// Look first in indexed meta-data and then the DICOM header if needed

			String mfdsDescription = r1.value("asset/meta/mf-dicom-series/description");
			if (mfdsDescription == null) mfdsDescription = grabDicomSeriesDescription(id);
			//
			String mfdsProtocol = r1.value("asset/meta/mf-dicom-series/protocol");		
			if (mfdsProtocol == null) mfdsProtocol = grabDicomSeriesProtocol(id);

			// Bail out if nothing. This could be enhanced to optionally fall back on
			// the Series UID for the name
			if (mfdsDescription == null && mfdsProtocol == null) {
				throw new Exception(
						"No Series protocol or description available in indexed meta-data or DICOM header");
			}
		
			// Make new name from protocol_description or some combination depending 
			// upon the exact context
	     	if (mfdsProtocol != null && mfdsDescription != null) {
	     		if (!mfdsDescription.equals(mfdsProtocol)) {
	     			newPssdName = mfdsProtocol + "_" + mfdsDescription;
	     		} else {
	     			newPssdName = mfdsProtocol;
	     		}
			} else if (mfdsProtocol != null) {
				newPssdName = mfdsProtocol;
			} else {
				newPssdName = mfdsDescription;
			}
		} else {
			
			// Bruker
			String mfdsID = r1.value("asset/meta/hfi-bruker-series/id");
			String mfdsProtocol = null;
			
			// The original algorithm set the name=protocol. However it did
			// not put the protocol in hfi-bruker-series.  So here we recover
			// the protocol from the name and update the meta-data 
			if (brukerFudge) {
				mfdsProtocol = pssdName;
				XmlDoc.Element brukerMeta = r1.element("asset/meta/hfi-bruker-series");
				setBrukerProtocol (id, brukerMeta, mfdsProtocol);
			} else {
				mfdsProtocol = r1.value("asset/meta/hfi-bruker-series/protocol");
			}
			
			// We need to write this function... Then we can remove the fudge code.
			//if (mfdsProtocol == null) mfdsProtocol = grabBrukerSeriesProtocol(id);
			if (mfdsID!=null) {
				newPssdName = "" + (Integer.parseInt(mfdsID)>>16);
				if (mfdsProtocol != null) newPssdName += "_" + mfdsProtocol;
			}

		}
		//
		if(pssdName!=null&&overwrite==false){
			throw new Exception("There is existing pssd-object/name. To overwrite it, set overwrite to true.");
		}

		if (newPssdName != null) updatePssdName(id,newPssdName);

	}
	
	private void updatePssdName(String id, String newPssdName) throws Throwable {
		
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", id);
		doc.push("meta");
		doc.push("pssd-object");
		doc.add("name",newPssdName);
		doc.pop();
		doc.pop();
		XmlDoc.Element r1 = executor().execute("asset.set", doc.root());	
	}
	
	
	private void setBrukerProtocol (String id, XmlDoc.Element brukerMeta, String protocol) throws Throwable {
		
		String bid = brukerMeta.value("id");
		String buid = brukerMeta.value("uid");
		//
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", id);
		doc.push("meta", new String[] {"action", "replace"});
		doc.push("hfi-bruker-series");
		if (bid!=null) doc.add("id", bid);
		if (buid!=null) doc.add("uid", buid);
		if (protocol!=null) doc.add("protocol", protocol);
		doc.pop();
		doc.pop();
		XmlDoc.Element r1 = executor().execute("asset.set", doc.root());	
	}

	/**
	 * Function to extract the protocol field from the DICOM header
	 * 
	 * @param id
	 * @return
	 * @throws Throwable
	 */
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

	/**
	 * Function to extract the Series description field from the DICOM header
	 * 
	 * @param id
	 * @return
	 * @throws Throwable
	 */
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

	/**
	 * 
	 * @param id
	 * @return
	 * @throws Throwable
	 */
	String getContentStatus(String id) throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", id);
		XmlDoc.Element r1 = executor().execute("asset.content.status", doc.root());
		return r1.value("asset/state");

	}

}