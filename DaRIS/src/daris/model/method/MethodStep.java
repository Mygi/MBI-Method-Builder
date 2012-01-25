package daris.model.method;

public class MethodStep extends Step {
	private Method _method;

	public MethodStep(int id, String name, String description, Method m) {

		super(id, name, description);

		_method = m;
	}

	public boolean isSimple() {

		return false;
	}

	public Method method() {

		return _method;
	}
}