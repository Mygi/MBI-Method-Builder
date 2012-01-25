package daris.model.dataset;

import arc.mf.client.xml.XmlElement;
import daris.model.datacontent.DataContent;
import daris.model.object.PSSDObjectRef;

public abstract class DataSetRef extends PSSDObjectRef {

	private DataContent _data;

	private String _sourceType;

	private String _type;

	private String _vid;

	public DataSetRef(XmlElement oe) {

		super(oe);

	}

	@Override
	protected void parse(XmlElement oe) {

		super.parse(oe);
		try {
			_sourceType = oe.stringValue("source/type",
					DataSet.SOURCE_TYPE_DERIVATION);
			XmlElement e = oe.element("data");
			if (e != null) {
				_data = new DataContent(e);
			}
		} catch (Throwable t) {
			throw new AssertionError(t.getMessage());
		}
		_vid = oe.value("vid");
		_type = oe.value("type");

	}

	public String sourceType() {

		return _sourceType;
	}

	public String vid() {

		return _vid;
	}

	public String type() {

		return _type;
	}

	public DataContent data() {

		return _data;
	}

	public DataSetRef(String proute, String id, String name,
			String description, boolean isleaf) {

		super(proute, id, name, description, isleaf);

	}

	@Override
	public String referentTypeName() {

		return "dataset";
	}

}
