package daris.client.ui.widget;

import java.util.List;
import java.util.Vector;

import arc.gui.gwt.style.FontWeight;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.image.Image;
import arc.gui.gwt.widget.image.LinearGradient;
import arc.gui.gwt.widget.label.IconAndLabel;
import arc.gui.gwt.widget.panel.AbsolutePanel;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.popup.PopupCloseHandler;
import arc.gui.gwt.widget.popup.PopupPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.table.Table;
import arc.gui.gwt.widget.table.Table.Row;
import arc.gui.gwt.widget.table.Table.RowClickHandler;
import arc.gui.gwt.widget.table.Table.RowOverHandler;
import arc.gui.image.StandardImages;
import arc.mf.client.util.ActionListener;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.HasBeforeSelectionHandlers;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.Widget;

import daris.client.Resource;

public class CTabPanel extends VerticalPanel implements
		HasBeforeSelectionHandlers<Integer>, HasSelectionHandlers<Integer> {

	public static interface TabListener {
		void tabAdded(CTabPanel tp, Tab tab);

		void tabRemoved(CTabPanel tp, Tab tab);
	}

	public static final int MIN_TAB_WIDTH = 100;

	public static final int BORDER_RADIUS = 4;

	public static final String TAB_COLOUR_DARK = "#bbb";

	public static final String TAB_COLOUR = "#d9d9d9";

	public static final String TAB_COLOUR_SELECTED = "#979797";

	public static final String TAB_LIST_COLOUR = "#E9E9E9";

	public static final String TAB_LIST_BORDER_COLOUR = "#888";

	public static final String IMG_TICK = Resource.INSTANCE.tickBlue16()
			.getSafeUri().asString();

	public static final String IMG_CLOSE = Resource.INSTANCE.tabClose16()
			.getSafeUri().asString();

	public static final String IMG_CLOSE_HOVER = Resource.INSTANCE
			.tabCloseHover16().getSafeUri().asString();

	public static final String IMG_CLOSE_DISABLED = Resource.INSTANCE
			.tabCloseDisabled16().getSafeUri().asString();

	public static final String IMG_LEFT = Resource.INSTANCE.left16()
			.getSafeUri().asString();

	public static final String IMG_LEFT_HOVER = Resource.INSTANCE.leftHover16()
			.getSafeUri().asString();

	public static final String IMG_RIGHT = Resource.INSTANCE.right16()
			.getSafeUri().asString();

	public static final String IMG_RIGHT_HOVER = Resource.INSTANCE
			.rightHover16().getSafeUri().asString();

	public static final String IMG_DOWN = Resource.INSTANCE.down16()
			.getSafeUri().asString();

	public static final String IMG_DOWN_HOVER = Resource.INSTANCE.downHover16()
			.getSafeUri().asString();

	public static final String IMG_CLOSE_ALL = Resource.INSTANCE.close16()
			.getSafeUri().asString();

	public static final String IMG_CLOSE_ALL_HOVER = Resource.INSTANCE
			.closeHover16().getSafeUri().asString();

	public static final int DEFAULT_FONT_SIZE = 12;

	public static final int DEFAULT_TAB_BAR_HEIGHT = 20;

	public static class Tab extends HorizontalPanel {

		private Widget _content;

		private Object _object;

		private boolean _closable;

		private boolean _enabled;

		private String _title;

		private HTML _titleHTML;

		private int _minWidth;

		private Image _tabCloseImage;

		private CTabPanel _tabPanel;

		public Tab(String title, Widget content) {

			this(null, title, null, content, true, true);
		}

		public Tab(String title, Widget content, boolean closable) {

			this(null, title, null, content, closable, true);
		}

		public Tab(String title, Object object, Widget content) {

			this(null, title, object, content, true, true);
		}

		public Tab(String title, Object object, Widget content, boolean closable) {

			this(null, title, object, content, closable, true);
		}

		public Tab(String title, Object object, Widget content,
				boolean closable, boolean enabled) {

			this(null, title, object, content, closable, enabled);
		}

		protected Tab(CTabPanel tabPanel, String title, Object object,
				Widget content, boolean closable, boolean enabled) {

			_tabPanel = tabPanel;
			_object = object;
			_content = content;
			_closable = closable;
			_enabled = enabled;

			// Title
			_title = title;
			_titleHTML = new HTML();
			_titleHTML.fitToParent();
			_titleHTML.style().setFloat(Style.Float.LEFT);
			_titleHTML.style().setProperty("textAlign", "center");
			_titleHTML.setPaddingTop(5);
			_titleHTML.setPaddingLeft(10);
			_titleHTML.setPaddingRight(5);
			_titleHTML.setFontSize(DEFAULT_FONT_SIZE);
			_titleHTML.setFontWeight(FontWeight.LIGHTER);
			_titleHTML.setCursor(Cursor.POINTER);
			_titleHTML.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {

					_tabPanel.selectTab(Tab.this);
				}
			});
			_titleHTML.setHTML(_title);
			if (_title == null) {
				_minWidth = MIN_TAB_WIDTH;
			} else {
				_minWidth = _title.length() * (DEFAULT_FONT_SIZE - 2) + 5;
				if (_minWidth < MIN_TAB_WIDTH) {
					_minWidth = MIN_TAB_WIDTH;
				}
			}
			add(_titleHTML);

			// Close Icon
			_tabCloseImage = new Image(IMG_CLOSE);
			_tabCloseImage.setHoverImage(IMG_CLOSE_HOVER);
			_tabCloseImage.setDisabledImage(IMG_CLOSE_DISABLED);
			_tabCloseImage.setBorder(1, "transparent");
			_tabCloseImage.setWidth(12);
			_tabCloseImage.setHeight(12);
			_tabCloseImage.style().setFloat(Style.Float.RIGHT);
			_tabCloseImage.style().setProperty("clear", "right");
			_tabCloseImage.style().setCursor(Style.Cursor.POINTER);
			_tabCloseImage.setMarginTop(3);
			_tabCloseImage.setMarginRight(3);
			if (!_enabled) {
				_tabCloseImage.disable();
			}
			_tabCloseImage.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {

					_tabPanel.removeTab(Tab.this);
				}
			});
			if (_closable) {
				_minWidth += 10;
				add(_tabCloseImage);
			}
			// Style Properties
			setBackgroundImage(new LinearGradient(
					LinearGradient.Orientation.TOP_TO_BOTTOM, TAB_COLOUR,
					TAB_COLOUR_DARK));
			setBorderRadiusTopLeft(BORDER_RADIUS);
			setBorderRadiusTopRight(BORDER_RADIUS);
			style().setFloat(Style.Float.LEFT);
			style().setVerticalAlign(VerticalAlign.MIDDLE);
			style().setProperty("minWidth", "" + _minWidth + "px");
			setWidth(_minWidth);
			setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
			setMarginLeft(1);
			setMarginRight(3);
			setMarginTop(1);
			setHeight100();
		}

		protected void setTabPanel(CTabPanel tabPanel) {

			_tabPanel = tabPanel;
		}

		protected void onSelect() {

			setBackgroundImage(new LinearGradient(
					LinearGradient.Orientation.TOP_TO_BOTTOM, TAB_COLOUR_DARK,
					TAB_COLOUR_SELECTED));
			_titleHTML.setFontWeight(FontWeight.NORMAL);
		}

		protected void onDeselect() {

			setBackgroundImage(new LinearGradient(
					LinearGradient.Orientation.TOP_TO_BOTTOM, TAB_COLOUR,
					TAB_COLOUR_DARK));
			_titleHTML.setFontWeight(FontWeight.LIGHTER);
		}

		public String title() {

			return _title;
		}

		public Object object() {

			return _object;
		}

		public void setObject(Object object) {

			_object = object;
		}

		public Widget content() {

			return _content;
		}

		public void setContent(Widget content) {

			_content = content;
			if (selected()) {
				_tabPanel.contentPanel().setContent(content);
			}
		}

		public boolean selected() {

			if (_tabPanel == null) {
				return false;
			}
			return _tabPanel.selectedIndex() == index();
		}

		public void select() {

			_tabPanel.selectTab(index());
		}

		public int index() {

			if (_tabPanel == null) {
				return -1;
			}
			return _tabPanel.indexOf(this);
		}
	}

	private static class TabBar extends AbsolutePanel {
		public static final int Z_INDEX_TABS = 998;

		public static final int Z_INDEX_CTRLS = 999;

		private CTabPanel _tabPanel;

		private HorizontalPanel _tabsArea;

		private HorizontalPanel _controlsAreaRight;

		private HorizontalPanel _controlsAreaLeft;

		// private Image _roamLeftImage;

		// private SimplePanel _roamRightBar;
		//
		// private Image _roamRightImage;
		//
		private Image _pickerImage;
		//
		// private Image _closeAllImage;

		private PopupPanel _tabPicker;

		public TabBar(CTabPanel tabPanel) {

			_tabPanel = tabPanel;

			_tabsArea = new HorizontalPanel();
			_tabsArea.setHeight100();
			_tabsArea.setPosition(Position.ABSOLUTE);
			_tabsArea.setLeft(0);
			_tabsArea.setZIndex(Z_INDEX_TABS);
			add(_tabsArea);

			initControlsAreaLeft();

			initControlsAreaRight();

			setOverflow(Overflow.HIDDEN);
			setBorderBottom(3, Style.BorderStyle.SOLID, TAB_COLOUR_SELECTED);
			setWidth100();
			setPreferredHeight(DEFAULT_TAB_BAR_HEIGHT);
		}

		private void initControlsAreaLeft() {

			_controlsAreaLeft = new HorizontalPanel();
			_controlsAreaLeft.setZIndex(Z_INDEX_CTRLS);
			_controlsAreaLeft.setPosition(Position.ABSOLUTE);
			_controlsAreaLeft.setWidth(22);
			_controlsAreaLeft.setLeft(0);
			_controlsAreaLeft.setHeight100();
			_controlsAreaLeft.setMarginTop(1);
			_controlsAreaLeft.setMarginRight(2);
			_controlsAreaLeft.setBackgroundImage(new LinearGradient(
					LinearGradient.Orientation.TOP_TO_BOTTOM, TAB_COLOUR_DARK,
					TAB_COLOUR_SELECTED));
			_controlsAreaLeft.setBorderRadiusTopRight(BORDER_RADIUS);

			final SimplePanel roamLeftBar = new SimplePanel();
			roamLeftBar.setWidth(20);
			roamLeftBar.setHeight100();
			roamLeftBar.setBorderRadius(3);
			roamLeftBar.setToolTip("Scroll left");
			roamLeftBar.style().setProperty("textAlign", "center");
			roamLeftBar.style().setVerticalAlign(VerticalAlign.MIDDLE);
			roamLeftBar.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {

					moveLeft();
				}
			});
			roamLeftBar.addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					roamLeftBar.setBorder(1, BorderStyle.SOLID, "#999999");
				}
			});
			roamLeftBar.addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					roamLeftBar.setBorder(1, "transparent");
				}
			});

			Image roamLeftImage = new Image(IMG_LEFT);
			roamLeftImage.setHoverImage(IMG_LEFT_HOVER);
			roamLeftImage.setCursor(Cursor.POINTER);
			roamLeftBar.add(roamLeftImage);

			_controlsAreaLeft.add(roamLeftBar);

		}

		private void initControlsAreaRight() {

			_controlsAreaRight = new HorizontalPanel();
			_controlsAreaRight.setZIndex(Z_INDEX_CTRLS);
			_controlsAreaRight.setPosition(Position.ABSOLUTE);
			_controlsAreaRight.setRight(0);
			_controlsAreaRight.setWidth(75);
			_controlsAreaRight.setHeight100();
			_controlsAreaRight.setMarginTop(1);
			_controlsAreaRight.setMarginLeft(2);
			_controlsAreaRight.setPaddingLeft(2);
			_controlsAreaRight.setBackgroundImage(new LinearGradient(
					LinearGradient.Orientation.TOP_TO_BOTTOM, TAB_COLOUR_DARK,
					TAB_COLOUR_SELECTED));
			_controlsAreaRight.setBorderLeft(1, BorderStyle.SOLID, "#f0f0f0");
			_controlsAreaRight.setBorderRadiusTopLeft(BORDER_RADIUS);

			/*
			 * roam right
			 */
			final SimplePanel roamRightBar = new SimplePanel();
			roamRightBar.setToolTip("Scroll right");
			roamRightBar.setWidth(20);
			roamRightBar.setHeight100();
			roamRightBar.setMarginRight(1);
			roamRightBar.setBorderRadius(3);
			roamRightBar.style().setProperty("textAlign", "center");
			roamRightBar.style().setVerticalAlign(VerticalAlign.MIDDLE);
			roamRightBar.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {

					moveRight();
				}
			});
			roamRightBar.addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					roamRightBar.setBorder(1, BorderStyle.SOLID, "#999999");
				}
			});
			roamRightBar.addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					roamRightBar.setBorder(1, "transparent");
				}
			});

			Image roamRightImage = new Image(IMG_RIGHT);
			roamRightImage.setHoverImage(IMG_RIGHT_HOVER);
			roamRightImage.setCursor(Cursor.POINTER);
			roamRightBar.add(roamRightImage);

			_controlsAreaRight.add(roamRightBar);

			/*
			 * picker
			 */
			final SimplePanel pickerBar = new SimplePanel();
			pickerBar.setToolTip("List all the tabs");
			pickerBar.setWidth(20);
			pickerBar.setHeight100();
			pickerBar.setMarginRight(1);
			pickerBar.setBorderRadius(3);
			pickerBar.style().setProperty("textAlign", "center");
			pickerBar.style().setVerticalAlign(VerticalAlign.MIDDLE);
			pickerBar.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {

					TabBar.this.showTabPicker();
				}
			});
			pickerBar.addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					pickerBar.setBorder(1, BorderStyle.SOLID, "#999999");
				}
			});
			pickerBar.addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					pickerBar.setBorder(1, "transparent");
				}
			});
			_pickerImage = new Image(IMG_DOWN);
			_pickerImage.setHoverImage(IMG_DOWN_HOVER);
			pickerBar.add(_pickerImage);

			_controlsAreaRight.add(pickerBar);

			/*
			 * close all
			 */

			final SimplePanel closeAllBar = new SimplePanel();
			closeAllBar.setToolTip("Close All Tabs");
			closeAllBar.setWidth(20);
			closeAllBar.setHeight100();
			closeAllBar.setMarginRight(3);
			closeAllBar.setBorderRadius(3);
			closeAllBar.style().setProperty("textAlign", "center");
			closeAllBar.style().setVerticalAlign(VerticalAlign.MIDDLE);
			closeAllBar.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {

					Dialog.confirm(
							"Confirm Close",
							"You are about to close all the tabs. Are you sure you want to continue?",
							new ActionListener() {

								@Override
								public void executed(boolean succeeded) {

									if (succeeded) {
										TabBar.this.tabPanel().removeAllTabs();
									}
								}
							});
				}
			});
			closeAllBar.addMouseOverHandler(new MouseOverHandler() {
				@Override
				public void onMouseOver(MouseOverEvent event) {
					closeAllBar.setBorder(1, BorderStyle.SOLID, "#999999");
				}
			});
			closeAllBar.addMouseOutHandler(new MouseOutHandler() {
				@Override
				public void onMouseOut(MouseOutEvent event) {
					closeAllBar.setBorder(1, "transparent");
				}
			});
			Image closeAllImage = new Image(IMG_CLOSE_ALL);
			closeAllImage.setHoverImage(IMG_CLOSE_ALL_HOVER);
			closeAllBar.add(closeAllImage);

			_controlsAreaRight.add(closeAllBar);

		}

		protected void showTabPicker() {

			_tabPicker = new PopupPanel();
			_tabPicker.setPartner(_pickerImage);
			_tabPicker.setAutoHideEnabled(true);
			final Table table = new Table();
			table.setCellPadding(3);
			table.setCellSpacing(2);
			for (Tab tab : _tabPanel.tabs()) {
				IconAndLabel label;
				if (tab.selected()) {
					label = new IconAndLabel(new arc.gui.image.Image(IMG_TICK,
							12, 12), tab.title());
					label.setFontWeight(FontWeight.BOLD);
				} else {
					label = new IconAndLabel(new arc.gui.image.Image(
							StandardImages.BLANK.path(), 12, 12), tab.title());
					label.setFontWeight(FontWeight.NORMAL);
				}
				label.setFontSize(DEFAULT_FONT_SIZE);
				Table.Row row = table.addRow();
				row.setData(tab);
				row.addCell(label);
			}
			table.addRowOverHandler(new RowOverHandler() {
				@SuppressWarnings("rawtypes")
				@Override
				public void over(Row r, MouseEvent event) {

					if (table != null) {
						for (int i = 0; i < table.rowCount(); i++) {
							Table.Row row = table.row(i);
							if (row != null) {
								row.setBackgroundColour(TAB_LIST_COLOUR);
								// row.style().clearBackgroundColor();
							}
						}
					}
					if (r != null) {
						r.setBackgroundColour(TAB_COLOUR_SELECTED);
					}
				}
			});
			table.addRowClickHandler(new RowClickHandler() {
				@Override
				public void clicked(Row r, ClickEvent event) {

					if (r != null) {
						Tab tab = (Tab) r.data();
						tab.select();
						if (_tabPicker != null) {
							_tabPicker.hide();
						}
					}
				}
			});
			SimplePanel cp = new SimplePanel();
			cp.fitToParent();
			cp.setBorderRadius(BORDER_RADIUS);
			cp.setBorder(1, TAB_LIST_BORDER_COLOUR);
			cp.setBackgroundColour(TAB_LIST_COLOUR);
			cp.setContent(new ScrollPanel(table, ScrollPolicy.AUTO));
			cp.setPadding(5);
			int _popupWidth = computeTabPickerWidth();
			int _popupHeight = computeTabPickerHeight();
			table.setHeight(_popupHeight - 20);
			table.setWidth(_popupWidth - 20);
			_tabPicker.setWidth(_popupWidth);
			_tabPicker.setHeight(_popupHeight);
			_tabPicker.setContent(cp);
			_tabPicker.addCloseHander(new PopupCloseHandler() {
				public void closed(PopupPanel p) {

					_tabPicker = null;
				}
			});
			_tabPicker.setPopupPositionAndShow(new PositionCallback() {
				@Override
				public void setPosition(int offsetWidth, int offsetHeight) {

					_tabPicker.setPopupPosition(computeTabPickerLeft(),
							computeTabPickerTop());
					_tabPicker.show();
				}
			});
			_tabPicker.setVisible(true);
		}

		private int computeTabPickerLeft() {

			int w = computeTabPickerWidth();
			int r = _pickerImage.absoluteLeft() + _pickerImage.getOffsetWidth();
			return r - w;
		}

		private int computeTabPickerTop() {

			return _pickerImage.absoluteTop() + _pickerImage.getOffsetHeight();
		}

		private int computeTabPickerWidth() {

			int minWidth = 100;
			for (Tab tab : _tabPanel.tabs()) {
				if (tab.width() > minWidth) {
					minWidth = tab.width();
				}
			}
			return minWidth + ScrollPanel.SCROLLBAR_WIDTH;
		}

		private int computeTabPickerHeight() {

			int top = computeTabPickerTop();
			int ch = Window.getClientHeight();
			int h = _tabPanel.tabs().size() * 25;
			if (top + h > ch) {
				h = ch - top;
			}
			return h + ScrollPanel.SCROLLBAR_WIDTH + 30;
		}

		protected void doLayoutChildren() {

			super.doLayoutChildren();
			int w1 = _tabsArea.widthWithMarginsAndPadding();
			int w = width();
			if (w1 > w) {
				if (_controlsAreaLeft.isAttached()
						&& _controlsAreaRight.isAttached()) {
					return;
				}
				if (!_controlsAreaLeft.isAttached()) {
					add(_controlsAreaLeft);
					_controlsAreaLeft.setLeft(0);
					_tabsArea.setLeft(_controlsAreaLeft
							.widthWithMarginsAndPadding());
				}
				if (!_controlsAreaRight.isAttached()) {
					add(_controlsAreaRight);
					_controlsAreaRight.setRight(0);
				}

				super.doLayoutChildren();
			} else {
				if (!_controlsAreaLeft.isAttached()
						&& !_controlsAreaRight.isAttached()) {
					return;
				}
				if (children().contains(_controlsAreaLeft)) {
					remove(_controlsAreaLeft);
				}
				if (children().contains(_controlsAreaRight)) {
					remove(_controlsAreaRight);
				}
				_tabsArea.setLeft(0);
				super.doLayoutChildren();
			}
		}

		private int indexOfFirstVisibleTab() {

			int index = -1;
			int left = children().contains(_controlsAreaLeft) ? _controlsAreaLeft
					.widthWithMarginsAndPadding() : 0;
			int al = _tabsArea.absoluteLeft();
			int l = _tabsArea.left();
			for (int i = 0; i < _tabPanel.numTabs(); i++) {
				Tab tab = _tabPanel.tabs().get(i);
				int tl = tab.absoluteLeft() - al;
				if (tl + l >= left) {
					index = i;
					break;
				}
			}
			assert (index != -1);
			return index;
		}

		private void moveLeft() {

			if (_tabsArea.absoluteRight() > _controlsAreaRight.absoluteLeft()) {
				int indexOfFirstVisibleTab = indexOfFirstVisibleTab();
				if (indexOfFirstVisibleTab < _tabPanel.tabs().size() - 1) {
					int widthOfFirstVisibleTab = _tabPanel.tabs()
							.get(indexOfFirstVisibleTab)
							.widthWithMarginsAndPadding();
					_tabsArea
							.setLeft(_tabsArea.left() - widthOfFirstVisibleTab);
				}
			}
		}

		private void moveRight() {

			int indexOfFirstVisibleTab = indexOfFirstVisibleTab();
			if (indexOfFirstVisibleTab > 0) {
				int indexOfLastInvisibleTab = indexOfFirstVisibleTab - 1;
				int widthOfLastInvisibleTab = _tabPanel.tabs()
						.get(indexOfLastInvisibleTab)
						.widthWithMarginsAndPadding();
				_tabsArea.setLeft(_tabsArea.left() + widthOfLastInvisibleTab);
			}
		}

		private void moveToVisibleArea(Tab tab) {

			while (isTabHidden(tab)) {
				if (isTabHiddenLeft(tab)) {
					moveRight();
				} else {
					moveLeft();
				}
			}
		}

		protected CTabPanel tabPanel() {

			return _tabPanel;
		}

		private boolean isTabHidden(Tab tab) {

			return isTabHiddenLeft(tab) || isTabHiddenRight(tab);
		}

		private boolean isTabHiddenLeft(Tab tab) {

			assert tab != null;
			int al = children().contains(_controlsAreaLeft) ? _controlsAreaLeft
					.absoluteRight() : absoluteLeft();
			if (tab.absoluteLeft() < al) {
				return true;
			}
			return false;
		}

		private boolean isTabHiddenRight(Tab tab) {

			assert tab != null;
			if (_controlsAreaRight.isAttached()) {
				if (tab.absoluteRight() > _controlsAreaRight.absoluteLeft()) {
					return true;
				}
			}
			return false;
		}

		public void addTab(Tab tab) {

			_tabsArea.add(tab);
			doLayoutChildren();
		}

		public void removeTab(int index) {

			removeTab(_tabPanel.tabAt(index));
		}

		public void removeTab(Tab tab) {

			_tabsArea.remove(tab);
			doLayoutChildren();
		}

		public void selectTab(int index) {

			int oldSelectedIndex = _tabPanel.selectedIndex();
			if (oldSelectedIndex >= 0) {
				Tab oldTab = _tabPanel.tabAt(oldSelectedIndex);
				oldTab.onDeselect();
			}
			Tab tab = _tabPanel.tabAt(index);
			tab.onSelect();
			moveToVisibleArea(tab);
		}
	}

	private List<Tab> _tabs;

	private TabBar _tabBar;

	private SimplePanel _contentPanel;

	private int _selectedIndex = -1;

	private List<TabListener> _listeners;

	public CTabPanel() {

		_tabs = new Vector<Tab>();
		_tabBar = new TabBar(this);
		add(_tabBar);
		_contentPanel = new SimplePanel();
		_contentPanel.setHeight100();
		_contentPanel.setWidth100();
		_contentPanel.setPadding(1);
		// _contentPanel.setBorder(3, BorderStyle.SOLID, COLOR_TAB_SELECTED);
		add(_contentPanel);
		_listeners = new Vector<TabListener>();
		setWidth100();
		setHeight100();
	}

	protected List<Tab> tabs() {

		return _tabs;
	}

	protected SimplePanel contentPanel() {

		return _contentPanel;
	}

	public void addTabListener(TabListener l) {

		_listeners.add(l);
	}

	public void removeTabListener(TabListener l) {

		_listeners.remove(l);
	}

	private void notifyOfTabAdded(Tab tab) {

		for (TabListener l : _listeners) {
			l.tabAdded(this, tab);
		}
	}

	private void notifyOfTabRemoved(Tab tab) {

		for (TabListener l : _listeners) {
			l.tabRemoved(this, tab);
		}
	}

	public void removeAllTabs() {

		while (_tabs.size() > 0) {
			removeTab(0);
		}
	}

	public int addTab(Tab tab, boolean fireEvents) {

		int index = _tabs.indexOf(tab);
		if (index == -1) {
			index = _tabs.size();
			tab.setTabPanel(this);
			_tabs.add(tab);
			_tabBar.addTab(tab);
			if (fireEvents) {
				notifyOfTabAdded(tab);
			}
			if (index == 0) {
				selectTab(index, fireEvents);
			}
		}
		return index;
	}

	public int addTab(Tab tab) {

		return addTab(tab, true);
	}

	public void removeTab(Tab tab, boolean fireEvents) {

		int index = _tabs.indexOf(tab);
		if (index != -1) {
			removeTab(index, fireEvents);
		}
	}

	public void removeTab(Tab tab) {

		removeTab(tab, true);
	}

	public void removeTab(int index, boolean fireEvents) {

		if (index < 0 || index >= _tabs.size()) {
			throw new AssertionError("Index out of bound.");
		}
		Tab tab = _tabs.get(index);
		if (_selectedIndex == index) {
			if (numTabs() > 1) {
				selectTab(index > 0 ? index - 1 : index + 1);
			} else {
				// remove the only tab
				_contentPanel.clear();
			}
		}
		if (_selectedIndex > index) {
			_selectedIndex--;
		}
		_tabBar.removeTab(index);
		_tabs.get(index).setTabPanel(null);
		_tabs.remove(index);
		if (_tabs.size() == 0) {
			_selectedIndex = -1;
			_contentPanel.clear();
		}
		if (fireEvents) {
			notifyOfTabRemoved(tab);
		}
	}

	public void removeTab(int index) {

		removeTab(index, true);
	}

	public void selectTab(Tab tab, boolean fireEvents) {

		int index = _tabs.indexOf(tab);
		if (index != -1) {
			selectTab(index, fireEvents);
		}
	}

	public void selectTab(Tab tab) {

		selectTab(tab, true);
	}

	public void selectTab(int index, boolean fireEvents) {

		if (index < 0 || index >= _tabs.size()) {
			throw new AssertionError("Index out of bound.");
		}
		if (index == _selectedIndex) {
			return;
		}
		if (fireEvents) {
			BeforeSelectionEvent<Integer> event = BeforeSelectionEvent.fire(
					this, index);
			if ((event != null) && event.isCanceled()) {
				return;
			}
		}
		_tabBar.selectTab(index);
		_contentPanel.setContent(_tabs.get(index).content());
		_selectedIndex = index;
		if (fireEvents) {
			SelectionEvent.fire(this, index);
		}
	}

	public void selectTab(int index) {

		selectTab(index, true);
	}

	public int selectedIndex() {

		return _selectedIndex;
	}

	public void setContent(int index, Widget content) {

		if (index >= 0 && index < _tabs.size()) {
			_tabs.get(index).setContent(content);
		}
	}

	public Tab selectedTab() {

		if (_selectedIndex >= 0 && _selectedIndex < _tabs.size()) {
			return _tabs.get(_selectedIndex);
		}
		return null;
	}

	public Tab tabAt(int index) {

		if (index < 0 || index >= _tabs.size()) {
			throw new AssertionError("Index out of bound.");
		}
		return _tabs.get(index);
	}

	public int indexOf(Tab tab) {

		return _tabs.indexOf(tab);
	}

	public int numTabs() {

		return _tabs.size();
	}

	public Tab tabOf(Object object) {

		for (Tab tab : _tabs) {
			if (object != null && tab.object() != null) {
				Object to = tab.object();
				if (to.equals(object)) {
					return tab;
				}
			}
		}
		return null;
	}

	public int indexOf(Object object) {

		Tab tab = tabOf(object);
		if (tab == null) {
			return -1;
		}
		return tab.index();
	}

	public void selectTab(Object object, boolean fireEvents) {

		Tab tab = tabOf(object);
		if (tab != null) {
			selectTab(tab, fireEvents);
		}
	}

	public void selectTab(Object object) {

		selectTab(object, true);
	}

	public void removeTab(Object object, boolean fireEvents) {

		Tab tab = tabOf(object);
		if (tab != null) {
			removeTab(tab, fireEvents);
		}
	}

	public void removeTab(Object object) {

		removeTab(object, true);
	}

	@Override
	public HandlerRegistration addBeforeSelectionHandler(
			BeforeSelectionHandler<Integer> handler) {

		return addHandler(handler, BeforeSelectionEvent.getType());
	}

	@Override
	public HandlerRegistration addSelectionHandler(
			SelectionHandler<Integer> handler) {

		return addHandler(handler, SelectionEvent.getType());
	}
}
