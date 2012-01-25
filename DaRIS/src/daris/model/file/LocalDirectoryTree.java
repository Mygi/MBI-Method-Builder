package daris.model.file;

import arc.gui.image.Image;
import arc.mf.object.tree.Container;
import arc.mf.object.tree.Tree;

public class LocalDirectoryTree implements Tree {

	private Container _root;
	
	public LocalDirectoryTree() {
		_root = new LocalDirectoryRootNode();
	}
	
	public boolean readOnly() {
		return true;
	}

	public Image icon() {
		return null;
	}
	
	public Container root() {
		return _root;
	}

	public void setReadOnly(boolean readOnly) {
		// Nothing to do.
	}

	public void discard() {
		_root.discard();
	}

}