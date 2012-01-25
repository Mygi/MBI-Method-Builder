package daris.client.ui.widget;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.resources.client.ImageResource;

import daris.client.Resource;

import arc.gui.gwt.widget.image.Image;
import arc.gui.gwt.widget.label.Label;
import arc.gui.gwt.widget.panel.AbsolutePanel;

public class LoadingMessage extends AbsolutePanel {

	public static final ImageResource LOADING_IMG_16 = Resource.INSTANCE
			.loading16();

	public static final ImageResource LOADING_IMG_100 = Resource.INSTANCE
			.loading100();

	private Image _loadingImg;
	private Label _loadingMsg;

	public LoadingMessage(String loadingMessage) {

		this(LOADING_IMG_100, loadingMessage);
	}

	public LoadingMessage(ImageResource loadingImage, String loadingMessage) {

		super();
		_loadingImg = new Image(loadingImage.getSafeUri().asString(),
				loadingImage.getWidth(), loadingImage.getHeight());
		_loadingImg.setPosition(Position.ABSOLUTE);
		add(_loadingImg);
		_loadingMsg = new Label(loadingMessage);
		_loadingMsg.setPosition(Position.ABSOLUTE);
		add(_loadingMsg);

	}

	@Override
	protected void doLayoutChildren() {

		super.doLayoutChildren();
		_loadingImg.setLeft(width() / 2 - _loadingImg.width() / 2);
		_loadingImg.setTop(height() / 2 - _loadingImg.height() / 2);
		_loadingMsg.setLeft(width() / 2 - _loadingMsg.width() / 2);
		_loadingMsg.setTop(_loadingImg.bottom());
	}

}
