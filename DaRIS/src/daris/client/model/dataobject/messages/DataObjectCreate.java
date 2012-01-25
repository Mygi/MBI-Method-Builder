package daris.client.model.dataobject.messages;

import daris.client.model.dataobject.DataObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.DObjectCreate;

public class DataObjectCreate extends DObjectCreate {

	public DataObjectCreate(DObjectRef po, DataObject o) {

		super(po, o);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.data-object.create";
	}

}
