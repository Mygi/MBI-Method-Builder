package daris.client.ui.graph;

import java.util.List;
import java.util.Vector;

import arc.gui.gwt.graphics.colour.Colour;
import arc.gui.gwt.graphics.g2d.Graphics2D;
import arc.gui.gwt.graphics.g2d.GraphicsImage;
import arc.gui.gwt.graphics.g2d.scene.GraphicsObject;
import arc.gui.gwt.graphics.g2d.scene.GraphicsObjectReadyHandler;
import arc.gui.gwt.graphics.g2d.scene.HitDistance;
import arc.gui.gwt.graphics.g2d.scene.Point;
import arc.gui.gwt.graphics.g2d.scene.PrepareRequest;
import arc.gui.gwt.graphics.g2d.scene.Rectangle;
import arc.gui.gwt.widget.HTML;

import com.google.gwt.resources.client.ImageResource;

public class Node implements GraphicsObject, GraphicsImage.LoadHandler {

	public static final double BORDER_WIDTH = 0.002;
	public static final double BORDER_WIDTH_SELECTED = 0.002;
	public static final Colour BORDER_COLOUR = Colours.DARK_GRAY;
	public static final Colour BORDER_COLOUR_SELECTED = Colours.BLUE;
	public static final Colour BACKGROUND_COLOUR = Colours.LIGHT_GRAY;
	public static final double WIDTH = 0.08;
	public static final double HEIGHT = 0.08;

	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int TOP = 2;
	public static final int DOWN = 3;

	public static final String FONT_WEIGHT = "normal";
	public static final String FONT_STYLE = "normal";
	public static final String FONT_VARIANT = "normal";
	public static final double FONT_SIZE = 0.0085;
	public static final String FONT_FAMILY = "sans-serif";

	public static final double TEXT_MARGIN_HORIZONTAL = 0.002;
	public static final double TEXT_MARGIN_VERTICAL = 0.003;

	private double _x;
	private double _y;
	private double _w;
	private double _h;
	private List<Port> _inputs;
	private List<Port> _outputs;
	private boolean _selected;
	private Object _object;
	private String _name;
	private int _id;

	private Colour _backgroundColour;
	private double _borderWidth;
	private Colour _borderColour;
	private double _fontSize;
	private GraphicsImage _icon;
	private GraphicsObjectReadyHandler _rh;

	public Node(int id, String name) {

		_id = id;
		_name = name;
		_x = 0.0;
		_y = 0.0;
		_w = WIDTH;
		_h = HEIGHT;
		_selected = false;
		_object = null;
		_backgroundColour = BACKGROUND_COLOUR;
		_borderWidth = BORDER_WIDTH;
		_borderColour = BORDER_COLOUR;
		_fontSize = FONT_SIZE;
	}

	public void setIcon(String url, int width, int height) {

		setIcon(new GraphicsImage(url, width, height));
	}

	public void setIcon(ImageResource icon) {

		setIcon(icon.getSafeUri().asString(), icon.getWidth(), icon.getHeight());
	}

	public void setIcon(GraphicsImage icon) {

		_icon = icon;
		_icon.setLoadHandler(this);
	}

	public int id() {

		return _id;
	}

	public String name() {

		return _name;
	}

	public boolean isAtomic() {

		return true;
	}

	public boolean canSelect() {

		return true;
	}

	public boolean isSelected() {

		return _selected;
	}

	public void setSelected(boolean selected) {

		_selected = selected;
		if (_selected) {
			setBorderColour(BORDER_COLOUR_SELECTED);
			setBorderWidth(BORDER_WIDTH_SELECTED);
		} else {
			setBorderColour(BORDER_COLOUR);
			setBorderWidth(BORDER_WIDTH);
		}
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

	public Object object() {

		return _object;
	}

	public void setObject(Object object) {

		_object = object;
	}

	protected void setSize(double width, double height) {

		_w = width;
		_h = height;

	}

	public boolean hasConnectedInput() {

		if (_inputs == null) {
			return false;
		}
		for (Port port : _inputs) {
			if (port.edges() != null) {
				if (!port.edges().isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}

	public Port createInputPort() {

		Port p = new Port();
		addInput(p);
		return p;
	}

	public void addInput(Port port) {

		if (_inputs == null) {
			_inputs = new Vector<Port>();
		}
		_inputs.add(port);
		port.setOwnerNode(this);
	}

	public boolean hasConnectedOutput() {

		if (_outputs == null) {
			return false;
		}
		for (Port port : _outputs) {
			if (port.edges() != null) {
				if (!port.edges().isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}

	public Port createOutputPort() {

		Port p = new Port();
		addOutput(p);
		return p;
	}

	public void addOutput(Port port) {

		if (_outputs == null) {
			_outputs = new Vector<Port>();
		}
		_outputs.add(port);
	}

	public List<Node> outputNodes() {

		if (_outputs == null || _outputs.isEmpty()) {
			return null;
		}
		Vector<Node> outputNodes = new Vector<Node>(_outputs.size());
		for (Port port : _outputs) {
			List<Node> nodes = port.connectedNodes();
			if (nodes != null) {
				outputNodes.addAll(nodes);
			}
		}
		return outputNodes;
	}

	public void setPosition(double x, double y) {

		double dx = x - _x;
		double dy = y - _y;

		if (_inputs != null) {
			for (Port port : _inputs) {
				port.setPosition(port.x() + dx, port.y() + dy);
			}
		}

		if (_outputs != null) {
			for (Port port : _outputs) {
				port.setPosition(port.x() + dx, port.y() + dy);
			}
		}
		_x = x;
		_y = y;
	}

	public void moveInputPorts(int boundary) {

		if (_inputs == null) {
			return;
		}
		if (boundary == LEFT || boundary == RIGHT) {
			double px = _x;
			double dy = _h / (2 * _inputs.size());
			double py = _y + dy;
			for (Port port : _inputs) {
				port.setPosition(px, py);
				port.setSize(Port.WIDTH, Port.HEIGHT);
			}
		} else {
			// TODO:
		}
	}

	public void moveOutputPorts(int boundary) {

		if (_outputs == null) {
			return;
		}
		if (boundary == LEFT || boundary == RIGHT) {
			double px = _x + _w;
			double dy = _h / (2 * _outputs.size());
			double py = _y + dy;
			for (Port port : _outputs) {
				port.setPosition(px, py);
				port.setSize(Port.WIDTH, Port.HEIGHT);
			}
		} else {
			// TODO:
		}
	}

	public Node nodeAt(Point p) {

		if (onOrInside(p)) {
			return this;
		}
		return null;
	}

	public boolean onOrInside(Point p) {

		if (p.x() < _x) {
			return false;
		}
		if (p.y() < _y) {
			return false;
		}
		if (p.x() > _x + _w) {
			return false;
		}
		if (p.y() > _y + _h) {
			return false;
		}
		return true;
	}

	public void setBackgroundColour(Colour backgroundColour) {

		_backgroundColour = backgroundColour;
	}

	public void setBorderWidth(double borderWidth) {

		_borderWidth = borderWidth;
	}

	public void setBorderColour(Colour borderColour) {

		_borderColour = borderColour;
	}

	@Override
	public void render(Graphics2D gc) {

		drawBox(gc, _backgroundColour);
		drawIcon(gc);
		drawBorder(gc, _borderWidth, _borderColour, LineStyle.SOLID);
		drawLabel(gc, TEXT_MARGIN_HORIZONTAL, TEXT_MARGIN_VERTICAL);
		drawPorts(gc);
		// drawStateIndicator(gc);
	}

	protected void drawIcon(Graphics2D gc) {

		if (_icon == null) {
			return;
		}
		int x = (int) (_x * gc.width());
		int y = (int) (_y * gc.height());
		int w = (int) (_w * gc.width());
		int h = (int) (_h * gc.height());
		int iwo = _icon.width();
		int iho = _icon.height();
		double r = (double) (iho / iwo);

		int iw = iwo;
		int ih = iho;
		if (iw > w / 2) {
			iw = w / 2;
			ih = (int) (iw * r);
		}
		if (iw < w / 10) {
			iw = w / 10;
			ih = (int) (iw * r);
		}
		if (ih > h / 3 * 2) {
			ih = h / 3 * 2;
			iw = (int) (ih / r);
		}
		if (ih < h / 6) {
			ih = h / 6;
			iw = (int) (ih / r);
		}

		int dx = x + (int) (_borderWidth / 2 * gc.width());
		int dy = y + h - ih;

		if (_icon != null) {
			gc.drawImage(_icon, 0, 0, iwo, iho, dx, dy, iw, ih, 1.0);
		}
	}

	protected void drawLabel(Graphics2D gc, double ox, double oy) {

		int fontSize = (int) (_fontSize * gc.width());
		gc.setTextFont(FONT_WEIGHT + " " + FONT_STYLE + " " + FONT_VARIANT
				+ " " + fontSize + "px " + FONT_FAMILY);
		gc.setTextBaseline("top");
		gc.setTextAlign("left");
		gc.setFillColour(Colours.BLACK);
		gc.fillText((int) ((_x + ox) * gc.width()),
				(int) ((_y + oy) * gc.height()), _name);
	}

	protected void drawBox(Graphics2D gc, Colour backgroundColour) {

		gc.setFillColour(backgroundColour);
		gc.fillRect((int) (_x * gc.width()), (int) (_y * gc.height()),
				(int) (_w * gc.width()), (int) (_h * gc.height()));

	}

	protected void drawBorder(Graphics2D gc, double borderWidth,
			Colour borderColour, LineStyle style) {

		gc.setStrokeColour(borderColour);
		GraphUtil.setLineWidth(gc, borderWidth);
		GraphUtil.strokeRectangle(gc, _x, _y, _w, _h, style);
	}

	protected void drawPorts(Graphics2D gc) {

		if (_inputs != null) {
			for (Port input : _inputs) {
				input.render(gc);
			}
		}
		if (_outputs != null) {
			for (Port output : _outputs) {
				output.render(gc);
			}
		}
	}

	protected void layoutSelf(Graphics2D gc) {

	}

	protected void shrink(double xratio, double yratio) {

		setSize(width() / xratio, height() / yratio);
		if (_inputs != null) {
			for (Port p : _inputs) {
				p.setSize(p.width() / xratio, p.height() / yratio);
				p.setPosition(p.x() / xratio, p.y() / yratio);
			}
		}
		if (_outputs != null) {
			for (Port p : _outputs) {
				p.setSize(p.width() / xratio, p.height() / yratio);
				p.setPosition(p.x() / xratio, p.y() / yratio);
			}
		}
		_x = _x / xratio;
		_y = _y / yratio;
		_fontSize = _fontSize / xratio;
	}

	protected HTML objectDetail() {

		return new HTML(_object.toString());
	}

	@Override
	public PrepareRequest prepare(Graphics2D gc, GraphicsObjectReadyHandler rh) {

		if (_icon == null) {
			rh.ready();
		}

		int gw = gc.width();
		int gh = gc.height();
		int dw = (int) (_icon.width() * gw);
		int dh = (int) (_icon.height() * gh);
		if (dw == 0 || dh == 0) {
			rh.ready();
			return null;
		}
		_rh = rh;
		if (_icon.loaded()) {
			_rh.ready();
		} else {
			_icon.setLoadHandler(this);
		}
		return null;
	}

	@Override
	public void loaded(GraphicsImage di) {

		_rh.ready();
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
