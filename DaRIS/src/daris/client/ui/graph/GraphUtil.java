package daris.client.ui.graph;

import arc.gui.gwt.graphics.g2d.Graphics2D;

public class GraphUtil {

	public static double[] DASH_PATTERN = { 0.003, 0.003 };

	public static double[][] ARROW = { { 0.0, 0.0 }, { -0.01, -0.004 }, { -0.01, 0.004 } };

	public static final int MIN_LINE_WIDTH = 1;

	public static final int MIN_PATTERN_LENGTH = 1;

	public static final int MIN_ARROW_WIDTH = 8;

	public static final int MIN_ARROW_HEIGHT = 10;

	public static void dashedLineTo(Graphics2D gc, double fromX, double fromY, double toX, double toY) {

		dashedLineTo(gc, fromX, fromY, toX, toY, DASH_PATTERN);
	}

	public static void dashedLineTo(Graphics2D gc, double fromX, double fromY, double toX, double toY,
			double[] dashPattern) {

		int cw = gc.width();
		int ch = gc.height();
		int x1 = (int) (fromX * cw);
		int y1 = (int) (fromY * ch);
		int x2 = (int) (toX * cw);
		int y2 = (int) (toY * ch);
		int[] pattern = toIntPattern(gc, dashPattern);
		dashedLineTo(gc, x1, y1, x2, y2, pattern);
	}

	public static void dashedLineTo(Graphics2D gc, int fromX, int fromY, int toX, int toY) {

		dashedLineTo(gc, fromX, fromY, toX, toY, toIntPattern(gc, DASH_PATTERN));
	}

	public static void dashedLineTo(Graphics2D gc, int fromX, int fromY, int toX, int toY, int[] dashPattern) {

		int dx = toX - fromX;
		int dy = toY - fromY;
		double angle = Math.atan2(dy, dx);
		int idx = 0;
		boolean draw = true;
		int x = fromX;
		int y = fromY;
		gc.moveTo(fromX, fromY);
		while (!((dx < 0 ? x <= toX : x >= toX) && (dy < 0 ? y <= toY : y >= toY))) {
			int dashLength = dashPattern[idx++ % dashPattern.length];
			double nx = x + (Math.cos(angle) * dashLength);
			x = (int) (dx < 0 ? Math.max((double) toX, nx) : Math.min((double) toX, nx));
			double ny = y + (Math.sin(angle) * dashLength);
			y = (int) (dy < 0 ? Math.max((double) toY, ny) : Math.min((double) toY, ny));
			if (draw) {
				gc.lineTo(x, y);
			} else {
				gc.moveTo(x, y);
			}
			draw = !draw;
		}
	}

	public static void lineTo(Graphics2D gc, double fromX, double fromY, double toX, double toY, LineStyle style) {

		switch (style) {
		case SOLID:
			dashedLineTo(gc, fromX, fromY, toX, toY);
			break;
		case DASHED:
			int x1 = (int) (fromX * gc.width());
			int y1 = (int) (fromY * gc.height());
			int x2 = (int) (toX * gc.width());
			int y2 = (int) (toY * gc.height());
			gc.moveTo(x1, y1);
			gc.lineTo(x2, y2);
			break;
		}
	}

	public static void lineTo(Graphics2D gc, int fromX, int fromY, int toX, int toY, LineStyle style) {

		switch (style) {
		case DASHED:
			dashedLineTo(gc, fromX, fromY, toX, toY);
			break;
		case SOLID:
			gc.moveTo(fromX, fromY);
			gc.lineTo(toX, toY);
			break;
		}
	}

	public static void setLineWidth(Graphics2D gc, double lineWidth) {

		int lw = (int) (lineWidth * gc.width());
		if (lw < MIN_LINE_WIDTH) {
			lw = MIN_LINE_WIDTH;
		}
		gc.setStrokeWidth(lw);
	}

	public static void strokeDashedLine(Graphics2D gc, double fromX, double fromY, double toX, double toY) {

		strokeDashedLine(gc, fromX, fromY, toX, toY, DASH_PATTERN);
	}

	public static void strokeDashedLine(Graphics2D gc, double fromX, double fromY, double toX, double toY,
			double[] dashPattern) {

		gc.beginPath();
		dashedLineTo(gc, fromX, fromY, toX, toY, dashPattern);
		gc.closePath();
		gc.stroke();
	}

	public static void strokeDashedLine(Graphics2D gc, int fromX, int fromY, int toX, int toY) {

		strokeDashedLine(gc, fromX, fromY, toX, toY, toIntPattern(gc, DASH_PATTERN));
	}

	public static void strokeDashedLine(Graphics2D gc, int fromX, int fromY, int toX, int toY, int[] dashPattern) {

		gc.beginPath();
		dashedLineTo(gc, fromX, fromY, toX, toY, dashPattern);
		gc.closePath();
		gc.stroke();
	}

	public static void strokeDashedRectangle(Graphics2D gc, double x, double y, double width, double height) {

		strokeDashedRectangle(gc, x, y, width, height, DASH_PATTERN);
	}

	public static void strokeDashedRectangle(Graphics2D gc, double x, double y, double width, double height,
			double[] dashPattern) {

		int cw = gc.width();
		int ch = gc.height();
		int ax = (int) (x * cw);
		int ay = (int) (y * ch);
		int w = (int) (width * cw);
		int h = (int) (height * ch);
		int[] pattern = toIntPattern(gc, dashPattern);
		strokeDashedRectangle(gc, ax, ay, w, h, pattern);
	}

	public static void strokeDashedRectangle(Graphics2D gc, int x, int y, int width, int height) {

		strokeDashedRectangle(gc, x, y, width, height, toIntPattern(gc, DASH_PATTERN));
	}

	public static void strokeDashedRectangle(Graphics2D gc, int x, int y, int width, int height, int[] dashPattern) {

		int ax = x;
		int ay = y;
		int bx = x + width;
		int by = y;
		int cx = x + width;
		int cy = y + height;
		int dx = x;
		int dy = y + height;
		gc.beginPath();
		dashedLineTo(gc, ax, ay, bx, by, dashPattern);
		dashedLineTo(gc, bx, by, cx, cy, dashPattern);
		dashedLineTo(gc, cx, cy, dx, dy, dashPattern);
		dashedLineTo(gc, dx, dy, ax, ay, dashPattern);
		gc.closePath();
		gc.stroke();
	}

	public static void strokeLine(Graphics2D gc, double fromX, double fromY, double toX, double toY, LineStyle style) {

		switch (style) {
		case DASHED:
			strokeDashedLine(gc, fromX, fromY, toX, toY);
			break;
		case SOLID:
			gc.beginPath();
			dashedLineTo(gc, fromX, fromY, toX, toY);
			gc.closePath();
			gc.stroke();
			break;
		}
	}

	public static void strokeLine(Graphics2D gc, int fromX, int fromY, int toX, int toY, LineStyle style) {

		switch (style) {
		case DASHED:
			strokeDashedLine(gc, fromX, fromY, toX, toY);
			break;
		case SOLID:
			gc.beginPath();
			gc.moveTo(fromX, fromY);
			gc.lineTo(toX, toY);
			gc.closePath();
			gc.stroke();
			break;
		}
	}

	public static void strokeRectangle(Graphics2D gc, double x, double y, double width, double height, LineStyle style) {

		strokeRectangle(gc, (int) (x * gc.width()), (int) (y * gc.height()), (int) (width * gc.width()),
				(int) (height * gc.height()), style);
	}

	public static void strokeRectangle(Graphics2D gc, int x, int y, int width, int height, LineStyle style) {

		switch (style) {
		case SOLID:
			gc.strokeRect(x, y, width, height);
			break;
		case DASHED:
			strokeDashedRectangle(gc, x, y, width, height);
			break;
		}
	}

	private static int[] toIntPattern(Graphics2D gc, double[] dashPattern) {

		int cw = gc.width();
		int ch = gc.height();
		int[] pattern = new int[dashPattern.length];
		for (int i = 0; i < dashPattern.length; i++) {
			pattern[i] = (int) (dashPattern[i] * Math.sqrt(cw * cw + ch * ch));
			if (pattern[i] < MIN_PATTERN_LENGTH) {
				pattern[i] = MIN_PATTERN_LENGTH;
			}
		}
		return pattern;
	}

	public static void strokeArrowLine(Graphics2D gc, int fromX, int fromY, int toX, int toY, LineStyle style,
			int[][] arrow, boolean doubleArrow) {

		double angle = Math.atan2(toY - fromY, toX - fromX);
		drawArrow(gc, translateArrow(rotateArrow(arrow, angle), toX, toY));
		if (doubleArrow) {
			angle = Math.atan2(fromY - toY, fromX - toX);
			drawArrow(gc, translateArrow(rotateArrow(arrow, angle), fromX, fromY));
		}
		strokeLine(gc, fromX, fromY, toX, toY, style);
	}

	private static void drawArrow(Graphics2D gc, int[][] arrow) {

		gc.beginPath();
		gc.moveTo(arrow[2][0], arrow[2][1]);
		for (int i = 0; i < 3; i++) {
			gc.lineTo(arrow[i][0], arrow[i][1]);
		}
		gc.closePath();
		gc.fill();
	}

	private static int[][] translateArrow(int[][] arrow, int x, int y) {

		int[][] rv = new int[3][2];
		for (int i = 0; i < 3; i++) {
			rv[i][0] = arrow[i][0] + x;
			rv[i][1] = arrow[i][1] + y;
		}
		return rv;
	}

	private static int[][] rotateArrow(int[][] arrow, double angle) {

		int[][] rv = new int[3][2];
		for (int i = 0; i < 3; i++) {
			rv[i][0] = (int) (arrow[i][0] * Math.cos(angle) - arrow[i][1] * Math.sin(angle));
			rv[i][1] = (int) (arrow[i][0] * Math.sin(angle) + arrow[i][1] * Math.cos(angle));
		}
		return rv;
	}

	public static void strokeArrowLine(Graphics2D gc, double fromX, double fromY, double toX, double toY,
			LineStyle style, double[][] arrow, boolean doubleArrow) {

		int x1 = (int) (fromX * gc.width());
		int y1 = (int) (fromY * gc.height());
		int x2 = (int) (toX * gc.width());
		int y2 = (int) (toY * gc.height());
		int[][] intArrow = new int[3][2];
		for (int i = 0; i < 3; i++) {
			intArrow[i][0] = (int) (arrow[i][0] * gc.width());
			intArrow[i][1] = (int) (arrow[i][1] * gc.width());
		}
		strokeArrowLine(gc, x1, y1, x2, y2, style, intArrow, doubleArrow);
	}

	public static void strokeArrowLine(Graphics2D gc, double fromX, double fromY, double toX, double toY,
			LineStyle style, boolean doubleArrow) {

		strokeArrowLine(gc, fromX, fromY, toX, toY, style, ARROW, doubleArrow);
	}
}
