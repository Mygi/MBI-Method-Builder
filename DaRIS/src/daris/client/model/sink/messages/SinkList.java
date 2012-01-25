package daris.client.model.sink.messages;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.sink.SinkRef;

public class SinkList extends ObjectMessage<List<SinkRef>> {

	public SinkList() {

	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

	}

	@Override
	protected String messageServiceName() {
		return "sink.list";
	}

	@Override
	protected List<SinkRef> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			List<String> ss = xe.values("sink");
			if (ss != null) {
				List<SinkRef> srs = new Vector<SinkRef>();
				for (String s : ss) {
					srs.add(new SinkRef(s));
				}
				if (!srs.isEmpty()) {
					return srs;
				}
			}
		}
		return null;
	}

	@Override
	protected String objectTypeName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String idToString() {
		// TODO Auto-generated method stub
		return null;
	}

}
