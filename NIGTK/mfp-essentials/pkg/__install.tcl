# ============================================================================
# Check if server version is no less than the required version
# ============================================================================
source requireServerVersion.tcl

# ============================================================================
# Include the util functions
# ============================================================================
source utils.tcl

# ============================================================================
# Install Plugins
# ============================================================================
set plugin_label           [string toupper PACKAGE_$package]
set plugin_namespace       /mflux/plugins
set plugin_zip             essentials-plugin.zip
set plugin_jar             essentials-plugin.jar
set module_class           nig.mf.plugin.NIGPluginModule
set plugin_libs            { nig-commons.jar }
loadPlugin $plugin_namespace $plugin_zip $plugin_jar $module_class $plugin_label $plugin_libs

# ============================================================================
# Define the mime types
# ============================================================================
source mime-types.tcl