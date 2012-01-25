
# MRI Method
#
# If Method pre-exists, action = 0 (do nothing), 1 (replace), 2 (create new)
#
proc createMRMethod { { action 0 } } {

	set name "Image HD: MRI Studies (Acquisitions)"
	set description "MRI acquisitions"
	#
	set type "Magnetic Resonance Imaging"
	#
	set name1 "MR (TP=0)"
	set desc1 "MR acquisition at time-point=0"
	#
	set name2 "MR (TP=1)"
	set desc2 "MR acquisition at time-point=1"
	#
	set name3 "MR (TP=2)"
	set desc3 "MR acquisition at time-point=2"
#
	set name4 "MR (TP=3)"
	set desc4 "MR acquisition at time-point=3"
#
	set name5 "MR (TP=4)"
	set desc5 "MR acquisition at time-point=4"
#
	set name6 "MR (TP=5)"
	set desc6 "MR acquisition at time-point=5"	
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
	:step < \
		:name ${name1} :description ${desc1} \
		:study < \
			:type ${type} :dicom < :modality MR > \
			:metadata < \
				:definition -requirement mandatory hfi.pssd.time-point \
				:value < :time-point constant(0) > \
			> \
		> \
	> \
	:step < \
		:name ${name2} :description ${desc2} \
		:study < \
			:type ${type} :dicom < :modality MR > \
			:metadata < \
				:definition -requirement mandatory hfi.pssd.time-point \
				:value < :time-point constant(0) > \
			> \
		> \
	> \
       :step < \
       		:name ${name3} :description ${desc3} \
       		:study < \
	       		:type ${type} :dicom < :modality MR > \
	       		:metadata < \
		      		 :definition -requirement mandatory hfi.pssd.time-point \
		      		 :value < :time-point constant(0) > \
	     		  > \
       		> \
	> \
       :step < \
       		:name ${name4} :description ${desc4} \
      	        :study < \
	       		:type ${type} :dicom < :modality MR > \
	      		:metadata < \
		     		  :definition -requirement mandatory hfi.pssd.time-point \
		     		  :value < :time-point constant(0) > \
	      		 > \
      		 > \
	> \
       :step < \
       		:name ${name5} :description ${desc5} \
       		:study < \
	       		:type ${type} :dicom < :modality MR > \
	       		:metadata < \
		       		:definition -requirement mandatory hfi.pssd.time-point \
		       		:value < :time-point constant(0) > \
	     		  > \
      		 > \
	> \
	:step < \
		:name ${name6} :description ${desc6} \
		:study < \
	        	:type ${type} :dicom < :modality MR > \
			 :metadata < \
				:definition -requirement mandatory hfi.pssd.time-point \
			        :value < :time-point constant(0) > \
			  > \
		  > \
	>" 
	
        # Create/update Method
        set id2 [xvalue id [om.pssd.method.update $args]]
        if { $id2 == "" } {
      	  # An existng Method was updated
      	  return $id
        } else {
      	  # A new Method was created
      	  return $id2
        }
}

#
# Neuropsychology Study Stream
#
#
# Demographics Study Stream
#
#
# Combined results  Study Stream
#
# If Method pre-exists, action = 0 (do nothing), 1 (replace), 2 (create new)
#
proc createCRMethod { { action 0 } } {

	set name "Image HD: Combined Results (CR) Studies"
	set description "Results Combined from all data at a given time point"
	#
	set type "Combined Results"
	#
	set name1 "CR (TP=0)"
	set desc1 "Combined Results at time-point=0"
	#
	set name2 "CR (TP=1)"
	set desc2 "Combined Results at time-point=1"
	#
	set name3 "CR (TP=2)"
	set desc3 "Combined Results at time-point=2"
#
	set name4 "CR (TP=3)"
	set desc4 "Combined Results at time-point=3"
#
	set name5 "CR (TP=4)"
	set desc5 "Combined Results at time-point=4"
#
	set name6 "CR (TP=5)"
	set desc6 "Combined Results at time-point=5"	
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
	:step < \
		:name ${name1} :description ${desc1} \
		:study < \
			:type ${type} \
			:metadata < \
				:definition -requirement mandatory hfi.pssd.time-point  \
				:value < :time-point constant(0) > \
			> \
			:metadata < \
				:definition -requirement mandatory hfi.pssd.ImageHD.combined \
				:value < :disease constant(Huntington's Disease) > \
			> \
		> \
	> \
	:step < \
		:name ${name2} :description ${desc2} \
		:study < \
			:type ${type} \
			:metadata < \
				:definition -requirement mandatory hfi.pssd.time-point  \
				:value < :time-point constant(1) > \
			> \
			:metadata < \
				:definition -requirement mandatory hfi.pssd.ImageHD.combined \
				:value < :disease constant(Huntington's Disease) > \
			> \
		> \
	> \
	:step < \
		:name ${name3} :description ${desc3} \
		:study < \
			:type ${type} \
			:metadata < \
				:definition -requirement mandatory hfi.pssd.time-point  \
				:value < :time-point constant(2) > \
			> \
			:metadata < \
				:definition -requirement mandatory hfi.pssd.ImageHD.combined \
				:value < :disease constant(Huntington's Disease) > \
			> \
		> \
	> \
	:step < \
		:name ${name4} :description ${desc4} \
		:study < \
			:type ${type} \
			:metadata < \
				:definition -requirement mandatory hfi.pssd.time-point  \
				:value < :time-point constant(3) > \
			> \
			:metadata < \
				:definition -requirement mandatory hfi.pssd.ImageHD.combined \
				:value < :disease constant(Huntington's Disease) > \
			> \
		> \
	> \
	:step < \
		:name ${name5} :description ${desc5} \
		:study < \
			:type ${type} \
			:metadata < \
				:definition -requirement mandatory hfi.pssd.time-point  \
				:value < :time-point constant(4) > \
			> \
			:metadata < \
				:definition -requirement mandatory hfi.pssd.ImageHD.combined \
				:value < :disease constant(Huntington's Disease) > \
			> \
		> \
	> \
	:step < \
		:name ${name6} :description ${desc6} \
		:study < \
			:type ${type} \
			:metadata < \
				:definition -requirement mandatory hfi.pssd.time-point  \
				:value < :time-point constant(5) > \
			> \
			:metadata < \
				:definition -requirement mandatory hfi.pssd.ImageHD.combined \
				:value < :disease constant(Huntington's Disease) > \
			> \
		> \
	>"
	
	# Create/update Method
	set id2 [xvalue id [om.pssd.method.update $args]]
	if { $id2 == "" } {
		# An existng Method was updated
		return $id
	} else {
		# A new Method was created
		return $id2
	}
}

# =======================================
# ImageHD - overall method for controls
# =======================================
#
# If Method pre-exists, action = 0 (do nothing), 1 (replace), 2 (create new)
#
proc createMethodForSubject_controls { mrMethodId { action 0 } } {

	set name "ImageHD Overall Method for Control Group"
	set description "Overall Method for the ImageHD Huntington's Disease project for the control group" 
	#
   	set margs ""
   	# See if Method pre-exists
   	set id [getMethodId $name]

   	# Set arguments based on desired action	
        set margs [setMethodUpdateArgs $id $action]
        if { $margs == "quit" } {
    		return
        }

	# Set Method body args
	set args "${margs} \
    		:namespace pssd/methods  \
    		:name ${name} \
   		:description ${description} \
		:subject < \
        		:project < \
        			:public < \
        				:metadata < \
        					:definition -requirement mandatory hfi.pssd.subject  \
        					:value < :control constant(true) :type constant(animal) > \
        				> \
        				:metadata < \
        					:definition -requirement mandatory hfi.pssd.animal.subject \
        					:value < :species constant(human) > \
        				> \
        				:metadata < :definition -requirement mandatory hfi.pssd.human.subject > \
        				:metadata < :definition -requirement mandatory hfi.pssd.identity > \
        				:metadata < :definition -requirement mandatory hfi.pssd.human.education > \
        				:metadata < :definition -requirement mandatory hfi.pssd.subject.exclusion >\
        			> \
        			:private < \
        				:metadata < :definition -requirement mandatory hfi.pssd.human.identity  > \
        			> \
        		> \
		> \
		:step < :name Studies :method < :id ${mrMethodId} > >"

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


# =======================================
# ImageHD - overall method for controls
# =======================================
#
# If Method pre-exists, action = 0 (do nothing), 1 (replace), 2 (create new)
#
proc createMethodForSubject_patients { mrMethodId crMethodId { action 0 } } {

	set name "ImageHD Overall Method for Patient Group"
	set description "Overall Method for the ImageHD Huntington's Disease project for the patient group" 	
	#
   	set margs ""
   	# See if Method pre-exists
   	set id [getMethodId $name]

   	# Set arguments based on desired action	
        set margs [setMethodUpdateArgs $id $action]
        if { $margs == "quit" } {
    		return
        }

	# Set Method body args
	set args "${margs} \
    		:namespace pssd/methods  \
    		:name ${name} \
   		:description ${description} \
		:subject < \
        		:project < \
        			:public < \
        				:metadata < \
        					:definition -requirement mandatory hfi.pssd.subject  \
        					:value < :control constant(true) :type constant(animal) > \
        				> \
        				:metadata < \
        					:definition -requirement mandatory hfi.pssd.animal.subject \
        					:value < :species constant(human) > \
        				> \
        				:metadata < :definition -requirement mandatory hfi.pssd.human.subject > \
        				:metadata < :definition -requirement mandatory hfi.pssd.identity > \
        				:metadata < :definition -requirement mandatory hfi.pssd.human.education > \
        				:metadata < :definition -requirement mandatory hfi.pssd.subject.exclusion > \
        			> \
        			:private < \
        				:metadata < :definition -requirement mandatory hfi.pssd.human.identity > \
        			> \
        		> \
		> \
		:step < \
			:name Studies \
			:branch -type or < \
				:method < :id ${mrMethodId} > \
				:method < :id ${crMethodId} > \
			> >"

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


#============================================================================#
# Create all Methods                                                         #
#============================================================================#
proc create_ImageHD_method { { action 0 } } {
    set mrMethodId [createMRMethod $action]
    set crMethodId [createCRMethod $action]
#
    set sbjControlsMethodId [createMethodForSubject_controls $mrMethodId $action]
    set sbjPatientsMethodId [createMethodForSubject_patients $mrMethodId $crMethodId $action]
}