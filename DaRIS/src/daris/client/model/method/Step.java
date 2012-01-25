package daris.client.model.method;

public class Step {

	private int _id;
	private String _name;
	private String _description;

	public Step(int id, String name, String description) {

		_id = id;
		_name = name;
	}

	public int id() {

		return _id;
	}

	public boolean isSimple() {

		return true;
	}

	public String name() {

		return _name;
	}

	public String description() {

		return _description;
	}

}
