package daris.model.object;

import java.util.HashMap;

import arc.mf.client.xml.XmlElement;

public abstract class PSSDObjectMeta {
	
	private HashMap<String, XmlElement> _mmap = new HashMap<String, XmlElement>();
	
	protected PSSDObjectMeta(XmlElement oe){
		
		String[] xpaths = topLevelElementXPaths();
		for(int i = 0 ;i<xpaths.length;i++){
			String xpath = xpaths[i];
			XmlElement e = oe.element(xpath);
			_mmap.put(xpath, e);
		}
		
	}
	
	public XmlElement getMeta(String topLevelElementXPath){
		
		return _mmap.get(topLevelElementXPath);
		
	}
	
	public abstract String[] topLevelElementXPaths();
	
	public abstract boolean editable();

}
