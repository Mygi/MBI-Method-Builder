package daris.client.model.dataobject;

import arc.mf.client.xml.XmlElement;
import daris.client.model.dataobject.messages.DataObjectCreate;
import daris.client.model.dataobject.messages.DataObjectUpdate;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.DObjectCreate;
import daris.client.model.object.messages.DObjectUpdate;

public class DataObject extends DObject {

	public static final String TYPE_NAME = "data-object";

	public DataObject(XmlElement xe) {

		super(xe);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String typeName() {

		return DataObject.TYPE_NAME;
	}

	@Override
	protected DObjectCreate objectCreateMessage(DObjectRef po) {

		return new DataObjectCreate(po, this);
	}

	@Override
	protected DObjectUpdate objectUpdateMessage() {

		return new DataObjectUpdate(this);
	}

}
