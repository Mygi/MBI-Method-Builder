package daris.model.sc;

import arc.mf.client.xml.XmlElement;

public class ContentItem {
	
	private String _status;
	
	private String _id;

	private String _assetId;

	private String _name;

	private String _description;
	
	private String _objectType;

	private String _mimeType;

	private long _size;
	
	public ContentItem(XmlElement oe){
		_objectType = oe.value("@type");
		_status = oe.value("@status");
		_id = oe.value("id");
		_assetId = oe.value("id/@asset");
		_name = oe.value("name");
		_description = oe.value("description");
		_mimeType = oe.value("type");
		try {
			_size = oe.longValue("data/size", 0);
		} catch (Throwable e) {
			throw new AssertionError("Error parsing object/data/size.");
		}
		
	}
	
	public String status(){
		return _status;
	}

	public String id() {

		return _id;
	}

	public String assetId() {

		return _assetId;
	}

	public String name() {

		return _name;
	}

	public String description() {

		return _description;
	}
	
	public String objectType(){
		return _objectType;
	}

	public String mimeType() {

		return _mimeType;
	}

	public long size() {

		return _size;
	}

}
