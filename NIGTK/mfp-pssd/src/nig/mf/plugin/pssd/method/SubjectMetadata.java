package nig.mf.plugin.pssd.method;

import arc.xml.XmlDoc;

public class SubjectMetadata {
	private int            _type;
	private XmlDoc.Element _meta;
	
	/**
	 * @param type metadata definition type.
	 */
	public SubjectMetadata(int type) {
		_type = type;
	}
	
	/**
	 * Which type of subject metadata?
	 * 
	 * @return
	 */
	public int type() { 
		return _type;
	}
	
}
