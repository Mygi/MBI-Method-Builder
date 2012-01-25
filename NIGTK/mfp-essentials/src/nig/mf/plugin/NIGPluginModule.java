package nig.mf.plugin;

import java.util.Collection;
import java.util.Vector;

import arc.mf.plugin.ConfigurationResolver;
import arc.mf.plugin.PluginModule;
import arc.mf.plugin.PluginService;

public class NIGPluginModule implements PluginModule {
	
	/** The collection of services can be created and cached.
	 */
	private static final Collection<PluginService> _services = createServices();

	/** The version of this module.
	 */
	public String version() {
		return "1.0";
	}

	/** A description of this module.
	 */
	public String description() {
		return "NIG PSSD Plugins Module for MediaFlux";
	}

	/** The company or person supplying this module.
	 */
	public String vendor() {
		return "Neuroimaging Group (NIG), Howard Florey Institute and the University of Melbourne.";
	}

	/** Returns the services available in this module. Returns a collection
	 ** of arc.mf.plugin.PluginService objects.
	 */
	public Collection<PluginService> services() {
		return _services;
	}

	/** Called once to initialise the set of available services within this
	 ** module.
	 */
	private static Collection<PluginService> createServices() {
		

		Vector<PluginService> svs = new Vector<PluginService>();
		svs.add(new SvcAssetCidGet());
		svs.add(new SvcAssetDICOMGrab());
		svs.add(new SvcAssetDMFGet());
		svs.add(new SvcAssetDMFPut());
		svs.add(new SvcAssetDocCopy());
		svs.add(new SvcAssetDocElementRename());
		svs.add(new SvcAssetDocElementRemove());
		svs.add(new SvcAssetMetaStringReplace());
		svs.add(new SvcAssetDocElementReplace());
		svs.add(new SvcAssetIdGet());
		svs.add(new SvcAssetPidSet());
		svs.add(new SvcIPAddressResolve());
		return svs;
	}

	/** Compatibility check.
	 */
	public boolean isCompatible(ConfigurationResolver config) {
		return false;
	}

	/** Initialisation on load.
	 */
	public void initialize(ConfigurationResolver config) throws Throwable {
	}

	/** Shutdown.
	 */
	public void shutdown(ConfigurationResolver config) throws Throwable {
	}

}
