package daris.client.ui.exmethod;

import java.util.List;

import com.google.gwt.resources.client.ImageResource;

import daris.client.model.exmethod.ExMethod;
import daris.client.model.exmethod.ExMethodStep;
import daris.client.model.exmethod.State;
import daris.client.model.method.BranchStep;
import daris.client.model.method.Method;
import daris.client.model.method.MethodStep;
import daris.client.model.method.Step;
import daris.client.model.method.StudyActionStep;
import daris.client.model.method.SubjectActionStep;
import daris.client.ui.graph.BranchNode;
import daris.client.ui.graph.Edge;
import daris.client.ui.graph.Graph;
import daris.client.ui.graph.Node;
import daris.client.ui.graph.Port;
import daris.client.ui.graph.SubGraphNode;

public class ExMethodGraph {

	// TODO: add icons.
	public static final ImageResource STUDY_ACTION_STEP_ICON = daris.client.Resource.INSTANCE
			.ruler12x32();
	public static final ImageResource SUBJECT_ACTION_STEP_ICON = daris.client.Resource.INSTANCE
			.document16();

	private static ImageResource iconFor(Step step) {

		if (step instanceof StudyActionStep) {
			return STUDY_ACTION_STEP_ICON;
		} else if (step instanceof SubjectActionStep) {
			return SUBJECT_ACTION_STEP_ICON;
		}
		return null;
	}

	private static void setNodeState(Node gn, ExMethod em, String stepPath) {

		ExMethodStep ems = em.step(stepPath);
		if (ems != null) {
			setNodeState(gn, ems.state());
		}
	}

	private static void setNodeState(Node gn, State state) {

		// switch (state) {
		// case incomplete:
		// gn.setState(Node.STATE_INCOMPLETE);
		// break;
		// case complete:
		// gn.setState(Node.STATE_COMPLETE);
		// break;
		// case waiting:
		// gn.setState(Node.STATE_WAITING);
		// break;
		// case abandoned:
		// gn.setState(Node.STATE_ABANDONED);
		// break;
		// }
	}

	private static Graph createMethodGraph(ExMethod em, Method m,
			MethodAndStep mas) {

		Graph graph = new Graph(m.id());
		// The highest level steps are sequential..
		Node lgn = null;
		List<Step> steps = m.steps();
		if (steps != null) {
			for (int i = 0; i < steps.size(); i++) {
				Step s = steps.get(i);
				MethodAndStep smas = new MethodAndStep(mas, em, s);
				Node gn;
				if (s.isSimple()) {
					gn = new Node(s.id(), s.name());
					gn.setObject(smas);
					if (s instanceof StudyActionStep) {
						gn.setIcon(iconFor(s));
					}
				} else {
					Graph sgraph = createSubGraph(em, s, smas);
					gn = new SubGraphNode(s.id(), s.name(), sgraph);
					gn.setObject(smas);
				}
				setNodeState(gn, em, smas.stepPath());
				graph.addNode(gn);
				if (lgn != null) {
					Edge e = new Edge(lgn.createOutputPort(),
							gn.createInputPort());
					graph.addEdge(e);
				}
				lgn = gn;
			}
		}
		return graph;
	}

	private static Graph createSubGraph(ExMethod em, Step s, MethodAndStep mas) {

		if (s instanceof MethodStep) {
			MethodStep ms = (MethodStep) s;
			return createMethodGraph(em, ms.method(), mas);
		}
		// Must be a branch..
		BranchStep bs = (BranchStep) s;
		Graph graph = new Graph(null);
		Node gn = new BranchNode(bs.id(), bs.name(),
				bs.type() == BranchStep.BRANCH_ONE ? BranchNode.BRANCH_ONE
						: BranchNode.BRANCH_ALL);
		graph.addNode(gn);
		List<Method> methods = bs.methods();
		if (methods != null) {
			Port op = gn.createOutputPort();
			for (int i = 0; i < methods.size(); i++) {
				Method m = methods.get(i);
				MethodAndStep smas = new MethodAndStep(mas, em, i + 1);

				Graph sgraph = createMethodGraph(em, m, smas);
				gn = new SubGraphNode(s.id(), m.name(), sgraph);
				gn.setObject(smas);

				graph.addNode(gn);
				graph.addEdge(new Edge(op, gn.createInputPort()));
			}
		}
		return graph;
	}

	public static Graph graphFor(ExMethod em) {

		return createMethodGraph(em, em.method(), null);
	}
}
