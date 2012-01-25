package daris.client.ui.graph;

import java.util.List;
import java.util.Vector;

import arc.gui.gwt.graphics.g2d.Graphics2D;
import arc.gui.gwt.graphics.g2d.scene.GraphicsObject;
import arc.gui.gwt.graphics.g2d.scene.GraphicsObjectReadyHandler;
import arc.gui.gwt.graphics.g2d.scene.HitDistance;
import arc.gui.gwt.graphics.g2d.scene.Point;
import arc.gui.gwt.graphics.g2d.scene.PrepareRequest;
import arc.gui.gwt.graphics.g2d.scene.Rectangle;

public class Graph implements GraphicsObject {

	public static interface Layout {
		void layoutGraph(Graphics2D gc, Graph graph);
	}

	public static double MARGIN_RIGHT = 0.02;
	public static double MARGIN_BOTTOM = 0.02;

	private double _x;
	private double _y;
	private double _w;
	private double _h;
	private List<Node> _nodes;
	private List<Edge> _edges;
	private String _id;
	private Layout _layout;

	public Graph(String id) {

		this(id, new TreeLayout());
	}

	public Graph(String id, Layout layout) {

		_id = id;
		_layout = layout;
		_x = 0;
		_y = 0;
		_w = 0;
		_h = 0;
		_nodes = new Vector<Node>();
		_edges = new Vector<Edge>();
	}

	public String id() {

		return _id;
	}

	public double x() {

		return _x;
	}

	public double y() {

		return _y;
	}

	public double width() {

		return _w;
	}

	public double height() {

		return _h;
	}

	public List<Node> nodes() {

		return _nodes;
	}

	public List<Node> inputNodes() {

		List<Node> inputs = new Vector<Node>();
		for (Node node : _nodes) {
			if (!node.hasConnectedInput()) {
				inputs.add(node);
			}
		}
		return inputs;
	}

	public void addNode(Node node) {

		_nodes.add(node);
	}

	protected Node findNodeAt(Point p) {

		if (_nodes == null) {
			return null;
		}
		for (Node node : _nodes) {
			if (node.onOrInside(p)) {
				return node;
			}
		}
		return null;
	}

	protected Node findAtomicNodeAt(Point p) {

		Node node = findNodeAt(p);
		if (node == null) {
			return null;
		}
		if (node.isAtomic()) {
			return node;
		} else {
			if (node instanceof SubGraphNode) {
				return ((SubGraphNode) node).findAtomicNodeAt(p);
			}
			return null;
		}
	}

	public List<Edge> edges() {

		return _edges;
	}

	public void addEdge(Edge edge) {

		_edges.add(edge);
	}

	protected double[] layoutNodes(Graphics2D gc) {

		_layout.layoutGraph(gc, this);
		double width = 0;
		double height = 0;
		for (Node node : _nodes) {
			if (node.x() + node.width() > width) {
				width = node.x() + node.width();
			}
			if (node.y() + node.height() > height) {
				height = node.y() + node.height();
			}
		}
		_w = width;
		_h = height;
		double xratio = width > 1.0 ? width + MARGIN_RIGHT : 1.0;
		double yratio = height > 1.0 ? height + MARGIN_BOTTOM : 1.0;
		if (xratio > 1.0 || yratio > 1.0) {
			shrink(xratio, yratio);
		}
		return new double[] { xratio, yratio };
	}

	public void shrink(double xratio, double yratio) {

		if (_nodes != null) {
			for (Node node : _nodes) {
				node.shrink(xratio, yratio);
			}
		}
		_w = _w / xratio;
		_h = _h / yratio;
		_x = _x / xratio;
		_y = _y / yratio;
	}

	public void setPosition(double x, double y) {

		// Move all sub-nodes/edges by the delta in position.
		double dx = x - _x;
		double dy = y - _y;
		// Move all of the nodes..
		for (Node node : _nodes) {
			node.setPosition(node.x() + dx, node.y() + dy);
		}
		_x = x;
		_y = y;
	}

	@Override
	public void render(Graphics2D gc) {

		if (gc.width() == 0 || gc.height() == 0) {
			return;
		}

		// draw nodes first
		for (Node node : _nodes) {
			node.render(gc);
		}

		// the draw edges
		for (Edge edge : _edges) {
			edge.render(gc);
		}

	}

	@Override
	public PrepareRequest prepare(Graphics2D gc, GraphicsObjectReadyHandler rh) {

		rh.ready();
		return null;
	}

	@Override
	public Object data() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setData(Object data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean fixedBoundingBox() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Rectangle boundingBox(Graphics2D gc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HitDistance hit(Graphics2D gc, int px, int py) {
		// TODO Auto-generated method stub
		return null;
	}
}
