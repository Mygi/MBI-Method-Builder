# ============================================================================
# Include the util functions
# ============================================================================
source utils.tcl

# ============================================================================
# Uninstall Plugins
# ============================================================================
set plugin_label      [string toupper PACKAGE_$package]
set plugin_namespace  mflux/plugins
set plugin_zip        transcode-plugin.zip
set plugin_jar        transcode-plugin.jar
set plugin_path       $plugin_namespace/$plugin_jar
set module_class      nig.mf.plugin.transcode.TranscodePluginModule
unloadPlugin $plugin_path $module_class

# ============================================================================
# Remove Transcode Providers
# ============================================================================
set providers { { dicom/series  nifti/series } \
                { dicom/series  analyze/series/nl } \
                { dicom/series  analyze/series/rl } \
                { dicom/series  siemens/rda } \
                { bruker/series analyze/series/nl } \
                { bruker/series analyze/series/rl } }
removeTranscodeProviders $providers