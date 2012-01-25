# This script detects when the Method structure changed to include 
# the study/dicom element.  This was done to separate STudy types
# from DICOM modalities and so at the same time, the Study types
# changed.
#
#source /Users/nebk/Software/Work/Eclipse/NIGTK-Method/mfp-nig-pssd/pkg/utils.tcl
source utils.tcl

proc checkMethodVersions { methodVersions } {
    set sameVersion 1
    set firstVersion [lindex $methodVersions 0]
    foreach methodVersion $methodVersions {
	if { $methodVersion != $firstVersion } {
	    set sameVersion 0
	}
    }
    return "$sameVersion $firstVersion"
}

proc regenerateMethods { } {
    #
    # Replace the internals of known Methods 
    #
    source pssd-method-AnimalMRISimple.tcl
    createMethod_animal_mri_simple 1
#
    source pssd-method-AnimalMRISimple_Private.tcl
    createMethod_animal_mri_simple_private 1
    #
    # Deprecated
    #source pssd-method-HumanMRISimple.tcl
    #createMethod_human_mri_simple 1
    #
    source pssd-method-HumanMRISimple_NoRSubject.tcl
    createMethod_human_mri_simple_no_rs 1
    #
    source pssd-method-ImageHD.tcl
    create_ImageHD_method 1
    #
    source pssd-method-EAE.tcl
    create_EAE_Method 1
    #
    source pssd-method-multi-mode.tcl
    create_multi-mode-method 1
}

proc migrateMethodsAndStudyTypes { } {
    #
    # Detect that the Method structure is the original version 1.0 and that
    # the Study types are old.  These things are coupled in this upgrade.
    #
    # Make a list of the current Methods
    set oldMethods [om.pssd.method.list]
    set nMethods [xcount method $oldMethods]
    if { $nMethods == "0" } {
	    set nProjects [xcount object om.pssd.collection.member.list]
	    # No Methods and Projects means we are in a first-time install, not a Migration. 
	    if { $nProjects == "0" } {
		    return 0
	    }
	    error "No Methods detected but some projects extant.  This is inconsistent."
    }
    set oldMethodCIDs [xvalues method $oldMethods]
    set oldMethodVersions [xvalues method/@version $oldMethods]
    #
    set r [checkMethodVersions $oldMethodVersions]
    set sameVersion [lindex $r 0]
    set methodVersion [lindex $r 1]

#   Detect if we have the old new Study types
    set entry  [xvalue entry/term [dictionary.entries.describe :dictionary pssd.study.types :size 1]]
    set oldValues { CR CT EM MR NM OM PT PET US }
    set fixTypes 0
    foreach oldValue $oldValues {
	if { $entry == $oldValue } {
	    set fixTypes 1
	}
    }
    puts "fixType $fixTypes"
    if { $sameVersion == "1" } {
	    if { $methodVersion == "1.0" } {
		    if { $fixTypes == "1" } {
                	puts "*** Detected old Study Types dictionary and Method version 1.0"
                	puts "    Migrate Study types, known Methods to V 1.1 and utilizing Projects"
                    
                        # Destroy types dictionary and re-generate with new Study types
                        #
			puts "    Destroy and re-generate Study types"
                        dictionary.destroy :name pssd.study.types
                	source pssd-studytypes.tcl
                	create_PSSD_StudyTypes
                    
                        # Regenerate Method internals
			puts "    Edit Methods with new structure"
                        regenerateMethods
                    
                        # Migrate all Projects to new Method Structure
			puts "      Update existing projects with new ExMethods"
                        om.pssd.project.method.replace
		    } else {
			    puts "*** The Method version is 1.0 but the Study types do not need to be upgraded"
		            puts "       This is inconsistent as it's a coupled upgrade."
		            puts "       The DaRIS administrator must resolve"		    
		    }
	    } else {
		    puts "*** Method version is not 1.0 so the Method migration does not need to be done"
		    if { $fixTypes == "1" } {
			    puts "       However, Study Types do need to be upgraded."
			    puts "       This is inconsistent as it's a coupled upgrade."
			    puts "       The DaRIS administrator must resolve"		    
		    }
	    }
    } else {
	    puts "*** Multiple Method versions detected"
	    puts "    The DaRIS administrator must resolve"
    }
}
