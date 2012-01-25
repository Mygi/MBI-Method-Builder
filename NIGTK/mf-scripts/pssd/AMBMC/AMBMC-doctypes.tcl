# This script creates addition (to the basic Doc Types defined in doctypes-PSSD.tcl) specific
# Document Types for meta-data to be attached to PSSD objects for the AMBMC  project.
# This script also creates the Method for this project.
# 
# 
##########################################################################################################################################################
#




##################################################################################################################################


proc createDocType-pssd-AMBMC-brain-removal  {} {
        asset.doc.type.update \
           :create true :type hfi.pssd.AMBMC.brain.removal  \
           :description "Document type for AMBMC Method describing how the brain is removed" \
           :definition < \
              :element -name method  -type enumeration  \
                         -enumerated-values "standard" \
                         -index true -min-occurs 1 -max-occurs 1 < \
                 :description "Method of removing optic nerve" \
              >\
              :element -name pre-incubation  -type document -min-occurs 0 -max-occurs infinity  < \
                :description "Describes the pre-incubation solution" \
                :element -name name  -type enumeration  \
                         -enumerated-values "X,Y" \
                         -index true -min-occurs 0 -max-occurs 1 < \
                   :description "Solution name" \
                 > \
                :element -name concentration -type float -min 0.0 \
                         -index true -min-occurs 0 -max-occurs 1 < \
                   :attribute -type enumeration -name units -default "M" -enumerated-values "M" -min-occurs 0 \
                   :description "Concentration (M) of solution" \
                > \
              > \
              :element -name duration -type float -min 0.0 \
                       -index true -min-occurs 0 -max-occurs 1 < \
                 :attribute -type enumeration -name units -enumerated-values "hr" -default "hr" -min-occurs 0 \
                 :description "Duration of process (hours)" \
              > \
              :element -name temperature -type float -min 0.0 \
                       -index true -min-occurs 0 -max-occurs 1 < \
                 :attribute -type enumeration -name units -enumerated-values "C,F" -default "C" -min-occurs 0 \
                 :description "Temperature of solution" \
              > \
              :element -name weight -type float -min 0.0 \
                       -index true -min-occurs 0 -max-occurs 1 < \
                 :attribute -type enumeration -name units -enumerated-values "g" -default "g" -min-occurs 0 \
                 :description "Weight (g) of brain on removal" \
              > \
              :element -name volume -type float -min 0.0 \
                       -index true -min-occurs 0 -max-occurs 1 < \
                 :attribute -type enumeration -name units -enumerated-values "cc" -default "cc" -min-occurs 0 \
                 :description "Volume (cc) of brain on removal" \
              > \
           >
}


proc destroyDocType-pssd-AMBMC-brain-removal { force } {
    if { $force != "true" && $force != "false" } {
                set force "false"
    }
    if { [xvalue exists [asset.doc.type.exists :type hfi.pssd.AMBMC.brian.removal]] == "true" } {
                asset.doc.type.destroy :type hfi.pssd.AMBMC.brain.removal :force $force
    }
}



#####################################################################################################################

#########################################################################################################


proc create-PSSD-EAE-DocTypes {} {
    createDocType-pssd-AMBMC-brain-removal
}

proc destroy-PSSD-EAE-DocTypes {} {
    set force "false"
    destroyDocType-pssd-AMBMC-brain-removal $force
}
