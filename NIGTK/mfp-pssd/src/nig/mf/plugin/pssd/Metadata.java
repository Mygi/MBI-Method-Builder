package nig.mf.plugin.pssd;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class Metadata {
	
	public static String modelNameForType(String type) {
		return "om.pssd." + type;
	}
	
	public static String tagForType(String type) {
		return "om.pssd.type." + type;
	}
	
	public static String requirementTagForType(String type,String req) {
		return "om.pssd.type." + type + "." + req;
	}
	
	/**
	 * Returns the definition of the given metadata type on the local server.
	 * 
	 * @param executor
	 * @param type
	 * @return
	 * @throws Throwable
	 */
	public static XmlDoc.Element lookup(ServiceExecutor executor,String type,XmlDoc.Element ve) throws Throwable {
		return lookup(executor,type,ve,null);
	}
	
	public static XmlDoc.Element lookup(ServiceExecutor executor,String type,XmlDoc.Element ve,XmlDoc.Element de) throws Throwable {
	
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("type",type);
		
		if ( ve != null || de != null ) {
			dm.push("template");
			
			if ( ve != null ) {
				dm.add(ve);
			}
			
			if ( de != null ) {
				dm.push("document");
				dm.add(de);
				dm.pop();
			}
			
			dm.pop();
		}
		
		return executor.execute("asset.doc.type.describe",dm.root());
	}

	/**
	 * @param te The type definition element - returned by asset.doc.type.describe
	 * @param type The object type for this metadata.
	 */
	public static void describeTypeDefn(ServiceExecutor executor,XmlWriter w,XmlDoc.Element te,String type,String req) throws Throwable {
		describeTypeDefn(executor,w,te,type,req,canEdit(executor,te.value("@name")));
	}
	
	/**
	 * @param te The type definition element - returned by asset.doc.type.describe
	 * @param type The object type for this metadata.
	 */
	public static void describeTypeDefn(ServiceExecutor executor,XmlWriter w,XmlDoc.Element te,String type,String req,boolean canEdit) throws Throwable {
		String name = te.value("@name");

		if ( type != null ) {
			XmlDoc.Element tage = te.element("tag[.='" + Metadata.requirementTagForType(type, "optional") + "']");
			req = ( tage == null ) ? "mandatory" : "optional";
			
		}
		
		if ( req != null ) {
			w.push("metadata",new String[] { "type", name, "requirement", req });
		} else {
			w.push("metadata",new String[] { "type", name });
		}
		
		w.add(te.element("label"));
		w.add(te.element("description"));
		w.add(te.element("definition"));
		w.add("editable",canEdit);
		w.pop();
	}
	
	public static boolean canEdit(ServiceExecutor executor,String type) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.push("perm");
		dm.add("access","publish");
		dm.add("resource",new String[] { "type", "document" },type);
		dm.pop();
		
		XmlDoc.Element r = executor.execute("actor.self.have", dm.root());
		if ( r.booleanValue("perm") ) {
			return true;
		}
		
		return false;
	}
}
