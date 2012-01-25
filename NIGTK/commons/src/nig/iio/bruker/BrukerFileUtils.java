package nig.iio.bruker;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import nig.compress.ZipUtil;
import nig.iio.analyze.AnalyzeHeader;

/**
 * 
 * This class provides methods to access Bruker ParaVision data directories and files. Below is the directory structure
 * of Bruker ParaVision data directory:
 * 
 * ${disk_unit}/${user}/nmr/${name}/${expno}/pdata/${procno}
 * ${disk_unit}/${user}/nmr/${study_name}/${acquisition_no}/pdata/${reconstruction_no}
 * ${disk_unit}/${user}/nmr/${session_name}/${run_no}/pdata/${reconstruction_no}
 * 
 * Note: Depend on the source of the documentation, the description about the Bruker ParaVision data directory uses
 * different terms: study <=> session exp(experiment) <=> run <=> acq(acquisition) <=> series pdata(processed data)
 * proc(process) <=> reco(reconstruction)
 * 
 * 
 * More detailed information about Bruker data format: {@link http
 * ://www.rockefeller.edu/spectroscopy/manuals/protonBruker.pdf} {@link http
 * ://imaging.mrc-cbu.cam.ac.uk/imaging/FormatBruker}
 * 
 * 
 * 
 * @author Wilson Liu
 * 
 */

public class BrukerFileUtils {

	private BrukerFileUtils() {

	}

	/**
	 * Check if the specified directory is a Bruker nmr directory.
	 * 
	 * @param dir
	 * @return
	 */
	public static boolean isNmrDir(File dir) {

		if (!dir.isDirectory()) {
			return false;
		}
		if (dir.getName().equals("nmr")) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Check if the specified directory is a Bruker study/session directory.
	 * 
	 * @param dir
	 * @return
	 */
	public static boolean isStudyDir(File dir) {
		if (!dir.isDirectory()) {
			return false;
		}
		File nmrDir = dir.getParentFile();
		File subjectFile = new File(dir.getAbsolutePath() + "/subject");
		if (nmrDir.getName().equals("nmr") && subjectFile.exists()) {
			if (subjectFile.isFile()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if the specified directory is a Bruker acquisition/series directory.
	 * 
	 * @param dir
	 * @return
	 */
	public static boolean isAcqDir(File dir) {
		if (!dir.isDirectory()) {
			return false;
		}
		File nmrDir = dir.getParentFile().getParentFile();
		File pdataDir = new File(dir.getAbsolutePath() + "/pdata");
		if (nmrDir.getName().equals("nmr") && pdataDir.exists()) {
			if (pdataDir.isDirectory()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if the specified directory is a Bruker pdata(Processed Data) directory.
	 * 
	 * @param dir
	 * @return
	 * 
	 */
	public static boolean isPdataDir(File dir) {
		if (!dir.isDirectory()) {
			return false;
		}
		File acqDir = dir.getParentFile();
		if (dir.getName().equals("pdata") && isAcqDir(acqDir)) {
			return true;
		}
		return false;
	}

	/**
	 * Check if the specified directory is a Bruker reconstruction directory. Normally an acquisition has only one
	 * reconstruction, but it is possible that an acquisition has multiple reconstructions. Therefore, if converting
	 * bruker data to other formats, each reconstruction within an acquisition will become an image of other format
	 * (e.g. a DICOM series, an Analyze/Nifti image)
	 * 
	 * @param dir
	 * @return
	 */
	public static boolean isRecoDir(File dir) {
		if (!dir.isDirectory()) {
			return false;
		}
		File pdataDir = dir.getParentFile();
		File nmrDir = pdataDir.getParentFile().getParentFile().getParentFile();
		if (pdataDir.getName().equals("pdata") && nmrDir.getName().equals("nmr")) {
			return true;
		}
		return false;
	}

	/**
	 * Get the acquisition sub-directories within the specified study/session directory.
	 * 
	 * @param studyDir
	 * @return An array of directories.
	 */
	public static File[] getAcqDirs(File studyDir) {
		if (!isStudyDir(studyDir)) {
			return null;
		}
		File[] acqDirs = studyDir.listFiles(new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory()) {
					if (isAcqDir(f)) {
						try {
							Integer.parseInt(f.getName());
							return true;
						} catch (NumberFormatException e) {
							return false;
						}
					}
				}
				return false;
			}
		});
		sortDirectoriesByName(acqDirs);
		return acqDirs;
	}

	
	/**
	 * Get the reconstruction sub-directories within the specified acquisition or pdata directory.
	 * 
	 * @param dir
	 *            an acq directory or a pdata directory
	 * @return
	 */
	public static File[] getRecoDirs(File dir) {
		File pdataDir;
		if (isAcqDir(dir)) {
			pdataDir = new File(dir.getAbsolutePath() + "/pdata");
			if (!pdataDir.exists()) {
				return null;
			}
		} else if (isPdataDir(dir)) {
			pdataDir = dir;
		} else {
			return null;
		}
		File[] recoDirs = pdataDir.listFiles(new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory()) {
					if (isRecoDir(f)) {
						try {
							Integer.parseInt(f.getName());
							return true;
						} catch (NumberFormatException e) {
							return false;
						}
					}
				}
				return false;
			}
		});
		sortDirectoriesByName(recoDirs);
		return recoDirs;
	}
	


	/**
	 * Sort the directories by its numeric name.
	 * 
	 * @param dirs
	 */
	private static void sortDirectoriesByName(File[] dirs) {
		Arrays.sort(dirs, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				int n1 = Integer.parseInt(((File) o1).getName());
				int n2 = Integer.parseInt(((File) o2).getName());
				return n1 - n2;
			}
		});
	}

	/**
	 * Create a series zip file. It zips the reco directory and other required meta files in its parent directories.
	 * 
	 * @param recoDir
	 * @param zipFile
	 * @param clevel compression level
	 * @throws Throwable
	 */
	public static void createImageSeriesZip(File recoDir, File zipFile, boolean fidWithImage, int clevel) throws Throwable {

		Set<File> filesToZip = new TreeSet<File>();
		findImageFilesToZip(recoDir, filesToZip, fidWithImage);
		ZipUtil.zip(filesToZip, getParentNmrDir(recoDir), zipFile, clevel);

	}

	/**
	 * Find the files to create series zip archive. All the required files are stored in the specified Set<File>
	 * 
	 * @param dir
	 * @param filesToZip
	 */
	private static void findImageFilesToZip(File dir, Set<File> filesToZip, boolean fidWithImage) {
		// "nmr" is "above" the study..
		// This exits the bottom-up recursive process
		if (dir.getName().toLowerCase().equals("nmr")) {
			return;
		}
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			// By only finding non-directories, we exclude other acquisition directories
			if (!files[i].isDirectory()) {
				
				// Filter out the fid (raw Fourier data). This is uploaded to a separate DataSet
				if (fidWithImage) {
					filesToZip.add(files[i]);
				} else {
					String name = files[i].getName().toUpperCase();
					boolean isFid = name.equals("FID") || name.equals("FID.ZIP") || name.equals("FID.GZ");
					if (!isFid) filesToZip.add(files[i]);  
				}
			}
		}
		// Now, go up to the parent..
		findImageFilesToZip(dir.getParentFile(), filesToZip, fidWithImage);
	}
	
	/**
	 * Create a fid zip file. It zips the full tree for this acquisition starting at the Study level
	 * but stops at the pdata level
	 * 
	 * @param acqDir
	 * @param zipFile
	 * @param clevel compression level
	 * @throws Throwable
	 */
	public static void createFidZip(File acqDir, File zipFile, int clevel) throws Throwable {

		Set<File> filesToZip = new TreeSet<File>();
		findFidFilesToZip(acqDir, filesToZip);
		ZipUtil.zip(filesToZip, getParentNmrDir(acqDir), zipFile, clevel);

	}

	/**
	 * Find the files to create series zip archive. All the required files are stored in the specified Set<File>
	 * 
	 * @param dir
	 * @param filesToZip
	 */
	private static void findFidFilesToZip(File dir, Set<File> filesToZip) {
		// "nmr" is "above" the study..
		// This exits the bottom-up recursive process
		if (dir.getName().toLowerCase().equals("nmr")) {
			return;
		}
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			// By only finding non-directories, we exclude other acquisition directories
			if (!files[i].isDirectory()) {
				filesToZip.add(files[i]);
			}
		}
		// Now, go up to the parent..
		boolean fidWithImage = false;                   // We are only in this function if we are uploading the fid to its own DataSet
		findImageFilesToZip(dir.getParentFile(), filesToZip, fidWithImage);
	}


	/**
	 * Returns the nmr directory.
	 * 
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public static File getParentNmrDir(File dir) throws Exception {

		if (isNmrDir(dir)) {
			return dir;
		} else if (isStudyDir(dir)) {
			return dir.getParentFile();
		} else if (isAcqDir(dir)) {
			return dir.getParentFile().getParentFile();
		} else if (isPdataDir(dir)) {
			return dir.getParentFile().getParentFile().getParentFile();
		} else if (isRecoDir(dir)) {
			return dir.getParentFile().getParentFile().getParentFile().getParentFile();
		} else {
			throw new Exception(dir.getAbsolutePath()
					+ " is not valid. It should be the descendant of the nmr directory.");
		}

	}

	/**
	 * Returns the study directory.
	 * 
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public static File getParentStudyDir(File dir) throws Exception {

		if (isStudyDir(dir)) {
			return dir;
		} else if (isAcqDir(dir)) {
			return dir.getParentFile();
		} else if (isPdataDir(dir)) {
			return dir.getParentFile().getParentFile();
		} else if (isRecoDir(dir)) {
			return dir.getParentFile().getParentFile().getParentFile();
		} else {
			throw new Exception(dir.getAbsolutePath()
					+ " is not valid.  It should be the descendant of the study directory.");
		}

	}

	/**
	 * Returns the acquisition directory.
	 * 
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public static File getParentAcqDir(File dir) throws Exception {

		if (isAcqDir(dir)) {
			return dir;
		} else if (isPdataDir(dir)) {
			return dir.getParentFile();
		} else if (isRecoDir(dir)) {
			return dir.getParentFile().getParentFile();
		} else {
			throw new Exception(dir.getAbsolutePath()
					+ " is not valid.  It should be the descendant of the acquisition directory.");
		}

	}

	/**
	 * Returns the pdata directory.
	 * 
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public static File getParentPdataDir(File dir) throws Exception {

		if (isPdataDir(dir)) {
			return dir;
		} else if (isRecoDir(dir)) {
			return dir.getParentFile();
		} else {
			throw new Exception(dir.getAbsolutePath()
					+ " is not valid.  It should be the descendant of the pdata directory.");
		}

	}

	/**
	 * Returns the subject (which is in text format and includes bruker metadata) file in the study directory.
	 * 
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public static File getSubjectFile(File dir) throws Exception {

		File studyDir = getParentStudyDir(dir);
		return getFile(studyDir, "subject");

	}

	/**
	 * Returns the imnd file in the acquisition directory.
	 * 
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public static File getImndFile(File dir) throws Exception {

		File acqDir = getParentAcqDir(dir);
		return getFile(acqDir, "imnd");

	}

	/**
	 * Returns the acqp file in the acquisition directory.
	 * 
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public static File getAcqpFile(File dir) throws Exception {

		File acqDir = getParentAcqDir(dir);
		return getFile(acqDir, "acqp");

	}

	/**
	 * Returns the method file in the acquisition directory.
	 * 
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public static File getMethodFile(File dir) throws Exception {

		File acqDir = getParentAcqDir(dir);
		return getFile(acqDir, "method");

	}

	/**
	 * Returns the 	 free-induction decay (FID) file within the acquisition directory.
	 * 
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public static File getFidFile(File dir) throws Exception {

		File acqDir = getParentAcqDir(dir);
		return getFile(acqDir, "fid");

	}

	/**
	 * Returns the reco file in the reconstruction directory.
	 * 
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public static File getRecoFile(File dir) throws Exception {

		if (!isRecoDir(dir)) {
			throw new Exception(dir.getAbsolutePath() + " is not a valid Bruker/ParaVision reco data directory.");
		}
		return getFile(dir, "reco");

	}

	/**
	 * Returns the 2dseq file in the reconstruction directory.
	 * 
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public static File get2dseqFile(File dir) throws Exception {

		if (!isRecoDir(dir)) {
			throw new Exception(dir.getAbsolutePath() + " is not a valid Bruker/ParaVision reco data directory.");
		}
		return getFile(dir, "2dseq");

	}

	/**
	 * Returns the meta file in the reconstruction directory.
	 * 
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public static File getMetaFile(File dir) throws Exception {

		if (!isRecoDir(dir)) {
			throw new Exception(dir.getAbsolutePath() + " is not a valid Bruker/ParaVision reco data directory.");
		}
		return getFile(dir, "meta");

	}

	/**
	 * Returns the d3proc file in the reconstruction directory.
	 * 
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public static File getD3procFile(File dir) throws Exception {

		if (!isRecoDir(dir)) {
			throw new Exception(dir.getAbsolutePath() + " is not a valid Bruker/ParaVision reco data directory.");
		}
		return getFile(dir, "d3proc");

	}

	/**
	 * Returns the roi file in the reconstruction directory.
	 * 
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public static File getRoiFile(File dir) throws Exception {

		if (!isRecoDir(dir)) {
			throw new Exception(dir.getAbsolutePath() + " is not a valid Bruker/ParaVision reco data directory.");
		}
		return getFile(dir, "roi");

	}

	/**
	 * Returns the procs file in the reconstruction directory.
	 * 
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public static File getProcsFile(File dir) throws Exception {

		if (!isRecoDir(dir)) {
			throw new Exception(dir.getAbsolutePath() + " is not a valid Bruker/ParaVision reco data directory.");
		}
		return getFile(dir, "procs");

	}

	public static File getFile(File dir, final String filename) throws Exception {

		if (!dir.isDirectory()) {
			throw new Exception(dir.getAbsolutePath() + " is not a directory.");
		}
		if (!dir.exists()) {
			throw new Exception(dir.getAbsolutePath() + " does not exist.");
		}
		File[] files = dir.listFiles(new FileFilter() {
			public boolean accept(File f) {
				if (f.getName().equals(filename)) {
					return true;
				} else {
					return false;
				}
			}
		});
		if (files != null) {
			if (files.length > 0) {
				return files[0];
			}
		}
		return null;

	}

	public static int getSeriesId(File f) throws Exception {

		File recoDir = null;
		File acqDir = null;
		if (f.isDirectory()) {
			if (isRecoDir(f)) {
				recoDir = f;
				acqDir = getParentAcqDir(f);
			}
		} else if (f.isFile()) {
			if (f.getName().equals("reco")) {
				recoDir = f.getParentFile();
				acqDir = getParentAcqDir(f);
			}
		}
		if (recoDir == null || acqDir == null) {
			throw new Exception("Failed to get series id.");
		}
		int expno = Integer.parseInt(acqDir.getName());
		int procno = Integer.parseInt(recoDir.getName());
		return (procno | (expno << 16));

	}

	public static void convertToAnalyze(File recoFile, File analyzeFile) throws Throwable {

		File recoDir = recoFile.getParentFile();
		if (!isRecoDir(recoDir)) {
			throw new Exception("Could not locate the reco directory.");
		}
		File acqpFile = getAcqpFile(recoDir);
		File methodFile = getMethodFile(recoDir);
		File _2dseqFile = get2dseqFile(recoDir);

		AnalyzeHeader hdr = new AnalyzeHeader();
		Arrays.fill(hdr.pixdim, (float) 2.0);
		hdr.bitpix = 8;
		hdr.datatype = AnalyzeHeader.DataType.DT_UNSIGNED_CHAR;
		hdr.vox_offset = 0;
		hdr.originator[0] = 46;
		hdr.originator[1] = 64;
		hdr.originator[2] = 37;
		hdr.dim[1] = 91;
		hdr.dim[2] = 109;
		hdr.dim[3] = 91;
		hdr.dim[4] = 1;
		hdr.glmin = 0;
		hdr.glmax = 255;
		hdr.roi_scale = (float) 0.00392157;

		/*
		 * Parse reco file
		 */
		BrukerMeta recoMeta = new BrukerMeta(recoFile);
		String recoWordType = recoMeta.getValueAsString("RECO_wordtype");
		if (recoWordType.equals("_16BIT_SGN_INT")) {
			hdr.datatype = AnalyzeHeader.DataType.DT_SIGNED_SHORT;
			hdr.bitpix = 16;
		} else if (recoWordType.equals("_32BIT_SGN_INT")) {
			hdr.datatype = AnalyzeHeader.DataType.DT_SIGNED_INT;
			hdr.bitpix = 32;
		} else {
			hdr.datatype = AnalyzeHeader.DataType.DT_UNSIGNED_CHAR;
			hdr.bitpix = 8;
		}

		String recoByteOrder = recoMeta.getValueAsString("RECO_byte_order");
		boolean bigEndian = false;
		if (recoByteOrder.equals("littleEndian")) {
			bigEndian = false;
		} else {
			// EPI is bigendian
			bigEndian = true;
		}

		short[] recoSize = recoMeta.getValueAsShortArray("RECO_size");
		if (!(recoSize.length == 2 || recoSize.length == 3)) {
			throw new Exception("Invalid RECO_size in " + recoFile.getAbsolutePath() + ". Expecting 2 or 3.");
		}
		if (recoSize.length == 2) {
			hdr.dim[1] = recoSize[0];
			hdr.dim[2] = recoSize[1];
		} else if (recoSize.length == 3) {
			hdr.dim[1] = recoSize[0];
			hdr.dim[2] = recoSize[1];
			hdr.dim[3] = recoSize[2];
		}

		short[] recoTransposition = recoMeta.getValueAsShortArray("RECO_transposition");
		if (recoTransposition[0] < 0 || recoTransposition[0] > 1) {
			throw new Exception("Unsupported RECO_transposition: " + recoMeta.get("RECO_transposition").toString());
		}
		short transposition = recoTransposition[0];

		float[] recoFov = recoMeta.getValueAsFloatArray("RECO_fov");
		if (recoFov.length < 2 || recoFov.length > 3) {
			throw new Exception("Wrong dimension of FOV: " + recoFov.length);
		}
		// if(FOVx10==true){
		// recoFov[0] *= 10;
		// recoFov[1] *= 10;
		// recoFov[2] *= 10;
		// }
		float xFov = recoFov[0];
		float yFov = recoFov[1];
		float zFov = recoFov[2];
		
		/*
		 * parse acqp file.
		 */
		BrukerMeta acqpMeta = new BrukerMeta(acqpFile);

		String acqSliceSepnMode = acqpMeta.getValueAsString("ACQ_slice_sepn_mode");
		if (hdr.dim[3] > 1 && acqSliceSepnMode.toUpperCase().equals("EQUIDISTANT")
				&& acqSliceSepnMode.toUpperCase().equals("CONTIGUOUS")) {
			System.out
					.println("Acqp file suggests the slices are not equidistant. Analyze images assume equidistant slices.");
		}

		float[] acqSliceSepn = acqpMeta.getValueAsFloatArray("ACQ_slice_sepn");
		hdr.pixdim[4] = acqSliceSepn[0];

		short acqNSListSize = acqpMeta.getValueAsShortArray("ACQ_ns_list_size")[0];
		short transposeVol = acqNSListSize;

		short ni = acqpMeta.getValueAsShortArray("NI")[0];
		hdr.dim[3] = ni;

		String acqMethod = acqpMeta.getValueAsString("ACQ_method");
		if (acqMethod.toUpperCase().equals("BLIP_EPI")) {
			// if(epiAlwayBigEndian){
			// bigEndian = true;
			// }
		}
		
		short acqNrCompleted = acqpMeta.getValueAsShortArray("ACQ_nr_completed")[0];
		hdr.dim[4] = acqNrCompleted;

		/*
		 * parse method file. 
		 */
		BrukerMeta methodMeta = new BrukerMeta(methodFile);
		float[] pvmFovCm = methodMeta.getValueAsFloatArray("PVM_FovCm");
		xFov = pvmFovCm[0] * 10;
		yFov = pvmFovCm[1] * 10;
		
		/*
		 * save analyze header.
		 */
		hdr.originator[0] = 0;
		hdr.originator[1] = 0;
		hdr.originator[2] = 0;

		if(hdr.dim[1] ==0 || hdr.dim[2] ==0 || hdr.dim[3]==0){
			throw new Exception("Some dim equals zero.");
		}
		
		hdr.pixdim[1] = xFov / hdr.dim[1];
		hdr.pixdim[2] = yFov / hdr.dim[2];
		if(transposition ==1 && transposeVol >1){
			int transposeSliceWidth = (hdr.bitpix/8) * hdr.dim[1];
			int transposeSliceSize = transposeSliceWidth *hdr.dim[2];
			hdr.dim[4] = transposeVol;
			hdr.dim[1] = (short)(hdr.dim[1] / hdr.dim[4]);
		} else {
			transposition = 0;
		}
		
		long actualImgSize = _2dseqFile.length();
		long imgSize = (hdr.bitpix /8)*hdr.dim[1]*hdr.dim[2]*hdr.dim[3]*hdr.dim[4];
		int sliceWidth = (hdr.bitpix/8) * hdr.dim[1];
		int sliceSize = (hdr.bitpix/8) * hdr.dim[1]*hdr.dim[2];
		if(actualImgSize!=imgSize && actualImgSize%(sliceSize*hdr.dim[3])==0){
			hdr.dim[4] = (short) (actualImgSize/(sliceSize*hdr.dim[3]));
			imgSize = (hdr.bitpix /8)*hdr.dim[1]*hdr.dim[2]*hdr.dim[3]*hdr.dim[4];
		}
		if(hdr.dim[4]==0){
			throw new Exception("Error. No slices to convert.");
		}
		if(hdr.dim[4]>0){
			hdr.dim[0] = 4; // 4D data
		} else {
			hdr.dim[0] = 3;
		}
		short nVol = hdr.dim[4];
		short nSlice = hdr.dim[3];
		// if(FOVx10==true){
		// hdr.pixdim[2] *= 10;
		// hdr.pixdim[3] *= 10;
		// hdr.pixdim[4] *= 10;
		// }
	
		//TODO:


	}

}
