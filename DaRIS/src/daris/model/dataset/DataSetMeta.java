package daris.model.dataset;

import arc.mf.client.xml.XmlElement;
import daris.model.object.PSSDObjectMeta;

public class DataSetMeta extends PSSDObjectMeta {

	private boolean _editable = false;
	
	public DataSetMeta(XmlElement oe) {
		
		super(oe);
		if (oe.element("meta/metadata") != null) {
			_editable = true;
		}
		
	}

	@Override
	public String[] topLevelElementXPaths() {
		
		return new String[]{"meta"};

	}

	public XmlElement getMeta(){
		
		return getMeta("meta");
		
	}

	@Override
	public boolean editable() {

		return _editable;

	}
	
}
