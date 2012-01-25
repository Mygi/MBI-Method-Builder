package daris.client.model.object.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class DObjectLockRenew extends ObjectMessage<Boolean> {
	private String _id;
	private int _timeout;

	public DObjectLockRenew(String id, int timeout) {

		_id = id;
		_timeout = timeout;
	}

	@Override
	protected String idToString() {

		return _id;
	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {

		return xe != null;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("id", _id);
		if (_timeout > 0) {
			w.add("timeout", _timeout);
		}
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.object.session.lock";
	}

	@Override
	protected String objectTypeName() {

		return "Lock";
	}

}
