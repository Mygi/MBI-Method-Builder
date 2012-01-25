package nig.compress;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

public class BZip2Util {

	private BZip2Util() {

	}

	public static final int BUFFER_SIZE = 2048;

	public static void bzip2(File f, File bz2f) throws Throwable {

		BufferedInputStream is = new BufferedInputStream(new FileInputStream(f), BUFFER_SIZE);
		BZip2CompressorOutputStream os = new BZip2CompressorOutputStream(new BufferedOutputStream(new FileOutputStream(
				bz2f), BUFFER_SIZE));
		try {
			IOUtils.copy(is, os, BUFFER_SIZE);
			os.flush();
		} finally {
			os.close();
			is.close();
		}

	}

	public static void bunzip2(File bz2f, File f) throws Throwable {

		BZip2CompressorInputStream is = new BZip2CompressorInputStream(new BufferedInputStream(
				new FileInputStream(bz2f), BUFFER_SIZE));
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
