# ============================================================================
# Create MIME Types
# ============================================================================
proc createMimeType { type description } {
    
    if { [xvalue exists [type.exists :type $type]] == "false" } {
	type.create :type $type :description $description
    }
    
}

createMimeType dicom/series      "DICOM series"
createMimeType analyze/series/nl "Analyze(Neurological)"
createMimeType analyze/series/rl "Analyze(Radiological)"
createMimeType nifti/series      "NIFTI series"
createMimeType siemens/rda       "RDA(Siemens Spectrum)"
createMimeType bruker/series     "Bruker/Paravision image series"
createMimeType bruker/fid        "Bruker Free-Induction Decay data"
