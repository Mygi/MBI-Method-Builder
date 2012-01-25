# ============================================================================
# Destroy the default shopping cart template (name: pssd)
# ============================================================================
foreach cartId [xvalues cart/@id [shopping.cart.describe  :list-all true :size infinity]] { shopping.cart.destroy :id $cartId }
shopping.cart.template.destroy :name pssd
# om.pssd.shoppingcart.template.destroy :force true

# ============================================================================
# Include utils.tcl functions
# ============================================================================
source utils.tcl

# ============================================================================
# Uninstall Plugins
# ============================================================================
set plugin_label [string toupper PACKAGE_$package]
set plugin_namespace  mflux/plugins
set plugin_jar        pssd-plugin.jar
set plugin_path       $plugin_namespace/$plugin_jar
set module_class      nig.mf.plugin.pssd.PSSDPluginModule
unloadPlugin $plugin_path $module_class

# ============================================================================
# Uninstall the trigger for /dicom namespace, which monitoring the arrivals of
# NON-PSSD style DICOM data and send notifications.
# ============================================================================
source triggers-uninstall.tcl




