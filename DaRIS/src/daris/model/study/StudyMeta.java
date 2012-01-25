package daris.model.study;

import arc.mf.client.xml.XmlElement;
import daris.model.object.PSSDObjectMeta;

public class StudyMeta extends PSSDObjectMeta {

	private boolean _editable = false;

	public StudyMeta(XmlElement oe) {

		super(oe);
		if (oe.element("meta/metadata") != null
				|| oe.element("method/metadata") != null) {
			_editable = true;
		}

	}

	@Override
	public String[] topLevelElementXPaths() {

		return new String[] { "meta", "method" };

	}

	public XmlElement getMeta() {

		return getMeta("meta");

	}

	public XmlElement getMethodMeta() {

		return getMeta("method");

	}
	
	@Override
	public boolean editable() {
 		
		return _editable;
		
	}

}
