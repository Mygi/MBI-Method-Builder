# This script detects when certain dictionary entries changed and migrates to the new dictionary
# Its is premised on the fact that the dictionary making scripts, only re-create dictionaries
# if they have been previously destroyed.
#
# This was done in stable 2-6 of NIGTK Dec 2011
#
# The dictionaries and the documents using them must be done one by one.  This is because if you
# destroy and regenerate all of the dictionaries first, then asset.set will fail when working 
# on the values for a specific dictionary because the values in the existing documents will be inconsistent;
# some values old, some values new, dictionaries all new.
#

source pssd-dictionaries.tcl

# Dictionary nig.ethics.organization 
# Document type hfi.pssd.ethics
set entry  [xvalue entry/term [dictionary.entries.describe :dictionary nig.ethics.organization :size 1]]
set oldValues { "AMREP AEC" "EandE" "FNI" "HFI" "MH" "MCRI" "MU" "RCH" "UoM" }
set fixEthics 0
foreach oldValue $oldValues {
    if { $entry == $oldValue } {
	set fixEthics 1
    }
}

if {$fixEthics == "1" } {
    puts "Migrating dictionary nig.ethics.organization to new entries"
    puts "   Destroying old nig.ethics.organization dictionary"
    dictionary.destroy :name nig.ethics.organization	
	
    # Regenerate
    puts "   Regenerating nig.ethics.organization dictionary"
    createDict_ethics_organization 
	
    # Now fix the entries in the known meta-data documents
    puts "   Fixing meta-data in hfi.pssd.ethics documents"
    set n [xvalue number-of-assets [nig.asset.doc.element.replace :type hfi.pssd.ethics :path ethics-id :attribute type \
	:old-value "AMREP AEC" \
	:new-value "Alfred Medical Research and Education Precinct Animal Ethics Committee" \
	:old-value "EandE" \
	:new-value "Eye and Ear Hospital Human Research Ethics Committee" \
	:old-value "FNI" \
	:new-value "Florey Neuroscience Institutes Animal Ethics Committee" \
	:old-value "HFI" \
	:new-value "Howard Florey Institute Animal Ethics Committee" \
	:old-value "MH" \
	:new-value "Melbourne Health Human Research Ethics Committee" \
	:old-value "MCRI" \
	:new-value "Murdoch Childrens Research Institute Animal Ethics Committee" \
	:old-value "MU"  \
	:new-value "Monash University Human Research Ethics Committee" \
	:old-value "RCH" \
	:new-value "Royal Children's Hospital Human Research Ethics Committee" \
	:old-value "UoM" \
	:new-value "University of Melbourne Human Research Ethics Committee"]]
    puts "   Number of assets updated =  $n"
}


# Dictionary nig.funding.organization
# Document type hfi.pssd.project
set entry  [xvalue entry/term [dictionary.entries.describe :dictionary nig.funding.organization :size 1]]
set oldValues {"ARC" "NHMRC" "NIF" "UoM"  "FNI"  "ANZCA" "Archer Trust"  "Denton Endowment" "Other" }
set fixFunding 0
foreach oldValue $oldValues {
    if { $entry == $oldValue } {
	set fixFunding 1
    }
}

if {$fixFunding == "1" } {
    puts "Migrating dictionary nig.funding.organization to new entries"
    puts "   Destroying old nig.funding.organization dictionary"
    dictionary.destroy :name nig.funding.organization

    # Regenerate
    puts "   Regenerating nig.funding.organization dictionary"
    createDict_funding_organization	

    puts "   Fixing meta-data in hfi.pssd.project documents"
    set n [xvalue number-of-assets [nig.asset.doc.element.replace :type hfi.pssd.project :path funding-id  :attribute type \
	:old-value "ARC" \
	:new-value "Australian Research Council" \
	:old-value "NHMRC" \
	:new-value "National Health and Medical Research Council" \
	:old-value "NIF" \
	:new-value "National Imaging Facility" \
	:old-value "UoM"  \
	:new-value "University of Melbourne" \
	:old-value "FNI"  \
	:new-value "Florey Neuroscience Institutes" \
	:old-value "ANZCA" \
	:new-value "Australian and New Zealand College of Anaesthetists" \
	:old-value "Archer Trust"  \
	:new-value "Fred P Archer Charitable Trust" \
        :old-value "Denton Endowment" \
	:new-value "Derek Denton Endowment" \
	:old-value "Other" \
	:new-value "Other funding entity"]]
    puts "   Number of assets updated =  $n"
}



# Dictionary nig.pssd.animal.genes
# Document type hfi.pssd.animal.modified-genetics
set entry  [xvalue entry/term [dictionary.entries.describe :dictionary nig.pssd.animal.genes :size 1]]
set oldValues { "LIF" "CNTF" }
set fixGenes 0
foreach oldValue $oldValues {
    if { $entry == $oldValue } {
	set fixGenes 1
    }
}

if {$fixGenes == "1" } {
    puts "Migrating dictionary nig.pssd.animal.genes to new entries"
    puts "   Destroying old nig.pssd.animal.genes dictionary"
    dictionary.destroy :name nig.pssd.animal.genes

    # Regenerate
    puts "   Regenerating nig.pssd.animal.genes"
    createDict_pssd_animal_genes
	
    # Now fix the entries in the known meta-data documents
    puts "   Fixing meta-data in hfi.pssd.animal.modified-genetics documents"
    set n [xvalue number-of-assets [nig.asset.doc.element.replace :type hfi.pssd.animal.modified-genetics \
               :path transgenic/gene \
       :old-value "LIF" \
       :new-value "Leukemic Inhibitory Factor" \
       :old-value "CNTF" \
       :new-value "Ciliary Neurotropic Factor"]]
    puts "   Number of assets updated =  $n"
}