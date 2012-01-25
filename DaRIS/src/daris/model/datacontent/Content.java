package daris.model.datacontent;

public class Content {

	/**
	 * the url of the content file.
	 */
	private String _url;

	/**
	 * the file extension of the content file.
	 */
	private String _ext;

	/**
	 * the size of the content file.
	 */
	private long _size;

	/**
	 * the units of the content size.
	 */
	private String _units;

	/**
	 * the mime type of the content file.
	 */
	private String _mimeType;

	public Content(String url, String ext, long size, String units,
			String mimeType) {

		_url = url;
		_ext = ext;
		_size = size;
		_units = units;
		_mimeType = mimeType;
	}

	/**
	 * The method returns the size of the content file.
	 * 
	 * @return
	 */
	public long size() {

		return _size;
	}

	/**
	 * The method returns the units of the size.
	 * 
	 * @return
	 */
	public String units() {

		return _units;
	}

	/**
	 * The method returns the mime type of the content file.
	 * 
	 * @return
	 */
	public String mimeType() {

		return _mimeType;
	}

	/**
	 * The method returns the extension of the content file.
	 * 
	 * @return
	 */
	public String extension() {

		return _ext;
	}

	/**
	 * Set the file extension.
	 * 
	 * @param ext
	 */
	protected void setExtension(String ext) {

		_ext = ext;
	}

	/**
	 * Returns the url of the content file.
	 * 
	 * @return
	 */
	public String url() {

		return _url;
	}

	/**
	 * Set the file url.
	 * 
	 * @param url
	 */
	protected void setUrl(String url) {

	}

}
