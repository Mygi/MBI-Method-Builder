package daris.client.model.method;

import java.util.List;
import java.util.Vector;

public class BranchStep extends Step {
	/**
	 * Branch to all sub-steps.
	 */
	public static final int BRANCH_ALL = 2;

	/**
	 * Branch to any sub-step.
	 */
	public static final int BRANCH_ONE = 1;

	private Vector<Method> _methods;
	private int _type;

	public BranchStep(int id, String name, String description, int type) {

		super(id, name, description);

		_type = type;
		_methods = new Vector<Method>();
	}

	public void addMethod(Method m) {

		_methods.add(m);
	}

	public boolean isSimple() {

		return false;
	}

	public List<Method> methods() {

		return _methods;
	}

	public int type() {

		return _type;
	}
}
