package nig.compress;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collection;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

public class TarUtil {

	private TarUtil() {

	}

	public static final int BUFFER_SIZE = 2048;
	public static final int COMPRESSION_NONE = 0;
	public static final int COMPRESSION_GZIP = 1;
	public static final int COMPRESSION_BZIP2 = 2;

	public static void tar(File dir, File tarFile, int compression)
			throws Throwable {

		tar(dir, true, tarFile, compression);

	}

	public static void tar(File dir, boolean self, File tarFile, int compression)
			throws Throwable {

		if (self) {
			tar(new File[] { dir }, dir.getParentFile(), tarFile, compression);
		} else {
			tar(dir.listFiles(), dir, tarFile, compression);
		}

	}

	public static void tar(File[] files, File baseDir, File tarFile,
			int compression) throws Throwable {

		TarArchiveOutputStream os;
		BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(tarFile), BUFFER_SIZE);
		switch (compression) {
		case COMPRESSION_GZIP:
			os = new TarArchiveOutputStream(new GzipCompressorOutputStream(bos));
			break;
		case COMPRESSION_BZIP2:
			os = new TarArchiveOutputStream(
					new BZip2CompressorOutputStream(bos));
			break;
		default:
			os = new TarArchiveOutputStream(bos);
			break;
		}
		tar(files, baseDir, os);
		os.close();

	}

	private static void tar(File[] files, File baseDir,
			TarArchiveOutputStream os) throws Throwable {

		String base = baseDir.getAbsolutePath();
		byte buffer[] = new byte[BUFFER_SIZE];
		for (File f : files) {
			String name = f.getAbsolutePath();
			if (name.startsWith(base)) {
				name = name.substring(base.length());
			}
			if (name.startsWith(System.getProperty("file.separator"))) {
				name = name.substring(1);
			}
			if (f.isDirectory()) {
				TarArchiveEntry entry = new TarArchiveEntry(f, name + "/");
				os.putArchiveEntry(entry);
				os.closeArchiveEntry();
				tar(f.listFiles(), baseDir, os);
			} else {
				TarArchiveEntry entry = new TarArchiveEntry(f, name);
				entry.setSize(f.length());
				os.putArchiveEntry(entry);
				BufferedInputStream is = new BufferedInputStream(
						new FileInputStream(f), BUFFER_SIZE);
				int count;
				while ((count = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
					os.write(buffer, 0, count);
				}
				is.close();
				os.closeArchiveEntry();
			}
		}

	}

	public static void untar(File tarFile, File toDir, int compression)
			throws Throwable {

		untar(tarFile, toDir, compression, null);

	}

	public static void untar(File tarFile, File toDir, int compression,
			Collection<File> files) throws Throwable {

		TarArchiveInputStream is;
		switch (compression) {
		case COMPRESSION_GZIP:
			is = new TarArchiveInputStream(new GzipCompressorInputStream(
					new BufferedInputStream(new FileInputStream(tarFile),
							BUFFER_SIZE)));
			break;
		case COMPRESSION_BZIP2:
			is = new TarArchiveInputStream(new BZip2CompressorInputStream(
					new BufferedInputStream(new FileInputStream(tarFile),
							BUFFER_SIZE)));
			break;
		default:
			is = new TarArchiveInputStream(new BufferedInputStream(
					new FileInputStream(tarFile), BUFFER_SIZE));
			break;
		}
		TarArchiveEntry entry;
		while ((entry = is.getNextTarEntry()) != null) {
			File destFile = new File(toDir.getAbsolutePath() + "/"
					+ entry.getName());
			if (entry.isDirectory()) {
				destFile.mkdirs();
			} else {
				File parentDir = destFile.getParentFile();
				if (!parentDir.exists()) {
					parentDir.mkdirs();
				}
				BufferedOutputStream os = new BufferedOutputStream(
						new FileOutputStream(destFile), BUFFER_SIZE);
				long size = entry.getSize();
				long n = 0;
				int count;
				byte buffer[] = new byte[BUFFER_SIZE];
				while ((count = is.read(buffer)) != -1 && n < size) {
					os.write(buffer, 0, count);
					n += count;
				}
				os.flush();
				os.close();
			}
			if (files != null) {
				files.add(destFile);
			}
		}

	}

}
