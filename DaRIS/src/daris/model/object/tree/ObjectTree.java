package daris.model.object.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import arc.gui.image.Image;
import arc.mf.client.util.Action;
import arc.mf.object.tree.Container;
import arc.mf.object.tree.Tree;
import daris.model.Model;
import daris.model.object.PSSDObjectRef;
import daris.model.object.messages.ObjectCreate;
import daris.model.object.messages.ObjectUpdate;
import daris.model.project.ProjectRef;
import daris.model.repository.RepositoryRootRef;

public class ObjectTree implements Tree, Model.EventHandler {

	private ObjectTreeNode _root;

	private Set<PSSDObjectRef> _projects;

	private TreeMap<PSSDObjectRef, ObjectTreeNode> _map;

	private PSSDObjectRef _selectedObject = null;

	private boolean _enableNodeDescription = false;

	public ObjectTree() {

		_projects = new TreeSet<PSSDObjectRef>();
		_map = new TreeMap<PSSDObjectRef, ObjectTreeNode>();
		_root = ObjectTreeNode.create(ObjectTree.this, null,
				RepositoryRootRef.instance());
		((RepositoryRootRef) _root.object()).refresh(new Action() {

			@Override
			public void execute() {

				addToMap(_root);
			}
		});
		Model.subscribe(this);
	}

	public boolean isNodeDescriptionEnabled() {

		return _enableNodeDescription;
	}

	public void enableNodeDescription() {

		_enableNodeDescription = true;
	}

	public void disableNodeDescription() {

		_enableNodeDescription = false;
	}

	public PSSDObjectRef getSelectedObject() {

		return _selectedObject;
	}

	public ObjectTreeNode getSelectedNode() {

		return getNode(_selectedObject);
	}

	void addToMap(ObjectTreeNode n) {

		_map.put((PSSDObjectRef) n.object(), n);
		if (n.object() instanceof ProjectRef) {
			_projects.add((ProjectRef) n.object());
		}
	}

	public ObjectTreeNode getNode(PSSDObjectRef key) {

		if (key instanceof RepositoryRootRef) {
			return _root;
		}
		return _map.get(key);
	}

	public PSSDObjectRef getObject(PSSDObjectRef key) {

		ObjectTreeNode n = getNode(key);
		if (n != null) {
			return (PSSDObjectRef) n.object();
		}
		return null;
	}

	public Set<PSSDObjectRef> getChildObjects(PSSDObjectRef key) {

		if (key instanceof RepositoryRootRef) {
			return _projects;
		}
		SortedMap<PSSDObjectRef, ObjectTreeNode> sm = getChildMap(key);
		return sm.keySet();
	}

	private Collection<ObjectTreeNode> getProjectNodes() {

		Vector<ObjectTreeNode> projectNodes = new Vector<ObjectTreeNode>(
				_projects.size());
		for (PSSDObjectRef o : _projects) {
			projectNodes.add(_map.get(o));
		}
		return projectNodes;
	}

	public Collection<ObjectTreeNode> getChildNodes(PSSDObjectRef key) {

		if (key instanceof RepositoryRootRef) {
			return getProjectNodes();
		}
		SortedMap<PSSDObjectRef, ObjectTreeNode> sm = getChildMap(key);
		return sm.values();
	}

	private SortedMap<PSSDObjectRef, ObjectTreeNode> getChildMap(
			PSSDObjectRef key) {

		// TODO: test
		PSSDObjectRef fco = new PSSDObjectRef(key.proute(), key.id() + ".1",
				null, null, false) {

			@Override
			protected ObjectCreate getChildCreateMessage(PSSDObjectRef child) {

				return null;
			}

			@Override
			protected ObjectUpdate getUpdateMessage() {

				return null;
			}
		};

		PSSDObjectRef lco = new PSSDObjectRef(key.proute(), key.id() + "."
				+ Integer.MAX_VALUE, null, null, false) {

			@Override
			protected ObjectCreate getChildCreateMessage(PSSDObjectRef child) {

				return null;
			}

			@Override
			protected ObjectUpdate getUpdateMessage() {

				return null;
			}
		};

		return _map.subMap(fco, lco);
	}

	@Override
	public Image icon() {

		return null;
	}

	@Override
	public Container root() {

		return _root;
	}

	@Override
	public boolean readOnly() {

		return false;
	}

	@Override
	public void setReadOnly(boolean readOnly) {

	}

	@Override
	public void discard() {

		_root = null;
		_map.clear();
		_projects.clear();
		Model.unsubscribe(this);
	}

	@Override
	public void handleEvent(Model.Event e) {

		switch (e.type()) {
		case Model.Event.OBJECT_CREATED:
			/*
			 * Add node
			 */
			addNode(e.parent(), e.object());
			break;
		case Model.Event.OBJECT_DELETED:
			/*
			 * Remove node
			 */
			removeNode(e.object());
			break;
		case Model.Event.OBJECT_SELECTED:
			if (_selectedObject != null) {
				_selectedObject.cancel();
			}
			_selectedObject = e.object();
			break;
		case Model.Event.OBJECT_UPDATED:
			updateNode(e.object());
			break;
		case Model.Event.CHILDREN_UPDATED:
			updateChildren(e.object(), e.children());
			break;
		default:
			break;
		}
	}

	public void refreshSelectedNode(boolean recursive) {

		ObjectTreeNode n = getSelectedNode();
		if (n != null) {
			refreshNode(n, recursive);
		}
	}

	public void refreshNode(ObjectTreeNode n, boolean recursive) {

		refreshObject((PSSDObjectRef) n.object(), recursive);
	}

	public void refreshSelectedObject(boolean recursive) {

		PSSDObjectRef o = getSelectedObject();
		if (o != null) {
			refreshObject(o, recursive);
		}
	}

	public void refreshObject(PSSDObjectRef o, final boolean recursive) {

		final PSSDObjectRef object = getObject(o);
		if (object != null) {
			object.refresh(new Action() {

				@Override
				public void execute() {

					object.refreshChildren(recursive ? new Action() {

						@Override
						public void execute() {

							for (PSSDObjectRef oo : getChildObjects(object)) {
								refreshObject(oo, recursive);
							}
						}
					} : null);
				}
			});
		}
	}

	private void addNode(PSSDObjectRef po, PSSDObjectRef o) {

		ObjectTreeNode pn = getNode(po);
		if (pn != null) {
			pn.add(ObjectTreeNode.create(this, pn, o), null);
		}
	}

	private void removeNode(PSSDObjectRef o) {

		ObjectTreeNode n = getNode(o);
		if (n != null) {
			ObjectTreeNode pn = n.parent();
			if (pn != null) {
				pn.remove(n, null);
			}
			Set<PSSDObjectRef> cos = getChildObjects(o);
			for (PSSDObjectRef co : cos) {
				removeNode(co);
			}
			_map.remove(o);
			if (o instanceof ProjectRef) {
				_projects.remove(o);
			}
			// Below is needed for other components listening to this object.
			Model.fireObjectDeletedEvent(o);
		}
	}

	private void updateNode(PSSDObjectRef o) {

		// TODO: this method need to be tested.
		PSSDObjectRef object = getObject(o);
		if (o != object) {
			/*
			 * If the object are not the same (even if they are equal), you need
			 * to refresh the one associated with the node.
			 */
			object.refresh(null);
		} else {
			ObjectTreeNode n = getNode(o);
			if (n != null) {
				n.update();
			}
		}
	}

	private void updateChildren(PSSDObjectRef o, List<PSSDObjectRef> cos) {

		if (o == null) {
			return;
		}
		ObjectTreeNode n = getNode(o);
		if (n != null) {
			Set<PSSDObjectRef> childObjects = getChildObjects(o);
			if (cos == null) {
				for (PSSDObjectRef co : childObjects) {
					removeNode(co);
				}
			} else {
				for (PSSDObjectRef co : cos) {
					if (childObjects.contains(co)) {
						updateNode(getObject(co));
					} else {
						addNode(o, co);
					}
				}
				// objects to be deleted.
				ArrayList<PSSDObjectRef> dcos = new ArrayList<PSSDObjectRef>();
				for (PSSDObjectRef co : childObjects) {
					if (!cos.contains(co)) {
						dcos.add(co);
					}
				}
				for (PSSDObjectRef dco : dcos) {
					removeNode(dco);
				}
			}
		}
	}
}
