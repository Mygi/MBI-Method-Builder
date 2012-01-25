package daris.model.dataobject;

import arc.mf.client.xml.XmlElement;
import daris.model.object.PSSDObjectRef;
import daris.model.object.messages.ObjectCreate;
import daris.model.object.messages.ObjectUpdate;

public class DataObjectRef extends PSSDObjectRef {

	public DataObjectRef(XmlElement oe) {

		super(oe);

	}

	public DataObjectRef(String proute, String id, String name,
			String description, boolean isleaf) {

		super(proute, id, name, description, isleaf);

	}

	@Override
	protected ObjectCreate getChildCreateMessage(PSSDObjectRef o) {

		throw new AssertionError("Could not create child for data-object.");

	}

	@Override
	protected ObjectUpdate getUpdateMessage() {

		// TODO:
		return null;

	}

	@Override
	public String referentTypeName() {

		return "data-object";
	}

}
