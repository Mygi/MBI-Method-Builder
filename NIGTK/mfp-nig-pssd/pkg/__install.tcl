# ============================================================================
# Check if server version is no less than the required version
# ============================================================================
source requireServerVersion.tcl

# ============================================================================
# Include the utils.tcl functions
# ============================================================================
source utils.tcl

#============================================================================
# Create dictionaries
#
# Note: it is created first because services may, when being reloaded, 
#       instantiate classes which specify dictionaries
#============================================================================
#Migration (stable 2-6) to new dictionary values
source migrate-dictionary-entries.tcl
#
source pssd-dictionaries.tcl
createUpdatePSSDDicts


#
#=============================================================================
# Method and Study type migration. Methods to V 1.1 and Study Types
# to meaningful strings with decoupling of DICOM Modality
# DOne in Dec 2011 and stable 2-7. Has to be done before the Study
# types dictionary is updated.  
#=============================================================================
source migrate-methods.tcl
migrateMethodsAndStudyTypes


#============================================================================
# Create Study Types
#
# Really just a dictionary, but we keep it logically separate
#============================================================================
source pssd-studytypes.tcl
create_PSSD_StudyTypes

# ============================================================================
# Install plugins
# ============================================================================
set plugin_label      [string toupper PACKAGE_$package]
set plugin_namespace  mflux/plugins
set plugin_zip        nig-pssd-plugin.zip
set plugin_jar        nig-pssd-plugin.jar
set module_class      nig.mf.plugin.pssd.ni.NIGPSSDPluginModule
loadPlugin $plugin_namespace $plugin_zip $plugin_jar $module_class $plugin_label
srefresh

#
# ============================================================================
# Domain access configuration
# ============================================================================
source pssd-domain-config.tcl

# ============================================================================
# Create namespaces
# ============================================================================
source pssd-namespaces.tcl

#=============================================================================
# Creates core doc types
#=============================================================================
source pssd-doctypes-core.tcl

#=============================================================================
# Create generic doc types in method
#=============================================================================
source pssd-doctypes-generic.tcl

#=============================================================================
# Create ethics doc types
#=============================================================================
source pssd-doctypes-ethics.tcl

#=============================================================================
# Create ImageHD doc types
#=============================================================================
source pssd-doctypes-ImageHD.tcl

#=============================================================================
# Create EAE doc types
#=============================================================================
source pssd-doctypes-EAE.tcl

#=============================================================================
# Create AnimalMRISimple method
#=============================================================================
source pssd-method-AnimalMRISimple.tcl
createMethod_animal_mri_simple 0

#=============================================================================
# Create AnimalMRISimple_Private method
#=============================================================================
source pssd-method-AnimalMRISimple_Private.tcl
createMethod_animal_mri_simple_private 0

#=============================================================================
# Create HumanMRISimple method
# This Method is now deprecated in favour of the NoRsubject flavour
#=============================================================================
#source pssd-method-HumanMRISimple.tcl
#createMethod_human_mri_simple 0

#=============================================================================
# Create HumanMRISimple_NoRSubject method
#=============================================================================
source pssd-method-HumanMRISimple_NoRSubject.tcl
createMethod_human_mri_simple_no_rs 0

#=============================================================================
# Create ImageHD method
#=============================================================================
source pssd-method-ImageHD.tcl
create_ImageHD_method 0

#=============================================================================
# Create EAE method
#=============================================================================
source pssd-method-EAE.tcl
create_EAE_Method 0


#=============================================================================
# Create Multi-mode testing method
#=============================================================================
source pssd-method-multi-mode.tcl
create_multi-mode-method  0

#=============================================================================
# Register doc types
#=============================================================================
source pssd-register-doctypes.tcl

#=============================================================================
# Register role-members
#=============================================================================
source pssd-register-rolemembers.tcl

#=============================================================================
# Set up roles & permissions
#=============================================================================
# One-off preparaion for the migration away from nif-user to a better structure
source migrate-role-structure.tcl
srefresh
#
source pssd-roleperms.tcl

# Fix user roles in the post nif-user structure
source migrate-role-structure-2.tcl
