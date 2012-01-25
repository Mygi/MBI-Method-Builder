# This script detects when certain dictionary entries changed and migrates to the new dictionary
# Its is premised on the fact that the dictionary making scripts, only re-create dictionaries
# if they have been previously destroyed.
#
# This was done in stable 2-6 of NIGTK Dec 2011
#

source dictionaries.tcl

# Dictionary pssd.research.organization 
set entry  [xvalue entry/term [dictionary.entries.describe :dictionary pssd.research.organization :size 1]]
set oldValues { "Baker IDI" "FNI" "La Trobe"  "LICR" "MHRI" "MCRI" "MU" "RMH" "SWUT" "UoM" "UQ" "VU" "WEHI" "Other" }
set fixResearch 0
foreach oldValue $oldValues {
    if { $entry == $oldValue } {
	set fixResearch 1
    }
}

if {$fixResearch == "1" } {
    puts "Migrating dictionary pssd.research.organization"
    puts "   Destroying old pssd.research.organization dictionary"
    dictionary.destroy :name pssd.research.organization

    # Regenerate
    puts "   Regenerating pssd.research.organization dictionary"
    createDict_research_organization
	
    # Now fix the entries in the known meta-data documents
    puts "   Fixing meta-data in pssd-project-harvest documents"
    set n [xvalue number-of-assets [nig.asset.doc.element.replace :type pssd-project-harvest :path project-owner/institution/name \
        :old-value "Baker IDI" \
        :new-value "Baker IDI Heart and Diabetes Institute" \
        :old-value "FNI" \
        :new-value "Florey Neuroscience Institutes" \
        :old-value "La Trobe" \
        :new-value "La Trobe University" \
        :old-value "LICR" \
        :new-value "Ludwig Institute for Cancer Research" \
        :old-value "MHRI" \
        :new-value "Mental Health Research Institute" \
        :old-value "MCRI" \
        :new-value "Murdoch Childrens Research Institute" \
        :old-value "MU" \
        :new-value "Monash University" \
        :old-value "RMH" \
        :new-value "Royal Melbourne Hospital" \
        :old-value "SWUT" \
        :new-value "Swinburne University of Technology" \
        :old-value "UoM" \
        :new-value "The University of Melbourne" \
        :old-value "UQ" \
        :new-value "The University of Queensland" \
        :old-value "VU" \
        :new-value "Victoria University" \
        :old-value "WEHI" \
        :new-value "Walter and Eliza Hall Institute" \
        :old-value "Other" \
        :new-value "Other research organization"]]
    puts "   Number of assets updated =  $n"
}
