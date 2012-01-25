package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.pssd.plugin.util.DistributedAssetUtil;
import nig.mf.pssd.plugin.util.DistributedQuery.ResultAssetType;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.BooleanType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcRoleCleanup extends PluginService {
	private Interface _defn;
	

	public SvcRoleCleanup() {
		_defn = new Interface();
		_defn.add(new Element("destroy", BooleanType.DEFAULT,
				"Set to true to destroy the dangling roles; else just lists them (defaults to false). It is recommended that you list first and validate before destroying roles.",
				0, 1));

	}

	public String name() {
		return "om.pssd.role.cleanup";
	}

	public String description() {
		return "Finds unused PSSD roles (i.e. roles that do not have assets with ACLs utilising them) and lists and optionally destroys them.  Roles are only found and destroyed on the local server.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		boolean destroy = false;
		if (args.value("destroy") != null) {
			if (args.booleanValue("destroy")) {
				destroy = true;
			}
		}

        // Do it
		cleanUp(w, destroy);		
	}
	

	private void cleanUp(XmlWriter w, boolean destroy) throws Throwable {
		
		String proute = null;

		// Find all roles
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add ("type", "role");
		XmlDoc.Element r = executor().execute("actor.list",dm.root());
		Collection<XmlDoc.Element> roles = r.elements("actor");
		
		// Iterate over roles
		if ( roles != null ) {
			for (XmlDoc.Element actor : roles) {
				
				// See if it a PSSD role
				String role = actor.value();
				
				// We are looking for roles of the form "pssd.<string>.N1.N2.N3"
				// The trailing CID is for a Project or an R-Subject
				if(role.indexOf(".")>0){
					String[] parts = role.split("\\.");     // Need to escape it
					int l = parts.length;
					
					// Need at least 3 dots (4 parts)
					if (l >= 4 && (parts[0].equals("pssd"))) {
							
						// Ensure that the last 3 parts are numeric and that the
						// part before is not (so that the CID depth is exactly 3)
						if (nig.mf.pssd.CiteableIdUtil.isNumeric(parts[l-1]) && 
							nig.mf.pssd.CiteableIdUtil.isNumeric(parts[l-2]) &&
							nig.mf.pssd.CiteableIdUtil.isNumeric(parts[l-3]) &&
							!nig.mf.pssd.CiteableIdUtil.isNumeric(parts[l-4])) {
								
							// Reconstruct the CID
							String cid = parts[l-3] + "." + parts[l-2] + "." + parts[l-1];
										
							// Find if there is an asset for this cid on the local server.
							// Would be a project or r-subject
							boolean includeChildren = false;
							if (!DistributedAssetUtil.assetExists(executor(), proute, null, cid, ResultAssetType.all, 
									includeChildren, true, null)) {
																// OK we can destroy the role
								w.add("role", role);
								if (destroy) {
									PSSDUtils.destroyRole(executor(), role);
								}
							}
						}					
					}
				}
			}
		}
	}
}
