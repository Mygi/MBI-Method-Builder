package daris.client.ui.dti.file;

import java.util.List;
import java.util.Vector;

import arc.gui.file.FileFilter;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.panel.HorizontalSplitPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.tree.TreeGUI;
import arc.gui.gwt.widget.tree.TreeGUIEventHandler;
import arc.gui.gwt.widget.window.Window;
import arc.gui.window.WindowProperties;
import arc.mf.client.dti.DTI;
import arc.mf.client.file.BrowserUploadElement;
import arc.mf.client.file.LocalFile;
import arc.mf.object.tree.Node;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FileUpload;

import daris.model.file.LocalDirectoryTree;

public class LocalFileSelector {

	public static final int MIN_WIDTH = 320;

	public static final int MIN_HEIGHT = 240;

	private Window _win;

	public LocalFileSelector(FileFilter ff, final LocalFileSelectionListener fsl) {

		WindowProperties wp = new WindowProperties();
		wp.setModal(false);
		wp.setCanBeResized(true);
		wp.setCanBeClosed(false);
		wp.setCanBeMoved(true);
		wp.setCenterInPage(true);
		wp.setTitle("Local File System");
		wp.setSize(MIN_WIDTH, MIN_HEIGHT);
		if (DTI.enabled()) {
			int width = (int) (com.google.gwt.user.client.Window
					.getClientWidth() * 0.6);
			width = width < MIN_WIDTH ? MIN_WIDTH : width;
			int height = (int) (com.google.gwt.user.client.Window
					.getClientHeight() * 0.6);
			height = height < MIN_HEIGHT ? MIN_HEIGHT : height;
			wp.setSize(width, height);

			final LocalFileGrid fileGrid = new LocalFileGrid(null,
					LocalFile.Filter.FILES, ff, true);

			TreeGUI treeGUI = new TreeGUI(new LocalDirectoryTree(),
					ScrollPolicy.AUTO);
			treeGUI.setHeight100();
			treeGUI.setPreferredWidth(0.3);
			treeGUI.setEventHandler(new TreeGUIEventHandler() {

				@Override
				public void clicked(Node n) {

				}

				@Override
				public void selected(Node n) {

					final LocalFile dir = (LocalFile) n.object();
					fileGrid.setDirectory(dir);
				}

				@Override
				public void deselected(Node n) {

				}

				@Override
				public void opened(Node n) {

				}

				@Override
				public void closed(Node n) {

				}

				@Override
				public void added(Node n) {

				}

				@Override
				public void removed(Node n) {

				}

				@Override
				public void changeInMembers(Node n) {

				}
			});

			HorizontalSplitPanel hsp = new HorizontalSplitPanel();
			hsp.fitToParent();
			hsp.add(treeGUI);
			hsp.add(fileGrid);

			ButtonBar bb = new ButtonBar(ButtonBar.Position.BOTTOM,
					ButtonBar.Alignment.RIGHT);
			bb.setHeight(28);
			bb.setBackgroundColour("#d9d9d9");
			final Button okButton = new Button("OK");
			okButton.disable();
			fileGrid.setSelectionHandler(new SelectionHandler<LocalFile>() {

				@Override
				public void selected(LocalFile o) {

					okButton.setEnabled(fileGrid.hasSelections());
				}

				@Override
				public void deselected(LocalFile o) {

					okButton.setEnabled(fileGrid.hasSelections());
				}
			});
			okButton.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {

					fsl.filesSelected(fileGrid.selections());
					_win.close();
				}
			});
			bb.add(okButton);
			Button cancelButton = new Button("Cancel");
			cancelButton.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {

					fsl.filesSelected(null);
					_win.close();
				}
			});
			cancelButton.setMarginRight(20);
			bb.add(cancelButton);

			VerticalPanel vp = new VerticalPanel();
			vp.fitToParent();

			vp.add(hsp);
			vp.add(bb);
			_win = Window.create(wp);
			_win.setContent(vp);

		} else {

			System.out
					.println("DTI is not enabled. Using browser file open dialog...");
			final SimplePanel sp = new SimplePanel();
			sp.setPadding(20);
			sp.fitToParent();
			final FileUpload fu = new FileUpload();
			fu.addChangeHandler(new ChangeHandler() {

				@Override
				public void onChange(ChangeEvent event) {

					String fn = fu.getFilename();
					if (fn != null) {
						LocalFile f = new BrowserUploadElement(
								(InputElement) (fu.getElement().cast()), null);
						List<LocalFile> fs = new Vector<LocalFile>(1);
						fs.add(f);
						fsl.filesSelected(fs);
						_win.close();
					}
				}
			});
			sp.setContent(fu);
			_win = Window.create(wp);
			_win.setContent(sp);
		}

	}

	public void show() {

		_win.centerInPage();
		_win.show();
	}

}
