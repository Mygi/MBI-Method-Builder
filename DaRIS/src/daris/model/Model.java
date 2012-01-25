package daris.model;

import java.util.List;
import java.util.Vector;

import daris.model.object.PSSDObjectRef;

/**
 * The model event register
 * 
 * @author wilson
 * 
 */
public class Model {

	public static interface EventHandler {

		public void handleEvent(Model.Event e);
	}

	public static class Event {

		// TODO: remove
		public static final int OBJECT_LOADING = 0;
		
		public static final int OBJECT_SELECTED = 1;

		public static final int OBJECT_UPDATED = 2;

		public static final int OBJECT_DELETED = 3;

		public static final int OBJECT_CREATED = 4;

		public static final int CHILDREN_UPDATED = 5;

		private int _type;

		private PSSDObjectRef _object;

		private PSSDObjectRef _parent;

		private List<PSSDObjectRef> _children;

		private Event(PSSDObjectRef object, int eventType) {

			this(null, object, null, eventType);
		}

		private Event(PSSDObjectRef parent, PSSDObjectRef object, int eventType) {

			this(parent, object, null, eventType);
		}

		private Event(PSSDObjectRef object, List<PSSDObjectRef> children,
				int eventType) {

			this(null, object, children, eventType);
		}

		private Event(PSSDObjectRef parent, PSSDObjectRef object,
				List<PSSDObjectRef> children, int eventType) {

			_parent = parent;
			_object = object;
			_children = children;
			_type = eventType;
		}

		public int type() {

			return _type;
		}

		public PSSDObjectRef object() {

			return _object;
		}

		public PSSDObjectRef parent() {

			return _parent;
		}

		public List<PSSDObjectRef> children() {

			return _children;
		}
		
		protected static Event objectLoadingEvent(PSSDObjectRef object) {

			return new Event(null, object, OBJECT_LOADING);
		}

		protected static Event objectSelectedEvent(PSSDObjectRef object) {

			return new Event(null, object, OBJECT_SELECTED);
		}

		protected static Event objectUpdatedEvent(PSSDObjectRef object) {

			return new Event(null, object, OBJECT_UPDATED);
		}

		protected static Event objectDeletedEvent(PSSDObjectRef object) {

			return new Event(null, object, OBJECT_DELETED);
		}

		protected static Event objectCreatedEvent(PSSDObjectRef parent,
				PSSDObjectRef object) {

			return new Event(parent, object, OBJECT_CREATED);
		}

		protected static Event childrenUpdatedEvent(PSSDObjectRef object,
				List<PSSDObjectRef> children) {

			return new Event(object, children, CHILDREN_UPDATED);
		}
	}

	private static List<EventHandler> _handlers;

	public static void subscribe(EventHandler l) {

		if (_handlers == null) {
			_handlers = new Vector<EventHandler>();
		}
		_handlers.add(l);
	}

	public static void unsubscribe(EventHandler l) {

		if (_handlers != null) {
			_handlers.remove(l);
		}
	}

	protected static void fireEvent(Event e) {

		if (_handlers != null) {
			for (int i = 0; i < _handlers.size(); i++) {
				_handlers.get(i).handleEvent(e);
			}
		}
	}
	
	// TODO: remove
	public static void fireObjectLoadingEvent(PSSDObjectRef object) {

		fireEvent(Event.objectLoadingEvent(object));
	}


	public static void fireObjectSelectedEvent(PSSDObjectRef object) {

		fireEvent(Event.objectSelectedEvent(object));
	}

	public static void fireObjectUpdatedEvent(PSSDObjectRef object) {

		fireEvent(Event.objectUpdatedEvent(object));
	}

	public static void fireObjectDeletedEvent(PSSDObjectRef object) {

		fireEvent(Event.objectDeletedEvent(object));
	}

	public static void fireObjectCreatedEvent(PSSDObjectRef parent,
			PSSDObjectRef object) {

		fireEvent(Event.objectCreatedEvent(parent, object));
	}

	public static void fireChildrenUpdatedEvent(PSSDObjectRef object,
			List<PSSDObjectRef> children) {

		fireEvent(Event.childrenUpdatedEvent(object, children));
	}
}
