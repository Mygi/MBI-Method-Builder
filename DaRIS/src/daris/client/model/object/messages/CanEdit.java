package daris.client.model.object.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;

public class CanEdit extends ObjectMessage<Boolean> {

	private String _id;
	private String _type;

	public CanEdit(String id) {

		_id = id;
	}

	public CanEdit(DObject o) {

		this(o.id());
	}

	public CanEdit(DObjectRef o) {

		this(o.id());
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("id", _id);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.user.canedit";
	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			return xe.booleanValue("edit", false);
		}
		return false;
	}

	@Override
	protected String objectTypeName() {

		return _type;
	}

	@Override
	protected String idToString() {

		return _id;
	}

}