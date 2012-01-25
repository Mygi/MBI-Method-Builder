package daris.client.model.object.tree;

import daris.client.model.object.DObject;
import daris.client.model.repository.RepositoryRef;
import arc.gui.image.Image;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.object.tree.Container;
import arc.mf.object.tree.Tree;

public class DObjectTree implements Tree {

	private DObjectTreeNode _root;
	private boolean _readOnly;

	public DObjectTree() {

		_root = new DObjectTreeNode(null, RepositoryRef.INSTANCE);
		if (RepositoryRef.INSTANCE.needToResolve()) {
			RepositoryRef.INSTANCE.resolve(new ObjectResolveHandler<DObject>() {
				@Override
				public void resolved(DObject o) {

					_root.notifyOfModified();
				}
			});
		}
	}

	@Override
	public Image icon() {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Container root() {

		return _root;
	}

	@Override
	public boolean readOnly() {

		return _readOnly;
	}

	@Override
	public void setReadOnly(boolean readOnly) {

		_readOnly = readOnly;
	}

	@Override
	public void discard() {

		_root.discard();
	}

}
