package daris.model.user.messages;

import daris.model.object.PSSDObjectRef;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class UserCanEdit extends ObjectMessage<Boolean> {

	private String _id;
	private String _type;

	public UserCanEdit(PSSDObjectRef o) {
		_id = o.id();
		_type = o.referentTypeName();
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
