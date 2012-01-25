package nig.mf.plugin.pssd.services;

import java.util.Collection;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;
import nig.mf.plugin.pssd.Project;
import nig.mf.pssd.plugin.util.DistributedAsset;


public class SvcProjectMembersList extends PluginService {
	
	
	private Interface _defn;

	public SvcProjectMembersList() {
		_defn = new Interface();
		Interface.Element me = new Interface.Element("id",CiteableIdType.DEFAULT, "The citable ID of the Project.", 1, 1);
		me.add(new Interface.Attribute("proute", CiteableIdType.DEFAULT,
				"In a federation, must specify the route to the peer that manages this citable ID.  If not supplied, the object is assumed to be local.", 0));
		_defn.add(me);
		_defn.add(new Interface.Element("dereference",BooleanType.DEFAULT, "Dereference any role members to users. Defaults to false.", 0, 1));
		_defn.add(new Interface.Element("detail",BooleanType.DEFAULT, "Show extra detail on user (email & names). Defaults to false.", 0, 1));
		_defn.add(new Interface.Element("ignore-system", BooleanType.DEFAULT, "Ignore SYSTEM domain users ?  Defaults to false.", 0, 1));
	}
	

	public String name() {
		return "om.pssd.project.members.list";
	}

	public String description() {
		return "List Project members (user members and role members) and their Project and data-use roles.  This is worked out from the roles that users hold.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		String id = args.value("id");
		String proute = args.value("id/@proute");
		Boolean deRef = args.booleanValue("dereference", false);
		Boolean showDetail = args.booleanValue("detail", false);
		Boolean ignoreSystem = args.booleanValue("ignore-system", false);

		
		// Set distributed citeable ID for the local Project and validate.
		DistributedAsset dID = new DistributedAsset(args.element("id"));
		String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dID);
		if (type == null) {
			throw new Exception("There is no asset associated with citable ID " + dID.toString());
		}
		if (!type.equals(Project.TYPE)) {
			throw new Exception("Object " + dID.getCiteableID() + " [type="
					+ type + "] is not a " + Project.TYPE);
		}

		// Get the members. We do this by making an implicit list at the lowest
		// Project role level. This will find all Project members.
		// This function will return their actual project roles as well in the XML
		boolean explicit = false;
		Collection<XmlDoc.Element> members = Project.membersWithProjectRole(executor(), proute,
									id, Project.GUEST_ROLE_NAME, explicit, Project.MEMBER_TYPE_ALL, deRef, showDetail);
		if (members==null) return;
		
		// Add to output
		for (XmlDoc.Element member : members) {
			if (ignoreSystem) {
				String domain = member.value("@domain");
				if (domain==null || (domain!=null && !domain.equalsIgnoreCase("system"))) {
					w.add(member);
				}
			} else {
				w.add(member);
			}
		}
	}
}



