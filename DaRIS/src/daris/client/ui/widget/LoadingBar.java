package daris.client.ui.widget;

import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.image.Image;
import arc.gui.gwt.widget.panel.AbsolutePanel;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Widget;

import daris.client.Resource;

public class LoadingBar extends ContainerWidget {

	private static ImageResource LOADING_BAR = Resource.INSTANCE
			.loading128x15();
	private static ImageResource LOADING_FINISHED_BAR = Resource.INSTANCE
			.loadingFinished128x15();
	private static ImageResource LOADING_FAILED_BAR = Resource.INSTANCE
			.loadingFailed128x15();

	private Image _loadingImg;
	private HTML _loadingMsg;
	private AbsolutePanel _ap;

	public LoadingBar(String loadingMsg) {

		_loadingImg = new Image(LOADING_BAR.getSafeUri().asString(),
				LOADING_BAR.getWidth(), LOADING_BAR.getHeight());
		_loadingImg.setPosition(Position.ABSOLUTE);
		_loadingMsg = new HTML(loadingMsg);
		_loadingMsg.setFontSize(10);
		_loadingMsg.setHeight(20);
		_loadingMsg.setPosition(Position.ABSOLUTE);
		_ap = new AbsolutePanel();
		_ap.fitToParent();
		initWidget(_ap);
		add(_loadingImg);
		add(_loadingMsg);
	}

	protected void doAdd(Widget w, boolean layout) {

		_ap.add(w);
	}

	protected boolean doRemove(Widget w, boolean layout) {

		return _ap.remove(w);
	}

	public void setMessage(String message) {

		_loadingMsg.setHTML(message);
		doLayoutChildren();
	}

	protected void setImage(ImageResource ir) {

		remove(_loadingImg);
		_loadingImg = new Image(ir.getSafeUri().asString(), ir.getWidth(),
				ir.getHeight());
		_loadingImg.setPosition(Position.ABSOLUTE);
		add(_loadingImg);
	}

	public void finished(String msg) {

		setMessage(msg);
		_loadingMsg.setColour("green");
		setImage(LOADING_FINISHED_BAR);
	}

	public void failed(String msg) {

		setMessage(msg);
		_loadingMsg.setColour("red");
		setImage(LOADING_FAILED_BAR);
	}

	protected void doLayoutChildren() {

		super.doLayoutChildren();
		_loadingImg.setLeft(width() / 2 - _loadingImg.width() / 2);
		_loadingImg.setTop(height() / 2 - _loadingImg.height() / 2);
		_loadingMsg.setLeft(width() / 2 - _loadingMsg.width() / 2);
		_loadingMsg.setTop(_loadingImg.bottom());
	}
}
