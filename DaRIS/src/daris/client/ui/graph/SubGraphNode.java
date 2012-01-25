package daris.client.ui.graph;

import arc.gui.gwt.graphics.g2d.Graphics2D;
import arc.gui.gwt.graphics.g2d.scene.Point;

public class SubGraphNode extends Node {

	public static final double MARGIN = 0.03;

	private Graph _graph;

	public SubGraphNode(int id, String name, Graph graph) {

		super(id, graph.id() == null ? name : graph.id() + "-" + name);
		_graph = graph;
	}

	public void layoutSelf(Graphics2D gc) {

		_graph.layoutNodes(gc);
		super.setSize(_graph.width() + MARGIN * 2, _graph.height() + MARGIN * 2);
	}

	public void setPosition(double x, double y) {

		super.setPosition(x, y);
		_graph.setPosition(x + MARGIN, y + MARGIN);
	}

	public Node nodeAt(Point p) {

		if (onOrInside(p)) {
			return _graph.findNodeAt(p);
		}
		return null;
	}

	public void render(Graphics2D gc) {

		// Draw text first, just in case there is not enough space for
		// it - is will then go behind the box.
		drawLabel(gc, TEXT_MARGIN_HORIZONTAL, TEXT_MARGIN_VERTICAL);
		_graph.render(gc);
		drawBorder(gc, BORDER_WIDTH, BORDER_COLOUR, LineStyle.DASHED);
		drawPorts(gc);
	}

	public boolean canSelect() {

		return false;
	}

	public boolean isAtomic() {

		return false;
	}

	protected Node findAtomicNodeAt(Point p) {

		return _graph.findAtomicNodeAt(p);
	}

	public void shrink(double xratio, double yratio) {

		super.shrink(xratio, yratio);
		_graph.shrink(xratio, yratio);
	}

}
