# This script creates the EAE Method

# ================================
# MRI scan
# ================================
proc createMRMethod { { action 0 } } {
	
        set name "EAE MRI"
        set description "Production of MRI data from mouse"
    	# 
	set type "Magnetic Resonance Imaging"
	#
	set name1 "Anaesthetize"
	set desc1 "Mouse is anaesthetized"
	#
	set name2 "Longitudinal MR"
	set desc2  "Longitudinal MR acquisition"
	#
        set name3 "Coronal  MR"
        set desc3 "Coronal MR acquisition"
	#
	set name4 "Recovery"
	set desc4 "Mouse recovers under warming light"
	#
        set name5 "Kill"
	set desc5 "Mouse is killed"

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
        :step < \
            :name ${name1} \
            :description ${desc1} \
            :subject -part p < \
               :metadata < \
                  :definition -requirement mandatory hfi.pssd.anaesthetic \
                  :value < \
                    :method constant(inhalation) \
                    :induction < \
                      :agent constant(isoflurane) \
                      :concentration constant(3) \
                    > \
                    :maintenance < \
                      :agent constant(isoflurane) \
                      :concentration < \
                      :min constant(0.5) \
    	              :max constant(1.0) \
                      > \
                    > \
                  > \
               > \
            > \
        > \
        :step < \
            :name ${name2} \
            :description ${desc2} \
            :study < :type ${type} :dicom < :modality MR > > \
        > \
        :step < \
            :name ${name3} \
            :description ${desc3} \
  	    :study < :type ${type} :dicom < :modality MR > > \
        > \
        :step < \
            :name ${name4} \
            :description ${desc4} \
            :subject -part p < \
               :metadata < \
                  :definition -requirement mandatory hfi.pssd.recovery \
                  :value < \
                    :method constant(warming light) \
                  > \
               > \
            > \
        > \
        :step < \
            :name ${name5} \
            :description ${desc5} \
            :subject -part p < \
               :metadata < \
                  :definition -requirement mandatory hfi.pssd.animal.kill \
                  :value < \
                    :route constant(intra-peritoneal injection) \
                    :agent constant(pentobarbitane) \
                    :concentration constant(100) \
                  > \
               > \
            > \
        >" 
        	 	
      # Create/update the Method
      set id2 [xvalue id [om.pssd.method.update $args]]
      if { $id2 == "" } {
         # An existng Method was updated
         return $id
      } else {
         # A new Method was created
         return $id2
      }
}

# ===============================================
#
# Optic nerve fixing
# Does not account for which nerve is removed
#
# ===============================================

proc createFixMethod { { action 0 } } {
	
	set name "EAE Optic Nerve Fix"
	set description "Remove and fix an optic nerve"
	#
	set name1 "Perfusion"
	set desc1 "Mouse brain is perfused in situ"
	set value1a "constant(sodium cacodylate)"	
	#
	set name2 "Remove Nerve"
	set desc2 "An optic nerve is removed from the mouse"
	set value2a "constant(retro-chiasmal trans-section)"
	#
	set name3 "Fix 1"
	set desc3 "First fixing step"
	set value3a "constant(sodium cacodylate)"
	#
	set name4 "Fix 2"
	set desc4 "Second fixing step"
	set value4a "constant(sodium cacodylate)"
	#
	set name5 "Post fix"
	set desc5 "Post fixing step"
	set value5a "constant(osmium tetraoxide)"
        set value5b "constant(potassium ferricyanide)"
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
                :name ${name1} \
                :description ${desc1} \
                :subject -part p < \
                   :metadata < \
                       :definition -requirement mandatory hfi.pssd.EAE.perfusion  \
                       :value < \
                         :agent < \
                           :name constant(paraformaldehyde) \
                           :concentration constant(4) \
                         > \
                         :agent < \
                           :name constant(gluteraldehyde) \
                           :concentration constant(2.5) \
                         > \
                         :buffer < \
                           :name ${value1a} \
                           :concentration constant(0.1) \
                         > \
                      > \
                   > \
                > \
            > \
            :step < \
                :name ${name2} \
                :description ${desc2} \
                :subject -part p <\
                   :metadata < \
                      :definition -requirement mandatory hfi.pssd.EAE.optic-nerve.removal  \
                      :value < \
                        :method ${value2a} \
                      > \
                   >\
                >\
            > \
            :step < \
                :name ${name3} \
                :description ${desc3} \
                :subject -part p <\
                   :metadata < \
                       :definition -requirement mandatory hfi.pssd.EAE.perfusion \
                       :value < \
                         :agent < \
                           :name constant(paraformaldehyde) \
                           :concentration constant(4) \
                         > \
                         :agent < \
                           :name constant(gluteraldehyde) \
                           :concentration constant(2.5) \
                         > \
                         :buffer < \
                           :name ${value3a} \
                           :concentration constant(0.1) \
                         > \
                         :duration constant(24) \
                      > \
                    > \
                > \
            > \
            :step < \
                :name ${name4} \
                :description ${desc4} \
                :subject -part p <\
                   :metadata < \
                       :definition -requirement mandatory hfi.pssd.EAE.perfusion  \
                       :value < \
                         :buffer < \
                           :name ${value4a}) \
                           :concentration constant(0.1) \
                         > \
                         :duration constant(240) \
                       > \
                    > \
                > \
            > \
            :step < \
                :name ${name5} \
                :description ${desc5} \
                :subject -part p <\
                   :metadata < \
                       :definition -requirement mandatory hfi.pssd.EAE.perfusion  \
                       :value < \
                         :agent < \
                           :name ${value5a} \
                           :concentration constant(2) \
                         > \
                         :agent < \
                           :name ${value5b} \
                           :concentration constant(1.5) \
                         > \
                         :buffer < \
                           :name constant(water) \
                           :concentration constant(100) \
                         > \
                         :duration constant(2) \
                       > \
                    > \
                > \
            >"
	
            # Create/update the Method
            set id2 [xvalue id [om.pssd.method.update $args]]
            if { $id2 == "" } {
               # An existng Method was updated
               return $id
            } else {
               # A new Method was created
               return $id2
            }
}
# ================================
# Optical Microscopy
# ================================

proc createOMMethod { { action 0 } } {	
    set name "EAE Optical Microscopy"
    set description "Process to produce stained and then digitized  histology images"
    #
    set type "Optical Microscopy"
    #
    set name1 "Section"
    set desc1 "Section the optic nerve into slices"
    #
    set name2 "Methylene blue stain "
    set desc2 "Stain slide with Methylene blue"
    set value2a "constant(methylene blue)"
    #
    set name3 "Low Mag  OM"
    set desc3 "Low magnification Optical Microscopy of slide"
    #
    set name4 "High Mag OM"
    set desc4 "High magnification Optical Microscopy of slide"
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
            :name ${name1} \
            :description ${desc1} \
            :subject -part p <\
               :metadata < \
                   :definition -requirement mandatory hfi.pssd.EAE.optic-nerve.section  \
                   :value <\
                     :thickness constant(0.5) \
                   >\
               >\
            >\
        > \
        :step < \
            :name ${name2} \
            :description ${desc2} \
            :subject -part p <\
               :metadata < \
                   :definition -requirement mandatory hfi.pssd.EAE.stain  \
                   :value < \
                     :agent < \
                       :name ${value2a} \
                     >\
                  >\
               >\
            >\
        > \
        :step < \
            :name ${name3} \
            :description ${desc3} \
            :study < \
               :type ${type} \
               :metadata <\
                 :definition -requirement mandatory hfi.pssd.EAE.microscopy  \
                 :value <\
                    :magnification constant(20) \
                 >\
               >\
            > \
        > \
        :step < \
            :name ${name4} \
            :description ${desc4} \
            :study < \
   	       :type ${type} \
               :metadata <\
                  :definition -requirement mandatory hfi.pssd.EAE.microscopy  \
                  :value <\
                     :magnification constant(100) \
                  >\
               >\
            > \
        >"
	
    # Create/update the Method
    set id2 [xvalue id [om.pssd.method.update $args]]
    if { $id2 == "" } {
       # An existng Method was updated
       return $id
    } else {
       # A new Method was created
       return $id2
    }
}




# ================================
# Electron Microscopy method.
# ================================

proc createEMMethod { { action 0 } } {
    set name "EAE Electron Microscopy"
    set description "Electron Microscopy"
    #
    set type "Electron Microscopy"
    #
    set name1 "Section"
    set desc1 "Section the optic nerve into slices"
    #
    set name2 "Electron Microscopy"
    set desc2 "Electron Microscopy of section"
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
                        :name ${name1} \
                        :description ${desc1} \
                        :subject -part p <\
                           :metadata < \
                               :definition -requirement mandatory hfi.pssd.EAE.optic-nerve.section  \
                               :value <\
                                 :thickness constant(0.08) \
                               >\
                           >\
                        >\
                    >\
                   :step < \
                        :name ${name2} \
                        :description ${desc2} \
                        :study < \
                           :type ${type} \
                           :metadata <\
                             :definition -requirement mandatory hfi.pssd.EAE.microscopy  \
                             :value <\
                                :magnification constant(8000) \
                             >\
                           >\
                        > \
                    >"
	
        # Create/update the Method
        set id2 [xvalue id [om.pssd.method.update $args]]
        if { $id2 == "" } {
           # An existng Method was updated
           return $id
        } else {
           # A new Method was created
           return $id2
        }
 }


###
# ================================
# EAE Optic Nerve - overall method
# ================================

proc createOverallMethod { mrMethodId fixMethodId OMMethodId EMMethodId { action 0 } } {

	set name "EAE Overall Optic Nerve Method"
	set description "EAE Optic Nerve"
	#
	set value1 "constant(Experimental Autoimmune Encephalomyelitis (EAE))"
	set name1 "Remove nerve and Fix"
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
            :subject <\
               :project <\
                  :public < \
                     :metadata < \
                        :definition -requirement mandatory hfi.pssd.subject  \
                        :value < \
                          :type constant(animal) \
                        > \
                     > \
                     :metadata < \
                        :definition -requirement mandatory hfi.pssd.animal.subject \
                        :value < \
                          :species constant(mouse) \
                        > \
                     > \
                     :metadata < \
                        :definition -requirement mandatory hfi.pssd.animal.disease \
                        :value < \
                          :disease ${value1} \
                        > \
                     > \
                     :metadata < \
                        :definition -requirement mandatory hfi.pssd.animal.genetics \
                        :value < \
                          :strain < \
                            :name constant(C57Black) \
                            :fraction constant(1.0) \
                          > \
                        > \
                     > \
                     :metadata < \
                        :definition -requirement mandatory hfi.pssd.identity \
                     > \
                  > \
               >\
            >\
            :step < \
                :name MRI \
                :method < :id $mrMethodId > \
            > \
            :step < \
                :name ${name1} \
                :method < :id $fixMethodId > \
            > \
            :step < \
                :name Microscopy \
                :branch -type or < \
                    :method < :id $OMMethodId > \
                    :method < :id $EMMethodId > \
                > \
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


#####################################################################################
proc create_EAE_Method { { action 0 } } {
    set mrMethodId [createMRMethod $action]
    set fixMethodId [createFixMethod $action]
    set OMMethodId [createOMMethod $action]
    set EMMethodId [createEMMethod $action]
    #
    set overallMethodId [createOverallMethod $mrMethodId $fixMethodId $OMMethodId $EMMethodId $action]
    return $overallMethodId
}