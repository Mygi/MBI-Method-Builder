
# ==========================================================================================
# Simple method for non-Human, animal  MRI acquisitions appropriate to standard aMRIF  usage
# This is similar to Animal-MRI-Simple but it puts some of the meta-data in :private
#===========================================================================================
#
# If Method pre-exists, action = 0 (do nothing), 1 (replace), 2 (create new)
#
proc createMethod_animal_mri_simple_private { { action 0 } } {
	set name "Animal-MRI-Simple-pvt"
	set description "Animal MRI acquisition with simple method amd some meta in private fields"
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
	# Set Method body
	set args "${margs} \
	    :namespace pssd/methods  \
	    :name ${name} \
	    :description ${description} \
	    :subject < \
	    	:project < \
			 :public < \
		 		:metadata < :definition -requirement mandatory hfi.pssd.animal.disease > \
		 		:metadata < :definition -requirement mandatory hfi.pssd.identity > \
			 > \
		   	 :private <\
		    		:metadata < :definition -requirement mandatory hfi.pssd.subject :value < :type constant(animal) > > \
    	    			:metadata < :definition -requirement mandatory hfi.pssd.animal.subject > \
	 		  > \
	   	 > \
	    > \
	    :step < \
		    :name ${name1} :description ${desc1} :study < :type ${type1} :dicom < :modality MR > > \
	    >"
	# Create/update the Method
	set id2 [xvalue id [om.pssd.method.for.subject.update $args]]
	if { $id2 == "" } {
	   # An existng Method was updated
	   return $id
	} else {
	   # A new Method was created
	   return $id2
	}
}