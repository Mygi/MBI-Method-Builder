package daris.model.method;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class MethodsRef extends ObjectRef<List<MethodRef>> {

	private static MethodsRef _instance;

	public static MethodsRef instance() {

		if (_instance == null) {
			_instance = new MethodsRef();
		}
		return _instance;

	}

	private boolean _forSubject = true;

	private MethodsRef() {

	}

	/**
	 * Set to false if you want to list all methods.
	 * 
	 * @param forSubject
	 */
	public void setForSubject(boolean forSubject) {
		_forSubject = forSubject;
	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {
		w.add("size", "infinity");
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
				List<MethodRef> ms = new Vector<MethodRef>(mes.size());
				for (XmlElement me : mes) {
					String id = me.value();
					String name = me.value("@name");
					String description = me.value("@description");
					ms.add(new MethodRef(id, name, description));
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

		return "methods";

	}

	@Override
	public String idToString() {

		return "methods";

	}

}
