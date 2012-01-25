package nig.mf.plugin.pssd.ni;

import java.util.Collection;
import java.util.Vector;


import arc.mf.plugin.ConfigurationResolver;
import arc.mf.plugin.PluginModule;
import arc.mf.plugin.PluginService;

public class NIGPSSDPluginModule implements PluginModule {

	/**
	 * The collection of services can be created and cached.
	 */
	private static final Collection<PluginService> _services = createServices();

	/**
	 * The version of this module.
	 */
	public String version() {
		return "1.0";
	}

	/**
	 * A description of this module.
	 */
	public String description() {
		return "NIG PSSD Plugins Module for MediaFlux";
	}

	/**
	 * The company or person supplying this module.
	 */
	public String vendor() {
		return "Neuroimaging Group (NIG), Howard Florey Institute and the University of Melbourne.";
	}

	/**
	 * Returns the services available in this module. Returns a collection of arc.mf.plugin.PluginService objects.
	 */
	public Collection<PluginService> services() {
		return _services;
	}

	/**
	 * Called once to initialise the set of available services within this module.
	 */
	private static Collection<PluginService> createServices() {

		Vector<PluginService> svs = new Vector<PluginService>();
		svs.add(new SvcStudyRetrofit());
		svs.add(new SvcMetadataGet());
		svs.add(new SvcMetadataDataGet());
		svs.add(new SvcProjectTimePointCheck());
		svs.add(new SvcStudyImport());
		svs.add(new SvcPSSDIdentityGrab());
		svs.add(new SvcPSSDDatasetNameGrab());
		svs.add(new SvcPSSDDatasetDescriptionGrab());
		svs.add(new SvcProjectMigrate());
		
		svs.add(new SvcSubjectEncrypt());
		svs.add(new SvcSubjectDecrypt());
		svs.add(new SvcSubjectMetaSet());
		//
		svs.add(new SvcUserCreate());
		svs.add(new SvcDICOMUserCreate());
		//
		svs.add(new SvcProjectMetaDataHarvest());

		/*
		 * Specialised one off services
		 */
		// svs.add(new SvcRSubjectPathologyMigrate());
		// svs.add(new SvcSubjectPathologyMigrate());
		// svs.add(new SvcDataSetMetaCopy());
		svs.add(new SvcPSSDBrukerDataSetTimeFix());

		return svs;
	}

	/**
	 * Compatibility check.
	 */
	public boolean isCompatible(ConfigurationResolver config) {
		return false;
	}

	/**
	 * Initialisation on load.
	 */
	public void initialize(ConfigurationResolver config) throws Throwable {
	}

	/**
	 * Shutdown.
	 */
	public void shutdown(ConfigurationResolver config) throws Throwable {
	}

}
