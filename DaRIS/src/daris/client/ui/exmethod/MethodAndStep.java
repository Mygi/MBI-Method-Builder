package daris.client.ui.exmethod;

import daris.client.model.exmethod.ExMethod;
import daris.client.model.method.Step;



public class MethodAndStep {
	private MethodAndStep _parent;
	private ExMethod _em;
	private Step _step;
	private int _stepId;

	public MethodAndStep(MethodAndStep parentStep, ExMethod em, Step step) {

		_parent = parentStep;
		_em = em;
		_step = step;
		_stepId = step.id();
	}

	public MethodAndStep(MethodAndStep parentStep, ExMethod em, int step) {

		_parent = parentStep;
		_em = em;
		_step = null;
		_stepId = step;
	}

	public MethodAndStep parent() {

		return _parent;
	}

	public ExMethod method() {

		return _em;
	}

	public String methodId() {

		return _em.id();
	}

	public Step step() {

		return _step;
	}

	public int stepId() {

		return _stepId;
	}

	/**
	 * The top level method identifier.
	 * 
	 * @return
	 */
	public String rootMethodId() {

		MethodAndStep ps = parent();
		if (ps == null) {
			return methodId();
		}
		return ps.rootMethodId();
	}

	/**
	 * Full step path from node down to this node.
	 * 
	 * @return
	 */
	public String stepPath() {

		MethodAndStep ps = parent();
		if (ps == null) {
			return String.valueOf(stepId());
		}
		return ps.stepPath() + "." + String.valueOf(stepId());
	}

	public String toString() {

		String html = "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" bordercolor=\"#f0f0f0\" width=\"100%\" height=\"100%\">";
		html += "<tr><td align = \"right\"><b>step:</b>&nbsp;</td><td><b>"
				+ (stepPath() == null ? "&nbsp;" : stepPath()) + "</b></td></tr>";
		html += "<tr><td align = \"right\"><b>id:</b>&nbsp;</td><td>" + _step.id() + "</td></tr>";
		html += "<tr><td align = \"right\"><b>name:</b>&nbsp;</td><td>"
				+ (_step.name() == null ? "&nbsp;" : _step.name()) + "</td></tr>";
		if (_step.description() != null) {
			html += "<tr><td align = \"right\"><b>description:</b>&nbsp;</td><td>" + _step.description() + "</td></tr>";
		}
		html += "<table>";
		return html;
	}

}
