package nig.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {

	/**
	 * Deletes the file/directory recursively and quietly. It does not throw exceptions if the deletion fails. Instead,
	 * it returns false.
	 * 
	 * @param f
	 *            the file/directory to delete.
	 * @return
	 */
	public static boolean delete(File f) {

		if (f == null) {
			return false;
		}

		// if the file is a directory, delete the contents first.
		if (f.exists()) {
			if (f.isDirectory()) {
				for (File ff : f.listFiles()) {
					if (ff.isDirectory()) {
						delete(ff);
					} else {
						try {
							ff.delete();
						} catch (Exception e) {
							return false;
						}
					}
				}
			}
		}

		// delete the file
		try {
			return f.delete();
		} catch (Exception e) {
			return false;
		}

	}

	/**
	 * Copy a File.
	 * 
	 * @param sf
	 * @param df
	 * @throws IOException
	 */
	public static void copyFile(File sf, File df) throws IOException {

		BufferedInputStream is = new BufferedInputStream(new FileInputStream(sf));
		BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(df));
		try {
			long count = IOUtils.copy(is, os);
			os.flush();
			if (count != sf.length()) {
				throw new IOException("Failed copy file " + sf.getAbsolutePath() + " to " + df.getAbsolutePath()
						+ ". File sizes are not the same");
			}
		} finally {
			os.close();
			is.close();
		}

	}

	/**
	 * Create a directory with unique  name from the given root name and system time in nano-seconds
	 * 
	 * @param rootName
	 * @return
	 * @throws IOException
	 */
	public static File createUniqueDirectory(String rootName)  throws IOException
	{
		
		String name = rootName + "-"+ Long.toString(System.nanoTime());
		File temp = new File(name);
		if(!(temp.mkdir())) {
			throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
		}
		return temp;
	}


}
