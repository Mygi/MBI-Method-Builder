package nig.compress;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;

public class ZipUtil {

	private ZipUtil() {

	}

	public static final int BUFFER_SIZE = 2048;
	public static final int CLEVEL = ZipArchiveOutputStream.DEFAULT_COMPRESSION;

	public static void zip(File dir, File zipFile) throws Throwable {

		zip(dir, true, zipFile, CLEVEL);

	}

	public static void zip(File dir, boolean self, File zipFile) throws Throwable {

		zip(dir, self, zipFile, CLEVEL);

	}

	public static void zip(File dir, File zipFile, int clevel) throws Throwable {

		zip(dir, true, zipFile, clevel);

	}

	public static void zip(File dir, boolean self, File zipFile, int clevel) throws Throwable {

		if (self) {
			zip(new File[] { dir }, dir.getParentFile(), zipFile, clevel);
		} else {
			zip(dir.listFiles(), dir, zipFile, clevel);
		}

	}

	public static void zip(File[] filesToZip, File baseDir, File zipFile) throws Throwable {

		zip(filesToZip, baseDir, zipFile, ZipArchiveOutputStream.DEFAULT_COMPRESSION);

	}

	public static void zip(File[] filesToZip, File baseDir, File zipFile, int clevel) throws Throwable {

		ZipArchiveOutputStream os = new ZipArchiveOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile),
				BUFFER_SIZE));
		try {
			os.setLevel(clevel);
			os.setMethod(ZipArchiveOutputStream.DEFLATED);
			zip(filesToZip, baseDir, os);
		} finally {
			os.close();
		}

	}

	public static void zip(Collection<File> filesToZip, File baseDir, File zipFile) throws Throwable {

		zip(filesToZip, baseDir, zipFile, ZipArchiveOutputStream.DEFAULT_COMPRESSION);

	}

	public static void zip(Collection<File> filesToZip, File baseDir, File zipFile, int clevel) throws Throwable {

		File[] files = new File[filesToZip.size()];
		int i = 0;
		for (File f : filesToZip) {
			files[i++] = f;
		}
		zip(files, baseDir, zipFile, clevel);

	}

	private static void zip(File[] filesToZip, File baseDir, ZipArchiveOutputStream os) throws Throwable {

		String base = baseDir.getAbsolutePath();
		byte buffer[] = new byte[BUFFER_SIZE];
		for (File f : filesToZip) {
			String name = f.getAbsolutePath();
			if (name.startsWith(base)) {
				name = name.substring(base.length());
			}
			if (name.startsWith(System.getProperty("file.separator"))) {
				name = name.substring(1);
			}
			if (f.isDirectory()) {
				ZipArchiveEntry entry = new ZipArchiveEntry(f, name + "/");
				os.putArchiveEntry(entry);
				os.closeArchiveEntry();
				zip(f.listFiles(), baseDir, os);
			} else {
				ZipArchiveEntry entry = new ZipArchiveEntry(f, name);
				entry.setSize(f.length());
				os.putArchiveEntry(entry);
				BufferedInputStream is = new BufferedInputStream(new FileInputStream(f), BUFFER_SIZE);
				int count;
				try {
					while ((count = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
						os.write(buffer, 0, count);
					}
				} finally {
					is.close();
				}
				os.closeArchiveEntry();
			}
		}

	}

	/**
	 * Extract zip file to the specified directory. (Using Apache Commons Compress Library)
	 * 
	 * @param file
	 *            the zip file to extract.
	 * @param toDir
	 *            the destination directory
	 * @throws Throwable
	 */
	public static void unzip2(File file, File toDir) throws Throwable {

		unzip2(file, toDir, null);

	}

	/**
	 * Extract zip file to the specified directory also save the extracted files into the specified Collection. (Using
	 * Apache Commons Compress Library)
	 * 
	 * @param file
	 *            the zip file to extract
	 * @param toDir
	 *            the destination directory.
	 * @param files
	 *            the Collection of the extracted files
	 * @throws Throwable
	 */
	public static void unzip2(File file, File toDir, Collection<File> files) throws Throwable {

		unzip2(file, toDir, files, false);

	}

	/**
	 * Extract zip file to the specified directory also save the extracted files into the specified Collection. (Using
	 * Apache Commons Compress Library). The order of unzipping does not preserve the order in which files were
	 * packed in to the zip archive.
	 * 
	 * @param file
	 *            the zip file to extract.
	 * @param toDir
	 *            the destination directory.
	 * @param files
	 *            the collection of the extracted files. Set to null if no references to the extracted files need to be
	 *            kept.
	 * @param stream
	 *            set to true to use ZipArchiveInputStream instead of ZipFile. See <a
	 *            href="http://commons.apache.org/compress/zip.html">Detail</a>
	 * @throws Throwable
	 */
	public static void unzip2(File file, File toDir, Collection<File> files, boolean stream) throws Throwable {

		byte[] buffer = new byte[BUFFER_SIZE];
		if (stream) {
			ZipArchiveInputStream zis = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(file)));
			ZipArchiveEntry entry;
			while ((entry = zis.getNextZipEntry()) != null) {
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
				if (files != null) {
					files.add(destFile);
				}
			}
			zis.close();
		} else {
			ZipFile zipFile = new ZipFile(file);
			Enumeration<?> entries = zipFile.getEntries();
			while (entries.hasMoreElements()) {
				ZipArchiveEntry entry = (ZipArchiveEntry) entries.nextElement();
				File destFile = new File(toDir.getAbsolutePath() + "/" + entry.getName());
				if (entry.isDirectory()) {
					destFile.mkdirs();
				} else {
					File parentDir = destFile.getParentFile();
					if (!parentDir.exists()) {
						parentDir.mkdirs();
					}
					BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(entry), BUFFER_SIZE);
					BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(destFile), BUFFER_SIZE);
					try {
						int count;
						while ((count = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
							os.write(buffer, 0, count);
						}
						os.flush();
					} finally {
						os.close();
						is.close();
					}
				}
				if (files != null) {
					files.add(destFile);
				}
			}
		}

	}

	public static void unzip(File file, File toDir) throws Throwable {

		unzip(file, toDir, null);

	}

	public static void unzip(File file, File toDir, Collection<File> files) throws Throwable {

		unzip(file, toDir, files, false);

	}

	/**
	 * Extract zip file to the specified directory also save the extracted files into the specified Collection. (Using
	 * java.util.zip).  The order of unzipping does preserve the order in which files were
	 * packed in to the zip archive.
	 * 
	 * @param file
	 *            the zip file to extract.
	 * @param toDir
	 *            the destination directory.
	 * @param files
	 *            the collection of the extracted files. Set to null if no references to the extracted files need to be
	 *            kept.
	 * @param stream
	 *            set to true to use ZipInputStream instead of ZipFile. See <a
	 *            href="http://java.sun.com/developer/technicalArticles/Programming/compression/">Detail</a>
	 * @throws Throwable
	 */
	public static void unzip(File file, File toDir, Collection<File> files, boolean stream) throws Throwable {

		byte[] buffer = new byte[BUFFER_SIZE];
		if (stream) {
			java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(new BufferedInputStream(
					new FileInputStream(file)));
			java.util.zip.ZipEntry entry;
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
				if (files != null) {
					files.add(destFile);
				}
			}
			zis.close();
		} else {
			java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(file);
			Enumeration<?> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				java.util.zip.ZipEntry entry = (java.util.zip.ZipEntry) entries.nextElement();
				File destFile = new File(toDir.getAbsolutePath() + "/" + entry.getName());
				if (entry.isDirectory()) {
					destFile.mkdirs();
				} else {
					File parentDir = destFile.getParentFile();
					if (!parentDir.exists()) {
						parentDir.mkdirs();
					}
					BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(entry), BUFFER_SIZE);
					BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(destFile), BUFFER_SIZE);
					try {
						int count;
						while ((count = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
							os.write(buffer, 0, count);
						}
						os.flush();
					} finally {
						os.close();
						is.close();
					}
				}
				if (files != null) {
					files.add(destFile);
				}
			}
		}

	}

}
