

# Create dictionary of Study types.  This populates the pssd.study.types dictionary (this
# could be changed to something more extensible)
#
proc create_PSSD_StudyTypes { } {

	if { [xvalue exists [dictionary.exists :name pssd.study.types]] == "false" } {
		dictionary.create :name pssd.study.types :description "PSSD Study Types" :case-sensitive true
	}
	
	addDictionaryEntry  pssd.study.types "Bone Densitometry (ultrasound)"
	addDictionaryEntry  pssd.study.types "Computed Radiography"
	addDictionaryEntry  pssd.study.types "Computed Tomography"
	addDictionaryEntry  pssd.study.types "Electrocardiography"
	addDictionaryEntry  pssd.study.types "Cardiac Electrophysiology"
	addDictionaryEntry  pssd.study.types "General Microscopy"
	addDictionaryEntry  pssd.study.types "Intravascular Optical Coherence Tomography"
	addDictionaryEntry  pssd.study.types "Magnetic Resonance Imaging"
	addDictionaryEntry  pssd.study.types "Optical Microscopy"
	addDictionaryEntry  pssd.study.types "Electron Microscopy"
	addDictionaryEntry  pssd.study.types "Mammography"
	addDictionaryEntry  pssd.study.types "Nuclear Medicine"	
	addDictionaryEntry  pssd.study.types "Positron Emission Tomography"
	addDictionaryEntry  pssd.study.types "Radio Fluoroscopy"
	addDictionaryEntry  pssd.study.types "Slide Microscopy"	
	addDictionaryEntry  pssd.study.types "Ultra Sound"
	addDictionaryEntry  pssd.study.types "X-Ray Angiography"
	addDictionaryEntry  pssd.study.types "Combined Results" "Combined Results from other Studies"	
}


#============================================================================#
proc destroy_PSSD_StudyTypes { } {

	set dicts { pssd.study.types }
	foreach dict $dicts {
		if { [xvalue exists [dictionary.exists :name $dict]] == "true" } {
			dictionary.destroy :name $dict
		}
	}
}
