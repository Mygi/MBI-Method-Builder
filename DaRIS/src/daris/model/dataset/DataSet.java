package daris.model.dataset;

import arc.mf.client.xml.XmlElement;
import daris.model.datacontent.DataContent;
import daris.model.object.PSSDObject;

/**
 * The class represents the dataset objects in the PSSD data model.
 * 
 * @author Wilson Liu
 * 
 */
public abstract class DataSet extends PSSDObject {

	/**
	 * Derivation source type.
	 */
	public static final String SOURCE_TYPE_DERIVATION = "derivation";

	/**
	 * Primary source type.
	 */
	public static final String SOURCE_TYPE_PRIMARY = "primary";
	/**
	 * Object type: dataset
	 */
	public static final String TYPE_NAME = "dataset";

	/**
	 * The data/asset content.
	 */
	private DataContent _data;

	/**
	 * The source type of this DataSet object.
	 */
	private String _sourceType;

	/**
	 * The MIME type of the this DataSet object.
	 */
	private String _type;

	/**
	 * The vid of this DataSet object.
	 */
	private String _vid;

	/**
	 * The constructor.
	 * 
	 * @param xe
	 *            The XML element that contains the DataSet object information.
	 * @throws Throwable
	 */
	protected DataSet(XmlElement xe) throws Throwable {
		super(xe);
		_sourceType = xe.stringValue("source/type", SOURCE_TYPE_DERIVATION);
		_vid = xe.value("vid");
		_type = xe.value("type");
		XmlElement e = xe.element("data");
		if (e != null) {
			_data = new DataContent(e);
		}
	}

	/**
	 * The method returns the data content object.
	 * 
	 * @return
	 */
	public DataContent data() {
		return _data;
	}

	/**
	 * The method returns the source type of this object.
	 * 
	 * @return
	 */
	public String sourceType() {
		return _sourceType;
	}

	/**
	 * The method returns the mime type of this object.
	 * 
	 * @return
	 */
	public String type() {
		return _type;
	}

	/**
	 * The methods returns the vid of the object.
	 * 
	 * @return
	 */
	public String vid() {
		return _vid;
	}

	/**
	 * The methods to check if the dataset has data content.
	 * 
	 * @return true if the dataset has data content.
	 */
	public boolean hasContent() {
		return _data != null;
	}

}
