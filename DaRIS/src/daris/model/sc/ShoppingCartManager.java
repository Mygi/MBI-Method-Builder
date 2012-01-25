package daris.model.sc;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import arc.mf.client.util.Action;
import arc.mf.event.Filter;
import arc.mf.event.Subscriber;
import arc.mf.event.SystemEvent;
import arc.mf.event.SystemEventChannel;
import arc.mf.model.shopping.events.ShoppingCartEvent;
import arc.mf.model.shopping.events.ShoppingEvents;
import arc.mf.object.ObjectMessage;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;
import daris.model.object.PSSDObjectRef;
import daris.model.sc.messages.ShoppingCartContentAdd;
import daris.model.sc.messages.ShoppingCartContentRemove;
import daris.model.sc.messages.ShoppingCartCreate;
import daris.model.sc.messages.ShoppingCartDescribe;
import daris.model.sc.messages.ShoppingCartDestroy;
import daris.model.sc.messages.ShoppingCartOrder;
import daris.model.sc.messages.ShoppingCartOutputRetrieve;
import daris.model.sc.messages.ShoppingCartUpdate;

public class ShoppingCartManager implements Subscriber {

	private static ShoppingCartManager _instance;

	public static ShoppingCartManager instance(boolean reset) {

		if (reset) {
			reset();
		}
		if (_instance == null) {
			_instance = new ShoppingCartManager();
		}
		return _instance;
	}

	public static ShoppingCartManager instance() {

		return instance(false);
	}

	public static void reset() {

		if (_instance != null) {
			SystemEventChannel.remove(_instance);
			_instance._carts.reset();
		}
		_instance = null;
	}

	private List<Filter> _eventFilters;
	private List<ShoppingCartListener> _listeners;
	private ShoppingCartsRef _carts = null;
	private ShoppingCartRef _editableCart = null;

	private ShoppingCartManager() {

		ShoppingEvents.initialize();
		SystemEventChannel.subscribe();
		SystemEventChannel.add(this);
		_eventFilters = new Vector<Filter>();
		_carts = ShoppingCartsRef.instance();
		_carts.reset();
	}

	protected boolean subscribe(ShoppingCartRef cart) {

		if (_eventFilters == null) {
			_eventFilters = new Vector<Filter>();
		}
		return _eventFilters.add(new Filter(
				ShoppingCartEvent.SYSTEM_EVENT_NAME, cart.id(), false));
	}

	protected boolean unsubscribe(ShoppingCartRef cart) {

		if (_eventFilters != null) {
			return _eventFilters.remove(new Filter(
					ShoppingCartEvent.SYSTEM_EVENT_NAME, cart.id(), false));
		}
		return true;
	}

	public void addListener(ShoppingCartListener l) {

		if (_listeners == null) {
			_listeners = new Vector<ShoppingCartListener>();
		}
		_listeners.add(l);
	}

	public void removeListener(ShoppingCartListener l) {

		if (_listeners != null) {
			_listeners.remove(l);
		}
	}

	@Override
	public List<Filter> systemEventFilters() {

		return _eventFilters;
	}

	@Override
	public void process(SystemEvent se) {

		if (se != null) {
			System.out.println(se);
			if (se instanceof ShoppingCartEvent) {
				final ShoppingCartEvent sce = (ShoppingCartEvent) se;
				if (sce.action() == ShoppingCartEvent.Action.MODIFY) {
					_carts.reset();
					_carts.resolve(new ObjectResolveHandler<List<ShoppingCartRef>>() {
						@Override
						public void resolved(List<ShoppingCartRef> carts) {

							if (carts != null) {
								for (ShoppingCartRef cart : carts) {
									if (cart.id().equals(sce.object())) {
										if (cart.status().value() == Status.Value.data_ready) {
											notifyOfDataReady(cart);
											if (cart.destination().type() == Destination.Type.download) {
												cart.download();
											}
										}
									}
								}
							}
						}
					});
				}
			}
		}
	}

	public void carts(ObjectResolveHandler<List<ShoppingCartRef>> rh) {

		_carts.resolve(rh);
	}

	private void notifyOfDeletion(ShoppingCartRef cart) {

		if (_listeners != null) {
			for (ShoppingCartListener l : _listeners) {
				l.deleted(cart);
			}
		}
	}

	private void notifyOfCreation(ShoppingCartRef cart) {

		if (_listeners != null) {
			for (ShoppingCartListener l : _listeners) {
				l.created(cart);
			}
		}
	}

	private void notifyOfUpdate(ShoppingCartRef cart) {

		if (_listeners != null) {
			for (ShoppingCartListener l : _listeners) {
				l.updated(cart);
			}
		}
	}

	private void notifyOfContentChange(ShoppingCartRef cart) {

		if (_listeners != null) {
			for (ShoppingCartListener l : _listeners) {
				l.contentChanged(cart);
			}
		}
	}

	private void notifyOfDataReady(ShoppingCartRef cart) {

		if (_listeners != null) {
			for (ShoppingCartListener l : _listeners) {
				l.dataReady(cart);
			}
		}
	}

	public void create(ShoppingCartRef cart) {

		new ShoppingCartCreate(cart).send(new ObjectMessageResponse<String>() {
			@Override
			public void responded(final String id) {

				_carts.reset();
				_carts.resolve(new ObjectResolveHandler<List<ShoppingCartRef>>() {
					@Override
					public void resolved(List<ShoppingCartRef> carts) {

						if (carts != null) {
							int idx = carts.indexOf(new ShoppingCartRef(id));
							if (idx != -1) {
								ShoppingCartRef c = carts.get(idx);
								notifyOfCreation(c);
							}
						}
					}
				});
			}
		});
	}

	public void destroy(final ShoppingCartRef cart) {

		new ShoppingCartDestroy(cart)
				.send(new ObjectMessageResponse<Boolean>() {

					@Override
					public void responded(Boolean r) {

						if (r) {
							notifyOfDeletion(cart);
						}
					}
				});
	}

	public void destroy(final List<ShoppingCartRef> carts) {

		if (carts.isEmpty()) {
			return;
		}
		new ShoppingCartDestroy(carts)
				.send(new ObjectMessageResponse<Boolean>() {

					@Override
					public void responded(Boolean r) {

						if (r) {
							for (ShoppingCartRef cart : carts) {
								notifyOfDeletion(cart);
							}
						}
					}
				});
	}

	public void commitChanges(final ShoppingCartRef cart) {

		new ShoppingCartUpdate(cart).send(new ObjectMessageResponse<Boolean>() {

			@Override
			public void responded(Boolean r) {

				if (r) {
					refresh(cart);
				}
			}
		});
	}

	public void refresh(final ShoppingCartRef cart) {

		cart.reset();
		cart.resolve(new ObjectResolveHandler<ShoppingCart>() {
			@Override
			public void resolved(ShoppingCart o) {

				assert o != null;
				notifyOfUpdate(cart);
			}
		});
	}

	/**
	 * Add content/object to the specified shopping-cart.
	 * 
	 * @param cart
	 * @param o
	 * @param recursive
	 */
	public void addContentItem(ShoppingCartRef cart, PSSDObjectRef o,
			boolean recursive) {

		ShoppingCartContentAdd msg = new ShoppingCartContentAdd(cart, o,
				recursive);
		sendContentAddMessage(cart, msg);
	}

	/**
	 * Add content/object to the only/editable shopping-cart.
	 * 
	 * @param o
	 * @param recursive
	 */
	public void addContentItem(final PSSDObjectRef o, final boolean recursive) {

		editableShoppingCart(new ObjectResolveHandler<ShoppingCartRef>() {

			@Override
			public void resolved(ShoppingCartRef cart) {

				ShoppingCartManager.this.addContentItem(cart, o, recursive);
			}
		});
	}

	/**
	 * Add contents/objects to the specified shopping-cart.
	 * 
	 * @param cart
	 * @param os
	 * @param recursive
	 */
	public void addContentItems(ShoppingCartRef cart,
			Collection<PSSDObjectRef> os, boolean recursive) {

		ShoppingCartContentAdd msg = new ShoppingCartContentAdd(cart, os,
				recursive);
		sendContentAddMessage(cart, msg);
	}

	/**
	 * Add contents/objects to the only/editable shopping-cart.
	 * 
	 * @param os
	 * @param recursive
	 */
	public void addContentItems(final Collection<PSSDObjectRef> os,
			final boolean recursive) {

		editableShoppingCart(new ObjectResolveHandler<ShoppingCartRef>() {

			@Override
			public void resolved(ShoppingCartRef cart) {

				ShoppingCartManager.this.addContentItems(cart, os, recursive);
			}
		});
	}

	private void sendContentAddMessage(final ShoppingCartRef cart,
			ObjectMessage<Boolean> msg) {

		msg.send(new ObjectMessageResponse<Boolean>() {

			@Override
			public void responded(Boolean r) {

				if (r) {
					cart.refresh(new Action() {
						@Override
						public void execute() {

							notifyOfContentChange(cart);
						}
					});
				}
			}
		});
	}

	/**
	 * Remove content/object from the specified shopping-cart.
	 * 
	 * @param cart
	 * @param o
	 * @param recursive
	 */
	public void removeContentItem(ShoppingCartRef cart, PSSDObjectRef o,
			boolean recursive) {

		ShoppingCartContentRemove msg = new ShoppingCartContentRemove(cart, o,
				recursive);
		sendContentRemoveMessage(cart, msg);
	}

	/**
	 * Remove content/object from the only/editable shopping-cart.
	 * 
	 * @param o
	 * @param recursive
	 */
	public void removeContentItem(final PSSDObjectRef o, final boolean recursive) {

		editableShoppingCart(new ObjectResolveHandler<ShoppingCartRef>() {

			@Override
			public void resolved(ShoppingCartRef cart) {

				ShoppingCartManager.this.removeContentItem(cart, o, recursive);
			}
		});
	}

	/**
	 * Remove contents/objects from the specified shopping-cart.
	 * 
	 * @param cart
	 * @param os
	 * @param recursive
	 */
	public void removeContentItems(ShoppingCartRef cart,
			Collection<PSSDObjectRef> os, boolean recursive) {

		ShoppingCartContentRemove msg = new ShoppingCartContentRemove(cart, os,
				recursive);
		sendContentRemoveMessage(cart, msg);
	}

	/**
	 * Remove contents/objects from the only/editable shopping-cart.
	 * 
	 * @param os
	 * @param recursive
	 */
	public void removeContentItems(final Collection<PSSDObjectRef> os,
			final boolean recursive) {

		editableShoppingCart(new ObjectResolveHandler<ShoppingCartRef>() {

			@Override
			public void resolved(ShoppingCartRef cart) {

				ShoppingCartManager.this
						.removeContentItems(cart, os, recursive);
			}
		});
	}

	public void removeContentItem(ShoppingCartRef cart, ContentItem item) {

		ShoppingCartContentRemove msg = new ShoppingCartContentRemove(cart,
				item.id(), false);
		sendContentRemoveMessage(cart, msg);
	}

	public void removeContentItem(final ContentItem item) {

		editableShoppingCart(new ObjectResolveHandler<ShoppingCartRef>() {

			@Override
			public void resolved(ShoppingCartRef cart) {

				ShoppingCartManager.this.removeContentItem(cart, item);
			}
		});
	}

	public void removeContentItems(ShoppingCartRef cart,
			Collection<ContentItem> items) {

		Collection<String> ids = null;
		if (items != null) {
			ids = new Vector<String>(items.size());
			for (ContentItem item : items) {
				ids.add(item.id());
			}
		}
		ShoppingCartContentRemove msg = new ShoppingCartContentRemove(
				cart.id(), ids, false);
		sendContentRemoveMessage(cart, msg);
	}

	public void removeContentItem(final Collection<ContentItem> items) {

		editableShoppingCart(new ObjectResolveHandler<ShoppingCartRef>() {

			@Override
			public void resolved(ShoppingCartRef cart) {

				ShoppingCartManager.this.removeContentItems(cart, items);
			}
		});
	}

	public void clearContentItems(ShoppingCartRef cart) {

		ShoppingCartContentRemove msg = new ShoppingCartContentRemove(cart,
				true);
		sendContentRemoveMessage(cart, msg);
	}

	public void clearContentItems() {

		editableShoppingCart(new ObjectResolveHandler<ShoppingCartRef>() {

			@Override
			public void resolved(ShoppingCartRef cart) {

				ShoppingCartManager.this.clearContentItems(cart);
			}
		});
	}

	private void sendContentRemoveMessage(final ShoppingCartRef cart,
			ObjectMessage<Boolean> msg) {

		msg.send(new ObjectMessageResponse<Boolean>() {

			@Override
			public void responded(Boolean r) {

				if (r) {
					cart.refresh(new Action() {
						@Override
						public void execute() {

							notifyOfContentChange(cart);
						}
					});
				}
			}
		});
	}

	public void order(final ShoppingCartRef cart) {

		if (cart.status().value() != Status.Value.editable) {
			return;
		}
		cart.contentItems(new ObjectResolveHandler<List<ContentItem>>() {

			@Override
			public void resolved(List<ContentItem> o) {

				if (o != null) {
					if (o.size() > 0) {
						ObjectMessage<Boolean> msg = new ShoppingCartOrder(cart);
						subscribe(cart);
						msg.send(new ObjectMessageResponse<Boolean>() {
							@Override
							public void responded(Boolean r) {

								refresh(cart);
							}
						});
					}
				}
			}
		}, false);
	}

	public void download(final ShoppingCartRef cart) {

		if (cart.status().value() != Status.Value.data_ready) {
			return;
		}
		if (cart.destination().type() != Destination.Type.download) {
			return;
		}
		ObjectMessage<Boolean> msg = new ShoppingCartOutputRetrieve(cart);
		msg.send(new ObjectMessageResponse<Boolean>() {

			@Override
			public void responded(Boolean r) {

				if (r) {
					unsubscribe(cart);
				}
			}
		});
	}

	public void editableShoppingCart(
			final ObjectResolveHandler<ShoppingCartRef> rh) {

		if (_editableCart != null) {
			if (_editableCart.status().value() == Status.Value.editable) {
				rh.resolved(_editableCart);
				return;
			}
		}
		new ShoppingCartDescribe(Status.Value.editable)
				.send(new ObjectMessageResponse<List<ShoppingCartRef>>() {
					@Override
					public void responded(List<ShoppingCartRef> cs) {

						if (cs != null) {
							if (!cs.isEmpty()) {
								_editableCart = cs.get(0);
								rh.resolved(_editableCart);
								return;
							}
						}
						new ShoppingCartCreate()
								.send(new ObjectMessageResponse<String>() {

									@Override
									public void responded(String id) {

										assert id != null;
										new ShoppingCartDescribe(id)
												.send(new ObjectMessageResponse<List<ShoppingCartRef>>() {
													@Override
													public void responded(
															List<ShoppingCartRef> r) {

														assert r != null;
														assert !r.isEmpty();
														_editableCart = r
																.get(0);
														rh.resolved(_editableCart);
													}
												});
									}
								});
					}
				});
	}

	public void clear(final Status.Value status) {

		_carts.resolve(new ObjectResolveHandler<List<ShoppingCartRef>>() {

			@Override
			public void resolved(List<ShoppingCartRef> carts) {

				if (carts != null) {
					if (!carts.isEmpty()) {
						final List<ShoppingCartRef> toDestroy = new Vector<ShoppingCartRef>();
						for (ShoppingCartRef cart : carts) {
							if (status != null) {
								if (status == cart.status().value()) {
									toDestroy.add(cart);
								}
							} else {
								toDestroy.add(cart);
							}
						}
						destroy(toDestroy);
					}
				}
			}
		});
	}

	public void clearAll() {

		clear(null);
	}

	public void clearAllExcept(final Status.Value status) {

		_carts.resolve(new ObjectResolveHandler<List<ShoppingCartRef>>() {
			@Override
			public void resolved(List<ShoppingCartRef> carts) {

				if (carts != null) {
					if (!carts.isEmpty()) {
						final List<ShoppingCartRef> toDestroy = new Vector<ShoppingCartRef>();
						for (ShoppingCartRef cart : carts) {
							if (status != null) {
								if (status != cart.status().value()) {
									toDestroy.add(cart);
								}
							} else {
								toDestroy.add(cart);
							}
						}
						destroy(toDestroy);
					}
				}
			}
		});
	}

}
