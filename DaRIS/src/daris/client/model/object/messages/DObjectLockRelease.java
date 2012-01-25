package daris.client.model.object.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;

public class DObjectLockRelease extends ObjectMessage<Null> {

	private String _id;

	public DObjectLockRelease(String id) {

		_id = id;
	}

	@Override
	protected String idToString() {

		return _id;
	}

	@Override
	protected Null instantiate(XmlElement xe) throws Throwable {

		return null;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("id", _id);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.object.session.unlock";
	}

	@Override
	protected String objectTypeName() {

		return "Lock";
	}

}
