package nig.mf.plugin.transcode;

import java.util.Collection;
import java.util.Vector;

import arc.mf.plugin.ConfigurationResolver;
import arc.mf.plugin.PluginModule;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.transcode.Transcoder;
import arc.mf.plugin.transcode.TranscoderPluginModule;

public class TranscodePluginModule implements PluginModule, TranscoderPluginModule {

	/**
	 * The collection of services can be created and cached.
	 */
	private static final Collection<PluginService> _services = createServices();

	/**
	 * The version of this module.
	 */
	public String version() {
		return "1.1";
	}

	/**
	 * A description of this module.
	 */
	public String description() {
		return "NIG image transcode plugin module for Mediaflux";
	}

	/**
	 * The company or person supplying this module.
	 */
	public String vendor() {
		return "Neuroimaging Group, Howard Florey Institute.";
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
		svs.add(new SvcTranscode());
		return svs;
	}

	/**
	 * Compatibility check.
	 */
	public boolean isCompatible(ConfigurationResolver config) {
		// Transcoders are not compatible --
		return false;
	}

	/**
	 * Initialization on load.
	 */
	public void initialize(ConfigurationResolver config) throws Throwable {

		Transcode.JavaXmxOption = config.configurationValue("JavaXmxOption");
		if (Transcode.JavaXmxOption == null) {
			// defaults to 512MB
			config.setConfigurationValue("JavaXmxOption", "-Xmx512m");
			Transcode.JavaXmxOption = "-Xmx512m";
		}

	}

	/**
	 * Shutdown.
	 */
	public void shutdown(ConfigurationResolver config) throws Throwable {

	}

	public Collection<Transcoder> transcoders() {
		return Transcode.transcoders();
	}

}