package daris.model.object.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.model.object.PSSDObjectRef;

public abstract class ObjectUpdate extends ObjectMessage<Boolean> {

	private PSSDObjectRef _o;

	protected PSSDObjectRef object() {

		return _o;
	}

	public ObjectUpdate(PSSDObjectRef o) {

		_o = o;

	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			return true;
		}
		return null;
	}

	@Override
	protected String idToString() {

		return _o.id();

	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("id", _o.id());
		if (_o.name() != null) {
			w.add("name", _o.name());
		}
		if (_o.description() != null) {
			w.add("description", _o.description());
		}

	}

}
