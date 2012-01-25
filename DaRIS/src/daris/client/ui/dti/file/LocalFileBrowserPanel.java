package daris.client.ui.dti.file;

import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.user.client.ui.Widget;

import arc.gui.InterfaceComponent;
import arc.gui.file.FileFilter;
import arc.gui.gwt.style.FontWeight;
import arc.gui.gwt.widget.image.LinearGradient;
import arc.gui.gwt.widget.label.Label;
import arc.gui.gwt.widget.panel.CenteringPanel;
import arc.gui.gwt.widget.panel.HorizontalSplitPanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.panel.CenteringPanel.Axis;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.tree.TreeGUI;
import arc.gui.gwt.widget.tree.TreeGUIEventHandler;
import arc.mf.client.file.LocalFile;
import arc.mf.object.tree.Node;
import daris.model.file.LocalDirectoryTree;

public class LocalFileBrowserPanel implements InterfaceComponent {

	private VerticalPanel _vp;
	private String _title;
	private boolean _showTitle;
	
	private TreeGUI _dirTreeGUI;
	private LocalFileGrid _fileGrid;

	public LocalFileBrowserPanel(String title, boolean showTitle, LocalFile dir, LocalFile.Filter f,
			FileFilter ff, boolean multiSelect) {
		
		_vp = new VerticalPanel();
		_vp.fitToParent();
		
		_title = title;
		_showTitle = showTitle;
		if(_showTitle){
			if(_title==null){
				_title = "Local File System";
			}
			_vp.add(createHeader(_title));
		}		

		_fileGrid = new LocalFileGrid(dir, f, ff,
				multiSelect);

		_dirTreeGUI = new TreeGUI(new LocalDirectoryTree(), ScrollPolicy.AUTO);
		_dirTreeGUI.setHeight100();
		_dirTreeGUI.setPreferredWidth(0.3);
		_dirTreeGUI.setEventHandler(new TreeGUIEventHandler() {

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
		_dirTreeGUI.enableNodeDrag();

		HorizontalSplitPanel hsp = new HorizontalSplitPanel();
		hsp.fitToParent();
		hsp.add(_dirTreeGUI);
		hsp.add(_fileGrid);
		hsp.setBorderLeft(1, BorderStyle.SOLID, "#979797");
		hsp.setBorderRight(1, BorderStyle.SOLID, "#979797");
		hsp.setBorderBottom(1, BorderStyle.SOLID, "#979797");
		
		_vp.add(hsp);
		
		
	}
	
	private CenteringPanel createHeader(String title){
		Label label = new Label(title);
		label.setFontSize(12);
		label.setFontWeight(FontWeight.BOLD);

		CenteringPanel cp = new CenteringPanel(Axis.HORIZONTAL);
		cp.setWidth100();
		cp.setHeight(20);
		cp.setMarginTop(1);
		cp.setBorderTop(1, BorderStyle.SOLID, "#979797");
		cp.setBorderLeft(1, BorderStyle.SOLID, "#979797");
		cp.setBorderRight(1, BorderStyle.SOLID, "#979797");
		cp.setBorderRadiusTopLeft(5);
		cp.setBorderRadiusTopRight(5);
		cp.setBackgroundImage(new LinearGradient(LinearGradient.Orientation.TOP_TO_BOTTOM, "#cdcdcd",
				"#979797"));
		cp.add(label);
		return cp;
	}

	protected TreeGUI directoryTreeGUI() {
		return _dirTreeGUI;
	}

	protected LocalFileGrid fileGrid() {
		return _fileGrid;
	}

	@Override
	public Widget gui() {
		return _vp;
	}

}
