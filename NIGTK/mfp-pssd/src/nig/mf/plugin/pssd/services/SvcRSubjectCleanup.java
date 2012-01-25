package nig.mf.plugin.pssd.services;

import java.util.Collection;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;
import nig.mf.plugin.pssd.*;
import nig.mf.pssd.plugin.util.DistributedAsset;


public class SvcRSubjectCleanup extends PluginService {
	private Interface _defn;

	public SvcRSubjectCleanup() {
		_defn = new Interface();
		_defn.add(new Interface.Element("list",BooleanType.DEFAULT,"If true, just list the R-Subjects rather than destroy them. Default is false (destroy RSubjects)",0,1));
	}

	public String name() {
		return "om.pssd.r-subject.cleanup";
	}

	public String description() {
		return "Destroys R-Subjects that are not associated with any Projects. Only finds and destroys RSubjects on the local server.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		
		String proute = null;
		// Parse
		Boolean list = args.booleanValue("list", false);
		
		// Find all R-Subjects
		XmlDoc.Element rSubjects = executor().execute("om.pssd.r-subject.find");
		if (rSubjects==null) return;
		
		// Get the CIDs
		Collection<String> ids = rSubjects.values("object/id");
		if (ids==null) return;
		
		// Iterate
		for (String id : ids) {
			// List/destroy if not in use
			if (!RSubject.hasRelatedSubjects(executor(), new DistributedAsset(proute,id))) {
				w.add("id", id);
				if (!list) {
					XmlDocMaker doc = new XmlDocMaker("args");
					doc.add("id", id);
						
					// Extra protection ; will not destroy if in use
					executor().execute("om.pssd.object.destroy", doc.root());
				}
			}
		}


	}
	
}
