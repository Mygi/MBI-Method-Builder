package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.xml.*;
import nig.mf.plugin.util.AssetRegistry;


public class SvcDICOMAERegID extends PluginService {
	private Interface _defn;

	public SvcDICOMAERegID() {
		_defn = new Interface();

	}

	public String name() {
		return "om.pssd.dicom.ae.id";
	}

	public String description() {
		return "Find the asset ID of the local DICOM Application Entity registry asset.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {

		// Find the Registry. Return if none yet.
		String id = AssetRegistry.findRegistry(executor(), SvcDICOMAEAdd.REGISTRY_ASSET_NAME);
		if (id==null) return;
		w.add("id", id);
	}	
}
