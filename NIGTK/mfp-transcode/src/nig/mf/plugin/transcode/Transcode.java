package nig.mf.plugin.transcode;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import nig.compress.ZipUtil;
import nig.dicom.siemens.CSAFileUtils;
import nig.io.FileUtils;
import nig.mf.MimeTypes;
import arc.archive.ArchiveExtractor;
import arc.archive.ArchiveInput;
import arc.archive.ArchiveRegistry;
import arc.mf.plugin.Exec;
import arc.mf.plugin.MimeTypeRegistry;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.transcode.Transcoder;
import arc.mf.plugin.transcode.TranscoderImpl;
import arc.mime.MimeType;

/**
 * Registration of conversions for the Mediaflux transcoding framework.
 */
public class Transcode {
	private static HashMap<String, Mapping> _mappings = new HashMap<String, Mapping>();


	public static class Mapping {

		public final String from;

		public final String fromDescription;

		public final String to;

		public final String toDescription;

		public final String provider;

		public final String[] arguments;

		public Mapping(String from, String fromDescription, String to,
				String toDescription, String program, String[] arguments) {

			this.from = from;
			this.fromDescription = fromDescription == null ? from : fromDescription;
			this.to = to;
			this.toDescription = toDescription == null ? to : toDescription;
			this.provider = program;
			this.arguments = arguments;
		}

	}

	// Transcode Proviers
	public static class Providers {

		private Providers() {

		}

		public static final String LONI_DEBABELER = "LONI Debabeler";

		public static final String PVCONV = "pvconv converter";

		public static final String NIG_TRANSCODER = "NIG Transcoder";
		
		public static final String MINC_TRANSCODER = "MINC Binary Transcoder";

	}


	private static void addMapping(String from, String fromDescription,
			String to, String toDescription, String provider, String[] arguments) {

		Mapping m = new Mapping(from, fromDescription, to, toDescription,
				provider, arguments);
		System.out.println("Adding mapping" + fromDescription + " / " + toDescription);
		_mappings.put(from + "-" + to, m);
		System.out.println("Number of mappings=" + _mappings.size());

	}

	public static Mapping getMapping(String fromMime, String toMime) {

		String mappingString = fromMime + "-" + toMime;
		return _mappings.get(mappingString);

	}

	public static Mapping getMapping(String mappingString) {

		return _mappings.get(mappingString);

	}

	public static String[] getMappingStrings() {

		Set<String> keys = _mappings.keySet();
		String[] mappingStrings = new String[keys.size()];
		keys.toArray(mappingStrings);
		return mappingStrings;

	}

	public static String[] getMappingToMimeTypes() {

		Collection<Transcode.Mapping> values = _mappings.values();
		String[] toMimeTypes = new String[values.size()];
		int i = 0;
		for (Transcode.Mapping m : values) {
			toMimeTypes[i++] = m.to;
		}
		return toMimeTypes;

	}

	static {

		// DICOM to ...
		addMapping(MimeTypes.DICOM_SERIES, "DICOM series",
				MimeTypes.ANALYZE_SERIES_NL, "Analyze(Neurological)",
				Providers.LONI_DEBABELER, new String[] { "-target", "analyze",
						"-mapping", "DicomToAnalyze_NL_Wilson_05Jan2007.xml" });
		addMapping(MimeTypes.DICOM_SERIES, "DICOM series",
				MimeTypes.ANALYZE_SERIES_RL, "Analyze(Radiological)",
				Providers.LONI_DEBABELER, new String[] { "-target", "analyze",
						"-mapping", "DicomToAnalyze_RL_Wilson_05Jan2007.xml" });
		addMapping(MimeTypes.DICOM_SERIES, "DICOM series",
				MimeTypes.NIFTI_SERIES, "NIFTI series",
				Providers.LONI_DEBABELER, new String[] { "-target", "nifti",
						"-mapping", "DicomToNifti_Wilson_23Feb2007.xml" });

		// debabeler
		addMapping(MimeTypes.DICOM_SERIES, "DICOM series",
				MimeTypes.MINC_SERIES, "MINC series with debabeler",
				Providers.LONI_DEBABELER, new String[] { "-target", "minc",
						"-mapping", "DicomToMinc_26Oct2011.xml" });
		
		// MINC binary. Can have only one transcoder per mimetype in/out pair
		/*
		addMapping(MimeTypes.DICOM_SERIES, "DICOM series",
				MimeTypes.MINC_SERIES, "MINC series with MINC dcm2mnc binary",
				Providers.MINC_TRANSCODER, null);
				*/

		// 
		addMapping(MimeTypes.DICOM_SERIES, "DICOM series",
				MimeTypes.SIEMENS_RDA, "RDA(Siemens Spectrum)",
				Providers.NIG_TRANSCODER, null);

		// Bruker to...
		addMapping(MimeTypes.BRUKER_SERIES, "Bruker/Paravision image series",
				MimeTypes.ANALYZE_SERIES_NL, "Analyze(Neurological)",
				Providers.PVCONV, null);
		addMapping(MimeTypes.BRUKER_SERIES, "Bruker/Paravision image series",
				MimeTypes.ANALYZE_SERIES_RL, "Analyze(Radiological)",
				Providers.PVCONV, null);
		addMapping(MimeTypes.BRUKER_SERIES, "Bruker/Paravision image series",
				MimeTypes.MINC_SERIES, "Minc",
				Providers.PVCONV, null);


	}

	// Java Maximum Heap Size
	public static String JavaXmxOption = "-Xmx2000m";

	public static Collection<Transcoder> transcoders() {

		if (_mappings == null) return null;
		
		Vector<Transcoder> transcoders = new Vector<Transcoder>(_mappings.size());
		for (Iterator<String> it = _mappings.keySet().iterator(); it.hasNext();) {
			Mapping mp = _mappings.get(it.next());
			if (mp != null) {
				try {
					MimeTypeRegistry.define(mp.to, mp.toDescription);
				} catch (Throwable t) {
					System.out.println(t.getMessage());
				}
				transcoders.add(new Transcoder(mp.from, mp.to,
						new TranscodeBridge(mp)));
			}
		}
		return transcoders;

	}

	public static String transcode(File in, String fromMime, MimeType fromContentType, String toMime,
			File out) throws Throwable {

		return transcode(in, fromContentType, getMapping(fromMime, toMime), out);

	}

	public static String transcode(File in, MimeType fromContentType, Mapping mapping, File out) throws Throwable {
		
		File tmpDir = PluginService.createTemporaryDirectory();
		Vector<File> files = new Vector<File>();
		try {
			
			// Unpack the content
			String name = fromContentType.name();
			if (name.equalsIgnoreCase("application/zip")) {
				ZipUtil.unzip2(in, tmpDir, files, true);
			} else if (name.equalsIgnoreCase("application/arc-archive")) {
				ArchiveInput ai = ArchiveRegistry.createInput(in);
				ArchiveExtractor.extract(ai, tmpDir, false, true, true);
			} else {
				throw new Exception ("Content MimeType " + name + " is not handled");
			}
			//
			Collection<File> inputs = Arrays.asList(tmpDir.listFiles());
			if (mapping.provider.equals(Providers.LONI_DEBABELER)) {
				final String debabelerJar = "loni-debabeler.jar";
				String[] jars = new String[] { debabelerJar };
				String[] options = { JavaXmxOption, "-Djava.awt.headless=true" };
				String mainClass = "edu.ucla.loni.debabel.events.engine.DebabelerEngine";
				String[] mainArgs = concat(
						new String[] { "-input", tmpDir.getAbsolutePath() },
						mapping.arguments);
				String[] args = concat(mainClass, mainArgs);
				// Run Debabeler externally. Runs in its own thread and so does not block
				// the server for large transcodes.
				String t = Exec.execJava(jars, options, args, null);
				// Run Debabeler internally.
				// edu.ucla.loni.debabel.events.engine.DebabelerEngine.main(mainArgs);
			} else if (mapping.provider.equals(Providers.PVCONV)) {
				// Iterate through the input files. For Bruker, should be a
				// single directory holding the Bruker structure
				for (Iterator<File> it = inputs.iterator(); it.hasNext();) {
					File f = it.next();
					// Convert the bruker series:
					// pvconv.pl <in> -outdir <out>
					// This command must be installed in the mediaflux
					// plugin/bin directory
					String cmd = "pvconv.pl";
					String args = "";
					//
					if (mapping.to.equals(MimeTypes.ANALYZE_SERIES_RL)) {
						args += "radio ";
					} else if (mapping.to.equals(MimeTypes.ANALYZE_SERIES_NL)) {
						args += "noradio ";
					} else if (mapping.to.equals(MimeTypes.MINC_SERIES)) {
						args += "-outtype minc ";
					}
					args += f.getAbsolutePath() + " ";
					args += "-outdir " + tmpDir.getAbsolutePath();
					Exec.exec(cmd, args);
				}
			} else if (mapping.provider.equals(Providers.NIG_TRANSCODER)) {
				// Siemens DICOM -> Siemens RDA
				for (Iterator<File> it = inputs.iterator(); it.hasNext();) {
					File f = it.next();
					File rdaFile = new File(tmpDir, f.getName().substring(0,
							f.getName().lastIndexOf("."))
							+ ".rda");
					if (CSAFileUtils.isCSADicomFile(f)) {
						CSAFileUtils.convertToSiemensRDA(f, rdaFile);
					}
				}
			} else if (mapping.provider.equals(Providers.MINC_TRANSCODER)) {
				// DICOM to MINC via the external MINC binary dcm2mnc
				// TODO: test when get Minc installed on Mac OS X
				for (Iterator<File> it = inputs.iterator(); it.hasNext();) {
					File f = it.next();
					// Convert the DICOM series:
					// dcm2mnc <in-dir> <out-dir>
					String cmd = "dcm2mnc";
					//
					String p = tmpDir.getAbsolutePath();
					String args = p + " " + p;
					
					// Will throw exception if binary missing
					Exec.exec(cmd, args);
				}
			}
			// Because we transform in-situ, generate a list of output files
			// with the input filtered out
			LinkedList<File> outputs = new LinkedList<File>(
					Arrays.asList(tmpDir.listFiles()));
			if (!outputs.removeAll(inputs)) {
				throw new Exception(
						"Error separating output files and input files.");
			}
			// Now zip the transformed files into the output
			if (outputs.size() > 0) {
				ZipUtil.zip(outputs, tmpDir, out);
			}
		} finally {
			FileUtils.delete(tmpDir);
		}
		return MimeTypes.ZIP;

	}

	/**
	 * Converts between a plugin transcoder and a transformer.
	 */
	private static class TranscodeBridge implements TranscoderImpl {

		private Mapping _mapping;

		/**
		 * Constructor.
		 */
		public TranscodeBridge(Mapping mapping) {

			_mapping = mapping;

		}

		/**
		 * Description of the transcoding.
		 */
		public String description() {

			if (_mapping != null) {
				return _mapping.from + " to " + _mapping.to + " transcoder.";
			}
			return null;

		}

		/**
		 * Returns the actual/encapsulation output MIME type of this transcoder
		 * (as distinct from the logical MIME type) for the given "fromMime"
		 * type to the "toMime" type. The output MimeType maybe different to the
		 * "toMime" type. This method is used by the caller, prior to doing the
		 * transcoding, to determine the output MimeType to prepare for.
		 */
		@Override
		public String outputType(MimeType fromMimeType, MimeType toMimeType) {

			return "application/x-zip";
		}

		/**
		 * Apply the transformation. In our usage in is the input zip file
		 * holding many files for one DataSet (e.g. one volume) out is the
		 * output zip file holdin the transformed data
		 */
		@Override
		public String transcode(File in, MimeType fromType,
				MimeType fromContentType, MimeType toType, File out,
				Map<String, String> params) throws Throwable {

			return Transcode.transcode(in, fromContentType, _mapping, out);
		}



	}

	protected static String[] concat(String[] a, String[] b) {

		if (a == null && b == null) {
			return null;
		}
		if (a == null) {
			return b;
		}
		if (b == null) {
			return a;
		}
		String[] c = new String[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);

		return c;

	}

	protected static String[] concat(String e, String[] a) {

		if (e == null) {
			return a;
		}
		return concat(new String[] { e }, a);

	}

	protected static String[] concat(String[] a, String e) {

		if (e == null) {
			return a;
		}
		return concat(a, new String[] { e });

	}

}