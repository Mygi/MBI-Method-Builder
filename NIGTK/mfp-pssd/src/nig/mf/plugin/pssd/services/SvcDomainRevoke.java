package nig.mf.plugin.pssd.services;




import arc.mf.plugin.PluginService;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;
import arc.xml.XmlDoc.Element;

public class SvcDomainRevoke extends PluginService {

	private Interface _defn;

	public SvcDomainRevoke() throws Throwable {
		_defn = new Interface();
		SvcDomainGrant.addInterface(_defn);
	}



	public Access access() {
		return ACCESS_MODIFY;
	}

	public Interface definition() {
		return _defn;
	}

	public String description() {
		return "Revokes specific services the permission to administer the specified domain.  This would prevent standard users the ability to grant/revoke project roles.";
	}

	public String name() {
		return "om.pssd.domain.revoke";
	}

	public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {

		// Get arguments and parse
		XmlDoc.Element authority = args.element("authority");
		String domain = args.value("domain");	
		
		// These services need to mess with roles
		boolean grant = false;
		SvcDomainGrant.grantRevoke (executor(), "om.pssd.project.members.replace", authority, domain, grant);
		SvcDomainGrant.grantRevoke (executor(), "om.pssd.project.members.remove", authority, domain, grant);
		SvcDomainGrant.grantRevoke (executor(), "om.pssd.project.members.add", authority, domain, grant);
		SvcDomainGrant.grantRevoke (executor(), "om.pssd.project.create", authority, domain, grant);
		SvcDomainGrant.grantRevoke (executor(), "om.pssd.project.update", authority, domain, grant);
	}
}
