# ============================================================================
# Check if server version is no less than the required version
# ============================================================================
source requireServerVersion.tcl

# ============================================================================
# Include utils.tcl functions
# ============================================================================
source utils.tcl

# ============================================================================
# Create Study types dictionary holder
# ============================================================================
createDictionary pssd.study.types

# ============================================================================
# Create Relationship Types
# ============================================================================
createRelationshipType pssd-private pssd-private-of

# ============================================================================
# Install Plugins
# ============================================================================
set plugin_label      [string toupper PACKAGE_$package]
set plugin_namespace  mflux/plugins
set plugin_zip        pssd-plugin.zip
set plugin_jar        pssd-plugin.jar
set module_class      nig.mf.plugin.pssd.PSSDPluginModule
set plugin_libs       { nig-commons.jar dcmtools.jar }
loadPlugin $plugin_namespace $plugin_zip $plugin_jar $module_class $plugin_label $plugin_libs
srefresh

# ============================================================================
# Install DICOM ingest FCP file
# ============================================================================
source fcp.tcl
install_fcp /pssd/fcp pssd-dicom-ingest.fcp "Import local DICOM data as study and datasets"
install_fcp /pssd/fcp pssd-object-attach.fcp "Import local file(s) or directory as attachment"
install_fcp /pssd/fcp pssd-dataset-primary.fcp "Import local file(s) or directory as primary dataset"
install_fcp /pssd/fcp pssd-dataset-derivation.fcp "Import local files(s) or directory as derived dataset"

# ============================================================================
# Create dictionaries
# ============================================================================
# Migration (stable 2-6) to new dictionary values
source migrate-dictionary-entries.tcl
#
source dictionaries.tcl
createUpdatePSSDDicts
#
source dictionaries-ANZSRC.tcl
createUpdateANZSRCPSSDDicts

# ============================================================================
# Create Doc Types
# ============================================================================
source doctypes.tcl
source doctypes-bruker.tcl
source doctypes-dicom.tcl
#
source doctypes-harvest.tcl

# ============================================================================
# Setup Roles and Permissions
# ============================================================================
# One-off preparation for the migration away from nif-user to a better structure
source migrate-role-structure.tcl
srefresh
#
if { [info exists bypassPerms] } {
    if { $bypassPerms == "true" } {

    } else {
        source roleperms.tcl
    }
} else {
    source roleperms.tcl
}

# ============================================================================
# Declare the "standard" object models
# Pass "model" from package install: package.install :arg -name model true/false
# ============================================================================
source models.tcl
set addModel 1
if { [info exists model] } {
    if { $model == "false" } {
        set addModel 0
    }
}
if { $addModel == 1 } {
    addPSSDModels
# DaRIS portal can handle this at basic level (RIA cannot).
    registerModelMetaData
}

# ============================================================================
# Install the trigger for /dicom namespace, which monitoring the arrivals of
# NON-PSSD style DICOM data and send notifications. The install script
# will first uninstall any existing triggers
# ============================================================================
source triggers-install.tcl


# ============================================================================
# Create the default shopping cart template (name: pssd)
# ============================================================================
om.pssd.shoppingcart.template.create

#=============================================================================
# Set up ANDS XSLT project meta-data harvesting asset
#=============================================================================
source ANDS-XSLT-asset-create.tcl
