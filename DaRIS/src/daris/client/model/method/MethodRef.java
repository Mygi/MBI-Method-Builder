package daris.client.model.method;

import daris.client.model.object.DObjectRef;

public class MethodRef extends DObjectRef {

	private String _name;
	private String _description;

	public MethodRef(String id, String name, String description) {

		super(id, null, false, false);
		_name = name;
		_description = description;
	}

	@Override
	public String referentTypeName() {

		return Method.TYPE_NAME;
	}

	public String name() {

		return _name;
	}

	public String description() {

		return _description;
	}

	@Override
	public String toString() {

		return id() + ": " + _name;
	}

}
