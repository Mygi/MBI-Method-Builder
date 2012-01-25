package nig.mf.plugin.pssd;

import arc.xml.XmlWriter;

public class PSSDObjectEvent extends arc.event.Event {

	public static final String EVENT_TYPE = "pssd-object";

	public static class Filter implements arc.event.Filter {

		private String _id;

		public Filter(String id) {

			_id = id;
		}

		public Filter() {

			this(null);
		}

		public boolean accept(arc.event.Event e) {

			if (!(e instanceof PSSDObjectEvent)) {
				return false;
			}
			if (!(e.type().equals(PSSDObjectEvent.EVENT_TYPE))) {
				return false;
			}
			if (_id != null) {
				PSSDObjectEvent poe = (PSSDObjectEvent) e;
				return _id.equals(poe.id());
			} else {
				return true;
			}
		}
	}

	public enum Action {
		create, update, destroy
	}

	private Action _action;
	private String _id;

	public PSSDObjectEvent(Action action, String id) {

		super(EVENT_TYPE, true);
		_action = action;
		_id = id;
	}

	public Action action() {

		return _action;
	}

	public String id() {

		return _id;
	}

	@Override
	public boolean equals(arc.event.Event e) {

		if (!super.equals(e)) {
			return false;
		}
		if (!(e instanceof PSSDObjectEvent)) {
			return false;
		}
		PSSDObjectEvent poe = (PSSDObjectEvent) e;
		return _action.equals(poe.action()) && _id.equals(poe.id());
	}

	@Override
	protected void saveState(XmlWriter w) throws Throwable {

		String action = _action.toString().toLowerCase();
		w.add("object", new String[] { "id", _id, "action", action });
	}
}
