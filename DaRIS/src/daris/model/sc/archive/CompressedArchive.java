package daris.model.sc.archive;

public abstract class CompressedArchive extends Archive {

	public static int DEFAULT_COMPRESSION_LEVEL = 6;

	protected CompressedArchive(Archive.Type type) {

		this(type, DEFAULT_COMPRESSION_LEVEL);
	}

	protected CompressedArchive(Archive.Type type, int compressionLevel)
			 {

		super(type);
		setCompressionLevel(compressionLevel);
	}

	public int compressionLevel() {

		String sv = parameterValue("compression-level");
		if (sv == null) {
			return DEFAULT_COMPRESSION_LEVEL;
		} else {
			return Integer.parseInt(sv);
		}
	}

	public void setCompressionLevel(int compressionLevel) {

		setParameter("compression-level", Integer.toString(compressionLevel));
	}
}
