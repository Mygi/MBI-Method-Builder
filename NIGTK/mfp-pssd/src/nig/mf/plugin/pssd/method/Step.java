package nig.mf.plugin.pssd.method;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

/**
 * A method is comprised of steps.
 * 
 * @author Jason Lohrey
 * 
 */
public abstract class Step {

	public static enum Type {
		subject, study, method, branch;
		public static Type stepTypeFor(XmlDoc.Element se) throws Throwable {

			if (se.element("subject") != null) {
				return subject;
			} else if (se.element("study") != null) {
				return study;
			} else if (se.element("method") != null) {
				return method;
			} else if (se.element("branch") != null) {
				return branch;
			} else {
				throw new Exception("Cannot determine the step type for " + se.toString());
			}
		}
	}

	private int _id;
	private String _name;
	private String _description;

	public Step(int id) {

		_id = id;
		_name = null;
	}

	public Step(int id, String name, String description) {

		_id = id;
		_name = name;
		_description = description;
	}

	public int id() {

		return _id;
	}

	public String name() {

		return _name;
	}

	public String description() {

		return _description;
	}

	/**
	 * Recursively expand all branches into substeps. This lets us expand a
	 * Method will references to other Methods.
	 * 
	 * @param proute
	 *            Server path to execute any server calls on. By including this
	 *            here, we can pass the server route down the nested Method
	 *            object so that any enclosed Methods can be reconstructed from
	 *            their CID. This model requires all sub-Methods are managed by
	 *            the same server. It would also be possible to discover the
	 *            primary Method object (or a replica) from the CID but some
	 *            other policy would need to be applied).
	 * 
	 * @param executor
	 * @return
	 * @throws Throwable
	 */
	public Step convertBranchesToSubSteps(String proute, ServiceExecutor executor) throws Throwable {

		return this;
	}

	public void save(XmlWriter w) throws Throwable {

		w.push("step", new String[] { "id", String.valueOf(id()) });

		if (_name != null) {
			w.add("name", _name);
		}

		saveStepBody(w);
		w.pop();
	}

	protected abstract void saveStepBody(XmlWriter w) throws Throwable;

	public void restore(XmlDoc.Element se) throws Throwable {

		restoreIdAndName(se);
		restoreStepBody(se);
	}

	protected void restoreIdAndName(XmlDoc.Element se) throws Throwable {

		_id = se.intValue("@id");
		_name = se.value("name");
	}

	protected abstract void restoreStepBody(XmlDoc.Element se) throws Throwable;
}
