package nig.iio.siemens;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class RDAFileUtils {

	/**
	 * Prevent the class to be instantiated.
	 */
	private RDAFileUtils() {
	};

	/**
	 * RDA file is always little endian.
	 */
	public static final boolean LittleEndian = true;

	/**
	 * Creates an RDA file using specified header, data and file path.
	 * 
	 * @param header
	 * @param data
	 * @param pathname
	 * @return the RDA Java File object.
	 * @throws IOException
	 */
	public static File createRDAFile(RDAFileHeader header, double[] data, String pathname) throws IOException {

		File f = new File(pathname);
		createRDAFile(header, data, f);
		return f;

	}

	/**
	 * Creates an RDA file using specified header, data, and save to the specified Java File object.
	 * 
	 * @param header
	 * @param data
	 * @param f
	 * @throws IOException
	 */
	public static void createRDAFile(RDAFileHeader header, double[] data, File f) throws IOException {

		if (header != null && data != null) {
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
			try {
				dos.write(header.toString().getBytes());
				for (int i = 0; i < data.length; i++) {
					if (LittleEndian) {
						dos.writeLong(Long.reverseBytes((Double.doubleToLongBits(data[i]))));
					} else {
						dos.writeDouble(data[i]);
					}
				}
			} finally {
				dos.close();
			}
		}

	}

}
