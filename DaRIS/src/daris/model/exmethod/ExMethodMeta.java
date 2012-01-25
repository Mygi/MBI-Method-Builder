package daris.model.exmethod;

import arc.mf.client.xml.XmlElement;
import daris.model.object.PSSDObjectMeta;

// TODO: remove this class.
public class ExMethodMeta extends PSSDObjectMeta {

	private boolean _editable = false;

	public ExMethodMeta(XmlElement oe) {

		super(oe);
		if (oe.element("meta/metadata") != null) {
			_editable = true;
		}

	}

	@Override
	public String[] topLevelElementXPaths() {

		// TODO Auto-generated method stub
		return new String[] { "meta" };

	}

	public XmlElement getMeta() {

		return getMeta("meta");

	}

	@Override
	public boolean editable() {

		return _editable;

	}

}
