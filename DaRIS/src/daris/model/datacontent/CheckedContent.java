package daris.model.datacontent;

public class CheckedContent extends Content {

	private Checksum _csum;

	public CheckedContent(String url, String ext, long size, String units,
			String mimeType, long checksum, int checksumBase) {

		super(url, ext, size, units, mimeType);
		_csum = new Checksum(checksum, checksumBase);
	}

	/**
	 * the checksum of the data content.
	 */
	public Checksum checksum() {

		return _csum;
	}

	protected void setChecksum(Checksum csum) {

		_csum = csum;
	}

	protected void setChecksum(long csum, int base) {

		_csum = new Checksum(csum, base);
	}
}
