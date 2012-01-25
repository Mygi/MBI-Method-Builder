package daris.client.model.dataset.messages;

import daris.client.model.object.DObjectRef;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class DataSetCount extends ObjectMessage<Integer> {
	private String _pid;

	public DataSetCount(String pid) {

		_pid = pid;
	}

	public DataSetCount(DObjectRef root) {

		this(root.id());
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		if (_pid != null) {
			w.add("pid", _pid);
		}
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.dataset.count";
	}

	@Override
	protected Integer instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			return xe.intValue("count", 0);
		}
		return 0;
	}

	@Override
	protected String objectTypeName() {

		return null;
	}

	@Override
	protected String idToString() {

		return _pid;
	}

}
