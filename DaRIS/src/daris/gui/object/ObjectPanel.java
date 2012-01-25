package daris.gui.object;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import arc.gui.form.FormEditMode;
import arc.gui.gwt.style.FontWeight;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.image.Image;
import arc.gui.gwt.widget.image.LinearGradient;
import arc.gui.gwt.widget.label.Label;
import arc.gui.gwt.widget.menu.ActionMenu;
import arc.gui.gwt.widget.panel.AbsolutePanel;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.menu.ActionEntry;
import arc.gui.menu.Menu;
import arc.mf.client.util.Action;

import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Widget;

import daris.client.Resource;
import daris.client.ui.widget.LoadingMessage;
import daris.model.Model;
import daris.model.Model.Event;
import daris.model.object.PSSDObjectRef;

public class ObjectPanel extends VerticalPanel implements
		daris.model.Model.EventHandler {

	public static final int HISTORY_SIZE = 10;

	public static final ImageResource LOAD_IMG = Resource.INSTANCE.load16();

	public static final ImageResource LOADING_IMG = Resource.INSTANCE
			.loading16();

	public static final ImageResource LOADED_IMG = Resource.INSTANCE.loaded16();

	public static final ImageResource BIG_LOADING_IMG = Resource.INSTANCE
			.loading100();

	private static class History {

		public static interface HistoryListener {

			void jumpTo(PSSDObjectRef o, Widget w);
		}

		public static final ImageResource PREV_IMG = Resource.INSTANCE
				.arrowLeftBlue16();
		public static final ImageResource NEXT_IMG = Resource.INSTANCE
				.arrowRightBlue16();
		public static final ImageResource PICK_IMG = Resource.INSTANCE
				.arrowDownBlue16();
		public static final ImageResource TICK_IMG = Resource.INSTANCE
				.tickBlue16();

		public static final int ICON_WIDTH = 16;

		public static final int ICON_HEIGHT = 16;

		private final int _size;
		private int _index;
		private List<PSSDObjectRef> _objects;
		private List<Widget> _widgets;
		private List<HistoryListener> _listeners;

		private HorizontalPanel _hp;
		private Image _prevImg;
		private Image _nextImg;
		private Image _pickImg;

		private History(int size) {

			_size = size;
			_index = 0;
			_widgets = new ArrayList<Widget>(_size);
			_objects = new ArrayList<PSSDObjectRef>(_size);
			_listeners = new Vector<HistoryListener>();

			_hp = new HorizontalPanel();
			_hp.setHeight100();
			_hp.setPaddingTop(2);

			_prevImg = createImage(PREV_IMG);
			_prevImg.setMarginRight(5);
			_prevImg.setToolTip("Go back to previously viewed object.");
			_prevImg.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {

					if (_index > 0) {
						_index--;
						if (_index == 0) {
							_prevImg.disable();
						}
						if (_index < _objects.size() - 1) {
							_nextImg.enable();
						}
						notifyListeners();
					}
				}
			});
			_hp.add(_prevImg);

			_pickImg = createImage(PICK_IMG);
			_pickImg.setMarginRight(5);
			_pickImg.setToolTip("Select a previously viewed object.");
			_pickImg.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {

					showPicker();
				}
			});
			_hp.add(_pickImg);

			_nextImg = createImage(NEXT_IMG);
			_nextImg.setToolTip("Go to next previously viewed object.");
			_nextImg.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {

					if (_index < _objects.size() - 1) {
						_index++;
						if (_index > 0) {
							_prevImg.enable();
						}
						if (_index == _objects.size() - 1) {
							_nextImg.disable();
						}
						notifyListeners();
					}
				}
			});
			_hp.add(_nextImg);

		}

		private Image createImage(ImageResource ir) {

			final Image i = new Image(ir.getURL(), ICON_WIDTH, ICON_HEIGHT);
			i.setCursor(Cursor.POINTER);
			return i;
		}

		private void notifyListeners() {

			for (HistoryListener l : _listeners) {
				l.jumpTo(_objects.get(_index), _widgets.get(_index));
			}
		}

		private BaseWidget widget() {

			return _hp;
		}

		private void addListener(HistoryListener l) {

			_listeners.add(l);
		}

		private void add(PSSDObjectRef o, Widget w) {

			int i = _objects.indexOf(o);
			if (i != -1) {
				_objects.remove(i);
				_widgets.remove(i);
			}
			if (_objects.size() == _size) {
				_objects.remove(0);
				_widgets.remove(0);
			}
			_objects.add(o);
			_widgets.add(w);
			_index = _objects.size() - 1;
			_pickImg.enable();
			if (_index > 0) {
				_prevImg.enable();
			}
			_nextImg.disable();
		}

		private void showPicker() {

			if (_objects.isEmpty()) {
				return;
			}
			Menu menu = new Menu();
			for (int i = 0; i < _objects.size(); i++) {
				PSSDObjectRef o = _objects.get(i);
				final int j = i;
				String label = o.referentTypeName() + " - " + o.id();
				Action action = new Action() {
					@Override
					public void execute() {

						if (_index != j) {
							_index = j;
							if (_index > 0) {
								_prevImg.enable();
							}
							if (_index < _objects.size() - 1) {
								_nextImg.enable();
							}
							notifyListeners();
						}
					}
				};
				if (j == _index) {
					menu.add(new ActionEntry(new arc.gui.image.Image(TICK_IMG
							.getURL(), 16, 16), label, action));
				} else {
					menu.add(new ActionEntry(label, action));
				}
			}
			ActionMenu actionMenu = new ActionMenu(menu);
			actionMenu.showAt(_pickImg.absoluteLeft() - 120,
					_pickImg.absoluteBottom());
		}
	}

	private static final String BORDER_COLOR = "#979797";
	private static final String BORDER_COLOR_LIGHT = "#cdcdcd";
	public static final int BORDER_RADIUS = 5;

	private Image _icon;
	private Label _label;
	private SimplePanel _sp;
	private PSSDObjectRef _o;
	private History _history;

	public ObjectPanel(PSSDObjectRef o) {

		fitToParent();
		AbsolutePanel ap = new AbsolutePanel();
		ap.setWidth100();
		ap.setHeight(20);
		ap.setMarginTop(1);
		ap.setBorderTop(1, BorderStyle.SOLID, BORDER_COLOR);
		ap.setBorderLeft(1, BorderStyle.SOLID, BORDER_COLOR);
		ap.setBorderRight(1, BorderStyle.SOLID, BORDER_COLOR);
		ap.setBorderRadiusTopLeft(BORDER_RADIUS);
		ap.setBorderRadiusTopRight(BORDER_RADIUS);
		ap.setBackgroundImage(new LinearGradient(
				LinearGradient.Orientation.TOP_TO_BOTTOM, BORDER_COLOR_LIGHT,
				BORDER_COLOR));

		_icon = new Image(LOAD_IMG.getURL(), 15, 15);
		_icon.setLeft(0);
		_icon.setTop(0);
		_icon.setMarginTop(1);
		_icon.setMarginLeft(3);
		_icon.setMarginRight(3);
		ap.add(_icon);

		_label = new Label();
		_label.setPaddingTop(2);
		_label.setHeight100();
		_label.setFontSize(12);
		_label.setFontWeight(FontWeight.BOLD);
		_label.setPosition(Position.ABSOLUTE);
		_label.setLeft(25);
		_label.setTop(0);
		ap.add(_label);

		_history = new History(HISTORY_SIZE);
		_history.addListener(new History.HistoryListener() {

			@Override
			public void jumpTo(PSSDObjectRef o, Widget w) {

				if (o != null && w != null) {
					_o = o;
					String labelText = _o.referentTypeName() + " - " + _o.id();
					_label.setText(labelText);
					_sp.setContent(w);
				}
			}
		});
		BaseWidget hw = _history.widget();
		hw.setPosition(Position.ABSOLUTE);
		hw.setRight(0);
		hw.setTop(0);
		hw.setMarginRight(10);
		ap.add(hw);

		add(ap);
		_sp = new SimplePanel();
		_sp.fitToParent();
		_sp.setBorderLeft(1, BorderStyle.SOLID, BORDER_COLOR);
		_sp.setBorderRight(1, BorderStyle.SOLID, BORDER_COLOR);
		_sp.setBorderBottom(1, BorderStyle.SOLID, BORDER_COLOR);
		add(_sp);
		setObject(o, false);
		/*
		 * start listening to the model events
		 */
		Model.subscribe(this);

	}

	public ObjectPanel() {

		this(null);
	}

	public void setObject(PSSDObjectRef o, boolean reload) {

		if (_o != null) {
			if (_o.equals(o)) {
				if (!reload) {
					return;
				}
			}
		}
		if (_o != null) {
			_o.cancel();
		}
		_o = o;
		if (_o != null) {
			String labelText = _o.referentTypeName() + " - " + o.id();
			_label.setText(labelText);
			Widget w = ObjectDetail.detailFor(_o, FormEditMode.READ_ONLY);
			_history.add(_o, w);
			_sp.setContent(w);
			_icon.setURL(LOADED_IMG.getURL());
		} else {
			_icon.setURL(LOAD_IMG.getURL());
			_label.setText(null);
			_sp.clear();
		}
	}

	private void setLoadingMessage(PSSDObjectRef o) {

		_icon.setURL(LOADING_IMG.getURL());
		String label = o.referentTypeName() + " - " + o.id();
		_label.setText(label);
		LoadingMessage lp = new LoadingMessage(BIG_LOADING_IMG, "Loading "
				+ label + "...");
		lp.fitToParent();
		_sp.setContent(lp);
	}

	public void reloadObject() {

		setObject(_o, true);
	}

	public void discard() {

		/*
		 * Stop listening the model events
		 */
		Model.unsubscribe(this);
	}

	@Override
	public void handleEvent(Event e) {

		switch (e.type()) {
		case Model.Event.OBJECT_LOADING:
			setLoadingMessage(e.object());
			break;
		case Model.Event.OBJECT_SELECTED:
			setObject(e.object(), true);
			break;
		case Model.Event.OBJECT_UPDATED:
			if (_o != null) {
				if (_o.equals(e.object())) {
					setObject(e.object(), true);
				}
			}
			break;
		case Model.Event.OBJECT_DELETED:
			if (_o != null) {
				if (_o.equals(e.object())) {
					setObject(null, true);
				}
			}
			break;
		}
	}

}
