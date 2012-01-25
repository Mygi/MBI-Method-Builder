package daris.client.ui.graph;

import java.util.List;

import arc.gui.gwt.graphics.g2d.Graphics2D;

public class TreeLayout implements Graph.Layout {

	public static enum Direction {
		horizontal, vertical;
	}

	public static final double MIN_HSPACE = 0.02;
	public static final double MIN_VSPACE = 0.02;
	private Direction _direction;
	private double _vspace;
	private double _hspace;

	public TreeLayout() {

		this(Direction.horizontal);
	}

	public TreeLayout(Direction direction) {

		_direction = direction;
		_hspace = MIN_HSPACE;
		_vspace = MIN_VSPACE;
	}

	@Override
	public void layoutGraph(Graphics2D gc, Graph graph) {

		if (_direction == Direction.horizontal) {
			layoutHorizontal(graph, gc);
		} else {
			// TODO:
			throw new AssertionError("Vertical layout is not implemented yet.");
		}
	}

	private void layoutHorizontal(Graph graph, Graphics2D gc) {

		// Layout the nodes themselves.. they may contain
		// sub-graphs etc. This will establish the size of
		// the nodes.
		List<Node> nodes = graph.nodes();
		for (Node node : nodes) {
			node.layoutSelf(gc);
		}

		double ox = 0;
		double oy = 0;
		nodes = graph.inputNodes();
		double width = columnWidth(nodes);
		for (Node node : nodes) {
			double height = rowHeight(node);
			// Center the node along the centre-line of the maximum
			// height of the downstream nodes.
			layoutNodeHorizontal(ox, oy, node, width, height, gc);
			oy += height;
		}
	}

	private double columnWidth(List<Node> nodes) {

		// Determine "column" width for this set of nodes..
		double width = 0;
		for (Node node : nodes) {
			if (node.width() > width) {
				width = node.width();
			}
		}
		return width;
	}

	private void layoutNodeHorizontal(double ox, double oy, Node node,
			double width, double height, Graphics2D gc) {

		double dy = (height > node.height()) ? height / 2 - node.height() / 2
				: 0.0;
		node.setPosition(ox, oy + dy);
		node.moveInputPorts(Node.LEFT);
		node.moveOutputPorts(Node.RIGHT);
		List<Node> nodes = node.outputNodes();
		if (nodes != null) {
			double swidth = columnWidth(nodes);
			for (int i = 0; i < nodes.size(); i++) {
				Node subNode = nodes.get(i);
				double snHeight = rowHeight(subNode);
				layoutNodeHorizontal(ox + width + _hspace, oy, nodes.get(i),
						swidth, snHeight, gc);
				oy += snHeight + _vspace;
			}
		}

	}

	private double rowHeight(Node node) {

		double height = node.height();
		List<Node> outputs = node.outputNodes();
		if (outputs == null || outputs.isEmpty()) {
			return height;
		}
		double nodeHeight = 0;
		for (int i = 0; i < outputs.size(); i++) {
			nodeHeight += rowHeight(outputs.get(i));
			if (i < outputs.size() - 1) {
				nodeHeight += _vspace;
			}
		}
		if (nodeHeight > height) {
			height = nodeHeight;
		}
		return height;
	}
}
