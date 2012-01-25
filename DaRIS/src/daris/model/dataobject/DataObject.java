package daris.model.dataobject;

import arc.mf.client.xml.XmlElement;
import daris.model.object.PSSDObject;

public class DataObject extends PSSDObject {

	/**
	 * the object type: data-object.
	 */
	public static final String TYPE_NAME = "data-object";

	/**
	 * The constructor.
	 * 
	 * @param xe
	 *            the XML element represents the data object information.
	 * @throws Throwable
	 */
	public DataObject(XmlElement xe) throws Throwable {
		super(xe);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String typeName() {

		return DataObject.TYPE_NAME;
		
	}

}
