package daris.client.ui.object.action;

import daris.client.ui.util.WindowUtil;
import arc.gui.window.Window;

public class ActionIntefaceUtil {

	public static final double WIN_WIDTH = 0.8;

	public static final double WIN_HEIGHT = 0.8;

	public static int windowWidth(Window owner) {

		return WindowUtil.windowWidth(owner, WIN_WIDTH);
	}

	public static int windowHeight(Window owner) {

		return WindowUtil.windowHeight(owner, WIN_HEIGHT);
	}
}
