package daris.gui.object;

import daris.model.object.PSSDObjectRef;
import arc.gui.object.register.ObjectGUI;

public class PSSDObjectGUIRegistry implements
		arc.gui.object.register.ObjectGUIRegistry {

	public static arc.gui.object.register.ObjectGUIRegistry instance() {
		if (_instance == null) {
			_instance = new PSSDObjectGUIRegistry();
		}
		return _instance;
	}

	private static arc.gui.object.register.ObjectGUIRegistry _instance;

	private PSSDObjectGUIRegistry() {

	}

	@Override
	public ObjectGUI guiFor(Object o) {

		if (o != null) {
			if (o instanceof PSSDObjectRef) {
				return PSSDObjectGUI.instance();
			}
		}
		return null;
	}

}
