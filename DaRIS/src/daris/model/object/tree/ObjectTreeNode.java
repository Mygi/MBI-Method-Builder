package daris.model.object.tree;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import arc.gui.image.Image;
import arc.mf.client.util.Fuzzy;
import arc.mf.client.util.ObjectUtil;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.CollectionResolveHandler;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.object.OrderedSetRef;
import arc.mf.object.tree.Container;
import arc.mf.object.tree.Node;
import arc.mf.object.tree.NodeListener;
import arc.mf.object.tree.TreeNodeAddHandler;
import arc.mf.object.tree.TreeNodeContentsHandler;
import arc.mf.object.tree.TreeNodeDescriptionHandler;
import arc.mf.object.tree.TreeNodeRemoveHandler;
import daris.client.Resource;
import daris.client.model.IDUtil;
import daris.model.dataset.DataSetRef;
import daris.model.object.PSSDObject;
import daris.model.object.PSSDObjectRef;
import daris.model.object.PSSDObjectRefFactory;
import daris.model.project.ProjectRef;
import daris.model.repository.RepositoryRootRef;

public class ObjectTreeNode implements Container {

	public static final Image ICON_FOLDER = new Image(Resource.INSTANCE
			.folderGrey16().getSafeUri().asString(), Resource.INSTANCE
			.folderGreyOpen16().getWidth(), Resource.INSTANCE
			.folderGreyOpen16().getHeight());

	public static final Image ICON_FOLDER_OPEN = new Image(Resource.INSTANCE
			.folderGreyOpen16().getSafeUri().asString(), Resource.INSTANCE
			.folderGreyOpen16().getWidth(), Resource.INSTANCE
			.folderGreyOpen16().getHeight());

	public static final Image ICON_DOCUMENT = new Image(Resource.INSTANCE
			.document16().getSafeUri().asString(), Resource.INSTANCE
			.document16().getWidth(), Resource.INSTANCE.document16()
			.getHeight());

	public static class ChildrenRef extends OrderedSetRef<PSSDObjectRef> {

		private PSSDObjectRef _po;

		public ChildrenRef(PSSDObjectRef parent) {

			_po = parent;
		}

		@Override
		protected void containsServiceArgs(XmlStringWriter w,
				PSSDObjectRef entry) {

			if (_po.proute() != null) {
				w.add("id", new String[] { "proute", _po.proute() }, entry.id());
			} else {
				w.add("id", entry.id());
			}
		}

		@Override
		protected String containsServiceName() {

			return "om.pssd.object.exists";
		}

		@Override
		protected boolean contains(XmlElement xe, PSSDObjectRef entry)
				throws Throwable {

			if (xe != null) {
				if (xe.value("exists") != null) {
					if (xe.value("exists").equals("true")) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		protected boolean isMember(PSSDObjectRef entry) {

			if (_po instanceof RepositoryRootRef && entry instanceof ProjectRef
			/* && entry.id().startsWith(_ref.id()) */) {
				return true;
			}
			if (ObjectUtil.equals(_po.proute(), entry.proute())
					&& IDUtil.isParent(_po.id(), entry.id())) {
				return true;
			}
			return false;
		}

		@Override
		protected void resolveServiceArgs(XmlStringWriter w, long start,
				int size, boolean count) {

			String id = _po.id();
			if (IDUtil.getIdDepth(id) >= IDUtil.PROJECT_ID_DEPTH) {
				w.add("id", id);
			}
			w.add("idx", start + 1);
			w.add("size", size);
			// w.add("isleaf", true);
		}

		@Override
		protected String resolveServiceName() {

			return "om.pssd.collection.member.list";
		}

		protected PSSDObjectRef instantiate(XmlElement oe) throws Throwable {

			return PSSDObjectRefFactory.instantiate(oe);
		}

		@Override
		protected String referentTypeName() {

			return "pssd-object-collection";
		}

		@Override
		protected String[] objectElementNames() {

			return new String[] { "object" };
		}

		@Override
		public int defaultPagingSize() {

			return 200;
		}
	}

	private ChildrenRef _childrenRef;

	private ObjectTreeNode _pn;

	private PSSDObjectRef _o;

	private List<NodeListener> _listeners;

	private ObjectTree _tree;

	private ObjectTreeNode(ObjectTree tree, ObjectTreeNode pn, PSSDObjectRef o) {

		_pn = pn;
		_o = o;
		// _o.refresh(null);
		_tree = tree;
		_tree.addToMap(this);
		_childrenRef = new ChildrenRef(o);
	}

	protected ObjectTree tree() {

		return _tree;
	}

	public ObjectTreeNode parent() {

		return _pn;
	}

	@Override
	public String type() {

		return _o.referentTypeName();
	}

	@Override
	public Image icon() {

		if (_o instanceof DataSetRef) {
			return ICON_DOCUMENT;
		}
		return ICON_FOLDER;
	}

	@Override
	public String name() {

		String id;
		if (_o instanceof RepositoryRootRef || _o instanceof ProjectRef) {
			id = _o.id();
		} else {
			id = IDUtil.getLastSection(_o.id());
		}
		if (_o.name() != null) {
			return id + ": " + _o.name();
		} else {
			return id;
		}
	}

	@Override
	public String path() {

		return _o.id();
	}

	@Override
	public void description(final TreeNodeDescriptionHandler dh) {

		if (!_tree.isNodeDescriptionEnabled()) {
			return;
		}
		_o.resolve(new ObjectResolveHandler<PSSDObject>() {

			@Override
			public void resolved(PSSDObject o) {

				if (o != null) {
					if (dh != null) {
						String desc = "<table border=1 cellpadding=3 cellspacing=1><tr><td><b>type:</b></td><td>"
								+ o.typeName() + "</td></tr>";
						desc += "<tr><td><b>id:</b></td><td>" + o.id()
								+ "</td></tr>";
						if (o.proute() != null) {
							desc += "<tr><td><b>proute:</b></td><td>"
									+ o.proute() + "</td></tr>";
						}
						if (o.name() != null) {
							desc += "<tr><td><b>name:</b></td><td>" + o.name()
									+ "</td></tr>";
						}
						if (o.description() != null) {
							desc += "<tr><td><b>description:</b></td><td>"
									+ o.description() + "</td></tr>";
						}
						desc += "</table>";
						dh.description(desc);
					}
				}
			}
		});
	}

	@Override
	public Object object() {

		return _o;
	}

	@Override
	public boolean readOnly() {

		return false;
	}

	@Override
	public Object subscribe(boolean descend, final NodeListener l) {

		if (_listeners == null) {
			_listeners = new Vector<NodeListener>();
		}
		_listeners.add(l);
		return l;
	}

	@Override
	public void unsubscribe(Object key) {

		_listeners.remove((NodeListener) key);
	}

	@Override
	public void discard() {

		if (_listeners != null) {
			_listeners.clear();
		}
	}

	@Override
	public Image openIcon() {

		if (_o instanceof DataSetRef) {
			return ICON_DOCUMENT;
		}
		return ICON_FOLDER_OPEN;
	}

	@Override
	public void contents(final long start, final long end,
			final TreeNodeContentsHandler ch) {

		_childrenRef.resolve(start, end,
				new CollectionResolveHandler<PSSDObjectRef>() {

					@Override
					public void resolved(List<PSSDObjectRef> childRefs)
							throws Throwable {

						if (childRefs == null) {
							ch.loaded(start, end, 0, null);
						}
						long total = childRefs.size();
						if (start > 0 || end < childRefs.size()) {
							childRefs = childRefs.subList((int) start,
									(int) end);
						}
						Collections.sort(childRefs);
						List<Node> childNodes = new Vector<Node>(childRefs
								.size());
						for (PSSDObjectRef childRef : childRefs) {
							childNodes.add(new ObjectTreeNode(_tree,
									ObjectTreeNode.this, childRef));
						}
						ch.loaded(start, end, total, childNodes);
					}
				});
	}

	@Override
	public void add(Node n, TreeNodeAddHandler ah) {

		ObjectTreeNode node = (ObjectTreeNode) n;
		notifyOfChildAdd(node);
		if (ah != null) {
			ah.added(n);
		}
	}

	@Override
	public void remove(Node n, TreeNodeRemoveHandler rh) {

		ObjectTreeNode node = (ObjectTreeNode) n;
		notifyOfChildRemove(node);
		if (rh != null) {
			rh.removed(n);
		}
	}

	public void update() {

		notifyOfUpdate();
	}

	private void notifyOfUpdate() {

		if (_listeners != null) {
			for (NodeListener l : _listeners) {
				l.modified(this);
			}
		}
	}

	private void notifyOfChildRemove(ObjectTreeNode cn) {

		if (_listeners != null) {
			for (NodeListener l : _listeners) {
				l.removed(this, cn);
			}
		}
	}

	private void notifyOfChildAdd(ObjectTreeNode cn) {

		if (_listeners != null) {
			for (NodeListener l : _listeners) {
				l.added(this, cn, -1);
			}
		}
	}

	public void refresh(boolean recursive) {

		_tree.refreshNode(this, recursive);
	}

	@Override
	public boolean sorted() {

		return true;
	}

	static ObjectTreeNode create(ObjectTree tree, ObjectTreeNode pn,
			PSSDObjectRef o) {

		return new ObjectTreeNode(tree, pn, o);
	}

	@Override
	public Fuzzy hasChildren() {

		return Fuzzy.MAYBE;
	}
}
