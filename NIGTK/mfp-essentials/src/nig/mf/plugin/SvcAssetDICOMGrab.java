package nig.mf.plugin;

import java.text.SimpleDateFormat;
import java.util.Date;

import nig.mf.plugin.util.AssetUtil;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dicom.DicomDateTime;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcAssetDICOMGrab extends PluginService {

	// Pull out into own class if need to be re-used
	// See DicomElements in hfi.mflux.pssd.dicom package

	private static final String MODALITY = "00080060";
	private static final String PROTOCOL = "00181030";

	// private static final String STUDY_UID = "0020000D";
	// private static final String STUDY_DESCRIPTION = "00081030";
	private static final String STUDY_DATE = "00080020";
	private static final String STUDY_TIME = "00080030";

	private static final String SERIES_UID = "0020000E";
	private static final String SERIES_ID = "00200011";
	private static final String SERIES_DESCRIPTION = "0008103E";
	private static final String SERIES_DATE = "00080021";
	private static final String SERIES_TIME = "00080031";

	private Interface _defn;

	public SvcAssetDICOMGrab() {

		_defn = new Interface();
		_defn.add(new Element("id", AssetType.DEFAULT,
						"The asset id of the DICOM Series (DICOM, PSS or PSSD). If not specified, cid must be specified.",
						0, 1));
		_defn.add(new Element("cid", CiteableIdType.DEFAULT,
				"The citeable id of the DICOM Series (DICOM, PSS or PSSD). If not specified, id must be specified.", 0,
				1));

	}

	public String name() {

		return "nig.dicom.metadata.grab";

	}

	public String description() {
		// description = dicom:description
		//
		// if dicom:description null then
		//
		// description = dicom:protocol

		return "This service extracts meta-data from the local DICOM file (content) header and (re)populates indexed meta data in the mf-dicom-series document type."
				+ "  It does not repopulate imin,imax and size. The content must be online, otherwise an exception is thrown.";

	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String id = args.value("id");
		String cid = args.value("cid");
		if (id == null && cid == null) {
			throw new Exception("Neither id nor cid is specified.");
		}
		if (id == null) {
			id = AssetUtil.getId(executor(), cid);
		}
		// Verify asset. Because the asset might be in any Data Model
		// (DICOM, PSS or PSSD) we only check the presence of the mf-dicom-series
		// meta data
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", id);
		doc.add("pdist", 0);        // Force local
		XmlDoc.Element r1 = executor().execute("asset.get", doc.root());
		if (r1.element("asset/meta/mf-dicom-series") == null) {
			throw new Exception("No asset/meta/mf-dicom-series found. Asset(id=" + id + ") is not a valid DICOM asset.");
		}

		//
		replaceDICOMMetaData(id);
	}

	private void replaceDICOMMetaData(String assetId) throws Throwable {

		if (getContentStatus(assetId).equals("online")) {

			// Create output
			XmlDocMaker docOut = new XmlDocMaker("args");
			docOut.add("id", assetId);
			docOut.push("meta", new String[] { "action", "merge" });
			docOut.push("mf-dicom-series", new String[] { "ns", "dicom" });

			// Get DICOM header
			XmlDocMaker doc = new XmlDocMaker("args");
			doc.add("id", assetId);
			XmlDoc.Element r1 = executor().execute("dicom.metadata.get", doc.root());

			// Find elements that match those in mf-dicom-series
			String uid = r1.value(tag(SERIES_UID));
			if (uid != null)
				docOut.add("uid", uid);
			//
			String id = r1.value(tag(SERIES_ID));
			if (id != null)
				docOut.add("id", id);
			//
			String description = r1.value(tag(SERIES_DESCRIPTION));
			if (description != null)
				docOut.add("description", description);
			//
			String modality = r1.value(tag(MODALITY));
			if (modality != null)
				docOut.add("modality", modality);
			//
			String protocol = r1.value(tag(PROTOCOL));
			if (protocol != null)
				docOut.add("protocol", protocol);

			// Combine Series date and time
			String dateS = r1.value(tag(SERIES_DATE));
			String time = r1.value(tag(SERIES_TIME));
			Date sdate = null;
			if (dateS != null && time != null) {
				SimpleDateFormat sDate = new SimpleDateFormat("dd-MMM-yyyy");
				Date date = sDate.parse(dateS);
				sdate = DicomDateTime.dateTime(date, time);
			}

			// Fall back on Study date and time
			if (sdate == null) {
				dateS = r1.value(tag(STUDY_DATE));
				time = r1.value(tag(STUDY_TIME));
				if (dateS != null && time != null) {
					SimpleDateFormat sDate = new SimpleDateFormat("dd-MMM-yyyy");
					Date date = sDate.parse(dateS);
					sdate = DicomDateTime.dateTime(date, time);
				}
			}
			if (sdate != null)
				docOut.add("sdate", sdate);

			// There isn't any simple way to work out the imin, imax and size
			// elements. These describe the number of slices in the zipped
			// up DICOM Series. They could be worked out by iterating
			// through all of the slices until an exception is reached
			// and accumulating the desired valued. However, these
			// values are rarely not present; they are set by the DICOM server

			docOut.pop();
			docOut.pop();
			executor().execute("asset.set", docOut.root());

		} else {
			throw new Exception("asset(id=" + assetId + ") content is not online");
		}

	}

	private String getContentStatus(String id) throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", id);
		XmlDoc.Element r1 = executor().execute("asset.content.status", doc.root());
		return r1.value("asset/state");

	}

	private String tag(String tagElement) throws Throwable {
		String t = "de[@tag='" + tagElement + "']/value";
		return t;

	}
}
