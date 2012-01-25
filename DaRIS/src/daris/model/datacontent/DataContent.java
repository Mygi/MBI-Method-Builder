package daris.model.datacontent;

import arc.mf.client.xml.XmlElement;

/**
 * The class for MF asset/object content.
 * 
 * @author Wilson Liu
 * 
 */
public class DataContent extends CheckedContent {

	private String _store;

	/**
	 * Constructor.
	 * 
	 * @param url
	 *            the URL of the content file.
	 * @param ext
	 *            the extension of the content file.
	 * @param size
	 *            the size of the content file.
	 * @param units
	 *            the units of the content size.
	 * @param mimeType
	 *            the MIME type of the content file.
	 * @param checksum
	 *            the checksum value of the content.
	 * @param checksumBase
	 *            the base/radix of the checksum.
	 * @param store
	 *            the data store on Mediaflux.
	 */
	public DataContent(String url, String ext, long size, String units,
			String mimeType, long checksum, int checksumBase, String store) {

		super(url, ext, size, units, mimeType, checksum, checksumBase);
		_store = store;

	}

	/**
	 * Constructor.
	 * 
	 * @param url
	 *            the URL of the content file.
	 * @param ext
	 *            the extension of the content file.
	 * @param size
	 *            the size of the content file.
	 * @param units
	 *            the units of the content size.
	 * @param mimeType
	 *            the MIME type of the content file.
	 * @param checksum
	 *            the checksum string of the content.
	 * @param checksumBase
	 *            the base/radix of the checksum.
	 * @param store
	 *            the data store on Mediaflux.
	 */
	public DataContent(String url, String ext, long size, String units,
			String mimeType, String checksum, int checksumBase, String store) {

		this(url, ext, size, units, mimeType, Long.parseLong(checksum, checksumBase), checksumBase, store);
	}

	/**
	 * Constructor.
	 * 
	 * @param xe
	 *            the XML element represent the data content.
	 * @throws Throwable
	 */
	public DataContent(XmlElement xe) throws Throwable {

		this(xe.value("url"), xe.value("type/@ext"), xe.longValue("size"), xe
				.value("size/@units"), xe.value("type"), xe.value("csum"), xe
				.intValue("csum/@base", 16), xe.value("store"));

	}

	public String store() {

		return _store;
	}

}
