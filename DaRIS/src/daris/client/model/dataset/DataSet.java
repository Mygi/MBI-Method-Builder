package daris.client.model.dataset;

import daris.client.model.object.DataContent;
import daris.client.model.object.DObject;
import arc.mf.client.xml.XmlElement;

public abstract class DataSet extends DObject {

	public static final String TYPE_NAME = "dataset";

	private SourceType _sourceType;
	private String _type;
	private String _vid;
	private DataContent _data;

	protected DataSet(XmlElement de) {

		super(de);
		try {
			_sourceType = SourceType.parse(de.stringValue("source/type", SourceType.derivation.toString()));
		} catch (Throwable e) {
			_sourceType = SourceType.derivation;
		}
		_vid = de.value("vid");
		_type = de.value("type");
		XmlElement ce = de.element("data");
		if (ce != null) {
			_data = new DataContent(ce);
		}
	}

	public DataContent data() {

		return _data;
	}

	public SourceType sourceType() {

		return _sourceType;
	}

	public String type() {

		return _type;
	}

	public String vid() {

		return _vid;
	}

	@Override
	public String typeName() {

		return DataSet.TYPE_NAME;
	}

}
