package daris.model.object.attachment;

public class Attachment {
	private String _assetId;
	private String _name;
	private String _mimeType;
	private String _ext;
	private long _size;

	public Attachment(String assetId) {
		this(assetId, null, null, null, -1);
	}

	public Attachment(String assetId, String name, String mimeType, String ext,
			long size) {
		_assetId = assetId;
		_name = name;
		_mimeType = mimeType;
		_ext = ext;
		_size = size;
	}

	public String assetId() {
		return _assetId;
	}

	public String name() {
		return _name;
	}

	public String extension() {
		return _ext;
	}

	public String mimeType() {
		return _mimeType;
	}

	public long size() {
		return _size;
	}
}
