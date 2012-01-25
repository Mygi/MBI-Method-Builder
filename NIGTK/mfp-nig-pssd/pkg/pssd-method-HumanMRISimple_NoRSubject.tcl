
# ===========================================================================
# Simple method for Human MRI acquisitions appropriate to standard RCH usage
# With no re-usable RSubject (now deprecated)
# =========================================================================== 
#
# If Method pre-exists, action = 0 (do nothing), 1 (replace), 2 (create new)
#
proc createMethod_human_mri_simple_no_rs { { action 0 } } {
	set name "Human-MRI-Simple-noRS"
	set description "Human MRI acquisition with simple method and no R-Subject"
	#
	set name1 "MRI acquisition" 
	set desc1 "MRI acquisition of subject" 
	set type1 "Magnetic Resonance Imaging"
	#
	set margs ""
	# See if Method pre-exists
	set id [getMethodId $name]
	    
	# Set arguments based on desired action	
	set margs [setMethodUpdateArgs $id $action]
	if { $margs == "quit" } {
		return
	}
	#
	set args "${margs} \
	    :namespace pssd/methods  \
	    :name ${name} \
	    :description ${description} \
	    :subject < \
	    	:project < \
		    :public < \
			    :metadata < :definition -requirement mandatory hfi.pssd.identity > \
			    :metadata < :definition -requirement mandatory hfi.pssd.subject :value < :type constant(animal) > > \
			    :metadata < :definition -requirement mandatory hfi.pssd.animal.subject :value < :species constant(human) > > \
			    :metadata < :definition -requirement mandatory hfi.pssd.human.subject > \
			    :metadata < :definition -requirement mandatory hfi.pssd.animal.disease > \
			    :metadata < :definition -requirement mandatory hfi.pssd.human.education > \
		    > \
		    :private < \
			    :metadata < :definition -requirement mandatory hfi.pssd.human.identity > \
		    > \
	       > \
   	     > \
	    :step < \
		    :name ${name1} :description ${desc1} :study < :type ${type1} :dicom < :modality MR > > \
	    >"
	set id2 [xvalue id [om.pssd.method.for.subject.update $args]]
	if { $id2 == "" } {
	   # An existng Method was updated
	   return $id
	} else {
	   # A new Method was created
	   return $id2
	}
}