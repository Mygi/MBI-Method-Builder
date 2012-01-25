package daris.client.ui.widget;

import arc.gui.gwt.widget.image.Image;
import arc.gui.gwt.widget.image.LinearGradient;
import arc.gui.gwt.widget.label.Label;
import arc.gui.gwt.widget.panel.AbsolutePanel;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.window.Window;
import arc.gui.window.WindowProperties;
import arc.mf.client.util.Action;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.resources.client.ImageResource;

public class ProgressDialog {

	public static final ImageResource IMG_LOADING = daris.client.Resource.INSTANCE
			.loading16();
	public static final int PROGRESS_BAR_HEIGHT = 20;
	public static final String PROGRESS_BAR_COLOR_LIGHT = "#2020ff";
	public static final String PROGRESS_BAR_COLOR_DARK = "#7070c0";

	private String _title;
	private String _message;
	private double _progress;
	private Window _win;
	private boolean _autoClose;
	private Action _onClose;
	private SimplePanel _progressBar;
	private AbsolutePanel _progressContainer;
	private boolean _showing;

	public ProgressDialog(String title, String message) {

		this(title, message, false, null);
	}

	public ProgressDialog(String title, String message, boolean autoClose) {

		this(title, message, autoClose, null);
	}

	public ProgressDialog(String title, String message, boolean autoClose,
			Action onClose) {

		_title = title;
		_message = message;
		_progress = 0.0;
		_autoClose = autoClose;
		_onClose = onClose;
		_showing = false;

		WindowProperties wp = new WindowProperties();
		wp.setModal(true);
		wp.setCanBeResized(false);
		wp.setCanBeClosed(true);
		wp.setCanBeMoved(true);
		wp.setCenterInPage(true);
		wp.setTitle(_title);
		wp.setSize(320, 100);
		_win = Window.create(wp);
		VerticalPanel vp = new VerticalPanel();
		vp.fitToParent();
		HorizontalPanel messageContainer = new HorizontalPanel();
		messageContainer.setMarginLeft(15);
		messageContainer.setMarginRight(15);
		messageContainer.setMarginTop(15);
		Image imgLoading = new Image(IMG_LOADING.getSafeUri().asString(),
				IMG_LOADING.getWidth(), IMG_LOADING.getHeight());
		messageContainer.add(imgLoading);
		Label msgLabel = new Label(_message);
		msgLabel.setMarginTop(3);
		msgLabel.setMarginLeft(2);
		msgLabel.setFontSize(11);

		messageContainer.add(msgLabel);
		vp.add(messageContainer);

		_progressContainer = new AbsolutePanel();
		_progressContainer.setHeight(PROGRESS_BAR_HEIGHT);
		_progressContainer.setWidth100();
		_progressContainer.setMarginLeft(15);
		_progressContainer.setMarginRight(15);
		_progressContainer.setMarginBottom(20);
		_progressContainer.setBorderRadius(3);
		_progressContainer.setBorder(1, "#909090");
		_progressContainer
				.setBackgroundImage(new LinearGradient(
						LinearGradient.Orientation.TOP_TO_BOTTOM, "#f0f0f0",
						"#e0e0e0"));

		_progressBar = new SimplePanel();
		_progressBar.setBorderRadius(3);
		_progressBar.setHeight100();
		_progressBar.setBackgroundImage(new LinearGradient(
				LinearGradient.Orientation.TOP_TO_BOTTOM,
				PROGRESS_BAR_COLOR_LIGHT, PROGRESS_BAR_COLOR_DARK));
		_progressBar.setPosition(Position.ABSOLUTE);
		_progressBar.setLeft(0);
		_progressContainer.add(_progressBar);
		vp.add(_progressContainer);
		_win.setContent(vp);
	}

	public void setProgress(double progress) {

		if (progress >= 1.0 && _autoClose) {
			close();
		}
		if (progress > 1) {
			progress = 1;
		}
		if (progress < 0) {
			progress = 0;
		}
		if (_progress == progress) {
			return;
		}
		_progress = progress;
		if (!_showing) {
			return;
		} else {
			_progressBar
					.setWidth((int) (_progressContainer.width() * _progress));
		}
	}

	public double progress() {

		return _progress;
	}

	public void close() {

		_win.close();
		if (_onClose != null) {
			_onClose.execute();
		}
	}

	public void show() {

		_win.show();
		_showing = true;
	}

	public void setCloseAction(Action onClose) {

		_onClose = onClose;
	}
}
