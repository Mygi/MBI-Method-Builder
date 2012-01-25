package daris.gui.object;

import java.util.List;

import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.button.ButtonBar.Alignment;
import arc.gui.gwt.widget.button.ButtonBar.Position;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.mf.client.file.LocalFile;
import arc.mf.object.ObjectMessage;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import daris.client.ui.dti.file.LocalFileSelectionListener;
import daris.client.ui.dti.file.LocalFileSelector;
import daris.model.object.PSSDObjectRef;
import daris.model.object.attachment.Attachment;
import daris.model.object.messages.ObjectAttach;
import daris.model.object.messages.ObjectAttachmentGet;
import daris.model.object.messages.ObjectDetach;

public class AttachmentPanel extends VerticalPanel {

	private AttachmentGrid _grid;
	private Button _addButton;
	private Button _removeButton;
	private Button _downloadButton;
	private PSSDObjectRef _o;
	private boolean _rendered = false;

	public AttachmentPanel(PSSDObjectRef o, boolean render) {

		_o = o;
		fitToParent();
		_grid = new AttachmentGrid(render ? _o : null);
		_grid.fitToParent();
		_grid.setMultiSelect(true);
		add(_grid);
		ButtonBar bb = new ButtonBar(Position.BOTTOM, Alignment.CENTER);
		bb.setHeight(28);
		bb.setWidth100();
		bb.setColourEnabled(false);
		bb.setMarginTop(3);
		bb.setBackgroundColour("#cccccc");
		_addButton = bb.addButton("Attach ...");
		_addButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				LocalFileSelector lfs = new LocalFileSelector(null,
						new LocalFileSelectionListener() {

							@Override
							public void filesSelected(List<LocalFile> files) {

								if (files == null) {
									return;
								}
								if (files.isEmpty()) {
									return;
								}
								for (LocalFile file : files) {
									ObjectMessage<Attachment> msg = new ObjectAttach(
											_o.id(), file);
									msg.send(new ObjectMessageResponse<Attachment>() {

										@Override
										public void responded(Attachment r) {

											if (r != null) {
												_grid.refresh();
											}
										}
									});
								}
							}
						});
				lfs.show();
			}
		});
		_removeButton = bb.addButton("Remove");
		_removeButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				List<Attachment> as = _grid.selections();
				if (as != null) {
					ObjectMessage<Boolean> msg = new ObjectDetach(as, _o);
					msg.send(new ObjectMessageResponse<Boolean>() {

						@Override
						public void responded(Boolean r) {

							if (r) {
								_grid.refresh();
							}
						}
					});
				}

			}
		});
		_downloadButton = bb.addButton("Download");
		_downloadButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				List<Attachment> as = _grid.selections();
				if (as == null) {
					return;
				}
				if (as.isEmpty()) {
					return;
				}
				ObjectMessage<Boolean> msg = new ObjectAttachmentGet(_o, as);
				msg.send();
			}
		});
		add(bb);
	}

	public void render() {

		if (!_rendered) {
			_grid.setData(_o);
			_rendered = true;
		}
	}
}
