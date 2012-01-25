package daris.client.ui.g2d;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;

public interface EventOverlayListener {
	public void zoomIn(int x, int y);

	public void zoomOut(int x, int y);

	public void mouseDown(MouseDownEvent event);

	public void mouseMove(MouseMoveEvent event);

	public void mouseOut(MouseOutEvent event);

	public void mouseUp(MouseUpEvent event);

	public void click(ClickEvent event);

	public void doubleClick(DoubleClickEvent event);

	public void beginMove(int x, int y);

	public void moveTo(int x, int y);

	public void endMove();
}