package daris.client.model.sink;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class SinkRef extends ObjectRef<Sink> {

	private String _name;

	public SinkRef(String name) {
		_name = name;
	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {
		w.add("name", _name);
	}

	@Override
	protected String resolveServiceName() {
		return "sink.describe";
	}

	@Override
	protected Sink instantiate(XmlElement xe) throws Throwable {
		if (xe != null) {
			XmlElement se = xe.element("sink");
			if (se != null) {
				return new Sink(se);
			}
		}
		return null;
	}

	@Override
	public String referentTypeName() {
		return Sink.TYPE_NAME;
	}

	@Override
	public String idToString() {
		// TODO Auto-generated method stub
		return null;
	}
}
