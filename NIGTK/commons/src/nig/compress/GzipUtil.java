package nig.compress;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

public class GzipUtil {

	private GzipUtil() {

	}

	public static final int BUFFER_SIZE = 2048;

	public static void gzip(File f, File gzf) throws Throwable {

		BufferedInputStream is = new BufferedInputStream(new FileInputStream(f), BUFFER_SIZE);
		GzipCompressorOutputStream os = new GzipCompressorOutputStream(new BufferedOutputStream(new FileOutputStream(
				gzf), BUFFER_SIZE));
		try {
			IOUtils.copy(is, os, BUFFER_SIZE);
			os.flush();
		} finally {
			os.close();
			is.close();
		}
	}

	public static void gunzip(File gzf, File f) throws Throwable {

		GzipCompressorInputStream is = new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(gzf),
				BUFFER_SIZE));
		BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(f), BUFFER_SIZE);
		try {
			IOUtils.copy(is, os, BUFFER_SIZE);
			os.flush();
		} finally {
			os.close();
			is.close();
		}

	}

}
