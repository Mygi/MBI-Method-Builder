package nig.mf.pssd.client.dicom;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;


import arc.mf.client.ServerClient;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;

import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.SetOfDicomFiles;
import com.pixelmed.network.StorageSOPClassSCU;


import nig.compress.ZipUtil;
import nig.dicom.util.DicomFileCheck;
import nig.dicom.util.DicomModify;
import nig.io.FileUtils;

/**
 * DICOM Client utilities
 * 
 * @author nebk
 *
 */

public class Dicom {
	
	/**
	 * Upload data in specified directory to the Mediaflux DICOM server via the dicom.ingest service interface
	 * and the nig.pssd DICOM engine
	 * 
	 * @param cxn The authenticated connection to the desired Mediaflux server
	 * @param id  Full citable ID of the form ServerUUID.NameSpace.Project.Subject.{ExMethod}.{Study}. 
	 * @param check If true, check that files are DICOM files
	 * @param clean If true, clean up the temporary directory where files are copied/edited
	 * @param dir The directory to recursively search for DICOM files to upload
	 * @param metaSetService the service to supply extra meta-data to the Subject from
	 * domain-specific meta-data.  Null for none
	 * @param anonName if true, anonymise the patient name field (0010,0010) in the DICOM file header
	 * 
	 */
	public static void uploadMF (ServerClient.Connection cxn, String id, Boolean check, Boolean clean, File dir,
			String metaSetService, Boolean anonName) throws Throwable {
		
		if (id==null) {
			throw new Exception("You must supply a citable identifier");
		} 

		// Copy valid files to temporary directory and flatten out file structure in preparation for zipping
		File tempDir = FileUtils.createUniqueDirectory("/tmp/DICOM-Service-");
		copyFilesToTemp (tempDir, dir, check);
		
		// ZIp up files into a temporary file, flattening out the structure as we go
		String name = "/tmp/DICOM-Service-" + Long.toString(System.nanoTime()) + ".zip";
		File zipFile= new File(name);
		ZipUtil.zip(tempDir, zipFile);
		
		// DIscover if server is configured for full or partial CIDs
		XmlStringWriter w = new XmlStringWriter();
		XmlDoc.Element r = cxn.execute("network.describe");
		
		// Now upload
		// I think you can't supply the id and prefix for the default PSS engine, only our nig.dicom engine
		// So you can probably only upload without specifying the ID for PSS engine. So these arguments
		// would probably be ignored.
		w = new XmlStringWriter();
		w.add("engine", "nig.dicom");
		w.add("arg", new String[] {"name", "nig.dicom.asset.namespace.root"}, "dicom");     // COuld be caller given arg.
		w.add("arg", new String[] {"name", "nig.dicom.id.ignore-non-digits"}, "true");
		w.add("arg", new String[] {"name", "nig.dicom.subject.create"}, "true");
		//
		if (metaSetService!=null) w.add("arg", new String[] {"name", "nig.dicom.subject.meta.set-service"}, metaSetService);
		if (anonName) {
			w.add("anonymize", true);
		} else {
			w.add("anonymize", false);
		}
		w.add("arg", new String[] {"name", "nig.dicom.id.citable"}, id);              // The CID prefix is not used

		//
		ServerClient.Input in = ServerClient.createInputFromURL("file:" + zipFile.getAbsolutePath());
		r = cxn.execute("dicom.ingest", w.document(), in, null);
		
		// CLeanup
		if (clean) {
			org.apache.commons.io.FileUtils.deleteDirectory(tempDir);
			zipFile.delete();
		}
	}




	/**
	 * Upload data in specified directory to DICOM server with the pixelmed client library
	 * Optional editing of DICOM meta-data is available
	 * 
	 * @param id  Full citable ID of the form ServerUUID.NameSpace.Project.Subject.{ExMethod}.{Study}
	 * @param dicomElement THe DICOM element to replace with the CID of the form '<group>,<element>' like "0010,0020"
	 * @param host The DICOM server host
	 * @param port The DICOM server port
	 * @param callingAET The server's AETitle
	 * @param calledAET  The client's AETitle
	 * @param check If true, check that files are DICOM files
	 * @param clean If true, clean up the temporary directory where files are copied/edited
	 * @param dir The directory to recursively search for DICOM files to upload
	 * 
	 * @throws Throwable
	 */
	public static void uploadSCU (String id, String dicomElement, String host, Integer port,
			String callingAET, String calledAET, Boolean check, Boolean clean, File dir) throws Throwable {

		if (!dir.isDirectory()) {
			throw new Exception ("The supplied path " + dir.getAbsolutePath() + " is not a directory");
		}

		// Copy (valid) files to temporary directory
		// File tempDir0 = org.apache.commons.io.FileUtils.getTempDirectory();
		SetOfDicomFiles dicomFiles = null;
		File tempDir = null;
		//
		if (id!=null) {
			tempDir = FileUtils.createUniqueDirectory("/tmp/DICOM-Client-");
			dicomFiles = copyFilesToTemp (tempDir, dir, check);

			// Edit meta-data
			if (id!=null) {
				String[] s = dicomElement.split(",");      
				if (s.length != 2) {
					throw new Exception ("DicomClient: the dicom element for edit must be of the form '<group>,<element>' e.g. '0010,0020'");
				}
				//
				editMetaData (dicomFiles, id, s[0], s[1]);				
			}
		} else {
			dicomFiles = makeList (dir, check);
		}

		// Now upload
		upload (dicomFiles, host, port, callingAET, calledAET);

		// Clean up
		if (clean && tempDir!=null) {
			org.apache.commons.io.FileUtils.deleteDirectory(tempDir);
		}

	}

	/**
	 * Strip leading 2 digits off CID
	 * 
	 * @param id
	 * @return
	 */
	public static String stripCID (String id) {
		// Should make some length checks

		int idx = id.indexOf(".");
		int idx2 = id.indexOf(".", idx+1);
		return id.substring(idx2+1);
	}

	private static void upload (SetOfDicomFiles dicomFiles, String host, Integer port, String callingAET, String calledAET) throws Throwable {

		System.out.println("   Uploading files");
		int compressionLevel = 0;
		int debugLevel = 0;

		// If there is an exception, let it fail. We could also write a Handler and continue
		// in the face of failures (e.g. see the om.pssd.dicom.send service)
		new StorageSOPClassSCU(host, port, calledAET, callingAET,
				dicomFiles, compressionLevel, null, null, 0, debugLevel);
	}


	private static void editMetaData (SetOfDicomFiles dicomFiles, String id, String group, String element) throws Throwable {
		int iGroup = Integer.parseInt(group, 16);
		int iElement = Integer.parseInt(element, 16);
		AttributeTag aTag = new AttributeTag(iGroup, iElement);

		Iterator<com.pixelmed.dicom.SetOfDicomFiles.DicomFile> it = dicomFiles.iterator();
		while (it.hasNext()) {
			com.pixelmed.dicom.SetOfDicomFiles.DicomFile file = it.next();
			File t = new File(file.getFileName());
			DicomModify.editFile(t, aTag, id);
		}
	}


	private static SetOfDicomFiles copyFilesToTemp (File tempDir, File inFile, Boolean check) throws Throwable {

		System.out.println("   Copying (valid) files to " + tempDir.getAbsolutePath());

		// Copy all (valid) files in the directory hieararchy and flatten out structure
		Collection<File> files = org.apache.commons.io.FileUtils.listFiles(inFile, null, true);
		SetOfDicomFiles dicomFiles = new SetOfDicomFiles();
		int i = 0;
		if (check) {
			for (File file : files) {
				if (DicomFileCheck.isDicomFile(file)) {
					String outFileName = tempDir.getAbsolutePath() + "/" + i + ".dcm";
					File outFile = new File(outFileName);
					org.apache.commons.io.FileUtils.copyFile(file, outFile);
					i++;
					dicomFiles.add(outFile);
				}
			}
		} else {
			for (File file : files) {
				String outFileName = tempDir.getAbsolutePath() + "/" + i + ".dcm";
				File outFile = new File(outFileName);
				org.apache.commons.io.FileUtils.copyFile(file, outFile);
				i++;
				dicomFiles.add(outFile);
			}
		}

		if (i==0) {
			throw new Exception ("   No (valid) files were copied to the temp. directory ");
		} else {
			System.out.println("   " + i + " (valid) files were copied to the temp. directory");
		}

		//
		return dicomFiles;
	}

	private static SetOfDicomFiles makeList (File inFile, Boolean check) throws Throwable {

		System.out.println("   Make list of (valid) files to  upload");

		Collection<File> files = org.apache.commons.io.FileUtils.listFiles(inFile, null, true);
		SetOfDicomFiles dicomFiles = new SetOfDicomFiles();
		int i = 0;
		if (check) {
			for (File file : files) {
				if (DicomFileCheck.isDicomFile(file)) {
					dicomFiles.add(file);
					i++;
				}
			}
		} else {
			for (File file : files) {
				dicomFiles.add(file);
				i++;
			}
		}

		if (i==0) {
			throw new Exception ("   No (valid) files were found");
		} else {
			System.out.println("   " + i + " (valid) files were found");
		}

		//
		return dicomFiles;
	}

}
