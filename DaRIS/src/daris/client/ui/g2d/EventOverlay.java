package daris.client.ui.g2d;

import arc.gui.gwt.dnd.DragHandler;
import arc.gui.gwt.widget.panel.SimplePanel;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.user.client.Event;

public class EventOverlay extends SimplePanel {

	public EventOverlay(final EventOverlayListener el) {

		setLeft(0);
		setTop(0);
		setPosition(Position.ABSOLUTE);
		fitToParent();

		addDomHandler(new MouseWheelHandler() {
			public void onMouseWheel(MouseWheelEvent event) {

				int dy = event.getDeltaY();

				if (dy < 0) {
					el.zoomIn(event.getX(), event.getY());
				} else if (dy > 0) {
					el.zoomOut(event.getX(), event.getY());
				}
			}

		}, MouseWheelEvent.getType());

		addMouseDownHandler(new MouseDownHandler() {
			public void onMouseDown(MouseDownEvent event) {

				el.mouseDown(event);
			}
		});

		addMouseMoveHandler(new MouseMoveHandler() {
			public void onMouseMove(MouseMoveEvent event) {

				el.mouseMove(event);
			}
		});

		addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {

				el.mouseOut(event);
			}

		});

		addMouseUpHandler(new MouseUpHandler() {
			public void onMouseUp(MouseUpEvent event) {

				el.mouseUp(event);
			}
		});

		addClickHandler(new ClickHandler() {
			@Override
			public void onClick(com.google.gwt.event.dom.client.ClickEvent event) {

				el.click(event);
			}
		});

		addDoubleClickHandler(new DoubleClickHandler() {
			@Override
			public void onDoubleClick(DoubleClickEvent event) {

				el.doubleClick(event);
			}

		});

		enableDragMove(new DragHandler() {

			public Cursor beginDrag(int x, int y) {

				el.beginMove(x, y);
				return Cursor.MOVE;
			}

			public void moveTo(int x, int y) {

				el.moveTo(x, y);
			}

			public void endDrag() {

				el.endMove();
			}

			public boolean isDragDown(MouseDownEvent event) {

				if (event.isShiftKeyDown()) {
					return false;
				}

				return true;
			}

			@Override
			public boolean willBeDropped() {

				return false;
			}

		});

	}

	public void onBrowserEvent(Event event) {

		super.onBrowserEvent(event);
	}
}
