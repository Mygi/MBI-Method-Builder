package nig.mf.plugin.pssd.services;

import java.util.Collection;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;
import nig.mf.plugin.pssd.ModelUser;
import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.pssd.CiteableIdUtil;
import nig.mf.pssd.plugin.util.PSSDUtil;

/**
* Remove the pssd-project/member
* 
* @author nebk
*
*/


public class SvcProjectMembersMetaRemove extends PluginService {
	
	
	private Interface _defn;

	public SvcProjectMembersMetaRemove() {
		_defn = new Interface();		
		_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT, "The citable identifier of the Project.  If none given, all Projects will be updated.", 0, 1));
	}
	

	public String name() {
		return "om.pssd.project.members.metadata.remove";
	}

	public String description() {
		return "Remove the pssd-project/{member,role-member} meta-data. This is a service function to be run once in the transition to fully role-based project member management.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// Executing user must have specific project admin or overall admin role to run this service
		String id = args.value("id");
		if (!ModelUser.checkHasRoleNoThrow(null, executor(), PSSDUtils.OBJECT_ADMIN_ROLE_NAME)) {
			throw new Exception ("Caller must hold the global administrator role.");
		}	

		if (id==null) {
			
			// Get the list of projects
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.push("federation");
			dm.add("pdist", 0);      // Force local
			dm.pop();
			XmlDoc.Element r = executor().execute("om.pssd.collection.member.list", dm.root());
			if (r==null) return;
			Collection<XmlDoc.Element> projects = r.elements("object/id");
			if (projects==null) return;		
			for (XmlDoc.Element project : projects) {
				String projectName = project.value();
				remove(executor(), projectName, w);
			}	
		} else {
			if (PSSDUtil.isValidProject(executor(), id, false)) {
				remove (executor(), id, w);
			} else {
				throw new Exception("Object " + id + "  must be a Project");
			}
		}
	}
	
	private void remove (ServiceExecutor executor, String id, XmlWriter w) throws Throwable {
		
		// We only want to operate on Projects that were created with this server
		String cidRoot = CiteableIdUtil.getParentId(id, 2);		
		//
		String proute = null;
		String serverCIDRoot = CiteableIdUtil.citeableIDRoot(executor, proute);
		if (!cidRoot.equals(serverCIDRoot)) return;
			
		// Get old meta-data
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", id);
		dm.add("pdist", 0);   // Force local
		XmlDoc.Element r = executor().execute("asset.get", dm.root());
		if (r==null) return;
		//
		XmlDoc.Element pssdProject= r.element("asset/meta/pssd-project");
		if (pssdProject==null) return;
		XmlDoc.Element member = pssdProject.element("member");
		XmlDoc.Element roleMember = pssdProject.element("role-member");
		if (member==null && roleMember==null) return;
		
		// Continue if something to do. Get all pssd-project elements
		Collection<XmlDoc.Element> elements = pssdProject.elements();
		if (elements==null) return;
		
		// Prepare new pssd-project meta-data
		XmlDocMaker dmOut = new XmlDocMaker("args");
		dmOut.add("cid", id);
		dmOut.push("meta", new String[] {"action", "add"});
		dmOut.push("pssd-project");
		//
		for (XmlDoc.Element element : elements) {
			String name = element.name();
			if (name.equals("member") || name.equals("role-member")) {
				// Drop
			} else {
				dmOut.add(element);
			}
		}
		dmOut.pop();
		dmOut.pop();
		
		// Remove old pssd-project
		dm = new XmlDocMaker("args");
		dm.add("cid", id);
		dm.push("meta", new String[] {"action", "remove"});
		dm.add("pssd-project");
		dm.pop();
		System.out.println("removing = " + dm.root().toString());
		executor.execute("asset.set", dm.root());
		
		// Replace with new pssd-project
		System.out.println("Setting  = " + dmOut.root().toString());
		executor.execute("asset.set", dmOut.root());
		//
		w.add("id", id);
	}	
}

