package daris.client.ui.util;

import arc.gui.window.Window;

public class WindowUtil {
	public static int windowWidth(double w) {
		return windowWidth(null, w);
	}

	public static int windowWidth(Window owner, double w) {

		if (owner != null) {
			if (owner.nativeWindow() instanceof arc.gui.gwt.widget.window.Window) {
				arc.gui.gwt.widget.window.Window nw = (arc.gui.gwt.widget.window.Window) (owner
						.nativeWindow());
				return (int) (nw.width() * w);
			}
		}
		return (int) (com.google.gwt.user.client.Window.getClientWidth() * w);
	}

	public static int windowHeight(double h) {
		return windowHeight(null, h);
	}

	public static int windowHeight(Window owner, double h) {

		if (owner != null) {
			if (owner.nativeWindow() instanceof arc.gui.gwt.widget.window.Window) {
				arc.gui.gwt.widget.window.Window nw = (arc.gui.gwt.widget.window.Window) (owner
						.nativeWindow());
				return (int) (nw.height() * h);
			}
		}
		return (int) (com.google.gwt.user.client.Window.getClientHeight() * h);
	}
}
