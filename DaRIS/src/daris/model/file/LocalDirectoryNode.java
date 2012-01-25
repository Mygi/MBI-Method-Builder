package daris.model.file;

import java.util.List;
import java.util.Vector;

import arc.gui.image.Image;
import arc.gui.util.HTMLUtil;
import arc.mf.client.file.FileHandler;
import arc.mf.client.file.LocalFile;
import arc.mf.client.util.Fuzzy;
import arc.mf.client.util.StringTokenizer;
import arc.mf.object.tree.Container;
import arc.mf.object.tree.Node;
import arc.mf.object.tree.NodeListener;
import arc.mf.object.tree.TreeNodeAddHandler;
import arc.mf.object.tree.TreeNodeContentsHandler;
import arc.mf.object.tree.TreeNodeDescriptionHandler;
import arc.mf.object.tree.TreeNodeRemoveHandler;
import daris.client.Resource;

public class LocalDirectoryNode implements Container {

	public static final Image ICON_FOLDER_OPEN = new Image(Resource.INSTANCE
			.folderVioletOpen16().getSafeUri().asString(), 16, 16);
	public static final Image ICON_FOLDER = new Image(Resource.INSTANCE
			.folderViolet16().getSafeUri().asString(), 16, 16);

	private LocalFile _dir;

	public LocalDirectoryNode(LocalFile dir) {

		_dir = dir;
	}

	public void add(Node n, TreeNodeAddHandler ah) {

	}

	public void contents(long start, long end, final TreeNodeContentsHandler ch) {

		_dir.files(LocalFile.Filter.DIRECTORIES, start, end, new FileHandler() {
			public void process(long start, long end, long total,
					List<LocalFile> files) {

				if (files == null) {
					ch.loaded(start, end, total, null);
				} else {
					List<Node> contents = new Vector<Node>(files.size());
					for (LocalFile f : files) {
						contents.add(new LocalDirectoryNode(f));
					}

					ch.loaded(start, end, total, contents);
				}
			}
		});
	}

	public void remove(Node n, TreeNodeRemoveHandler rh) {

	}

	public String name() {

		return _dir.name();
	}

	public Object object() {

		return _dir;
	}

	public String type() {

		return getClass().getName();
	}

	public Image icon() {

		return ICON_FOLDER;
	}

	public Image openIcon() {

		return ICON_FOLDER_OPEN;
	}

	public boolean sorted() {

		return true;
	}

	public String path() {

		return _dir.path();
	}

	public void description(TreeNodeDescriptionHandler dh) {

		String d = "Local directory:<br/>";

		d += "<div style=\"font-size: smaller\">";

		int indent = 2;
		StringTokenizer st = new StringTokenizer(path(), "/");
		while (st.hasMoreTokens()) {
			d += HTMLUtil.indentLeft("/" + st.nextToken(), indent);
			indent += 2;
		}

		d += "</div>";

		if (_dir.description() != null) {
			d += "<br/>" + _dir.description();
		}

		dh.description(d);
	}

	public boolean readOnly() {

		// TODO Auto-generated method stub
		return true;
	}

	public Object subscribe(boolean descend, NodeListener l) {

		// TODO Auto-generated method stub
		return null;
	}

	public void unsubscribe(Object key) {

		// TODO Auto-generated method stub

	}

	public void discard() {

	}

	@Override
	public Fuzzy hasChildren() {

		// TODO Auto-generated method stub
		return Fuzzy.MAYBE;
	}

}
