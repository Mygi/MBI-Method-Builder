package daris.gui.object.tree;

import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.tree.TreeGUI;
import arc.gui.gwt.widget.tree.TreeGUIEventHandler;
import arc.mf.client.util.Action;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.object.tree.Node;
import daris.gui.object.PSSDObjectGUIRegistry;
import daris.model.object.PSSDObject;
import daris.model.object.PSSDObjectRef;
import daris.model.object.tree.ObjectTree;
import daris.model.object.tree.ObjectTreeNode;
import daris.model.repository.RepositoryRootRef;

public class ObjectTreeGUI extends TreeGUI {

	public ObjectTreeGUI() {

		super(new ObjectTree(), ScrollPolicy.VERTICAL);
		setEventHandler(new TreeGUIEventHandler() {

			private PSSDObjectRef _lastClick = null;
			private boolean _lastClickResponded = false;

			private PSSDObjectRef object(Node n) {

				return (PSSDObjectRef) ((ObjectTreeNode) n).object();
			}

			@Override
			public void clicked(Node n) {

				final PSSDObjectRef o = object(n);
				if (o != null) {
					// Note: since the objectRef associated with the tree node
					// is the incomplete, light-weight one. We need to resolve
					// to get the whole detail for the objectPanel.
					if (_lastClick != null) {
						if (_lastClick != o && !_lastClickResponded) {
							_lastClick.cancel();
						}
					}
					o.startLoading();
					o.resolve(new ObjectResolveHandler<PSSDObject>() {
						@Override
						public void resolved(PSSDObject ro) {

							_lastClickResponded = true;
							o.select(null);
						}
					});
					_lastClick = o;
					_lastClickResponded = false;
				}
			}

			@Override
			public void selected(Node n) {

				if (n != null) {
					onSelected((ObjectTreeNode) n);
				}
			}

			@Override
			public void deselected(Node n) {

				if (n != null) {
					onDeselected((ObjectTreeNode) n);
				}
			}

			@Override
			public void opened(Node n) {

			}

			@Override
			public void closed(Node n) {

			}

			@Override
			public void added(Node n) {

				/*
				 * Auto select the newly added node
				 */
				PSSDObjectRef o = object(n);
				if (o != null) {
					o.select(null);
				}
			}

			@Override
			public void removed(Node n) {

				ObjectTreeNode pn = ((ObjectTreeNode) n).parent();
				if (pn != null) {
					PSSDObjectRef po = object(pn);
					po.select(null);
				}
			}

			@Override
			public void changeInMembers(Node n) {

			}
		});
		setHeight100();
		enableNodeDrag();
		// setShowToolTip(false);
		RepositoryRootRef.instance().refresh(new Action() {

			@Override
			public void execute() {

				select(tree().root());
				RepositoryRootRef.instance().select(null);
			}
		});
		/*
		 * ObjectGUI registry.
		 */
		setObjectRegistry(PSSDObjectGUIRegistry.instance());

	}

	@Override
	protected void added(final Node n) {

		super.added(n);
		if (isOpen(((ObjectTreeNode) n).parent())) {
			select(n);
		}
	}

	@Override
	protected void removed(final Node n) {

		super.removed(n);
		ObjectTreeNode pn = ((ObjectTreeNode) n).parent();
		if (pn != null) {
			select(pn);
		}
	}

	protected void onSelected(ObjectTreeNode n) {

	}

	protected void onDeselected(ObjectTreeNode n) {

	}

	public void enableNodeDescriptionToolTip() {

		((ObjectTree) tree()).enableNodeDescription();
	}

	public void disableNodeDescriptionToolTip() {

		((ObjectTree) tree()).disableNodeDescription();
	}

}
