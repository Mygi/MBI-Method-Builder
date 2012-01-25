package daris.client.model.dataobject.messages;

import daris.client.model.dataobject.DataObject;
import daris.client.model.object.messages.DObjectUpdate;

public class DataObjectUpdate extends DObjectUpdate {

	public DataObjectUpdate(DataObject o) {

		super(o);
	}

	@Override
	protected String messageServiceName() {

		// TODO service not implemented yet.
		return "om.pssd.data-object.update";
	}

}
