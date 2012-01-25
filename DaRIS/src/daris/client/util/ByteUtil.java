package daris.client.util;

public class ByteUtil {

	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1)
				+ (si ? "" : "i");
		return NumberUtil.toFixed(bytes / Math.pow(unit, exp), 1) + " " + pre
				+ "B";
	}

}
