package daris.client.model.object.messages;

import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public abstract class DObjectCreate extends ObjectMessage<DObjectRef> {

	private DObjectRef _po;
	private DObject _o;

	protected DObjectCreate(DObjectRef po, DObject o) {

		_po = po;
		_o = o;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		_o.createServiceArgs(w);
	}

	@Override
	protected DObjectRef instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			String id = xe.value("id");
			if (id != null) {
				return new DObjectRef(id, null, false, false);
			}
		}
		return null;
	}

	@Override
	protected String objectTypeName() {

		return _o.typeName();
	}

	@Override
	protected String idToString() {

		// TODO Auto-generated method stub
		return null;
	}

	protected DObjectRef parentObject() {

		return _po;
	}

}
