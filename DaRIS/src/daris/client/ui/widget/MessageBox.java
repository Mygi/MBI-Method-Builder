package daris.client.ui.widget;

import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.popup.PopupPanel;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;

public class MessageBox {

	public static enum Position {
		TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER
	}

	public static class BasePoint {

		private Position _position;
		private int _x;
		private int _y;

		public BasePoint(Position position, int x, int y) {
			_position = position;
			_x = x;
			_y = y;
		}

		public int x() {
			return _x;
		}

		public int y() {
			return _y;
		}

		public int topLeftX(int offsetWidth, int offsetHeight, int clientWidth,
				int clientHeight) {
			int tlx = 0;
			switch (_position) {
			case TOP_LEFT:
				tlx = _x;
				break;
			case TOP_RIGHT:
				tlx = _x - offsetWidth;
				break;
			case BOTTOM_LEFT:
				tlx = _x;
				break;
			case BOTTOM_RIGHT:
				tlx = _x - offsetWidth;
				break;
			case CENTER:
				tlx = _x - offsetWidth / 2;
				break;
			}
			if (tlx < 0) {
				tlx = 0;
			}
			if (tlx + offsetWidth > clientWidth) {
				tlx = clientWidth - offsetWidth;
			}
			return tlx;
		}

		public int topLeftY(int offsetWidth, int offsetHeight, int clientWidth,
				int clientHeight) {
			int tly = 0;
			switch (_position) {
			case TOP_LEFT:
				tly = _y;
				break;
			case TOP_RIGHT:
				tly = _y;
				break;
			case BOTTOM_LEFT:
				tly = _y - offsetHeight;
				break;
			case BOTTOM_RIGHT:
				tly = _y - offsetHeight;
				break;
			case CENTER:
				tly = _y - offsetHeight / 2;
				break;
			}
			if (tly < 0) {
				tly = 0;
			}
			if (tly + offsetHeight > clientHeight) {
				tly = clientHeight - offsetHeight;
			}
			return tly;
		}
	}

	private static PopupPanel _pp;

	public static void display(final BasePoint bp, String title,
			String message, int seconds) {

		if (_pp != null) {
			_pp.hide();
		}
		_pp = new PopupPanel();
		_pp.setAutoHideEnabled(true);

		HTML html = new HTML(
				"<div style=\"font-size:9pt\" width=\"100%\" height=\"100%\"><b>"
						+ title + "</b><br>" + message + "</div>");
		html.fitToParent();
		html.setOpacity(0.9);
		html.setPadding(5);
		html.setBackgroundColour("#dddddd");
		html.setBorder(1, "#f0f0f0");
		html.setBorderRadius(3);
		_pp.setContent(html);
		_pp.setPopupPositionAndShow(new PositionCallback() {

			@Override
			public void setPosition(int offsetWidth, int offsetHeight) {

				int clientWidth = com.google.gwt.user.client.Window
						.getClientWidth();
				int clientHeight = com.google.gwt.user.client.Window
						.getClientHeight();
				int tlx = bp.topLeftX(offsetWidth, offsetHeight, clientWidth,
						clientHeight);
				int tly = bp.topLeftY(offsetWidth, offsetHeight, clientWidth,
						clientHeight);
				_pp.setPopupPosition(tlx, tly);
			}
		});
		_pp.show();
		final PopupPanel pp = _pp;
		Timer timer = new Timer() {
			@Override
			public void run() {

				pp.hide();
				cancel();
			}
		};
		timer.schedule(seconds * 1000);
	}

	public static void display(Position position, String title, String message,
			int seconds) {
		int clientWidth = com.google.gwt.user.client.Window.getClientWidth();
		int clientHeight = com.google.gwt.user.client.Window.getClientHeight();
		BasePoint bp;
		switch (position) {
		case TOP_LEFT:
			bp = new BasePoint(Position.TOP_LEFT, 0, 0);
			break;
		case TOP_RIGHT:
			bp = new BasePoint(Position.TOP_RIGHT, clientWidth, 0);
			break;
		case CENTER:
			bp = new BasePoint(Position.CENTER, clientWidth / 2,
					clientHeight / 2);
			break;
		case BOTTOM_LEFT:
			bp = new BasePoint(Position.BOTTOM_LEFT, 0, clientHeight);
			break;
		case BOTTOM_RIGHT:
			bp = new BasePoint(Position.BOTTOM_RIGHT, clientWidth, clientHeight);
			break;
		default:
			bp = new BasePoint(Position.CENTER, clientWidth / 2,
					clientHeight / 2);
			break;
		}
		display(bp, title, message, seconds);
	}

	public static void display(String title, String message, int seconds) {
		display(Position.CENTER, title, message, seconds);
	}
}
