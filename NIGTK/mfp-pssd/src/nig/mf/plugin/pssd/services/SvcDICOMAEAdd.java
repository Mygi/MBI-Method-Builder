package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;
import nig.mf.plugin.util.AssetRegistry;

public class SvcDICOMAEAdd extends PluginService {
	
	public static final String DOCTYPE = "pssd-dicom-server-registry";
	public static final String REGISTRY_ASSET_NAME = "pssd-dicom-server-registry";


	
	private Interface _defn;

	public SvcDICOMAEAdd() {
		_defn = new Interface();
		addInterface (_defn, false, null);
	}

	/**
	 * This function is re-used by a number of AE services (registry and dicom send)
	 * 
	 * @param defn
	 * @param childrenOptional Means that the host,port,aet children elements are optional (min=0)
	 * @param nameDescription Lets you over-ride the default description for the name attribute.  Give null
	 * for default value
	 */
	public static void addInterface (Interface defn, boolean childrenOptional, String nameDescription)  {
		int minOccurs = 1;
		if (childrenOptional) minOccurs = 0;
		//
		String d = "A convenience name that this AE, after creation, may be referred to by.";
		if (nameDescription!=null) d = nameDescription;
		Interface.Element me = new Interface.Element("ae",XmlDocType.DEFAULT,
				"The DICOM Application Entity (e.g. a DICOM server).",1,1);
		me.add(new Interface.Attribute("name", StringType.DEFAULT, d, 0));
		me.add(new Interface.Element("host", StringType.DEFAULT, "The AE host name or IP address.", minOccurs, 1));
		me.add(new Interface.Element("port", IntegerType.DEFAULT, "The port number of the AE", minOccurs, 1));
		me.add(new Interface.Element("aet",StringType.DEFAULT,"The AET of the AE.",minOccurs, 1));
		defn.add(me);
	}
	
	public String name() {
		return "om.pssd.dicom.ae.add";
	}

	public String description() {
		return "Adds a DICOM Application Entity to the DICOM AE registry.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {
		
		// Parse
		XmlDoc.Element ae = args.element("ae");
		
		// Add the 'remote' attribute. We also have a 'local' AE but this is stored
		// as a sever property and fetced by om.pssd.ae.list.  It's not stored
		// in the AE registry.
		XmlDoc.Attribute att = new XmlDoc.Attribute("type", "remote");
		ae.add(att);

		// See if the singleton asset already exists; else create.
		String id = AssetRegistry.findAndCreateRegistry(executor(), REGISTRY_ASSET_NAME);
		
		// Add the new role. 
		Boolean added = AssetRegistry.addItem (executor(), id, ae, DOCTYPE);

		// Return
		if (added) w.add("id", id);
	
	}
}
