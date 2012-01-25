package daris.model.method;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import daris.model.object.PSSDObject;

public class Method extends PSSDObject {

	public static final String TYPE_NAME = "method";

	private static Step instantiateStep(XmlElement se) throws Throwable {

		int id = se.intValue("@id");
		String name = se.value("name");
		String description = se.value("description");

		// Sub-method? If so, the method may be inline, or a reference
		// to some external method.
		XmlElement me = se.element("method");
		if (me != null) {
			if (me.element("step") == null) {
				MethodReferenceStep mrs = new MethodReferenceStep(id, name,
						description);
				mrs.setReferencedMethod(me.value("id"));
				return mrs;
			}
			Method m = new Method(me);
			m.setSteps(instantiateSteps(me.elements("step")));
			MethodStep ms = new MethodStep(id, name, description, m);
			return ms;
		}

		// Branch?
		XmlElement be = se.element("branch");
		if (be != null) {
			String ts = be.value("@type");
			int type;
			if (ts.equalsIgnoreCase("or")) {
				type = BranchStep.BRANCH_ONE;
			} else {
				type = BranchStep.BRANCH_ALL;
			}
			BranchStep bs = new BranchStep(id, name, description, type);
			List<XmlElement> mes = be.elements("method");
			if (mes != null) {
				for (int i = 0; i < mes.size(); i++) {
					XmlElement mse = mes.get(i);
					Method m = new Method(mse);
					m.setSteps(instantiateSteps(mse.elements("step")));
					bs.addMethod(m);
				}
			}
			return bs;
		}

		// Must be an action step of one kind of another.
		XmlElement sse = se.element("study");
		if (sse != null) {
			String type = sse.value("type");
			// TODO: populate study meta
			// List<XmlElement> ediableStudyMeta = sse.elements("metadata");
			StudyActionStep as = new StudyActionStep(id, name, description,
					type, null);
			return as;
		}
		// Must be a subject action
		// TODO: subject meta and rsubjectmeta
		SubjectActionStep as = new SubjectActionStep(id, name, description,
				null, null);
		return as;

	}

	private static List<Step> instantiateSteps(List<XmlElement> ses)
			throws Throwable {

		if (ses == null) {
			return null;
		}
		List<Step> steps = new Vector<Step>(ses.size());
		for (XmlElement se : ses) {
			steps.add(instantiateStep(se));
		}
		return steps;
	}

	private List<Step> _steps;
	private String _author;

	public Method(XmlElement xe) throws Throwable {

		super(xe);
		XmlElement me = null;
		if (xe.name().equals("object")) {
			// xe is the result of om.pssd.object.describe
			me = xe.element("method");
		} else if (xe.name().equals("method")) {
			// xe is the result of om.pssd.method.describe or (om.pssd.object.describe ex-method)
			me = xe;
		}
		if (me == null) {
			throw new Exception(
					"Error creating method object. The XML element is null");
		}
		_author = me.value("author");
		List<XmlElement> ses = me.elements("step");
		if (ses != null) {
			setSteps(instantiateSteps(ses));
		}

	}

	public Method(String id, String name, String description) {

		super(null, id, name, description, false, 0, true);
	}

	public void setSteps(List<Step> steps) {

		_steps = steps;
	}

	public List<Step> steps() {

		return _steps;
	}

	public String author() {

		return _author;
	}

	@Override
	public String typeName() {

		return Method.TYPE_NAME;
	}
}
