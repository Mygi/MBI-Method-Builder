package daris.client.model;

import java.util.List;
import java.util.Vector;

import daris.client.model.object.DObjectRef;

/**
 * Object Tree Model.
 * 
 * @author wilson
 * 
 */

public class Model {

	public static interface Subscriber {

		public boolean matches(Event e);

		public void processEvent(Event e);
	}

	public static class Event {

		public static final int OBJECT_CREATED = 1;
		public static final int OBJECT_UPDATED = 2;
		public static final int OBJECT_DESTROYED = 3;
		private int _type;
		private DObjectRef _o;

		protected Event(int type, DObjectRef o) {

			_type = type;
			_o = o;
		}

		public DObjectRef object() {

			return _o;
		}

		public int type() {

			return _type;
		}
	}

	// TODO: optimisation using TreeSet/TreeMap and filters.
	private static List<Subscriber> _subscribers;

	public static boolean subscribe(Subscriber s) {

		if (_subscribers == null) {
			_subscribers = new Vector<Subscriber>();
		}
		if (_subscribers.contains(s)) {
			return false;
		}
		return _subscribers.add(s);
	}

	public static boolean unsubscribe(Subscriber s) {

		if (_subscribers != null) {
			if (_subscribers.contains(s)) {
				return _subscribers.remove(s);
			}
		}
		return true;
	}

	public static void fireEvent(Event event) {

		if (_subscribers != null) {
			List<Subscriber> matches = new Vector<Subscriber>();
			for (Subscriber s : _subscribers) {
				if (s.matches(event)) {
					matches.add(s);
					if (event.type() != Event.OBJECT_DESTROYED) {
						break;
					}
				}
			}
			for (Subscriber s : matches) {
				s.processEvent(event);
			}
		}
	}

	public static void objectUpdated(DObjectRef o) {

		fireEvent(new Event(Event.OBJECT_UPDATED, o));
	}

	public static void objectCreated(DObjectRef o) {

		fireEvent(new Event(Event.OBJECT_CREATED, o));
	}

	public static void objectDestroyed(DObjectRef o) {

		fireEvent(new Event(Event.OBJECT_DESTROYED, o));
	}

}
