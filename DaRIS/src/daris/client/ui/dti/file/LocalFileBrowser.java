package daris.client.ui.dti.file;

import arc.gui.file.FileFilter;
import arc.gui.gwt.widget.panel.HorizontalSplitPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.tree.TreeGUI;
import arc.gui.gwt.widget.tree.TreeGUIEventHandler;
import arc.gui.gwt.widget.window.Window;
import arc.gui.gwt.widget.window.WindowCloseListener;
import arc.gui.window.WindowProperties;
import arc.mf.client.file.LocalFile;
import arc.mf.object.tree.Node;
import daris.model.file.LocalDirectoryTree;

public class LocalFileBrowser {

	public static final double WIDTH = 0.5;
	public static final double HEIGHT = 0.5;

	private Window _win;
	private HorizontalSplitPanel _hsp;
	private LocalFileGrid _fileGrid;
	private boolean _showing;

	private LocalFileBrowser() {

		/*
		 * disables multi select.
		 */
		_fileGrid = new LocalFileGrid(null, LocalFile.Filter.FILES, null, false);

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
				_fileGrid.setDirectory(dir);
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
		treeGUI.enableNodeDrag();

		_hsp = new HorizontalSplitPanel();
		_hsp.fitToParent();
		_hsp.add(treeGUI);
		_hsp.add(_fileGrid);

	}

	public void setFileFilter(FileFilter ff) {

		_fileGrid.setFileFilter(ff);
	}

	public void setMultiSelect(boolean multiSelect) {

		_fileGrid.setMultiSelect(multiSelect);
	}

	public void show(Window owner) {

		if (_showing) {
			_win.close();
		}
		WindowProperties wp = new WindowProperties();
		wp.setModal(false);
		wp.setTitle("Local Files");
		wp.setCanBeResized(true);
		wp.setCanBeClosed(true);
		wp.setCanBeMoved(true);
		wp.setOwnerWindow(owner);
		wp.setSize(WIDTH, HEIGHT);
		wp.setCenterInPage(true);
		_win = Window.create(wp);
		_win.addCloseListener(new WindowCloseListener() {

			@Override
			public void closed(Window w) {
				_showing = false;
			}
		});
		_win.setContent(_hsp);
		_win.centerInPage();
		_win.show();
		_showing = true;
	}

	private static LocalFileBrowser _instance;

	public static LocalFileBrowser instance() {
		if (_instance == null) {
			_instance = new LocalFileBrowser();
		}
		return _instance;
	}
}
