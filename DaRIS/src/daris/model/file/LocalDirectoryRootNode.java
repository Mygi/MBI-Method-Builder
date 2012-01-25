package daris.model.file;

import java.util.List;
import java.util.Vector;

import arc.gui.image.Image;
import arc.mf.client.dti.DTI;
import arc.mf.client.dti.DTIReadyListener;
import arc.mf.client.file.FileHandler;
import arc.mf.client.file.LocalFile;
import arc.mf.client.util.Fuzzy;
import arc.mf.object.tree.Container;
import arc.mf.object.tree.Node;
import arc.mf.object.tree.NodeListener;
import arc.mf.object.tree.TreeNodeAddHandler;
import arc.mf.object.tree.TreeNodeContentsHandler;
import arc.mf.object.tree.TreeNodeDescriptionHandler;
import arc.mf.object.tree.TreeNodeRemoveHandler;
import daris.client.Resource;

public class LocalDirectoryRootNode implements Container, DTIReadyListener {

	public static final Image ICON_COMPUTER = new Image(Resource.INSTANCE
			.computer16().getSafeUri().asString(), 16, 16);

	public static final String ROOT_NAME = "Local Files";

	private long _start;
	private long _end;
	private TreeNodeContentsHandler _ch;

	public LocalDirectoryRootNode() {

	}

	public void add(Node n, TreeNodeAddHandler ah) {

	}

	public void contents(long start, long end, final TreeNodeContentsHandler ch) {

		// If not enabled, then return nothing .. add a listener in case the DTI
		// becomes active.
		if (!DTI.enabled()) {
			// ch.loaded(start, end, 0, null);
			_start = start;
			_end = end;
			_ch = ch;
			DTI.addReadyListener(this);
			return;
		}

		DTI.fileSystem().roots(new FileHandler() {
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

		return ROOT_NAME;
	}

	public Object object() {

		return null;
	}

	public String type() {

		return getClass().getName();
	}

	public Image icon() {

		return ICON_COMPUTER;
	}

	public Image openIcon() {

		return ICON_COMPUTER;
	}

	public boolean sorted() {

		return true;
	}

	public String path() {

		return null;
	}

	public void description(TreeNodeDescriptionHandler dh) {

		if (DTI.enabled()) {
			dh.description("Files on the local file system. These can be copied into/from the server.");
		}

		dh.description("Files on the local file system.<br/>Not available - desktop integration has not activated.");
	}

	public boolean readOnly() {

		return true;
	}

	public Object subscribe(boolean descend, NodeListener l) {

		return null;
	}

	public void unsubscribe(Object key) {

	}

	public void failed(String reason) {

		DTI.removeReadyListener(this);

		if (_ch != null) {
			_ch.loaded(_start, _end, 0, null);
			_ch = null;
		}

	}

	public void activated() {

		DTI.removeReadyListener(this);

		if (_ch != null) {
			contents(_start, _end, _ch);
		}

	}

	public void deactivated() {

		DTI.removeReadyListener(this);

		if (_ch != null) {
			_ch.loaded(_start, _end, 0, null);
			_ch = null;
		}
	}

	public void discard() {

		DTI.removeReadyListener(this);
	}

	@Override
	public Fuzzy hasChildren() {

		// TODO Auto-generated method stub
		return Fuzzy.MAYBE;
	}

}