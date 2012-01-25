package daris.model.object.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.model.object.PSSDObjectRef;

public abstract class ObjectCreate extends ObjectMessage<String> {

	private PSSDObjectRef _po;

	private PSSDObjectRef _o;
	
	protected PSSDObjectRef parent(){
		return _po;
	}
	
	protected PSSDObjectRef object(){
		return _o;
	}

	public ObjectCreate(PSSDObjectRef parent, PSSDObjectRef object) {

		_po = parent;
		_o = object;

	}

	@Override
	protected String instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			return xe.value("id");
		}
		return null;

	}
	
	@Override
	protected void messageServiceArgs(XmlWriter w) {

		if (_o.name() != null) {
			w.add("name", _o.name());
		}
		if (_o.description() != null) {
			w.add("description", _o.description());
		}

	}

	@Override
	protected String idToString() {

		return _o.referentTypeName();

	}

}
