package nig.mf.plugin.pssd.services;


import arc.mf.plugin.*;
import arc.xml.*;
import nig.mf.plugin.util.AssetRegistry;

public class SvcDICOMAERemove extends PluginService {
	
	
	private Interface _defn;

	public SvcDICOMAERemove() {
		_defn = new Interface();
		SvcDICOMAEAdd.addInterface(_defn, false, null);
	}

	public String name() {
		return "om.pssd.dicom.ae.remove";
	}

	public String description() {
		return "Removes a DICOM Application Entity from the DICOM AE registry.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {

		XmlDoc.Element ae = args.element("ae");
		
		// Add the 'remote' attribute. We also have a 'local' AE but this is stored
		// as a sever property and fetced by om.pssd.ae.list.  It's not stored
		// in the AE registry.
		XmlDoc.Attribute att = new XmlDoc.Attribute("type", "remote");
		ae.add(att);

		// See if the singleton asset already exists; else create.
		String id = AssetRegistry.findRegistry(executor(), SvcDICOMAEAdd.REGISTRY_ASSET_NAME);
		if (id==null) return;
		
		// Add the new role. 
		Boolean removed = AssetRegistry.removeItem(executor(), id,  ae, SvcDICOMAEAdd.DOCTYPE);

		// Return
		if (removed) w.add("id", id);
	}
}
