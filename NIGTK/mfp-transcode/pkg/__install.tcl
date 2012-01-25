# ============================================================================
# Check if server version is no less than the required version
# ============================================================================
source requireServerVersion.tcl


source utils.tcl

# ============================================================================
# Create MIME Types; these are primarily made in the essentials package.
# but in case you use this package without it, they are made here too.
# ============================================================================
createMimeType dicom/series      "DICOM series"
createMimeType analyze/series/nl "Analyze(Neurological)"
createMimeType analyze/series/rl "Analyze(Radiological)"
createMimeType nifti/series      "NIFTI series"
createMimeType siemens/rda       "RDA(Siemens Spectrum)"
createMimeType bruker/series     "Bruker/Paravision image series"
createMimeType bruker/fid        "Bruker Free-Induction Decay data"

# ============================================================================
# Install Plugins
# ============================================================================
set plugin_label      [string toupper PACKAGE_$package]
set plugin_namespace  mflux/plugins
set plugin_zip        transcode-plugin.zip
set plugin_jar        transcode-plugin.jar
set plugin_path       $plugin_namespace/$plugin_jar
set module_class      nig.mf.plugin.transcode.TranscodePluginModule

set plugin_libs       { nig-commons.jar dcmtools.jar loni-debabeler.jar }


# Import the archive
asset.import :url archive:///$plugin_zip \
    :namespace -create yes $plugin_namespace \
    :label -create yes $plugin_label :label PUBLISHED \
    :update true

# Add plugin module
set javaXmxOption "-Xmx512m"
if { [xvalue uuid [server.uuid]] == "1004" } {
    set javaXmxOption "-Xmx8000m"
}
if { [xvalue uuid [server.uuid]] == "1005" } {
    set javaXmxOption "-Xmx2000m"
}
if { [xvalue exists [plugin.module.exists :path $plugin_path :class $module_class]] == "true" } {
        plugin.module.remove :path $plugin_path :class $module_class    
}
plugin.module.add :path $plugin_path :class $module_class :config -name JavaXmxOption "\"${javaXmxOption}\"" :lib libs/nig-commons.jar :lib libs/dcmtools.jar :lib libs/loni-debabeler.jar

# Because the MF class loader does not work for loni-debabler.jar (the jar file contains SPI files, MF class loader 
# does not handle it properly), we have to put the loni-debabler.jar file into ${MF_HOME}/plugin/bin directory.
# Note: the server need to be restarted to load the jar files in ${MF_HOME}/plugin/bin/ directory.
asset.get :id path=/mflux/plugins/libs/loni-debabeler.jar :url file:[xvalue property\[@key='mf.home'\] [server.java.environment] ]/plugin/bin/loni-debabeler.jar

system.service.reload

srefresh


