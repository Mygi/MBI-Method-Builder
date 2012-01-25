package daris.client.model.object.messages;

import daris.client.model.object.DObject;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public abstract class DObjectUpdate extends ObjectMessage<Boolean> {

	private DObject _o;

	protected DObjectUpdate(DObject o) {

		_o = o;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		_o.updateServiceArgs(w);
	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			return true;
		}
		return null;
	}

	@Override
	protected String objectTypeName() {

		return _o.typeName();
	}

	@Override
	protected String idToString() {

		return _o.id();
	}

}
