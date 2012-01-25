package daris.client.ui.dicom;

import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.input.TextBox;
import arc.gui.gwt.widget.label.Label;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.panel.VerticalSplitPanel;
import arc.mf.client.util.ActionListener;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;

import daris.client.Resource;

public class DicomImageNavigator extends VerticalSplitPanel {

	public static final ImageResource IMG_FIRST = Resource.INSTANCE.first16();

	public static final ImageResource IMG_LAST = Resource.INSTANCE.last16();

	public static final ImageResource IMG_NEXT = Resource.INSTANCE.next16();

	public static final ImageResource IMG_PREV = Resource.INSTANCE.prev16();

	private String _assetId;
	private int _size;
	private int _index = 0;

	private Button _firstButton;
	private Button _prevButton;
	private TextBox _indexField;
	private Button _nextButton;
	private Button _lastButton;
	private boolean _imageLoaded;
	private boolean _metadataLoaded;

	private DicomImagePanel _ip;
	private DicomMetadataGrid _mg;

	public DicomImageNavigator(String assetId, int size) {

		super(10);

		_assetId = assetId;
		_size = size;
		_index = 0;

		_ip = new DicomImagePanel(_assetId);
		_ip.seek(_index, null);
		_ip.setPreferredHeight(0.4);
		add(_ip);

		VerticalPanel vp = new VerticalPanel();
		vp.setWidth100();

		ButtonBar bb = new ButtonBar(ButtonBar.Position.BOTTOM, ButtonBar.Alignment.CENTER);
		bb.setHeight(28);
		bb.setWidth100();
		bb.setColourEnabled(false);
		bb.setBackgroundColour("#DDDDDD");

		_firstButton = new Button("<img width=\"" + IMG_FIRST.getWidth() + "px\" height=\"" + IMG_FIRST.getHeight()
				+ "px\" src=\"" + IMG_FIRST.getSafeUri().asString()
				+ "\" style=\"vertical-align: middle; padding-top: 0px;\"></img>", false);
		_firstButton.setWidth(30);
		_firstButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				first();
			}
		});
		bb.add(_firstButton);

		_prevButton = new Button("<img width=\"" + IMG_PREV.getWidth() + "px\" height=\"" + IMG_PREV.getHeight()
				+ "px\" src=\"" + IMG_PREV.getSafeUri().asString()
				+ "\" style=\"vertical-align: middle; padding-top: 0px;\"></img>", false);
		_prevButton.setWidth(30);
		_prevButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				prev();
			}
		});
		bb.add(_prevButton);
		_indexField = new TextBox();
		_indexField.setFontSize(12);
		_indexField.setWidth(30);
		_indexField.setAlignment(TextAlignment.RIGHT);
		_indexField.setValue("" + (_index + 1));
		_indexField.addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {

				_indexField.selectAll();
			}
		});
		_indexField.addKeyPressHandler(new KeyPressHandler() {

			@Override
			public void onKeyPress(KeyPressEvent event) {

				int keyCode = event.getNativeEvent().getKeyCode();
				switch (keyCode) {
				case KeyCodes.KEY_UP:
				case KeyCodes.KEY_DOWN:
				case KeyCodes.KEY_LEFT:
				case KeyCodes.KEY_RIGHT:
					// case KeyCodes.KEY_BACKSPACE:
				case KeyCodes.KEY_ESCAPE:
				case KeyCodes.KEY_HOME:
				case KeyCodes.KEY_END:
					// case KeyCodes.KEY_DELETE:
				case KeyCodes.KEY_ENTER:
				case KeyCodes.KEY_TAB:
					return;
				}
				char charCode = event.getCharCode();
				if (!Character.isDigit(charCode)) {
					((TextBox) event.getSource()).cancelKey();
				}
			}
		});
		_indexField.addValueChangeHandler(new ValueChangeHandler<String>() {

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {

				int value = Integer.parseInt(_indexField.value());
				if (value > 0 && value <= _size) {
					setImage(value - 1);
				} else {
					_indexField.setValue("" + (_index + 1));
				}
			}
		});
		bb.add(_indexField);

		Label sizeLabel = new Label("/" + _size);
		sizeLabel.setFontSize(12);
		bb.add(sizeLabel);

		_nextButton = new Button("<img width=\"" + IMG_NEXT.getWidth() + "px\" height=\"" + IMG_NEXT.getHeight()
				+ "px\" src=\"" + IMG_NEXT.getSafeUri().asString()
				+ "\" style=\"vertical-align: middle; padding-top: 0px;\"></img>", false);
		_nextButton.setWidth(30);
		_nextButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				next();
			}
		});
		bb.add(_nextButton);

		_lastButton = new Button("<img width=\"" + IMG_LAST.getWidth() + "px\" height=\"" + IMG_LAST.getHeight()
				+ "px\" src=\"" + IMG_LAST.getSafeUri().asString()
				+ "\" style=\"vertical-align: middle; padding-top: 0px;\"></img>", false);
		_lastButton.setWidth(30);
		_lastButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				last();
			}
		});
		bb.add(_lastButton);
		vp.add(bb);

		_mg = new DicomMetadataGrid(_assetId);
		_mg.seek(_index, null);
		vp.add(_mg);
		add(vp);

		fitToParent();
	}

	private void lockControlsBeforeLoading() {

		_firstButton.disable();
		_prevButton.disable();
		_indexField.disable();
		_nextButton.disable();
		_lastButton.disable();
		_imageLoaded = false;
		_metadataLoaded = false;
	}

	private void unlockControlsAfterLoaded() {

		if (_imageLoaded && _metadataLoaded) {
			_firstButton.enable();
			_prevButton.enable();
			_indexField.enable();
			_nextButton.enable();
			_lastButton.enable();
		}
	}

	public void setImage(int index) {

		assert index >= 0 && index < _size;
		if (_index != index) {
			lockControlsBeforeLoading();
			_ip.seek(index, new ActionListener() {

				@Override
				public void executed(boolean succeeded) {

					_imageLoaded = succeeded;
					unlockControlsAfterLoaded();
				}
			});
			_mg.seek(index, new ActionListener() {

				@Override
				public void executed(boolean succeeded) {

					_metadataLoaded = succeeded;
					unlockControlsAfterLoaded();
				}
			});
			_mg.refresh();
			_index = index;
			_indexField.setValue("" + (_index + 1));
		}
	}

	public void next() {

		if (_index < _size - 1) {
			setImage(_index + 1);
		}
	}

	public void prev() {

		if (_index > 0) {
			setImage(_index - 1);
		}
	}

	public void first() {

		if (_size > 0) {
			setImage(0);
		}
	}

	public void last() {

		if (_size > 0) {
			setImage(_size - 1);
		}
	}

}
