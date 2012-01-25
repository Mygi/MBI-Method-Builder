package daris.client.model.sc;

import java.util.List;
import java.util.Vector;

import arc.mf.client.util.ObjectUtil;
import arc.mf.event.Filter;
import arc.mf.event.Subscriber;
import arc.mf.event.SystemEvent;
import arc.mf.event.SystemEventChannel;
import arc.mf.model.shopping.events.ShoppingCartEvent;
import arc.mf.model.shopping.events.ShoppingEvent;
import arc.mf.model.shopping.events.ShoppingEvents;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;
import daris.client.model.object.DObjectRef;
import daris.client.model.sc.messages.ShoppingCartContentAdd;
import daris.client.model.sc.messages.ShoppingCartContentClear;
import daris.client.model.sc.messages.ShoppingCartContentRemove;
import daris.client.model.sc.messages.ShoppingCartCreate;
import daris.client.model.sc.messages.ShoppingCartDestroy;
import daris.client.model.sc.messages.ShoppingCartList;
import daris.client.model.sc.messages.ShoppingCartOrder;
import daris.client.model.sc.messages.ShoppingCartOutputRetrieve;

public class ShoppingCartManager {

	public static interface Listener {

		void activated(ShoppingCartRef sc);

		void collectionModified();

		void contentModified(ShoppingCartRef sc);

		void created(ShoppingCartRef sc);

		void deactivated(ShoppingCartRef sc);

		void destroyed(ShoppingCartRef sc);

		void modified(ShoppingCartRef sc);
	}

	public static interface Logger {

		public static enum LogType {
			error, info
		}

		void log(LogType type, String msg);
	}

	private static ShoppingCartRef _asc;
	private static List<Listener> _listeners;
	private static Subscriber _subscriber;
	private static Logger _logger;

	private static boolean _initialized;

	public static void initialize() {
		if (!_initialized) {
			_initialized = true;
			_subscriber = new Subscriber() {

				private List<Filter> _filters;

				@Override
				public void process(SystemEvent se) {
					if (se instanceof ShoppingEvent) {
						ShoppingEvent sse = (ShoppingEvent) se;
						switch (((ShoppingEvent) se).action()) {
						case CREATE:
							notifyOfCreate(new ShoppingCartRef(sse.cartId()));
							break;
						case DESTROY:
							notifyOfDestroy(new ShoppingCartRef(sse.cartId()));
							if (_asc != null) {
								if (_asc.scid() == sse.cartId()) {
									// if the active shopping cart is destroyed,
									// make a new one.
									deactivate(_asc);
									updateActiveShoppingCart();
								}
							}
							break;
						}
					} else if (se instanceof ShoppingCartEvent) {
						ShoppingCartEvent sce = (ShoppingCartEvent) se;
						switch (((ShoppingCartEvent) se).action()) {
						case MODIFY:
							final ShoppingCartRef sc = new ShoppingCartRef(
									sce.cartId());
							notifyOfModify(sc);
							sc.resolve(new ObjectResolveHandler<ShoppingCart>() {

								@Override
								public void resolved(ShoppingCart c) {
									if (!Status.editable.equals(c.status())) {
										if (isActive(c)) {
											// if the activate shopping cart
											// status has been changed from
											// editable to something else.
											// we need to make a new
											// editable and active shopping
											// cart.
											deactivate(_asc);
											updateActiveShoppingCart();
										}
										if (Status.data_ready.equals(c.status())
												&& DeliveryMethod.download
														.equals(c.destination()
																.method())) {
											download(sc);
										}
									}
								}
							});
							break;
						}
					}
				}

				@Override
				public List<Filter> systemEventFilters() {
					if (_filters == null) {
						_filters = new Vector<Filter>(2);
						_filters.add(new Filter(ShoppingEvent.SYSTEM_EVENT_NAME));
						_filters.add(new Filter(
								ShoppingCartEvent.SYSTEM_EVENT_NAME));
					}
					return _filters;
				}
			};
			ShoppingEvents.initialize();
			SystemEventChannel.subscribe();
			SystemEventChannel.add(_subscriber);
			updateActiveShoppingCart();
		}
	}

	public static boolean addListener(Listener l) {
		if (l == null) {
			return false;
		}
		if (_listeners == null) {
			_listeners = new Vector<Listener>();
		}
		return _listeners.add(l);
	}

	public static boolean removeListener(Listener l) {
		if (l == null) {
			return false;
		}
		if (_listeners == null) {
			return true;
		}
		return _listeners.remove(l);
	}

	private static void updateActiveShoppingCart() {
		new ShoppingCartList(Status.editable)
				.send(new ObjectMessageResponse<List<ShoppingCartRef>>() {

					@Override
					public void responded(List<ShoppingCartRef> scs) {
						if (scs != null) {
							if (!scs.isEmpty()) {
								activate(scs.get(0));
								return;
							}
						}
						new ShoppingCartCreate(null, null, null, null)
								.send(new ObjectMessageResponse<ShoppingCartRef>() {

									@Override
									public void responded(ShoppingCartRef sc) {
										if (sc != null) {
											activate(sc);
										}
									}
								});
					}
				});
	}

	public static ShoppingCartRef activeShoppingCart() {
		return _asc;
	}

	public static boolean isActive(ShoppingCartRef sc) {
		if (sc == null) {
			return false;
		}
		return ObjectUtil.equals(_asc, sc);
	}

	public static boolean isActive(ShoppingCart sc) {
		if (_asc == null || sc == null) {
			return false;
		}
		return _asc.scid() == sc.scid();
	}

	private static void activate(ShoppingCartRef sc) {
		if (sc != null) {
			if (!ObjectUtil.equals(_asc, sc)) {
				deactivate(_asc);
				_asc = sc;
				notifyOfActivate(_asc);
			}
		}
	}

	private static void deactivate(ShoppingCartRef sc) {
		if (sc != null) {
			if (ObjectUtil.equals(_asc, sc)) {
				_asc = null;
				notifyOfDeactivate(sc);
			}
		}
	}

	private static void logError(String msg) {
		if (_logger != null) {
			_logger.log(Logger.LogType.error, msg);
		}
	}

	private static void logInfo(String msg) {
		if (_logger != null) {
			_logger.log(Logger.LogType.info, msg);
		}
	}

	private static void notifyOfCreate(ShoppingCartRef sc) {
		if (_listeners != null) {
			for (Listener l : _listeners) {
				l.created(sc);
			}
		}
		logInfo("Shopping cart " + sc.scid() + ": created.");
	}

	private static void notifyOfDestroy(ShoppingCartRef sc) {
		if (_listeners != null) {
			for (Listener l : _listeners) {
				l.destroyed(sc);
			}
		}
		logInfo("Shopping cart " + sc.scid() + ": destroyed.");
	}

	private static void notifyOfModify(ShoppingCartRef sc) {
		if (_listeners != null) {
			for (Listener l : _listeners) {
				l.modified(sc);
			}
		}
		logInfo("Shopping cart " + sc.scid() + ": modified.");
	}

	private static void notifyOfActivate(ShoppingCartRef sc) {
		if (_listeners != null) {
			for (Listener l : _listeners) {
				l.activated(sc);
			}
		}
		logInfo("Shopping cart " + sc.scid() + ": activated.");
	}

	private static void notifyOfDeactivate(ShoppingCartRef sc) {
		if (_listeners != null) {
			for (Listener l : _listeners) {
				l.deactivated(sc);
			}
		}
		logInfo("Shopping cart " + sc.scid() + ": deactivated.");
	}

	public static void notifyOfCollectionModify() {
		if (_listeners != null) {
			for (Listener l : _listeners) {
				l.collectionModified();
			}
		}
	}

	public static void notifyOfContentModify(ShoppingCartRef sc) {
		if (_listeners != null) {
			for (Listener l : _listeners) {
				l.contentModified(sc);
			}
		}
	}

	public static void addContent(final ShoppingCartRef sc, DObjectRef o) {

		new ShoppingCartContentAdd(sc, o)
				.send(new ObjectMessageResponse<Boolean>() {

					@Override
					public void responded(Boolean r) {
						if (r != null) {
							if (r) {
								notifyOfContentModify(sc);
							}
						}
					}
				});
	}

	public static void addContent(final ShoppingCartRef sc, List<DObjectRef> os) {

		new ShoppingCartContentAdd(sc, os)
				.send(new ObjectMessageResponse<Boolean>() {

					@Override
					public void responded(Boolean r) {
						if (r != null) {
							if (r) {
								notifyOfContentModify(sc);
							}
						}
					}
				});
	}

	public static void removeContent(final ShoppingCartRef sc,
			final List<ContentItem> items) {

		new ShoppingCartContentRemove(sc, items)
				.send(new ObjectMessageResponse<Boolean>() {

					@Override
					public void responded(Boolean r) {
						if (r != null) {
							if (r) {
								notifyOfContentModify(sc);
							}
						}
					}
				});
	}

	public static void clearContent(final ShoppingCartRef sc) {
		new ShoppingCartContentClear(sc)
				.send(new ObjectMessageResponse<Boolean>() {

					@Override
					public void responded(Boolean r) {
						if (r != null) {
							if (r) {
								notifyOfContentModify(sc);
							}
						}
					}
				});
	}

	public static void destroy(final Status... status) {
		new ShoppingCartList(null)
				.send(new ObjectMessageResponse<List<ShoppingCartRef>>() {
					@Override
					public void responded(List<ShoppingCartRef> scs) {
						if (scs != null) {
							if (!scs.isEmpty()) {
								final List<ShoppingCartRef> dscs = new Vector<ShoppingCartRef>();
								for (ShoppingCartRef sc : scs) {
									for (Status s : status) {
										if (s == sc.status()) {
											dscs.add(sc);
										}
									}
								}
								if (!dscs.isEmpty()) {
									new ShoppingCartDestroy(dscs).send();
								}
							}
						}
					}
				});
	}

	public static void destroy(final List<Status> states) {
		new ShoppingCartList(null)
				.send(new ObjectMessageResponse<List<ShoppingCartRef>>() {
					@Override
					public void responded(List<ShoppingCartRef> scs) {
						if (scs != null) {
							if (!scs.isEmpty()) {
								final List<ShoppingCartRef> dscs = new Vector<ShoppingCartRef>();
								for (ShoppingCartRef sc : scs) {
									for (Status s : states) {
										if (s == sc.status()) {
											dscs.add(sc);
										}
									}
								}
								if (!dscs.isEmpty()) {
									new ShoppingCartDestroy(dscs).send();
								}
							}
						}
					}
				});
	}

	public static void download(final ShoppingCartRef sc) {
		new ShoppingCartOutputRetrieve(sc)
				.send(new ObjectMessageResponse<Boolean>() {

					@Override
					public void responded(Boolean r) {
						if (r != null) {
							if (r) {
								logInfo("Shopping cart " + sc.scid()
										+ ": started downloading.");
								return;
							}
						}
						logError("Shopping cart " + sc.scid()
								+ ": failed to download.");
					}
				});
	}

	public static void order(final ShoppingCartRef sc) {
		new ShoppingCartOrder(sc).send(new ObjectMessageResponse<Boolean>() {

			@Override
			public void responded(Boolean r) {
				if (r != null) {
					if (r) {
						logInfo("Shopping cart " + sc.scid() + ": ordered.");
						return;
					}
				}
				logError("Shopping cart " + sc.scid() + ": failed to order.");
			}
		});
	}

	public static void reset() {

		if (_initialized) {
			SystemEventChannel.remove(_subscriber);
			if (_listeners != null) {
				_listeners.clear();
			}
			_asc = null;
			_initialized = false;
		}
	}

	public static void setLogger(Logger logger) {
		_logger = logger;
	}

	public static void summarize(final ObjectMessageResponse<Summary> rh) {
		new ShoppingCartList(null)
				.send(new ObjectMessageResponse<List<ShoppingCartRef>>() {

					@Override
					public void responded(List<ShoppingCartRef> scs) {
						rh.responded(new Summary(scs));
					}
				});

	}

	public static class Summary {

		private int _nbTotal = 0;
		private int _nbEditable = 0;
		private int _nbAssigned = 0;
		private int _nbAwaitProcessing = 0;
		private int _nbProcessing = 0;
		private int _nbFulfilled = 0;
		private int _nbDataReady = 0;
		private int _nbError = 0;
		private int _nbRejected = 0;
		private int _nbWithdrawn = 0;

		public Summary(List<ShoppingCartRef> scs) {
			if (scs != null) {
				_nbTotal = scs.size();
				for (ShoppingCartRef sc : scs) {
					switch (sc.status()) {
					case editable:
						_nbEditable++;
						break;
					case assigned:
						_nbAssigned++;
					case await_processing:
						_nbAwaitProcessing++;
						break;
					case processing:
						_nbProcessing++;
						break;
					case fulfilled:
						_nbFulfilled++;
						break;
					case error:
						_nbError++;
						break;
					case rejected:
						_nbRejected++;
						break;
					case withdrawn:
						_nbWithdrawn++;
						break;
					case data_ready:
						_nbDataReady++;
						break;
					}
				}
			}
		}

		public int nbTotal() {
			return _nbTotal;
		}

		public int nbEditable() {
			return _nbEditable;
		}

		public int nbAssigned() {
			return _nbAssigned;
		}

		public int nbAwaitProcessing() {
			return _nbAwaitProcessing;
		}

		public int nbProcessing() {
			return _nbProcessing;
		}

		public int nbFulfilled() {
			return _nbFulfilled;
		}

		public int nbDataReady() {
			return _nbDataReady;
		}

		public int nbError() {
			return _nbError;
		}

		public int nbRejected() {
			return _nbRejected;
		}

		public int nbWithdrawn() {
			return _nbWithdrawn;
		}

		@Override
		public String toString() {
			return "Total number of shopping carts: " + _nbTotal;
		}

	}

}
