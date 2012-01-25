package daris.client.model.sink.messages;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.sink.Sink;

public class SinkDescribe extends ObjectMessage<List<Sink>> {

	public SinkDescribe() {

	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

	}

	@Override
	protected String messageServiceName() {
		return "sink.describe";
	}

	@Override
	protected List<Sink> instantiate(XmlElement xe) throws Throwable {
		if (xe != null) {
			List<XmlElement> ses = xe.elements("sink");
			if (ses != null) {
				List<Sink> ss = new Vector<Sink>();
				for (XmlElement se : ses) {
					ss.add(new Sink(se));
				}
				if (!ss.isEmpty()) {
					return ss;
				}
			}
		}
		return null;
	}

	@Override
	protected String objectTypeName() {
		return null;
	}

	@Override
	protected String idToString() {
		return null;
	}

}
