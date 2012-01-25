package daris.client.model.method;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class MethodListRef extends ObjectRef<List<MethodRef>> {

	private boolean _forSubject;

	public MethodListRef(boolean forSubject) {

		_forSubject = forSubject;
	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		w.add("forsubject", _forSubject);
	}

	@Override
	protected String resolveServiceName() {

		return "om.pssd.method.list";
	}

	@Override
	protected List<MethodRef> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			List<XmlElement> mes = xe.elements("method");
			if (mes != null) {
				List<MethodRef> ms = new Vector<MethodRef>();
				for (XmlElement me : mes) {
					ms.add(new MethodRef(me.value(), me.value("@name"), me
							.value("@description")));
				}
				if (!ms.isEmpty()) {
					return ms;
				}
			}
		}
		return null;
	}

	@Override
	public String referentTypeName() {

		return "list of methods";
	}

	@Override
	public String idToString() {

		return null;
	}

}
