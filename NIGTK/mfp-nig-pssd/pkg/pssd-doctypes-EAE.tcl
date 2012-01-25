# This script creates addition (to the basic Doc Types defined in doctypes-PSSD.tcl) specific
# Document Types for meta-data to be attached to PSSD objects for the HFI'sEAE project.
# This script also creates the Method for this project.
# 
# 
##########################################################################################################################################################
#
# Perfusion
# DocType if used must have all elements  included
# 

proc createDocType-pssd-EAE-perfusion {} {
       asset.doc.type.update \
           :create true :type hfi.pssd.EAE.perfusion \
           :description "Document type for perfusion of mouse brains in EAE experiment" \
           :definition < \
              :element -name agent  -type document -min-occurs 0 -max-occurs infinity  < \
                :description "Describes a perfusion agent" \
                :element -name name  -type enumeration  \
                         -enumerated-values "paraformaldehyde,gluteraldehyde,formalin,osmium tetraoxide,potassium ferricyanide" \
                         -index true -min-occurs 0 -max-occurs 1 < \
                   :description "Agent name" \
                 > \
                :element -name concentration -type float -min 0.0 -max 100.0 \
                         -index true -min-occurs 0 -max-occurs 1 < \
                   :attribute -type enumeration -name units -default "%" -enumerated-values "%" -min-occurs 0 \
                   :description "Concentration (%) of agent in buffer" \
                > \
              > \
              :element -name buffer  -type document -min-occurs 0 -max-occurs 1 < \
                :description "Describes the perfusion buffer" \
                :element -name name  -type enumeration  \
                         -enumerated-values "sodium cacodylate,phosphate buffered saline,water" \
                         -index true -min-occurs 0 -max-occurs 1 < \
                   :description "Buffer  name" \
                 > \
                :element -name concentration -type float -min 0.0 -max 100.0 \
                         -index true -min-occurs 0 -max-occurs 1 < \
                   :attribute -type enumeration -name units -enumerated-values "M" -default "M" -min-occurs 0 \
                   :description "Concentration (M) of buffer in water solution" \
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
              :element -name delivery -type enumeration \
                       -index true -min-occurs 0 -max-occurs 1 \
                 -enumerated-values "aorta,cardiac puncture" < \
                 :description "Delivery method of perfusion solution"  \
              > \
           >
}

proc destroyDocType-pssd-EAE-perfusion  { force } {
    if { $force != "true" && $force != "false" } {
                set force "false"
    }
    if { [xvalue exists [asset.doc.type.exists :type hfi.pssd.EAE.perfusion]] == "true" } {
                asset.doc.type.destroy :type hfi.pssd.EAE.perfusion :force $force
    }
}

###############################################################################################################

# May later add concentrations etc to the agent element

proc createDocType-pssd-EAE-stain {} {
       asset.doc.type.update \
           :create true :type hfi.pssd.EAE.stain \
           :description "Document type for staining of tissue slices in  EAE experiment" \
           :definition < \
              :element -name agent  -type document -min-occurs 0 -max-occurs infinity  < \
                :description "Describes a staining  agent" \
                :element -name name  -type enumeration  \
                         -enumerated-values "methylene blue" \
                         -index true -min-occurs 0 -max-occurs 1 < \
                   :description "Staining agent name" \
                 > \
              > \
           >
}

proc destroyDocType-pssd-EAE-stain  { force } {
    if { $force != "true" && $force != "false" } {
                set force "false"
    }
    if { [xvalue exists [asset.doc.type.exists :type hfi.pssd.EAE.stain]] == "true" } {
                asset.doc.type.destroy :type hfi.pssd.EAE.stain :force $force
    }
}



##################################################################################################################################


proc createDocType-pssd-EAE-optic-nerve-removal  {} {
        asset.doc.type.update \
           :create true :type hfi.pssd.EAE.optic-nerve.removal  \
           :description "Document type for EAE Method describing how the optic nerve is removed" \
           :definition < \
              :element -name method  -type enumeration  \
                         -enumerated-values "retro-chiasmal trans-section" \
                         -index true -min-occurs 1 -max-occurs 1 < \
                 :description "Method of removing optic nerve" \
              >\
              :element -name which -type enumeration  \
                       -enumerated-values "left,right" \
                       -index true -min-occurs 0 -max-occurs 2 < \
                 :description "Which nerve was removed" \
              >\
           >
}


proc destroyDocType-pssd-EAE-optic-nerve-removal { force } {
    if { $force != "true" && $force != "false" } {
                set force "false"
    }
    if { [xvalue exists [asset.doc.type.exists :type hfi.pssd.EAE.optic-nerve.removal]] == "true" } {
                asset.doc.type.destroy :type hfi.pssd.EAE.optic-nerve.removal :force $force
    }
}



#####################################################################################################################


proc createDocType-pssd-EAE-optic-nerve-section  {} {
        asset.doc.type.update \
           :create true :type hfi.pssd.EAE.optic-nerve.section   \
           :description "Document type for EAE Method describing how the optic nerve is sectioned " \
           :definition < \
             :element -name thickness  -type float -min 0.0 \
                      -index true -min-occurs 0 -max-occurs 1 < \
                :attribute -type enumeration -name units -enumerated-values "micron" -default "micron" -min-occurs 0 \
                :description "Thickness of slice in micron (micrometres)" \
             > \
           >
}


proc destroyDocType-pssd-EAE-optic-nerve-section  { force } {
    if { $force != "true" && $force != "false" } {
                set force "false"
    }
    if { [xvalue exists [asset.doc.type.exists :type hfi.pssd.EAE.optic-nerve.section]] == "true" } {
                asset.doc.type.destroy :type hfi.pssd.EAE.optic-nerve.section :force $force
    }
}



#####################################################################################################################


proc createDocType-pssd-EAE-microscopy  {} {
        asset.doc.type.update \
           :create true :type hfi.pssd.EAE.microscopy   \
           :description "Document type for EAE Method describing microscopy acquisition" \
           :definition < \
             :element -name magnification -type integer -min 1 \
                      -index true -min-occurs 0 -max-occurs 1 < \
                :description "Magnification of imaging" \
             > \
           >
}


proc destroyDocType-pssd-EAE-microscopy  { force } {
    if { $force != "true" && $force != "false" } {
                set force "false"
    }
    if { [xvalue exists [asset.doc.type.exists :type hfi.pssd.EAE.microscopy]] == "true" } {
                asset.doc.type.destroy :type hfi.pssd.EAE.microscopy :force $force
    }
}



#########################################################################################################


proc create-PSSD-EAE-DocTypes {} {
    createDocType-pssd-EAE-perfusion
    createDocType-pssd-EAE-optic-nerve-removal
    createDocType-pssd-EAE-optic-nerve-section
    createDocType-pssd-EAE-stain
    createDocType-pssd-EAE-microscopy
}

proc destroy-PSSD-EAE-DocTypes {} {
    set force "false"
    destroyDocType-pssd-EAE-perfusion $force
    destroyDocType-pssd-EAE-optic-nerve-removal $force
    destroyDocType-pssd-EAE-optic-nerve-section $force
    destroyDocType-pssd-EAE-stain  $force
    destroyDocType-pssd-EAEmicroscopy  $force
}

#
create-PSSD-EAE-DocTypes
