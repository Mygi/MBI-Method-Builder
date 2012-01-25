package daris.client.model.object.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import arc.gui.image.Image;
import arc.mf.client.util.Fuzzy;
import arc.mf.client.util.ObjectUtil;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.object.tree.Container;
import arc.mf.object.tree.Node;
import arc.mf.object.tree.NodeListener;
import arc.mf.object.tree.TreeNodeAddHandler;
import arc.mf.object.tree.TreeNodeContentsHandler;
import arc.mf.object.tree.TreeNodeDescriptionHandler;
import arc.mf.object.tree.TreeNodeRemoveHandler;
import daris.client.Resource;
import daris.client.model.IDUtil;
import daris.client.model.Model;
import daris.client.model.Model.Event;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.project.Project;
import daris.client.model.repository.Repository;
import daris.client.model.repository.RepositoryRef;

public class DObjectTreeNode implements Container, Model.Subscriber {

	public static final Image FOLDER_ICON = new Image(Resource.INSTANCE
			.folderGrey16().getSafeUri().asString(), 16, 16);
	public static final Image FOLDER_OPEN_ICON = new Image(Resource.INSTANCE
			.folderGreyOpen16().getSafeUri().asString(), 16, 16);
	public static final Image FILE_ICON = new Image(Resource.INSTANCE
			.document16().getSafeUri().asString(), 16, 16);

	private DObjectTreeNode _pn;
	private DObjectRef _o;
	private List<NodeListener> _listeners;

	public DObjectTreeNode(DObjectTreeNode pn, DObjectRef o) {

		_o = o;
		_pn = pn;
		Model.subscribe(this);
	}

	@Override
	public String type() {

		return _o.referentTypeName();
	}

	@Override
	public Image icon() {

		return iconFor(_o);
	}

	@Override
	public String name() {

		return nameFor(_o);
	}

	@Override
	public String path() {

		return pathFor(_o);
	}

	@Override
	public void description(TreeNodeDescriptionHandler dh) {

		String description = descriptionFor(_o);
		if (dh != null) {
			dh.description(description);
		}
	}

	@Override
	public Object object() {

		return _o;
	}

	@Override
	public boolean readOnly() {

		return isReadOnly(_o);
	}

	@Override
	public Object subscribe(boolean descend, NodeListener l) {

		if (l == null) {
			return null;
		}
		if (_listeners == null) {
			_listeners = new Vector<NodeListener>();
		}
		_listeners.add(l);
		return l;
	}

	@Override
	public void unsubscribe(Object key) {

		if (_listeners == null) {
			return;
		}
		_listeners.remove(key);
	}

	public boolean hasSubscribers() {

		if (_listeners == null) {
			return false;
		}
		return !_listeners.isEmpty();
	}

	private void notifyOfChildAdd(Node cn, int idx) {

		if (_listeners != null) {
			for (NodeListener l : _listeners) {
				l.added(this, cn, idx);
			}
			/*
			 * if the child node is not subscribed by a TreeNodeGUI, discard it.
			 * This results in: you will then need to reopen the parent node to
			 * re-add the children.
			 */
			DObjectTreeNode cnode = ((DObjectTreeNode) cn);
			if (!cnode.hasSubscribers()) {
				cnode.discard();
			}
		}
	}

	private void notifyOfChildRemove(Node cn) {

		if (_listeners != null) {
			for (NodeListener l : _listeners) {
				l.removed(this, cn);
				/*
				 * discard() will be called by the listener.
				 */
			}
		}
	}

	private void notifyOfChangeInMembers() {

		if (_listeners != null) {
			for (NodeListener l : _listeners) {
				l.changeInMembers(this);
			}
		}
	}

	void notifyOfModified() {

		if (_listeners != null) {
			for (NodeListener nl : new ArrayList<NodeListener>(_listeners)) {
				nl.modified(this);
			}
		}
	}

	@Override
	public void discard() {

		if (_listeners != null) {
			_listeners.clear();
		}
		Model.unsubscribe(this);
	}

	@Override
	public boolean sorted() {

		return _o.childrenRef().sorted();
	}

	@Override
	public Image openIcon() {

		return openIconFor(_o);
	}

	@Override
	public void contents(final long start, final long end,
			final TreeNodeContentsHandler ch) {

		assert start >= 0 && end >= 0 && start <= end;
		_o.childrenRef().resolve(new ObjectResolveHandler<List<DObjectRef>>() {

			@Override
			public void resolved(List<DObjectRef> cos) {

				if (cos == null) {
					ch.loaded(start, end, 0, null);
					return;
				}
				if (cos.isEmpty()) {
					ch.loaded(start, end, 0, null);
					return;
				}
				int start0 = (int) start;
				if (start0 >= cos.size()) {
					ch.loaded(start, end, cos.size(), null);
					return;
				}
				int end0 = end < cos.size() ? (int) end : cos.size();
				List<DObjectRef> ros = cos.subList(start0, end0);
				if (ros.isEmpty()) {
					ch.loaded(start, end, cos.size(), null);
					return;
				}
				List<Node> cns = new Vector<Node>();
				for (DObjectRef ro : ros) {
					cns.add(new DObjectTreeNode(DObjectTreeNode.this, ro));
				}
				ch.loaded(start0, end0, cos.size(), cns);
			}
		});

	}

	@Override
	public void add(Node cn, TreeNodeAddHandler ah) {

		notifyOfChildAdd(cn, -1);
		if (ah != null) {
			ah.added(cn);
		}

	}

	@Override
	public void remove(Node cn, TreeNodeRemoveHandler rh) {

		notifyOfChildRemove(cn);
		if (rh != null) {
			rh.removed(cn);
		}
	}

	private static Image openIconFor(DObjectRef o) {

		String id = o.id();
		if (id == null) {
			return FOLDER_OPEN_ICON;
		} else if (IDUtil.isProjectId(id)) {
			return FOLDER_OPEN_ICON;
		} else if (IDUtil.isSubjectId(id)) {
			return FOLDER_OPEN_ICON;
		} else if (IDUtil.isExMethodId(id)) {
			return FOLDER_OPEN_ICON;
		} else if (IDUtil.isStudyId(id)) {
			return FOLDER_OPEN_ICON;
		} else if (IDUtil.isDataSetId(id)) {
			return FILE_ICON;
		} else if (IDUtil.isDataObjectId(id)) {
			return FILE_ICON;
		}
		return null;
	}

	private static Image iconFor(DObjectRef o) {

		String id = o.id();
		if (id == null) {
			return FOLDER_ICON;
		} else if (IDUtil.isProjectId(id)) {
			return FOLDER_ICON;
		} else if (IDUtil.isSubjectId(id)) {
			return FOLDER_ICON;
		} else if (IDUtil.isExMethodId(id)) {
			return FOLDER_ICON;
		} else if (IDUtil.isStudyId(id)) {
			return FOLDER_ICON;
		} else if (IDUtil.isDataSetId(id)) {
			return FILE_ICON;
		} else if (IDUtil.isDataObjectId(id)) {
			return FILE_ICON;
		}
		return null;

	}

	private static String nameFor(DObjectRef o) {

		if (o.referent() != null) {
			DObject oo = o.referent();
			String name = oo.name();
			if (oo instanceof Repository) {
				String serverName = ((Repository) oo).server().name();
				return oo.id() + (serverName == null ? "" : ": " + serverName);
			} else if (oo instanceof Project) {
				return oo.id() + (name == null ? "" : ": " + name);
			} else {
				return IDUtil.getLastSection(o.id())
						+ (name == null ? "" : ": " + name);
			}
		} else {
			return o.id();
		}
	}

	private static String pathFor(DObjectRef o) {

		return o.id();
	}

	private static String descriptionFor(DObjectRef o) {

		if (o.resolved()) {
			return o.referent().description();
		} else {
			return o.referentTypeName() + " " + o.id();
		}
	}

	private static boolean isReadOnly(DObjectRef o) {

		if (o instanceof RepositoryRef) {
			return true;
		} else {
			return false;
		}
	}

	protected void destroy() {

		if (_pn != null) {
			_pn.remove(this, null);
		}
		discard();
	}

	protected void updateMembers() {

		notifyOfChangeInMembers();
	}

	@Override
	public Fuzzy hasChildren() {

		// TODO Auto-generated method stub
		return Fuzzy.MAYBE;
	}

	@Override
	public void processEvent(Event e) {

		int type = e.type();
		switch (type) {
		case Event.OBJECT_CREATED:

			final DObjectRef o = e.object();
			if (o.referent() != null) {
				add(new DObjectTreeNode(DObjectTreeNode.this, o), null);
			} else {
				o.resolve(new ObjectResolveHandler<DObject>() {
					@Override
					public void resolved(DObject oo) {

						add(new DObjectTreeNode(DObjectTreeNode.this, o), null);
					}
				});
			}
			break;
		case Event.OBJECT_UPDATED:
			_o.reset();
			_o.resolve(new ObjectResolveHandler<DObject>() {
				@Override
				public void resolved(DObject oo) {

					notifyOfModified();
				}
			});
			break;
		case Event.OBJECT_DESTROYED:
			_pn.remove(this, null);
			break;
		}
	}

	@Override
	public boolean matches(Event e) {

		int type = e.type();
		String id = e.object().id();
		String id_ = _o.id();
		if (type == Event.OBJECT_CREATED) {
			String pid = IDUtil.getParentId(id);
			if (IDUtil.isProjectId(id)) {
				pid = null;
			}
			return ObjectUtil.equals(pid, id_);
		} else if (type == Event.OBJECT_UPDATED) {
			return ObjectUtil.equals(id, id_);
		} else if (type == Event.OBJECT_DESTROYED) {
			return ObjectUtil.equals(id, id_)
					|| (id != null && id_ != null && id_.startsWith(id));
		} else {
			return false;
		}
	}

	@Override
	public boolean equals(Object o) {

		if (o == null) {
			return false;
		}
		if (!(o instanceof DObjectTreeNode)) {
			return false;
		}
		return ObjectUtil.equals(path(), ((DObjectTreeNode) o).path());
	}

}
