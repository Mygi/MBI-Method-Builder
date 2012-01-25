package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

public class SvcMethodForSubjectCreate extends PluginService {
	private Interface _defn;

	public SvcMethodForSubjectCreate() throws Throwable {
		_defn = new Interface();
		addInterface(_defn);
	}

	public static void addInterface(Interface defn) throws Throwable {
		// Add the rest of the method information..
		SvcMethodCreate.addInterface(defn);

		// We specifically require subject information only when creating the Method. Optional for updates
		Interface.Element se = new Interface.Element("subject",XmlDocType.DEFAULT,"Description of the subject.",0,1);
		Interface.Element pe = new Interface.Element("project",XmlDocType.DEFAULT,"Project specific subject metadata. ",0,1);
		
		Interface.Element ie = new Interface.Element("public",XmlDocType.DEFAULT,"Publically accessible information for the subject.",0,1);
		ie.add(SvcMethodCreate.metadataInterfaceDefn("Subject identity information."));
		pe.add(ie);
		
		ie = new Interface.Element("private",XmlDocType.DEFAULT,"Information that can only be accessed by the subject administrator.",0,1);
		ie.add(SvcMethodCreate.metadataInterfaceDefn("Subject identity information."));
		pe.add(ie);
		
		se.add(pe);
		
		Interface.Element re = new Interface.Element("rsubject",XmlDocType.DEFAULT,"R-Subject (multi-project) specific subject metadata.",0,1);
		
		ie = new Interface.Element("identity",XmlDocType.DEFAULT,"Identity of the subject.",1,1);
		ie.add(SvcMethodCreate.metadataInterfaceDefn("Subject identity information."));
		re.add(ie);
		
		ie = new Interface.Element("public",XmlDocType.DEFAULT,"Publically accessible information for the subject.",0,1);
		ie.add(SvcMethodCreate.metadataInterfaceDefn("Subject identity information."));
		re.add(ie);
		
		ie = new Interface.Element("private",XmlDocType.DEFAULT,"Information that can only be accessed by the subject administrator.",0,1);
		ie.add(SvcMethodCreate.metadataInterfaceDefn("Subject identity information."));
		re.add(ie);
		
		se.add(re);
		
		defn.add(se);
		
	}
	
	public String name() {
		return "om.pssd.method.for.subject.create";
	}

	public String description() {
		return "Creates a research method for a subject on the local server.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable { 
		
		// Because we allow editing and creating via the same interface definition (really they should
		// be separated) we must test that the subject is specified in this creation service
		XmlDoc.Element subject = args.element("subject");
		if (subject==null) {
			throw new Exception ("You must specify the subject element as you are creating the Method");
		}
		XmlDoc.Element name = args.element("name");
		if (name==null) {
			throw new Exception ("You must specify the name element as you are creating the Method");
		}

		// Add the rest of the method information..
		Boolean replace = null;             // Irrelevant to creation
		SvcMethodCreate.execute(executor(),null,args,w,replace);
	}
}
