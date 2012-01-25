package daris.client.ui.widget;

import java.util.HashMap;
import java.util.Map;

import arc.gui.gwt.style.FontWeight;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.TextAlignment;
import arc.gui.gwt.widget.image.LinearGradient;
import arc.gui.gwt.widget.panel.AbsolutePanel;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;

import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

public class SimpleTabPanel extends VerticalPanel {

	public interface TabListener {

		void selected(String tabName);

		void deselected(String tabName);
	}

	public static final int TAB_BAR_HEIGHT = 22;
	public static final String TAB_COLOR_LIGHT = "#cdcdcd";
	public static final String TAB_COLOR = "#979797";

	private static class TabButton extends SimplePanel {

		public static final int BORDER_RADIUS = 5;
		public static final int FONT_SIZE = 12;
		public static final String FONT_FAMILY = "Helvetica";

		private HTML _html;
		private String _tabName;
		private boolean _selected;

		public TabButton(String tabName) {

			this(tabName, false);
		}

		public TabButton(String tabName, boolean selected) {

			setHeight100();
			setWidth(tabName.length() * FONT_SIZE + 10);
			setBackgroundImage(new LinearGradient(LinearGradient.Orientation.TOP_TO_BOTTOM, TAB_COLOR_LIGHT, TAB_COLOR));
			setOpacity(0.7);
			setBorderRadiusTopLeft(BORDER_RADIUS);
			setBorderRadiusTopRight(BORDER_RADIUS);
			setMarginRight(3);
			setMarginTop(3);

			_tabName = tabName;
			_html = new HTML(tabName);
			_html.setFontSize(FONT_SIZE);
			_html.setFontFamily(FONT_FAMILY);
			_html.setFontWeight(FontWeight.NORMAL);
			_html.setTextAlignment(TextAlignment.CENTER);
			_html.setPaddingTop(3);
			_html.fitToParent();
			setContent(_html);
			_selected = selected;
		}

		public boolean isSelected() {

			return _selected;
		}

		public String tabName() {

			return _tabName;
		}

		public void setSelected(boolean selected) {

			setOpacity(selected ? 1.0 : 0.7);
			_html.setFontWeight(selected ? FontWeight.BOLD : FontWeight.NORMAL);
			_selected = selected;
		}
	}

	private HorizontalPanel _hp;
	private SimplePanel _sp;
	private Map<String, TabButton> _buttons;
	private Map<String, Widget> _widgets;
	private Map<String, TabListener> _listeners;
	private String _selectedTabName;

	public SimpleTabPanel() {

		_hp = new HorizontalPanel();
		_hp.setHeight100();
		_hp.setPosition(Position.ABSOLUTE);
		_hp.setLeft(0);

		AbsolutePanel ap = new AbsolutePanel();
		ap.setOverflow(Overflow.HIDDEN);
		ap.setPreferredHeight(TAB_BAR_HEIGHT);
		ap.add(_hp);
		add(ap);

		_sp = new SimplePanel();
		_sp.fitToParent();
		_sp.setBorderTop(2, BorderStyle.SOLID, TAB_COLOR);
		_sp.setBorderLeft(1, BorderStyle.SOLID, TAB_COLOR);
		_sp.setBorderRight(1, BorderStyle.SOLID, TAB_COLOR);
		_sp.setBorderBottom(1, BorderStyle.SOLID, TAB_COLOR);
		add(_sp);
		_buttons = new HashMap<String, TabButton>();
		_widgets = new HashMap<String, Widget>();
		_listeners = new HashMap<String, TabListener>();
	}

	public void putTab(String tabName, Widget tabContent) {

		putTab(tabName, tabContent, true);
	}

	public void putTab(final String tabName, Widget tabContent, boolean autoSelect) {

		TabButton button = _buttons.get(tabName);
		if (button != null) {
			_widgets.put(tabName, tabContent);
			if (tabName.equals(_selectedTabName)) {
				_sp.setContent(tabContent);
			} else {
				if (autoSelect) {
					selectTab(tabName);
				}
			}
		} else {
			button = new TabButton(tabName);
			button.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {

					TabButton b = (TabButton) event.getSource();
					if (!b.isSelected()) {
						selectTab(b.tabName());
					}
				}
			});
			_hp.add(button);
			_buttons.put(tabName, button);
			_widgets.put(tabName, tabContent);
			if (autoSelect) {
				selectTab(tabName);
			}
		}
	}

	public void removeTab(String tabName) {

		removeTab(tabName, true);
	}

	public void removeTab(String tabName, boolean autoSelect) {

		TabButton button = _buttons.get(tabName);
		if (button != null) {
			_hp.remove(button);
			_buttons.remove(tabName);
			_widgets.remove(tabName);
			if (tabName.equals(_selectedTabName)) {
				_sp.clear();
				_selectedTabName = null;
			}
			if (autoSelect) {
				if (_buttons.size() > 0) {
					selectTab(_buttons.keySet().iterator().next());
				}
			}
		}
	}

	public void setTabListener(String tabName, TabListener tl) {

		_listeners.put(tabName, tl);
	}

	public void selectTab(String tabName) {

		if (tabName.equals(_selectedTabName)) {
			return;
		}
		TabButton button = _buttons.get(tabName);
		Widget widget = _widgets.get(tabName);
		if (button == null) {
			return;
		}
		for (TabButton btn : _buttons.values()) {
			if (btn == button) {
				btn.setSelected(true);
			} else {
				btn.setSelected(false);
			}
		}
		_sp.setContent(widget);
		notifyOfDeselect(_selectedTabName);
		_selectedTabName = tabName;
		notifyOfSelect(_selectedTabName);

	}

	private void notifyOfDeselect(String tabName) {

		TabListener l = _listeners.get(tabName);
		if (l != null) {
			l.deselected(tabName);
		}
	}

	private void notifyOfSelect(String tabName) {

		TabListener l = _listeners.get(tabName);
		if (l != null) {
			l.selected(tabName);
		}
	}

	public String getSelectedTabName() {

		return _selectedTabName;
	}

	public Widget getSelectedTabContent() {

		if (_selectedTabName == null) {
			return null;
		}
		return _widgets.get(_selectedTabName);
	}

	public Widget getTabContent(String tabName) {

		return _widgets.get(tabName);
	}

}
