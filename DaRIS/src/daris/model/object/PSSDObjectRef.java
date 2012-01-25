package daris.model.object;

import arc.mf.client.util.Action;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.ObjectUtil;
import arc.mf.client.util.ThrowableUtil;
import arc.mf.client.xml.XmlDoc;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectRef;
import arc.mf.object.ObjectResolveHandler;
import daris.client.model.IDUtil;
import daris.model.Model;
import daris.model.object.messages.ObjectCreate;
import daris.model.object.messages.ObjectDescribe;
import daris.model.object.messages.ObjectDestroy;
import daris.model.object.messages.ObjectExists;
import daris.model.object.messages.ObjectUpdate;

public abstract class PSSDObjectRef extends ObjectRef<PSSDObject> implements Comparable<PSSDObjectRef> {

	private String _id;

	private String _assetId;

	private int _version = 0;

	private String _name;

	private String _description;

	private String _proute;

	private boolean _isleaf;

	private boolean _editable;

	private XmlElement _vmeta = null;

	private XmlElement _emeta = null;

	private PSSDObjectRefSetRef _childrenRef;

	private boolean _foredit = false;

	private XmlElement _metaToSave = null;

	public XmlElement metaToSave() {

		return _metaToSave;
	}

	public void setMetaToSave(XmlElement metaToSave) {

		_metaToSave = metaToSave;
	}

	public void setMetaToSave(String metaToSave) {

		try {
			setMetaToSave(XmlDoc.parse(metaToSave));
		} catch (Throwable t) {
			ThrowableUtil.rethrowAsUnchecked(t);
		}
	}

	public void setMetaToSave(XmlStringWriter w) {

		setMetaToSave(w.document());
	}

	protected PSSDObjectRef(PSSDObject o) {

		this(o.proute(), o.id(), o.name(), o.description(), o.isleaf());
	}

	protected PSSDObjectRef(String proute, String id, String name, String description, boolean isleaf) {

		_proute = proute;
		_id = id;
		_name = name;
		_description = description;
		_isleaf = isleaf;
		_childrenRef = new PSSDObjectRefSetRef(this);
	}

	protected PSSDObjectRef(XmlElement oe) {

		parse(oe);
		_childrenRef = new PSSDObjectRefSetRef(this);
	}

	protected void parse(XmlElement oe) {

		_proute = oe.value("id/@proute");
		_id = oe.value("id");
		_name = oe.value("name");
		_description = oe.value("description");
		_assetId = oe.value("id/@asset");
		try {
			_version = oe.intValue("@version", 0);
			_isleaf = oe.booleanValue("isleaf", false);
		} catch (Throwable t) {
			throw new AssertionError(t.getMessage());
		}
		XmlElement me = oe.element("meta");
		if (me != null) {
			if (me.element("metadata") != null) {
				_emeta = me;
			} else {
				_vmeta = me;
			}
		}

		_metaToSave = null;
	}

	public PSSDObjectRefSetRef childrenRef() {

		return _childrenRef;
	}

	/**
	 * 
	 * @param child
	 *            The uncommitted object to hold the values.
	 * @param rh
	 */

	public void createChild(PSSDObjectRef child, final ObjectMessageResponse<PSSDObjectRef> rh) {

		ObjectCreate msg = getChildCreateMessage(child);
		if (msg != null) {
			msg.send(new ObjectMessageResponse<String>() {

				@Override
				public void responded(String id) {

					if (id == null) {
						rh.responded(null);
					} else {
						new ObjectDescribe(id, _proute, false).send(new ObjectMessageResponse<XmlElement>() {

							@Override
							public void responded(XmlElement oe) {

								if (oe == null) {
									rh.responded(null);
								} else {
									PSSDObjectRef cref = PSSDObjectRefFactory.instantiate(oe);
									Model.fireObjectCreatedEvent(PSSDObjectRef.this, cref);
									rh.responded(cref);
								}
							}
						});
					}
				}
			});
		}
	}

	public String description() {

		return _description;
	}

	/**
	 * Asynchronous method to to destroy the object on the server side.
	 * 
	 * @param action
	 *            the action will be done when the result is returned. Can be
	 *            null if no action is required.
	 */
	public void destroy(final Action action) {

		final PSSDObjectRef self = this;
		exists(new ObjectMessageResponse<Boolean>() {

			@Override
			public void responded(Boolean exists) {

				if (exists) {
					// still exists, delete it
					new ObjectDestroy(PSSDObjectRef.this).send(new ObjectMessageResponse<Boolean>() {

						@Override
						public void responded(Boolean r) {

							Model.fireObjectDeletedEvent(self);
							if (action != null) {
								action.execute();
							}
						}
					});
				} else {
					// does not exist any more, notify the local listeners.
					Model.fireObjectDeletedEvent(self);
					if (action != null) {
						action.execute();
					}
				}
			}
		});
	}

	public boolean equals(Object o) {

		if (o instanceof PSSDObjectRef) {
			PSSDObjectRef ro = (PSSDObjectRef) o;
			if (ObjectUtil.equals(ro.proute(), _proute) && ObjectUtil.equals(ro.id(), _id)) {
				return true;
			}
		}
		return false;
	}

	public int hashCode() {

		return (_id + _proute).hashCode();
	}

	/**
	 * Asynchronous method to check if the object still exists on the server
	 * side.
	 * 
	 * @param rh
	 *            the response handler, called when the result is returned.
	 */
	public void exists(ObjectMessageResponse<Boolean> rh) {

		new ObjectExists(this).send(rh);
	}

	public String id() {

		return _id;
	}

	public String assetId() {

		return _assetId;
	}

	@Override
	public String idToString() {

		return _id;
	}

	public boolean isleaf() {

		return _isleaf;
	}

	public String name() {

		return _name;
	}

	public String proute() {

		return _proute;
	}

	public int version() {

		return _version;
	}

	public boolean editable() {

		return _editable;
	}

	public void metaForView(ObjectResolveHandler<XmlElement> rh) {

		metaForView(rh, _foredit);
	}

	public void metaForView(final ObjectResolveHandler<XmlElement> rh, boolean refresh) {

		if (refresh) {
			reset();
		} else {
			if (_vmeta != null) {
				rh.resolved(_vmeta);
				return;
			}
		}
		setForEdit(false);
		resolve(new ObjectResolveHandler<PSSDObject>() {

			@Override
			public void resolved(PSSDObject o) {

				_vmeta = o.metaForView();
				rh.resolved(_vmeta);
			}
		});
	}

	public void metaForEdit(ObjectResolveHandler<XmlElement> rh) {

		metaForEdit(rh, !_foredit);
	}

	public void metaForEdit(final ObjectResolveHandler<XmlElement> rh, boolean refresh) {

		if (refresh) {
			reset();
		} else {
			if (_emeta != null) {
				rh.resolved(_emeta);
				return;
			}
		}
		setForEdit(true);
		resolve(new ObjectResolveHandler<PSSDObject>() {

			@Override
			public void resolved(PSSDObject o) {

				_emeta = o.metaForEdit();
				rh.resolved(_emeta);
			}
		});
	}

	@Override
	public String referentTypeName() {

		return "object";
	}

	/**
	 * The method to check if the object still exists on the server side. If it
	 * does, reload it from the server and notify all the listeners by calling
	 * objectUpdated(); if it does not exist any more, notify all the listeners
	 * by calling objectDestroyed();
	 * 
	 * @param action
	 *            post action. Can be null if no post action required.
	 */
	public void refresh(final Action action) {

		final PSSDObjectRef self = this;
		exists(new ObjectMessageResponse<Boolean>() {

			public void responded(Boolean exists) {

				if (exists) {
					PSSDObjectRef.this.reset();
					resolve(new ObjectResolveHandler<PSSDObject>() {

						public void resolved(PSSDObject o) {

							Model.fireObjectUpdatedEvent(self);
							if (action != null) {
								action.execute();
							}
						}
					});
				} else {
					Model.fireObjectDeletedEvent(self);
					// TODO: check if action should be executed. Since the
					// object does not exist, if the action includes operations
					// on the destroyed object, it may cause problem.
					// if(action!=null) {
					// action.execute();
					// }
				}
			}
		});
	}

	public void refreshChildren(final Action action) {

		_childrenRef.reset();
		_childrenRef.resolve(new ObjectResolveHandler<PSSDObjectRefSet>() {

			@Override
			public void resolved(PSSDObjectRefSet children) {

				Model.fireChildrenUpdatedEvent(PSSDObjectRef.this, children);
				if (action != null) {
					action.execute();
				}
			}
		});
	}

	public void resolve(ObjectResolveHandler<PSSDObject> rh, boolean foredit) {

		if (foredit != _foredit) {
			setForEdit(foredit);
			reset();
		}
		resolve(rh);
	}

	/**
	 * The method to select the object locally.
	 * 
	 * @param action
	 *            post action. Can be null if no post action required.
	 */
	public void select(final Action action) {

		Model.fireObjectSelectedEvent(this);
		if (action != null) {
			action.execute();
		}
	}

	// TODO: remove
	public void startLoading() {

		Model.fireObjectLoadingEvent(this);
	}

	public void setForEdit(boolean foredit) {

		if (foredit != _foredit) {
			reset();
		}
		_foredit = foredit;
	}

	/**
	 * The method to update the object with specified arguments.
	 * 
	 * @param action
	 *            the post action. Can be null if no post action is required.
	 */
	public void update(final ActionListener al) {

		final PSSDObjectRef self = this;
		ObjectUpdate msg = getUpdateMessage();
		if (msg != null) {
			msg.send(new ObjectMessageResponse<Boolean>() {

				@Override
				public void responded(Boolean succeeded) {

					if (succeeded == null) {
						al.executed(false);
						return;
					}
					if (!succeeded) {
						al.executed(false);
						return;
					}
					PSSDObjectRef.this.reset();
					PSSDObjectRef.this.setForEdit(false);
					// TODO: validate to see whether optimisation is required.
					// Since the listeners may call refresh().
					resolve(new ObjectResolveHandler<PSSDObject>() {

						public void resolved(PSSDObject o) {

							Model.fireObjectUpdatedEvent(self);
							al.executed(true);
						}
					});
				}
			});
		}
	}

	/**
	 * 
	 * @param child
	 *            the uncommitted object to hold the values.
	 * @return
	 */
	protected abstract ObjectCreate getChildCreateMessage(PSSDObjectRef child);

	protected abstract ObjectUpdate getUpdateMessage();

	@Override
	protected PSSDObject instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			XmlElement oe = xe.element("object");
			if (oe != null) {
				parse(oe);
				return PSSDObjectFactory.instantiate(oe);
			}
		}
		return null;
	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		w.add("id", new String[] { "proute", _proute }, _id);
		w.add("foredit", _foredit);
		w.add("isleaf", true);
	}

	@Override
	protected String resolveServiceName() {

		return "om.pssd.object.describe";
	}

	public void setDescription(String description) {

		_description = description;
	}

	public void setId(String id) {

		_id = id;
	}

	public void setName(String name) {

		_name = name;
	}

	public void setProute(String proute) {

		_proute = proute;
	}

	@Override
	public int compareTo(PSSDObjectRef o) {

		return IDUtil.compare(_id, o.id());
	}

	public String toString() {

		return referentTypeName() + ": " + id();
	}

}
