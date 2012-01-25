package nig.mf.pssd.client.bruker;

import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import nig.iio.bruker.BrukerFileUtils;
import nig.iio.bruker.BrukerMeta;
import nig.iio.bruker.NIGBrukerIdentifierMetaData;
import nig.iio.metadata.ImageMetaDataContainer;
import nig.mf.Executor;
import nig.mf.client.util.ClientExecutor;
import nig.mf.plugin.util.DateUtil;
import nig.mf.pssd.client.util.CiteableIdUtil;
import arc.mf.client.RemoteServer;
import arc.mf.client.ServerClient;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;



public class ParaVisionUpload {

	// This class sets the defaults for arguments that can be passed in
	// to the main porgram
	public static class Options {

		public boolean verbose = false;
		public long wait = 60000;	
		public int image = 1;                          // Upload image file (0->no, 1->yes)
		public int fid = 2;                            // Upload FID file (0->no, 1->yes in own DataSet, 2->yes with Image DataSet)
		public int clevel = 0;                         // ZipArchiveOutputStream.DEFAULT_COMPRESSION;   Compression level
								                       // Takes a lot of time to compress data with not much transmission benefit.
		public String cid_delimiter = "_";             // We parse CIDs from <String><delim><cid> in the "SUBJECT_id" field of the SUbject meta-data file.
		                                               // The delimiter, if used, nust be a single character only.
	                                      	           // If no delimiter is required, set to the string "null". The string is then assume to hold just the CID
		public boolean cid_is_full = false;            // Are we supplying a full cid S.N.P.S or P.S only 
		public boolean auto_subject_create = false;    // Will we allow auto-subject creation ?
		public boolean nig_subject_meta_add = false;   // Using the NIG layout of "SUBJECT_id", parse the subject identifier and 
		                                               // locate subject meta-data 
		public String cid = null;                      // Citable ID of destination Subject. If supplied, over-rides that embedded in Subject file.

		public void print () {
			System.out.println("verbose              = " + verbose);
			System.out.println("wait                 = " + wait);
			System.out.println("image                = " + image);
			System.out.println("fid                  = " + fid);
			System.out.println("clevel               = " + clevel);
			System.out.println("cid                  = " + cid);
			System.out.println("cid_delimiter        = " + cid_delimiter);
			System.out.println("cid_is_full          = " + cid_is_full);
			System.out.println("auto_subject_create  = " + auto_subject_create);
			System.out.println("nig_subject_meta_add = " + nig_subject_meta_add);
		}
	}

	public static final String HELP_ARG = "--help";
	public static final String VERBOSE_ARG = "-verbose";
	public static final String WAIT_ARG = "-wait";
	public static final String UPLOAD_IMAGE_ARG = "-image";
	public static final String UPLOAD_FID_ARG = "-fid";
	public static final String COMPRESSION_LEVEL = "-clevel";
	public static final String FID_WITH_IMAGE = "-fid-with-image";
	public static final String CID_DELIMITER = "-cid-delimiter";
	public static final String CID_IS_FULL = "-cid-is-full";
	public static final String AUTO_SUBJECT_CREATE = "-auto-subject-create";
	public static final String NIG_SUBJECT_META_ADD = "-nig-subject-meta-add";
	public static final String CITABLE_ID = "-id";

	// User credential
	private static String domain_ = null;
	private static String user_ = null;
	private static String password_ = null;




	/**
	 * Creates a connection to a remote Mediaflux server.
	 * 
	 * @return the connection
	 * 
	 */
	private static ServerClient.Connection createServerConnection() throws Throwable {
		boolean useHttp = false;
		boolean encrypt = false;
		String host = System.getProperty("mf.host");
		if (host == null) {
			throw new Exception("Cannot find system property 'mf.host'");
		}

		String p = System.getProperty("mf.port");
		if (p == null) {
			throw new Exception("Cannot find system property 'mf.port'");
		}
		int port = Integer.parseInt(p);

		String transport = System.getProperty("mf.transport");
		if (transport == null) {
			throw new Exception("Cannot find system property 'mf.transport'");
		}

		if (transport.equalsIgnoreCase("TCPIP")) {
			useHttp = false;
			encrypt = false;
		} else if (transport.equalsIgnoreCase("HTTP")) {
			useHttp = true;
			encrypt = false;
		} else if (transport.equalsIgnoreCase("HTTPS")) {
			useHttp = true;
			encrypt = true;
		} else {
			throw new Exception("Unexpected transport: " + transport + ", expected one of [tcpip,http,https]");
		}

		RemoteServer server = new RemoteServer(host, port, useHttp, encrypt);
		ServerClient.Connection cxn = server.open();
		return cxn;
	}

	/**
	 * 
	 * the main function of this command line tool.
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {

		String srcPath = null;
		Options ops = new Options();

		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase(HELP_ARG)) {
				printHelp(System.out);
				System.exit(0);
			} else if (args[i].equalsIgnoreCase(VERBOSE_ARG)) {
				ops.verbose = true;
			} else if (args[i].equalsIgnoreCase(WAIT_ARG)) {
				if (++i == args.length) {
					System.err.println("ParaVisionUpload: error: -wait option must specify a positive integer.");
					System.exit(1);
				}
				try {
					ops.wait = Long.parseLong(args[i]) * 1000;
				} catch (Throwable t) {
					System.err.println("ParaVisionUpload: error: -wait option must specify a positive integer.");
					System.exit(1);
				}
				if (ops.wait < 0) {
					System.err.println("ParaVisionUpload: error: -wait option must specify a positive integer.");
					System.exit(1);
				}
			} else if (args[i].equalsIgnoreCase(UPLOAD_IMAGE_ARG)) {
				if (++i == args.length) {
					System.err.println("ParaVisionUpload: error: -image option must specify a positive integer.");
					System.exit(1);
				}
				try {
					ops.image = Integer.parseInt(args[i]);
				} catch (Throwable t) {
					System.err.println("ParaVisionUpload: error: -image option must specify a positive integer (0 or 1).");
					System.exit(1);
				}
				if (ops.image < 0 || ops.image > 1 ) {
					System.err.println("ParaVisionUpload: error: -image option must specify a positive integer (0 or 1).");
					System.exit(1);
				}
			} else if (args[i].equalsIgnoreCase(UPLOAD_FID_ARG)) {
				if (++i == args.length) {
					System.err.println("ParaVisionUpload: error: -fid option must specify a positive integer.");
					System.exit(1);
				}
				try {
					ops.fid = Integer.parseInt(args[i]);
				} catch (Throwable t) {
					System.err.println("ParaVisionUpload: error: -fid option must specify a positive integer (0, 1 or 2).");
					System.exit(1);
				}
				if (ops.fid < 0 || ops.fid > 2 ) {
					System.err.println("ParaVisionUpload: error: -fid option must specify a positive integer (0, 1, or 2).");
					System.exit(1);
				}
			} else if (args[i].equalsIgnoreCase(COMPRESSION_LEVEL)) {
				if (++i == args.length) {
					System.err.println("ParaVisionUpload: error: -clevel option must specify a non-negativeinteger.");
					System.exit(1);
				}
				try {
					ops.clevel = Integer.parseInt(args[i]);
				} catch (Throwable t) {
					System.err.println("ParaVisionUpload: error: -clevel option must specify a positive integer");
					System.exit(1);
				}
			} else if (args[i].equalsIgnoreCase(CITABLE_ID)) {
				if (++i == args.length) {
					System.err.println("ParaVisionUpload: error: -id option must specify a CID string.");
					System.exit(1);
				}
				ops.cid = args[i];
			} else if (args[i].equalsIgnoreCase(CID_DELIMITER)) {
				ops.cid_delimiter = args[++i];
				if (ops.cid_delimiter.equals("null")) {
					ops.cid_delimiter = null;
				} else {
					int l = ops.cid_delimiter.length();
					if (l!=1) {
						System.err.println("ParaVisionUpload: error: -cid-delimiter option must specify a single character.");
						System.exit(1);		    		
					}
				}
			} else if (args[i].equalsIgnoreCase(CID_IS_FULL)) {
				ops.cid_is_full = true;
			} else if (args[i].equalsIgnoreCase(AUTO_SUBJECT_CREATE)) {
				ops.auto_subject_create= true;
			} else if (args[i].equalsIgnoreCase(NIG_SUBJECT_META_ADD)) {
				ops.nig_subject_meta_add= true;
			} else if (srcPath == null) {
				srcPath = args[i];
			} else {
				System.err.println("ParaVisionUpload: error: unexpected argument = " + args[i]);
				printHelp(System.err);
				System.exit(1);
			}
		}

		// CHeck on fid upload method
		if (ops.fid==2) {
			if (!(ops.image==1)) {
				System.err.println("To upload the fid file into the image DataSet you must also set -image to 1");
				printHelp(System.err);
				System.exit(1);
			}
		}

		if (ops.verbose) ops.print();
		//
		if (srcPath == null) {
			System.err.println("ParaVisionUpload: error: missing argument: src-path");
			printHelp(System.err);
			System.exit(1);
		}

		try {

			// Get user credential
			domain_ = System.getProperty("mf.domain");
			if (domain_ == null) {
				throw new Exception("Cannot find system property 'mf.domain'");
			}

			user_ = System.getProperty("mf.user");
			if (user_ == null) {
				throw new Exception("Cannot find system property 'mf.user'");
			}

			password_ = System.getProperty("mf.password");
			if (password_ == null) {
				throw new Exception("Cannot find system property 'mf.password'");
			}

			upload(new File(srcPath), ops);

		} catch (Throwable t) {
			System.err.println("ParaVisionUpload: error: " + t.getMessage());
			t.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Bruker Directory Hierarchy is
	 * nmr
	 *    <Study (Session) Directory >
	 *       Subject
	 *       <Acquisition Directories>
	 *          imnd
	 *          acqp
	 *          fid
	 *          pdata 
	 *             <Reconstruction Directories>
	 *                 2dseq
	 *                 reco
	 *                 meta
	 * 
	 * @param cxn
	 * @param dir
	 * @param ops
	 * @throws Throwable
	 */
	private static void upload(File dir, Options ops) throws Throwable {
		if (!dir.exists()) {
			throw new Exception("Directory does not exist: " + dir.getAbsolutePath());
		}
		if (BrukerFileUtils.isStudyDir(dir)) {
			if (ops.verbose) System.out.println("Input is study level");
			uploadStudy(dir, ops);
		} else if (BrukerFileUtils.isAcqDir(dir)) {
			if (ops.verbose) System.out.println("Input is acquisition level");
			if (ops.image==1) {
				File[] recoDirs = BrukerFileUtils.getRecoDirs(dir);
				uploadSeries(recoDirs, ops);
			}
			if (ops.fid==1) {
				File studyDir = BrukerFileUtils.getParentStudyDir(dir);
				File fidFile = BrukerFileUtils.getFidFile(dir);
				uploadFidFile (studyDir, dir, fidFile, ops);				
			}
		} else if (BrukerFileUtils.isPdataDir(dir)) {
			if (ops.verbose) System.out.println("Input is Pdata level");
			if (ops.image==1) {
				File[] recoDirs = BrukerFileUtils.getRecoDirs(dir);
				uploadSeries(recoDirs, ops);
			}
			// Cannot upload fid file from this level
		} else if (BrukerFileUtils.isRecoDir(dir)) {
			System.out.println("Input is reconstruction level");
			if (ops.image==1) {
				uploadSeries(dir, ops);
			}
			// Cannot upload fid file from this level
		} else {
			throw new Exception(dir.getAbsolutePath() + " is not a valid Bruker data directory.");
		}
	}

	/**
	 * Iterate over the acquisition directories (one per 'image' acquisition)
	 * and upload all of the reconstructions for that acquisition
	 * 
	 * @param cxn
	 * @param studyDir
	 * @param ops
	 * @throws Throwable
	 */
	private static void uploadStudy(File studyDir, Options ops) throws Throwable {
		File[] acqDirs = BrukerFileUtils.getAcqDirs(studyDir);
		if (acqDirs != null) {
			for (int i = 0; i < acqDirs.length; i++) {
				if (ops.verbose) {
					System.out.println("   Acquisition " + acqDirs[i].toString());
				}
				
				// Upload image, possibly with FID as well
				if (ops.image==1) {
					File[] recoDirs = BrukerFileUtils.getRecoDirs(acqDirs[i]);
					uploadSeries(recoDirs, ops);
				}

				// Upload FID file into own DataSet
				if (ops.fid==1) {
					File fidFile = BrukerFileUtils.getFidFile(acqDirs[i]);
					if (fidFile!=null) {
						// Sometimes acquisutions have no data
						uploadFidFile (studyDir, acqDirs[i], fidFile, ops);
					}
				}
			}
		}
	}

	/**
	 * Upload one fid file (the Primary raw data before it is reconstructed) into a Primary DataSet
	 * 
	 * @param cxn
	 * @param studyDir
	 * @param acqDir
	 * @param fidFile
	 * @param ops
	 * 
	 * @throws Throwable
	 */
	private static void uploadFidFile (File studyDir, File acqDir, File fidFile, Options ops) throws Throwable {
		if (ops.verbose) {
			System.out.print("Uploading " + fidFile.getAbsolutePath() + "...");
		}

		// Create a zip file of the fid file. This is the whole tree but stopping at the "pdata" level
		// as this holds the reconstruction images 
		File zipFile = File.createTempFile("nig_pv_upload", ".zip");
		BrukerFileUtils.createFidZip(acqDir, zipFile, ops.clevel);
		if (ops.verbose){
			System.out.println("Size of fid file = " + zipFile.length());
		}

		// Create the connection to MF. We defer it to now because the creation of large zip
		// files was triggering an MF timeout. So each fid is uploaded with a new connection
		ServerClient.Connection cxn = createServerConnection();
		cxn.connect(domain_, user_, password_);


		try {
			//
			// Find and read the Bruker meta-data files that identify the Series (PSSD DataSet) and the Study
			File subjectFile = BrukerFileUtils.getFile(studyDir, "subject");
			if (subjectFile == null) {
				throw new Exception("Could not find subject file.");
			}
			BrukerMeta subjectMeta = new BrukerMeta(subjectFile);

			File acqpFile = BrukerFileUtils.getFile(acqDir, "acqp");
			if (acqpFile == null) {
				throw new Exception("Could not find acqp file.");
			}
			BrukerMeta acqpMeta = new BrukerMeta(acqpFile);

			// Extract the required P.S citable ID from the Bruker SUBJECT_id meta-data in the  Bruker subject file
			// EXception if CID or SUbject does not exist
			String subjectCID = getSubjectCID (cxn, subjectMeta, ops);

			// Extract optional P.S.ExM or P.S.ExM.St from the SUBJECT_study_name meta-data in the Bruker subject file 
			String exMethodCID = null;
			String studyCID = null;
			getStudyExMethodCIDs (cxn, subjectMeta, subjectCID, exMethodCID, studyCID, ops);		


			// Search for a pre-existing Study in Mediaflux by the studyUID. We only look for the Study
			// in the CID tree of interest.  If the Study already exists elsewhere, that's fine.
			//
			// (1) If study is found in Mediaflux, update it; 
			// (2) If study is not found in Mediaflux then
			//    (2.1) no studyCID found in SUBJECT_study_name field of Bruker subject file, create a new study.
			//    (2.2) studyCID found in SUBJECT_study_name field of Bruker subject file, it is a pre-created study
			//           (without UID), try update it.
			// Extract some identifying meta-data from the Bruker meta
			Study study = updateOrCreateStudy (cxn, subjectMeta, subjectCID, exMethodCID, studyCID);
			if (studyCID==null) studyCID = study.id();

			// We have now dealt with the Study.  Move on to the DataSets (Series)
			// Acquisition level meta-data

			// NB the time is in format : e.g. <09:56:25  8 Apr 2010>
			// Convert to standard MF time
			String acqTime = convertAcqTime (acqpMeta);
			String protocol = acqpMeta.getValueAsString("ACQ_protocol_name");
			String seriesDescription = acqpMeta.getValueAsString("ACQ_scan_name");

			// Reconstruction level meta-data. We don't have these for the fid file
			String seriesUID = null;
			String seriesID = null;
			String seriesName = protocol;

			// Search for the Bruker series/DataSet in Mediaflux by seriesUID
			// How can we find a pre-existing fid file without meta-data ???
			// PrimaryDataSet brukerSeries = PrimaryDataSet.find(cxn, studyCID, seriesUID, seriesID);
			PrimaryDataSet brukerSeries = null;

			// Update/upload
			boolean isImage = false;
			if (brukerSeries != null) {

				// if found, update it
				PrimaryDataSet.update(cxn, brukerSeries.id(), seriesName, seriesDescription, seriesUID, seriesID,
						protocol, acqTime, zipFile);
				PSSDUtil.logInfo(cxn, "Bruker primary dataset " + brukerSeries.id() + " has been updated.");
			} else {
				// if not found, create it
				brukerSeries = PrimaryDataSet.create(cxn, isImage, studyCID, seriesName, seriesDescription, seriesUID,
						seriesID, protocol, acqTime, zipFile);
				PSSDUtil.logInfo(cxn, "Bruker series/dataset " + brukerSeries.id() + " has been created.");
			}

			// Search for DICOM derivation series/dataset in Mediaflux.
			// The chain should be Primary (fid) -> Derived (bruker) -> Derived (DICOM)
			/*
			DerivationDataSet dicomSeries = DerivationDataSet.find(cxn, studyCID, seriesUID, seriesID);
			if (dicomSeries != null) {

				 // if found, set/update primary on the DICOM derivation series/dataset.
				DerivationDataSet.setPrimary(cxn, dicomSeries.id(), brukerSeries.id());
				PSSDUtil.logInfo(cxn, "DICOM derivation dataset/series " + dicomSeries.id()
						+ " has set primary to Bruker primary dataset/series " + brukerSeries.id());
			}
			 */
			if (ops.verbose) {
				System.out.println("done.");
			}
		} finally {
			zipFile.delete();
			cxn.close();
		}
	}


	/**
	 * Upload Series (volumes) in the given reconstruction directories. One volume per reconstruction directory.
	 * 
	 * @param cxn
	 * @param recoDirs
	 * @param ops
	 * @parma doImage If true upload the Bruker image reconstruction
	 * @throws Throwable
	 */
	private static void uploadSeries(File[] recoDirs, Options ops) throws Throwable {
		if (recoDirs != null) {
			for (int j = 0; j < recoDirs.length; j++) {
				File recoDir = recoDirs[j];
				uploadSeries(recoDir, ops);
			}
		}
	}

	/**
	 * Upload one Series (Volume) from  one reconstruction directory into one DataSet
	 * 
	 * @param cxn
	 * @param recoDir
	 * @param ops
	 * 
	 * @throws Throwable
	 */
	private static void uploadSeries(File recoDir, Options ops) throws Throwable {
		if (ops.verbose) {
			System.out.print("Uploading " + recoDir.getAbsolutePath() + "...");
		}

		// Create a zip file that includes the image volume and required meta files.
		File zipFile = File.createTempFile("nig_pv_upload", ".zip");
		boolean fidWithImage = (ops.fid==2);
		if (ops.verbose) {
			System.out.println("Creating zip file with contents in : " + zipFile.toString());
		}
		//
		BrukerFileUtils.createImageSeriesZip(recoDir, zipFile, fidWithImage, ops.clevel);
		if (ops.verbose) {
			System.out.println("Finished creating zip file of size " + zipFile.length());
		}

		// Create the connection to MF. We defer it to now because the creation of large zip
		// files was triggering an MF timeout. So each Series is uploaded with a new connection
		ServerClient.Connection cxn = createServerConnection();
		cxn.connect(domain_, user_, password_);


		// Read Bruker meta-data files	
		try {

			// Find and read the Bruker meta-data files that identify the Series (PSSD DataSet) and the Study
			File subjectFile = BrukerFileUtils.getSubjectFile(recoDir);
			if (subjectFile == null) {
				throw new Exception("Could not find subject file.");
			}
			BrukerMeta subjectMeta = new BrukerMeta(subjectFile);
			//System.out.println(subjectMeta.toString());

			File acqpFile = BrukerFileUtils.getAcqpFile(recoDir);
			if (acqpFile == null) {
				throw new Exception("Could not find acqp file.");
			}
			BrukerMeta acqpMeta = new BrukerMeta(acqpFile);

			// Sometimes the reco file (reconstruction parameters is missing).  We only get the UID out of it
			// so can live without it.
			File recoFile = BrukerFileUtils.getRecoFile(recoDir);
			BrukerMeta recoMeta = null;
			if (recoFile == null) {
				PSSDUtil.logInfo(cxn, "Skipping reconstruction as could not find reco file in " + recoDir.getAbsolutePath());
				zipFile.delete();
				cxn.close();
				return;
				// throw new Exception("Could not find reco file in " + recoDir.getAbsolutePath());
			} else {
				recoMeta = new BrukerMeta(recoFile);
			}

			// Extract the required P.S citable ID from the Bruker SUBJECT_id meta-data in the  Bruker subject file
			// EXception if CID or SUbject does not exist
			String subjectCID = getSubjectCID (cxn, subjectMeta, ops);

			// Extract optional P.S.ExM or P.S.ExM.St from the SUBJECT_study_name meta-data in the Bruker subject file 
			String exMethodCID = null;
			String studyCID = null;
			getStudyExMethodCIDs (cxn, subjectMeta, subjectCID, exMethodCID, studyCID, ops);		


			// Search for a pre-existing Study in Mediaflux by the studyUID. We only look for the Study
			// in the CID tree of interest.  If the Study already exists elsewhere, that's fine.
			//
			// (1) If study is found in Mediaflux, update it; 
			// (2) If study is not found in Mediaflux then
			//    (2.1) no studyCID found in SUBJECT_study_name field of Bruker subject file, create a new study.
			//    (2.2) studyCID found in SUBJECT_study_name field of Bruker subject file, it is a pre-created study
			//           (without UID), try update it.
			// Extract some identifying meta-data from the Bruker meta
			Study study = updateOrCreateStudy (cxn, subjectMeta, subjectCID, exMethodCID, studyCID);
			if (studyCID==null) studyCID = study.id();

			// We have now dealt with the Study.  Move on to the DataSets (Series)
			// Acquisition level meta-data

			// NB the time is in format : e.g. <09:56:25  8 Apr 2010>
			// Convert to standard MF time
			String acqTime = convertAcqTime (acqpMeta);
			String protocol = acqpMeta.getValueAsString("ACQ_protocol_name");
			String seriesDescription = acqpMeta.getValueAsString("ACQ_scan_name");

			// Reconstruction level meta-data
			String seriesID = "" + BrukerFileUtils.getSeriesId(recoDir);
			String seriesUID = null;
			if (recoMeta!=null) {
				seriesUID = recoMeta.getValueAsString("RECO_base_image_uid");
			}

			// Combination meta-data
			String seriesName = BrukerFileUtils.getParentAcqDir(recoDir).getName() + "_" + protocol;

			// Search for the Bruker series/DataSet in Mediaflux by seriesUID
			PrimaryDataSet brukerSeries = PrimaryDataSet.find(cxn, studyCID, seriesUID, seriesID);


			// Update/upload
			boolean isImage = true;
			if (brukerSeries != null) {
				if (ops.verbose) {
					System.out.println("Updating DataSet " + brukerSeries.id());
				}

				// if found, update it
				PrimaryDataSet.update(cxn, brukerSeries.id(), seriesName, seriesDescription, seriesUID, seriesID,
						protocol, acqTime, zipFile);
				PSSDUtil.logInfo(cxn, "Bruker primary FID dataset " + brukerSeries.id() + " has been updated.");
			} else {
				if (ops.verbose) {
					System.out.println("Creating DataSet ");
				}

				// if not found, create it
				brukerSeries = PrimaryDataSet.create(cxn, isImage, studyCID, seriesName, seriesDescription, seriesUID,
						seriesID, protocol, acqTime, zipFile);
				PSSDUtil.logInfo(cxn, "Bruker primary FID dataset " + brukerSeries.id() + " has been created.");
			}
			if (ops.verbose) {
				System.out.println("Finished creating/updating DataSet " + brukerSeries.id());
			}



			// The chain should be Primary (fid) -> Derived (bruker) -> Derived (DICOM)
			// Search for DICOM derivation series/dataset in Mediaflux.
			DerivationDataSet dicomSeries = DerivationDataSet.find(cxn, studyCID, seriesUID, seriesID);
			if (dicomSeries != null) {

				// if found, set/update primary on the DICOM derivation series/dataset.
				DerivationDataSet.setPrimary(cxn, dicomSeries.id(), brukerSeries.id());
				PSSDUtil.logInfo(cxn, "DICOM derivation dataset/series " + dicomSeries.id()
						+ " has set primary to Bruker primary dataset/series " + brukerSeries.id());
			}
			if (ops.verbose) {
				System.out.println("done.");
			}
		} finally {
			zipFile.delete();
			cxn.close();
		}
	}

	/**
	 * 
	 * prints the help information for this command line tool.
	 * 
	 * @param os
	 * 
	 */
	public static void printHelp(PrintStream os) {
		os.println("ParaVisionUpload");
		os.println();
		os.println("Synopsis:");
		os.println("   Packages up ParaVision study, experiment or processed experiment");
		os.println("   data into a ZIP archive and associates with the corresponding DICOM");
		os.println("   series in a Mediaflux server.");
		os.println();
		os.println("Usage:");
		os.println("   " + ParaVisionUpload.class.getName() + " [options..] <src-path>");
		os.println();
		os.println("     src-path is a file path for a ParaVision, of the following form:");
		os.println("       study      - <DiskUnit>/data/<user>/nmr/<name>");
		os.println("       experiment - <DiskUnit>/data/<user>/nmr/<name>/<expno>");
		os.println("       processed  - <DiskUnit>/data/<user>/nmr/<name>/<expno>/pdata/<procno>");
		os.println();
		os.println("Java Properties:");
		os.println("    -mf.host      [Required]: The name or IP address of the Mediaflux host.");
		os.println("    -mf.port      [Required]: The server port number.");
		os.println("    -mf.domain    [Required]: The logon domain.");
		os.println("    -mf.user      [Required]: The logon user.");
		os.println("    -mf.password  [Required]: The logon user's password.");
		os.println("    -mf.transport [Optional]: Required if the port number is non-standard.");
		os.println("                              One of [HTTP, HTTPS, TCPIP]. By default the");
		os.println("                              following transports are inferred from the port:");
		os.println("                                80    = HTTP");
		os.println("                                443   = HTTPS");
		os.println("                                other = TCPIP");
		os.println();
		os.println("Options:");
		os.println("   " + HELP_ARG + "         Displays this help. <src-path> not required in when");
		os.println("                  requesting help.");
		os.println("   " + VERBOSE_ARG + "       Enables tracing. By default, no tracing.");
		os.println();
		os.println("   " + WAIT_ARG + " <secs>  (default 60) specifies the amount of time to try to find");
		os.println("                  a matching series.");
		os.println("   " + UPLOAD_IMAGE_ARG + " <0 or 1> (default 1) indicates whether to upload the Image Series");
		os.println("   " + UPLOAD_FID_ARG + " <0, 1, 2> (default 2) indicates whether to upload the fid file (1=own DataSet, 2=with image zip in Image DataSet");
		os.println("   " + COMPRESSION_LEVEL + " <clevel> (default 0) gives the compression level 0-6");
		os.println("   " + CID_DELIMITER + " We parse the CID from the 'SUBJECT_id' field of the SUbject meta-data file");
		os.println("                         This gives a delimiter to separate names from the CID in the form: <string><delim><cid>.");
		os.println("                         Defaults to '_'. Use the string 'null' if none required in which case the name string should hold just the CID");
		os.println("   " + CID_IS_FULL+ " if supplied indicates that the Server.Namespace CID prefix should not be added.");
		os.println("                      When not supplied, the CID is assumed to be partial starting with the Project.");
		os.println("   " + AUTO_SUBJECT_CREATE+ " if supplied indicates that subjects can be auto-created from the CID.");
		os.println("   " + CITABLE_ID + " if supplied then this gives the Subject's citable ID. Over-rides extraction from the Subject meta-data");
		os.println("   " + NIG_SUBJECT_META_ADD + " if supplied indicates that the 'SUBJECT_id' field is configured with the NIG layout.");
		os.println("                                Parse this and extract extra meta-data and locate on the Subject");
		os.println();
	}

	/**
	 * Get subject CID. If does not exist, optionally try to create
	 * 
	 * @param cxn
	 * @param subjectMeta
	 * @param ops
	 * @return
	 * @throws Throwable
	 */
	private static String getSubjectCID (ServerClient.Connection cxn, BrukerMeta subjectMeta, Options ops) throws Throwable {

		// Externally supplied SUbject ID over-rides values found in data
		String subjectCID = null;
		if (ops.cid != null) {
			subjectCID = ops.cid;
		} else {
			// Get the Subject CID
			// Strip off leading characters ahead of the delimiter if requested
			subjectCID = subjectMeta.getValueAsString("SUBJECT_id");
			if (ops.cid_delimiter!=null) {
				subjectCID = stripCID (ops, subjectCID);
			}
		}
		//
		String projectIDRoot = CiteableIdUtil.getProjectIdRoot(cxn);
		if (!ops.cid_is_full) subjectCID = projectIDRoot + "." + subjectCID;			
		//
		if (!CiteableIdUtil.isSubjectId(subjectCID)) {
			String errMsg = null;
			if (ops.cid!=null) {
				errMsg = "The supplied -cid string " + ops.cid + " is not a valid citable ID";
			} else {
				errMsg = "No valid citeable id found in SUBJECT_id field of the Bruker subject file."
					+ " (SUBJECT_id=" + subjectMeta.getValueAsString("SUBJECT_id") + ")";
			}
			PSSDUtil.logError(cxn, errMsg);
			throw new Exception(errMsg);
		}

		// See if the Subject already exists
		// If not and if requested, auto-create
		if (!Subject.exists(cxn, subjectCID)) {
			if (ops.auto_subject_create) {
				if (!createSubject (cxn,  subjectCID, subjectMeta)) {
					String errMsg = "Failed to auto-create the Subject with "
						+ " (SUBJECT_id=" + subjectMeta.getValueAsString("SUBJECT_id") + ")";
					PSSDUtil.logError(cxn, errMsg);
					throw new Exception(errMsg);		
				}


			} else {
				String errMsg = "No valid citeable id found in SUBJECT_id field of the Bruker subject file."
					+ " (SUBJECT_id=" + subjectMeta.getValueAsString("SUBJECT_id") + ")";
				PSSDUtil.logError(cxn, errMsg);
				throw new Exception(errMsg);
			}			
		}

		// Now see if the user wants to locate any meta-data on the Subject by parsing the
		// Subject identifier further.  This is domain specific.  
		if (ops.nig_subject_meta_add) {
			addNIGSubjectMeta (cxn, subjectCID, subjectMeta, ops);
		}

		return subjectCID;
	}


	private static void  addNIGSubjectMeta (ServerClient.Connection cxn, String subjectCID, BrukerMeta subjectMeta, Options ops) throws Throwable {

		// Get the full subject identifier
		String fullID= subjectMeta.getValueAsString("SUBJECT_id");

		// Parse into NIG bits
		// TODO: reimplement with a pattern specification like the Shopping Cart
		// <Project Description>_<Coil>_<Animal ID>_<Gender>_<Experiment Group>_<Invivo/exvivo>_<date><delim><cid>

		// First pull off the CID. The only way to do this safely is by working backwards
		// since the delimiter may be embedded elsewhere in the strings
		String id = fullID;
		if (ops.cid==null && ops.cid_delimiter!=null) {        // If the CID is supplied externally it's because it's not in the SUbject_ID so don't stript it
			id = removeCID (ops.cid_delimiter, fullID);
		}

		// Split into bits around the "_" delimiter (not the same as ops.cid_delimiter)
		String[] parts = id.split("_");
		if (parts.length==7) {
			setNIGSubjectMetaData (cxn, subjectCID, parts);		
		} else {
			// Not the correct format
			String errMsg = "The subject identifier was not of the correct form to extract the NIG meta-data";
			PSSDUtil.logError(cxn, errMsg);
		}	
	}

	private static void setNIGSubjectMetaData (ServerClient.Connection cxn, String subjectCID, String[] parts) throws Throwable {


		// We need to know where things go.  That's why this is a domain-specific activity
		NIGBrukerIdentifierMetaData brukerMeta = new NIGBrukerIdentifierMetaData(parts);

		// Pop the meta-data in the container
		ImageMetaDataContainer metaContainer = new ImageMetaDataContainer(brukerMeta);

		// Create the executor wrapper 
		Executor executor = new ClientExecutor(cxn);

		// Add it on
		nig.iio.metadata.DomainMetaManager.addSubjectMetaData(executor, subjectCID, metaContainer);
	}

	/**
	 * Function to try to auto-create a Subject of the given CID, if it is of the correct depth
	 * 
	 * 
	 * @param cid
	 * @param sm
	 * @return
	 * @throws Throwable
	 */
	private static boolean createSubject (ServerClient.Connection cxn, String subjectCID, BrukerMeta subjectMeta) throws Throwable {

		// CHeck CID depth
		if (!nig.mf.pssd.CiteableIdUtil.isSubjectId(subjectCID)) return false;

		// Get Project CID and get any Methods
		String pid = nig.mf.pssd.CiteableIdUtil.getProjectId(subjectCID);
		XmlStringWriter w = new XmlStringWriter();
		w.add("id", pid);
		XmlDoc.Element r = cxn.execute("om.pssd.object.describe", w.document());
		Collection methods = r.elements("object/method");

		// We can't proceed if there are no Methods or more than one (how could we choose ?)
		if (methods==null) return false;
		if (methods.size()>1) return false;

		// Get the Method CID
		Iterator it = methods.iterator();
		XmlDoc.Element method = (XmlDoc.Element)it.next();
		String mid = method.value("id");
		if (mid==null) return false;

		// If the Subject CID has not been allocated, import it.  
		if (!nig.mf.pssd.client.util.CiteableIdUtil.cidExists(cxn, subjectCID)) {
			String importedCID = nig.mf.pssd.client.util.CiteableIdUtil.importCid(cxn, subjectCID);
			if (!importedCID.equals(subjectCID)) {
				String errMsg = "   Imported Subject CID:" + importedCID + " is not consistent with expected:" + subjectCID;
				PSSDUtil.logError(cxn, errMsg);
				throw new Exception(errMsg);
			}
		}


		// We already know the asset does not exist so now we can try to create it
		String subjectNumber = nig.mf.pssd.CiteableIdUtil.getLastSection(subjectCID);
		w = new XmlStringWriter();
		w.add("pid", pid);
		w.add("subject-number", subjectNumber);
		w.add("method", mid);

		// Create the subject
		// TODO: set the pre-specified Method meta-data. I don't have a service for this...
		r = cxn.execute("om.pssd.subject.create", w.document());

		return true;
	}




	private static void getStudyExMethodCIDs (ServerClient.Connection cxn, BrukerMeta subjectMeta, 
			String subjectCID, String exMethodCID, String studyCID, Options ops) throws Throwable  {

		String projectIDRoot = CiteableIdUtil.getProjectIdRoot(cxn);
		//String cid2 = projectIDRoot + "." + subjectMeta.getValueAsString("SUBJECT_study_name");

		String cid2 =  subjectMeta.getValueAsString("SUBJECT_study_name");
		if (ops.cid_delimiter!=null) cid2 = stripCID(ops, cid2);
		if (!ops.cid_is_full) cid2 = projectIDRoot + "." + cid2;
		if (CiteableIdUtil.isCiteableId(cid2) && cid2.startsWith(subjectCID)) {
			if (CiteableIdUtil.isStudyId(cid2)) {
				studyCID = cid2;
				exMethodCID = nig.mf.pssd.CiteableIdUtil.getParentId(studyCID);
			} else if (CiteableIdUtil.isExMethodId(cid2)) {
				exMethodCID = cid2;
			}
		}
	}


	/**
	 * Parse <String><delim><cid> and return <cid>
	 * The <String> may contain the delimiter as well of course
	 * 
	 * @param ops
	 * @param cid
	 * @return
	 * @throws Throwable
	 */
	private static String stripCID (Options ops, String cid) throws Throwable {
		String[] t = cid.split(ops.cid_delimiter);  
		int n = t.length;
		if (n>=1) cid = t[n-1];
		return cid;
	}

	// Pull of the CID
	// Delimiter already checked to be a single character
	private static String removeCID (String delimiter, String cid) throws Throwable {
		int l = cid.length() - 1;		 
		int idx = cid.lastIndexOf(delimiter);
		//
		String id = cid;
		if (idx>=0) {
			id = cid.substring(0, idx);
		}
		return id;
	}


	private static Study updateOrCreateStudy (ServerClient.Connection cxn, BrukerMeta subjectMeta, String subjectCID, String exMethodCID, String studyCID) throws Throwable {

		// Search for a pre-existing Study in Mediaflux by the studyUID. We only look for the Study
		// in the CID tree of interest.  If the Study already exists elsewhere, that's fine.
		//
		// (1) If study is found in Mediaflux, update it; 
		// (2) If study is not found in Mediaflux then
		//    (2.1) no studyCID found in SUBJECT_study_name field of Bruker subject file, create a new study.
		//    (2.2) studyCID found in SUBJECT_study_name field of Bruker subject file, it is a pre-created study
		//           (without UID), try update it.
		// Extract some identifying meta-data from the Bruker meta
		String studyID = subjectMeta.getValueAsString("SUBJECT_study_nr");
		String studyUID = subjectMeta.getValueAsString("SUBJECT_study_instance_uid");
		String studyName = PSSDUtil.BRUKER_STUDY_TYPE;
		Study study = Study.find(cxn, subjectCID, exMethodCID, studyUID);       // Restrict query to CID tree
		if (study == null) {
			// Can't find the Bruker study pre-existing
			if (studyCID != null) {

				// There is a pre-created Study in MF but since we did not find the Bruker study by 
				// UID we just update the Study with the Bruker meta-data
				study = Study.update(cxn, studyCID, studyName, null, studyUID, studyID, domain_, user_);
				PSSDUtil.logInfo(cxn, "Bruker study " + study.id() + " has been updated.");
			} else if (exMethodCID != null) {

				// Auto-create Study with given parent ExMethod and set Bruker meta-data
				study = Study.create(cxn, exMethodCID, studyName, null, studyUID, studyID, domain_, user_);
				PSSDUtil.logInfo(cxn, "Bruker study " + study.id() + " has been created.");
			} else {

				// Auto-create Study with first ExMethod of Subject and set Bruker meta-data
				study = Study.createFromSubjectCID(cxn, subjectCID, studyName, null, studyUID, studyID, domain_, user_);
				PSSDUtil.logInfo(cxn, "Bruker study " + study.id() + " has been created.");
			}
		} else {

			// Found the Study UID pre-existing in MF in the desired CID tree.  
			// Update it with Bruker meta-data
			study = Study.update(cxn, study.id(), study.name(), study.description(), studyUID, studyID, domain_, user_);
			PSSDUtil.logInfo(cxn, "Bruker study " + studyCID + " has been updated.");
		}
		return study;
	}

	private static String convertAcqTime (BrukerMeta acqpMeta) throws Throwable {

		String t = acqpMeta.getValueAsString("ACQ_time"); 
		String acqTime = null;
		try {
			acqTime = DateUtil.convertDateString(t, "HH:mm:ss dd MMM yyyy", "dd-MMM-yyyy HH:mm:ss");
		} catch (Exception e) {
			// Better to have no time than wrong time
			acqTime = null;
		}
		return acqTime;
	}



}
