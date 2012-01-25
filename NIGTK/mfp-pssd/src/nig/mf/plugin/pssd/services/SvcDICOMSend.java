package nig.mf.plugin.pssd.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collection;

import nig.dicom.util.DicomModify;
import nig.io.FileUtils;
import nig.mf.plugin.util.AssetUtil;
import nig.mf.pssd.plugin.util.DistributedAssetUtil;
import nig.mf.pssd.plugin.util.DistributedQuery;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginTask;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.SetOfDicomFiles;
import com.pixelmed.network.StorageSOPClassSCU;

public class SvcDICOMSend extends PluginService {
	private Interface _defn;

	public SvcDICOMSend() throws Throwable {

		_defn = new Interface();
		Interface.Element me = new Interface.Element(
				"pid",
				CiteableIdType.DEFAULT,
				"The identity of the parent object; can be a Project, Subject, ExMethod, Study or DataSet.  All child DataSets (and in a federation children will be found on all peers in the federsation) containing DICOM data will be found and sent.",
				1, 1);
		_defn.add(me);
		//
		_defn.add(new Interface.Element("asset-type", new EnumType(DistributedQuery.ResultAssetType.stringValues()),
				"Specify type of asset to send. Defaults to all.", 0, 1));
		//
		String nameDescription = "A convenience name that this AE may be referred to by. In this case, the AE is looked up in the DICOM AE registry and the children elements host, port, aet are ignored.  Don't specify the name if you want to specify these children elements directly.";
		SvcDICOMAEAdd.addInterface(_defn, true, nameDescription);
		//
		_defn.add(new Interface.Element("aet", StringType.DEFAULT, "The calling (our) AET.", 1, 1));

		_defn.add(new Interface.Element(
				"patient-name-action",
				new EnumType(new String[] { "unchanged", "anonymize", "use-subject-id" }),
				"Sets the action performed on the patient name field of the DICOM file header before sending. Defaults to unchanged. Note: it will not change the local objects but only the intermediate files extracted from the objects.",
				0, 1));
		_defn.add(new Interface.Element(
				"exception-on-fail",
				BooleanType.DEFAULT,
				"Behaviour on failure; the default is to throw an exception.  Otherwise it will continue for each DataSet and wirte summaries to the output XMLWriter.",
				0, 1));
	}

	@Override
	public String name() {

		return "om.pssd.dicom.send";
	}

	@Override
	public String description() {

		return "Send DICOM DataSets to a DICOM Application Entity (server).  Each DataSet is sent in a separate DICOM client call.";
	}

	@Override
	public Interface definition() {

		return _defn;
	}

	@Override
	public Access access() {

		return ACCESS_ACCESS;
	}

	@Override
	public boolean canBeAborted() {

		return true;
	}

	@Override
	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// Parse
		String pid = args.value("pid");
		String type = args.stringValue("asset-type", "all");
		String name = args.value("ae/@name");
		String patientNameAction = args.stringValue("patient-name-action", "unchanged");
		boolean anonymise = false;
		if (patientNameAction.equals("anonymize"))
			anonymise = true;
		Boolean exceptionOnFail = args.booleanValue("exception-on-fail", true);
		//
		String host = null;
		Integer port = null;
		String calledAET = null;

		// Look up in DICOM AE registry if name provided
		if (name != null) {
			String[] ae = lookUp(executor(), name); // Exception if not found
			host = ae[0];
			port = Integer.parseInt(ae[1]);
			calledAET = ae[2];
		} else {
			host = args.value("ae/host");
			port = args.intValue("ae/port");
			calledAET = args.value("ae/aet");
		}
		// Check
		if (host == null || port == null || calledAET == null) {
			throw new Exception(
					"All of the host, port and calledAET must be filled in either directly or by name look up in the AE registry - see om.pssd.dicom.ae.list");
		}
		//
		String callingAET = args.value("aet");
		int compressionLevel = 0;
		int debugLevel = 0;

		PluginTask.checkIfThreadTaskAborted();
		// Find the DataSets with DICOM content. Do a distributed query.
		XmlDocMaker dm = new XmlDocMaker("args");
		String query = "(cid='" + pid + "' or cid starts with '" + pid + "')";
		query += " and model='om.pssd.dataset' and type='dicom/series'";
		DistributedQuery.appendResultAssetTypePredicate(query, DistributedQuery.ResultAssetType.instantiate(type));
		dm.add("where", query);
		XmlDoc.Element r = executor().execute("asset.query", dm.root());

		// Iterate over DataSets. An option could be added to copy all files
		// from all DataSets to a temporary folder and then sent in one giant
		// push.
		Collection<String> dataSets = r.values("id");
		if (dataSets != null) {
			int nDataSets = dataSets.size();
			PluginTask.threadTaskBeginSetOf(nDataSets);
			for (String id : dataSets) {

				PluginTask.checkIfThreadTaskAborted();

				String cid = nig.mf.pssd.plugin.util.CiteableIdUtil.idToCid(executor(), id);
				String scid = nig.mf.pssd.plugin.util.CiteableIdUtil.getSubjectCID(cid);
				// Get the content into a stream
				PluginTask.setCurrentThreadActivity("Getting content of " + cid);
				InputStream is = AssetUtil.getContentInStream(executor(), id);
				if (is != null) {
					boolean useSubjectId = patientNameAction.equals("use-subject-id");
					// Unzip stream into temporary directory
					File tempDir = createTemporaryDirectory();
					SetOfDicomFiles dcmFiles = new SetOfDicomFiles();
					PluginTask.setCurrentThreadActivity("Extracting " + (anonymise ? "and anonymizing" : "")
							+ (useSubjectId ? "and overwriting" : "") + " DICOM files in dataset" + cid);
					PluginTask.checkIfThreadTaskAborted();
					// TODO: clean up the temp files if aborted?
					unzipContent(useSubjectId ? scid : null, anonymise, is, tempDir, dcmFiles);
					PluginTask.checkIfThreadTaskAborted();
					// TODO: clean up the temp files if aborted?
					DICOMHandler dh = new DICOMHandler();

					try {
						PluginTask.setCurrentThreadActivity("Sending DICOM dataset " + cid);
						new StorageSOPClassSCU(host, port, calledAET, callingAET, dcmFiles, compressionLevel, dh, null,
								0, debugLevel);
						if (dh.getNumberFailed() > 0) {
							if (exceptionOnFail) {
								throw new Exception("Failed to send DICOM data for cid " + cid);
							}
							w.add("id", new String[] { "status", "fail", "completed", "reason", "transfer failed",
									"" + dh.getNumberCompleted(), "failed", "" + dh.getNumberFailed() }, cid);
						} else {
							w.add("id", new String[] { "status", "pass", "completed", "" + dh.getNumberCompleted() },
									cid);
						}
					} catch (Throwable t) {
						if (exceptionOnFail) {
							throw new Exception("Failed to make association with AE");
						}

						w.add("id", new String[] { "status", "fail", "reason", "association failed" }, cid);

					}
					// Clean up
					FileUtils.delete(tempDir);
					PluginTask.threadTaskCompleted();
				}
			}
		}

	}

	/**
	 * Unzip stream into a temporary directory and populate a set of DICOM files
	 * 
	 * @param scid
	 *            Subject CID
	 * @param is
	 * @param toDir
	 * @param dcmFiles
	 * @throws Throwable
	 */
	private void unzipContent(String scid, boolean anonymise, InputStream is, File toDir, SetOfDicomFiles dcmFiles)
			throws Throwable {

		// Set tag for DICOM edit
		int group = Integer.parseInt("0010", 16);
		int element = Integer.parseInt("0010", 16);
		AttributeTag aTag = new AttributeTag(group, element);

		java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(new BufferedInputStream(is));
		int BUFFER_SIZE = 2048;

		java.util.zip.ZipEntry entry;
		byte[] buffer = new byte[BUFFER_SIZE];
		while ((entry = zis.getNextEntry()) != null) {
			File destFile = new File(toDir.getAbsolutePath() + "/" + entry.getName());
			if (entry.isDirectory()) {
				destFile.mkdirs();
			} else {
				File parentDir = destFile.getParentFile();
				if (!parentDir.exists()) {
					parentDir.mkdirs();
				}
				BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(destFile), BUFFER_SIZE);
				try {
					int count;
					while ((count = zis.read(buffer, 0, BUFFER_SIZE)) != -1) {
						os.write(buffer, 0, count);
					}
					os.flush();
				} finally {
					os.close();
				}
			}

			// Edit the file so the patient name is the citable ID of the
			// Subject
			if (scid != null) {
				DicomModify.editFile(destFile, aTag, scid);
			}
			if (anonymise) {
				DicomModify.editFile(destFile, aTag, " ");
			}
			dcmFiles.add(destFile);
		}
		zis.close();
	}

	public String[] lookUp(ServiceExecutor executor, String name) throws Throwable {

		// Get the registry
		XmlDoc.Element r = executor.execute("om.pssd.dicom.ae.list");
		if (r == null)
			return null;

		// Get collection
		Collection<XmlDoc.Element> items = r.elements("ae");
		if (items == null)
			return null;
		//
		String names = "";
		for (XmlDoc.Element item : items) {
			String regName = item.value("@name");
			names += regName + ",";
			//
			if (name.equals(regName)) {
				String host = item.value("host");
				String port = item.value("port");
				String aet = item.value("aet");
				String[] ae = { host, port, aet };
				return ae;
			}
		}

		int l = names.length();
		String t = names.substring(0, l - 1);
		throw new Exception("Failed to look up the AE name '" + name
				+ "' in the DICOM AE registry - available names are '" + t + "'");

	}

	public class DICOMHandler extends com.pixelmed.network.MultipleInstanceTransferStatusHandler {
		private int _nFailed = 0;
		private int _nCompleted = 0;
		private int _nRemaining = 0;
		private int _nWarning = 0;
		String _instanceUID = null;

		private DICOMHandler() {

		};

		public void updateStatus(int nRemaining, int nCompleted, int nFailed, int nWarning, String sopInstanceUID) {

			_nFailed = nFailed;
			_nCompleted = nCompleted;
			_nRemaining = nRemaining;
			_nWarning = nWarning;
			_instanceUID = sopInstanceUID;
		}

		public int getNumberCompleted() {

			return _nCompleted;
		}

		public int getNumberFailed() {

			return _nFailed;
		}

		public int getNumberRemaining() {

			return _nRemaining;
		}

		public int getNumberWarning() {

			return _nWarning;
		}

		public String sopInstanceUID() {

			return _instanceUID;
		}

		public String toString() {

			return _instanceUID + ":" + _nCompleted + "/" + +_nFailed + "/" + _nWarning + "/" + _nRemaining;
		}

	}

}
