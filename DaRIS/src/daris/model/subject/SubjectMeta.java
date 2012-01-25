package daris.model.subject;

import arc.mf.client.xml.XmlElement;
import daris.model.object.PSSDObjectMeta;

public class SubjectMeta extends PSSDObjectMeta {

	private boolean _editable;
	
	public SubjectMeta(XmlElement oe) {

		super(oe);
		if (oe.element("public/metadata") != null || oe.element("private/metadata") != null) {
			_editable = true;
		} else {
			_editable = false;
		}

	}

	@Override
	public String[] topLevelElementXPaths() {

		return new String[]{"public", "private"};

	}
	
	public XmlElement getPublicMeta(){
		
		return getMeta("public");
		
	}
	
	public XmlElement getPrivateMeta(){
		
		return getMeta("private");
		
	}

	@Override
	public boolean editable() {

		return _editable;

	}
	
}
