package nig.mf.plugin.pssd.method;

import nig.mf.plugin.pssd.Metadata;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

/**
 * Definition of metadata that can be added to a subject. There are 3 parts to a 
 * subject:
 * 
 *  1) Project Subject
 *  2) R-Subject
 *  3) Identity
 *  
 * @author Jason Lohrey
 *
 */
public class SubjectMetadataDefinition {
	public static final int TYPE_PS_PUBLIC  = 1;
	public static final int TYPE_PS_PRIVATE = 2;
	public static final int TYPE_RS_PUBLIC  = 3;
	public static final int TYPE_RS_PRIVATE = 4;
	public static final int TYPE_RS_IDENTITY = 5;
	
	public static final int REQ_OPTIONAL = 1;
	public static final int REQ_MANDATORY = 2;
	
	private int            _type;
	private XmlDoc.Element _md;
	private int            _req;
	
	/**
	 * 
	 * @param type
	 * @param md
	 */
	public SubjectMetadataDefinition(int type,XmlDoc.Element md,int req) {
		_type = type;
		_md   = md;
		_req  = req;
	}
	
	/**
	 * Which type of subject metadata?
	 * 
	 * @return
	 */
	public int subjectType() { 
		return _type;
	}
	
	/**
	 * Population requirement
	 * 
	 * @return
	 */
	public int requirement() {
		return _req;
	}
	
	/**
	 * The name of the metadata type.
	 * 
	 * @return
	 */
	public String metadataTypeName() throws Throwable {
		return _md.value("@name");
	}
	
	/**
	 * The description of the metadata..
	 * 
	 * @return
	 */
	public XmlDoc.Element metadata() {
		return _md;
	}
	
	/**
	 * Writes the definition to the given output. The
	 * definition is written in an interchange format.
	 * 
	 * @param w
	 * @throws Throwable
	 */
	public void write(ServiceExecutor executor,XmlWriter w) throws Throwable {
		String req = ( requirement() == REQ_MANDATORY )? "mandatory" : "optional";
		
		Metadata.describeTypeDefn(executor, w, _md, null,req);
	}
}
