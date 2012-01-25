package daris.client.ui.graph;

import arc.gui.gwt.graphics.g2d.Graphics2D;

public class BranchNode extends Node {

	/**
	 * Branch to any sub-step.
	 */
	public static final int BRANCH_ONE = 1;

	/**
	 * Branch to all sub-steps.
	 */
	public static final int BRANCH_ALL = 2;

	public static final String BRANCH_ANY_NAME = "?";
	public static final String BRANCH_ALL_NAME = null;

	public static final double DEFAULT_ONE_WIDTH = 0.016;
	public static final double DEFAULT_ONE_HEIGHT = 0.048;

	public static final double DEFAULT_ALL_WIDTH = 0.008;
	public static final double DEFAULT_ALL_HEIGHT = 0.048;

	private int _type;

	public BranchNode(int id, String name, int type) {

		super(id, type == BRANCH_ONE ? BRANCH_ANY_NAME : BRANCH_ALL_NAME);
		if (type == BRANCH_ALL) {
			super.setSize(DEFAULT_ALL_WIDTH, DEFAULT_ALL_HEIGHT);
		} else {
			super.setSize(DEFAULT_ONE_WIDTH, DEFAULT_ONE_HEIGHT);
		}
		_type = type;
	}

	public boolean canSelect() {

		return false;
	}

	public boolean isAtomic() {
		
		return false;
	}

	public void render(Graphics2D gc) {

		// Draw text first, just in case there is not enough space for
		// it - is will then go behind the box.
		if (_type == BRANCH_ONE) {
			super.drawBox(gc, BACKGROUND_COLOUR);
			// super.drawLabel(gc);
			super.drawBorder(gc, BORDER_WIDTH, BORDER_COLOUR, LineStyle.SOLID);
		} else {
			super.drawBox(gc, BACKGROUND_COLOUR);
		}
	}

}
