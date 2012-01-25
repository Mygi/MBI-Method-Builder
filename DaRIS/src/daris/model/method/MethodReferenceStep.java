package daris.model.method;

public class MethodReferenceStep extends Step {

	private String _methodId;

	public MethodReferenceStep(int id, String name, String description) {

		super(id, name, description);
	}

	public String referencedMethod() {

		return _methodId;
	}

	public void setReferencedMethod(String mid) {

		_methodId = mid;
	}

}