package daris.client.ui.graph;

import java.util.List;
import java.util.Vector;

import arc.gui.gwt.graphics.g2d.SingleBufferedGraphics2D;
import arc.gui.gwt.graphics.g2d.scene.Point;
import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.popup.PopupPanel;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;

public class GraphWidget extends ContainerWidget {

	public static final Point DEFAULT_OFFSET = new Point(0.008, 0.008);

	private List<GraphListener> _listeners;
	private Point _offset;
	private SingleBufferedGraphics2D _gc;
	private Graph _graph;
	private int _w;
	private int _h;
	private Node _selected;
	private Node _mouseOverNode;
	private PopupPanel _toolTip;

	/*
	 * 
	 */
	private boolean _initRender = true;

	public GraphWidget(Graph graph, Point offset) {

		_gc = new SingleBufferedGraphics2D();
		_gc.fitToParent();
		initWidget(_gc);
		_graph = graph;
		_w = -1;
		_h = -1;
		_offset = offset;
		addMouseMoveHandler(new MouseMoveHandler() {
			@Override
			public void onMouseMove(MouseMoveEvent event) {

				Node n = findAtomicNodeForMouseEvent(event);
				GraphWidget.this.setCursor(n == null ? Cursor.DEFAULT
						: Cursor.POINTER);
				setMouseOverNode(n);
			}
		});
		addMouseOutHandler(new MouseOutHandler(){

			@Override
			public void onMouseOut(MouseOutEvent event) {
				setMouseOverNode(null);
			}});
		addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				Node n = findAtomicNodeForMouseEvent(event);
				if (n != null) {
					select(n);
				}
			}
		});
		addDoubleClickHandler(new DoubleClickHandler() {

			@Override
			public void onDoubleClick(DoubleClickEvent event) {

				Node n = findAtomicNodeForMouseEvent(event);
				if (n != null) {
					open(n);
				}
			}
		});
	}

	private void setMouseOverNode(final Node n) {

		if (n == null) {
			_mouseOverNode = null;
			if (_toolTip != null) {
				_toolTip.hide();
			}
			return;
		}
		if (n == _mouseOverNode) {
			return;
		}
		if (_toolTip != null) {
			if (_toolTip.isShowing()) {
				_toolTip.hide();
			}
		}
		_mouseOverNode = n;
		_toolTip = new PopupPanel();
		_toolTip.setWidth(60);
		_toolTip.setHeight(30);

		HTML html = n.objectDetail();
		html.fitToParent();
		html.setFontSize(10);
		html.setOpacity(0.9);
		html.setPadding(5);
		html.setBackgroundColour("#dddddd");
		html.setBorder(1, "#f0f0f0");
		html.setBorderRadius(3);
		html.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				_toolTip.hide();
			}
		});
		_toolTip.setContent(html);
		_toolTip.setPopupPositionAndShow(new PositionCallback() {

			@Override
			public void setPosition(int offsetWidth, int offsetHeight) {

				int px = ((int) (_mouseOverNode.x() * _gc.width()))
						+ _gc.absoluteLeft();
				int py = ((int) ((_mouseOverNode.y() + _mouseOverNode.height()) * _gc
						.height())) + _gc.absoluteTop();
				int cw = com.google.gwt.user.client.Window.getClientWidth();
				int ch = com.google.gwt.user.client.Window.getClientHeight();
				if (px + offsetWidth > cw) {
					px = px - (px + offsetWidth - cw);
				}
				if (py + offsetHeight > ch) {
					py = py - (py + offsetHeight - ch);
				}
				_toolTip.setPopupPosition(px, py);
			}
		});
		Timer t = new Timer() {
			@Override
			public void run() {
				if (_toolTip.isShowing()) {
					_toolTip.hide();
				}
			}
		};
		t.schedule(3000);
	}

	@SuppressWarnings("rawtypes")
	private Node findAtomicNodeForMouseEvent(MouseEvent me) {

		double x = (((double) me.getX()) / ((double) _gc.width()));
		double y = (((double) me.getY()) / ((double) _gc.height()));
		return _graph.findAtomicNodeAt(new Point(x, y));
	}

	public GraphWidget(Graph graph) {

		this(graph, DEFAULT_OFFSET);
	}

	public boolean isSupported() {

		return _gc.isSupported();
	}

	public void setOffset(double ox, double oy) {

		_offset = new Point(ox, oy);
	}

	// public void layoutGraph() {
	//
	// /*
	// * layout graph before rendering.
	// */
	// _ratios = _graph.layoutNodes(_gc);
	// _graph.setPosition(_offset.x(), _offset.y());
	//
	// }

	public void render() {

		_gc.clear();
		_graph.render(_gc);
		_gc.flush();
	}

	private void renderIfResized() {

		int w = width();
		int h = height();
		if (w != _w || h != _h) {
			_w = w;
			_h = h;
			render();
		}
	}

	protected void doLayoutChildren() {

		super.doLayoutChildren();
		// We must re-render because a change of canvas size will clear the
		// display.
		//
		// TODO - optimize so we are no over rendering..
		if (_initRender) {

			_initRender = false;
			/*
			 * resize to the size of parent.
			 */
			int pw = parent(this).width();
			int ph = parent(this).height();
			resizeTo(pw, ph, true);
			/*
			 * layout graph nodes
			 */
			double[] ratio = _graph.layoutNodes(_gc);
			if (ratio[0] > 0 || ratio[1] > 0) {
				// over-sized
				int w = pw;
				int h = ph;
				if (ratio[0] > 1.0) {
					w = (int) (w * ratio[0]);
					// _ratios[0] = 1.0;
				}
				if (ratio[1] > 1.0) {
					h = (int) (h * ratio[1]);
					// _ratios[1] = 1.0;
				}
				// enlarge to fit the graph
				resizeTo(w, h, true);
			}
			_graph.setPosition(_offset.x(), _offset.y());
			resizeTo(parent(this).width(), parent(this).height());
			render();
		}
		renderIfResized();
	}

	public void addGraphListener(GraphListener gl) {

		if (_listeners == null) {
			_listeners = new Vector<GraphListener>();
		}
		_listeners.add(gl);
	}

	public void removeGraphListener(GraphListener gl) {

		if (_listeners != null) {
			_listeners.remove(gl);
		}
	}

	private void notifyOfOpen(Node n) {

		if (_listeners != null) {
			for (GraphListener l : _listeners) {
				l.open(n);
			}
		}
	}

	private void notifyOfSelect(Node n) {

		if (_listeners != null) {
			for (GraphListener l : _listeners) {
				l.select(n);
			}
		}
	}

	private void notifyOfDeselect(Node n) {

		if (_listeners != null) {
			for (GraphListener l : _listeners) {
				l.deselect(n);
			}
		}
	}

	private void open(Node node) {

		if (_selected != null) {
			if (!_selected.equals(node)) {
				_selected.setSelected(false);
				notifyOfDeselect(_selected);
			}
		}
		if (node.canSelect()) {
			node.setSelected(true);
			_selected = node;
			notifyOfOpen(node);
		} else {
			_selected = null;
		}
		render();
	}

	private void select(Node node) {

		if (_selected != null) {
			_selected.setSelected(false);
			notifyOfDeselect(_selected);
			if (_selected.equals(node)) {
				_selected = null;
				render();
				return;
			}
		}
		if (node.canSelect()) {
			node.setSelected(true);
			_selected = node;
			notifyOfSelect(_selected);
		}
		render();
	}

}