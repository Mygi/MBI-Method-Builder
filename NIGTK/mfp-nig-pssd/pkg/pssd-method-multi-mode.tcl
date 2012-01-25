
# ===========================================================================
# Simple method for multi mode animal acquisitions 
# =========================================================================== 
#
# If Method pre-exists, action = 0 (do nothing), 1 (replace), 2 (create new)
#
proc create_multi-mode-method { { action 0 } } {
	
	set name "Multi mode imaging acquisitions"
	set description "Imaging acquisitions utilising multiple modes"
#
	set type1 "Magnetic Resonance Imaging"
	set name1 "MRI acquisition" 
	set desc1 "MRI acquisition of subject" 
#
	set type2 "Positron Emission Tomography"
	set name2 "PET acquisition" 
	set desc2 "PET acquisition of subject"
#
	set type3 "Electron Microscopy"
	set name3 "Electron Microscopy acquisition"
	set desc3 "EM acquisition of subject"
#
	set type4 "Optical Microscopy"
        set name4 "Optical Microscopy acquisition"
        set desc4 "OM acquisition of subject"
#
	set type5 "Computed Tomography"
	set name5 "Computed Tomography acquisition"
        set desc5 "CT acquisition of subject"
#
	set type6 "Nuclear Medicine"
        set name6 "Nuclear Medicine acquisition"
        set desc6 "NM acquisition of subject"
#
	set margs ""
	# See if Method pre-exists
	set id [getMethodId $name]
	    
	# Set arguments based on desired action	
	set margs [setMethodUpdateArgs $id $action]
	if { $margs == "quit" } {
		return
	}
        
	# Set Method body
	set args "${margs} \
	:namespace pssd/methods  \
	:name ${name} \
	:description ${description} \
	:subject < \
		:project < \
			:public < \
				:metadata < :definition -requirement mandatory hfi.pssd.identity > \
				:metadata <  :definition -requirement mandatory hfi.pssd.subject  :value < :type constant(animal) > > \
				:metadata < :definition -requirement mandatory hfi.pssd.animal.subject > \
				:metadata < :definition -requirement mandatory hfi.pssd.animal.disease > \
			> \
		> \
	> \
	:step < :name ${name1} :description ${desc1} :study < :type ${type1} :dicom < :modality MR > > > \
	:step < :name ${name2} :description ${desc2} :study < :type ${type2} :dicom < :modality PT > > > \
	:step < :name ${name3} :description ${desc3} :study < :type ${type3}  > > \
	:step < :name ${name4} :description ${desc4} :study < :type ${type4}  > > \
	:step < :name ${name5} :description ${desc5} :study < :type ${type5} :dicom < :modality CT > > > \
	:step < :name ${name6} :description ${desc6} :study < :type ${type6} :dicom < :modality NM > > >"
     
    # Create/update Method
    set id2 [xvalue id [om.pssd.method.for.subject.update $args]]
    if { $id2 == "" } {
       # An existng Method was updated
       return $id
    } else {
       # A new Method was created
       return $id2
    }
}