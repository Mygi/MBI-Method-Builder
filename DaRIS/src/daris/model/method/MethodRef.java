package daris.model.method;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class MethodRef extends ObjectRef<Method> {

	private String _id;

	private String _name;

	private String _description;

	private boolean _expand;

	public String id() {

		return _id;
	}

	public String name() {

		return _name;
	}

	public String description() {

		return _description;
	}

	public void setExpand(boolean expand) {

		_expand = expand;
	}

	public MethodRef(String id, String name, String description, boolean expand) {

		_id = id;
		_name = name;
		_description = description;
		_expand = expand;
	}

	public MethodRef(String id, String name, String description) {

		this(id, name, description, false);
	}

	public MethodRef(String id, String name) {

		this(id, name, null, false);
	}

	public MethodRef(String id) {

		this(id, null, null, false);
	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		if (_id != null) {
			w.add("id", _id);
		}
		if (_name != null) {
			w.add("name", _name);
		}
		if (_expand) {
			w.add("expand", _expand);
		}
	}

	@Override
	protected String resolveServiceName() {

		return "om.pssd.method.describe";
	}

	@Override
	protected Method instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			Method m = new Method(xe);
			_name = m.name();
			_description = m.description();
			return m;
		}
		return null;
	}

	@Override
	public String referentTypeName() {

		return "method";
	}

	@Override
	public String idToString() {

		return _id;
	}

	public boolean equals(Object o) {

		if (o != null) {
			if (o instanceof MethodRef) {
				return ((MethodRef) o).id().equals(_id);
			}
		}
		return false;
	}

	public String toString() {

		return _id + ": " + _name;
	}

}
