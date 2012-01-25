#============================================================================================
# This dictionary is not research domain specific.  I.e. the list of organizations transcends
# research domains. Therefore it's ok for it to be in the PSSD package. It is used in the
# om.pssd.project.metadata.harvest service to harvest meta-data for ANDS.
#

proc createDict_research_organization { } {

	if { [xvalue exists [dictionary.exists :name pssd.research.organization]] == "false" } {
		dictionary.create :name pssd.research.organization :description "Research Organizations" :case-sensitive true
	}
	addDictionaryEntry  pssd.research.organization "Baker IDI Heart and Diabetes Institute"
	addDictionaryEntry  pssd.research.organization "Florey Neuroscience Institutes"
	addDictionaryEntry  pssd.research.organization "La Trobe University"
	addDictionaryEntry  pssd.research.organization "Ludwig Institute for Cancer Research"
	addDictionaryEntry  pssd.research.organization "Mental Health Research Institute"
	addDictionaryEntry  pssd.research.organization "Murdoch Childrens Research Institute"
	addDictionaryEntry  pssd.research.organization "Monash University"
	addDictionaryEntry  pssd.research.organization "Royal Melbourne Hospital"
	addDictionaryEntry  pssd.research.organization "Swinburne University of Technology"
	addDictionaryEntry  pssd.research.organization "The University of Melbourne"
	addDictionaryEntry  pssd.research.organization "The University of Queensland"
	addDictionaryEntry  pssd.research.organization "Victoria University"
	addDictionaryEntry  pssd.research.organization "Walter and Eliza Hall Institute"
	addDictionaryEntry  pssd.research.organization "Other research organization"
}


# The reference is the NEMA standard (http://medical.nema.org/) volume 03 Section C.7.3.1.1 Modality
# Modality is DICOM element  (0008,0060)
proc createDict_DICOM_Modalities {} {
	if { [xvalue exists [dictionary.exists :name pssd.dicom.modality]] == "false" } {
		dictionary.create :name pssd.dicom.modality \
		   :description "DICOM Modality strings. See http://medical.nema.org, volume 03 Section C.7.3.1.1 Modality." :case-sensitive true	
	}
	addDictionaryEntry  pssd.dicom.modality AR "Autorefraction"
	addDictionaryEntry  pssd.dicom.modality AU "Audio"
	addDictionaryEntry  pssd.dicom.modality BDUS "Bone Densitometry (ultrasound)"
	addDictionaryEntry  pssd.dicom.modality BI "Biomagnetic imaging"
	addDictionaryEntry  pssd.dicom.modality CR "Computed Radiography"
	addDictionaryEntry  pssd.dicom.modality CT "Computed Tomography"
	addDictionaryEntry  pssd.dicom.modality DG "Diaphanography"
	addDictionaryEntry  pssd.dicom.modality DX "Digital Radiography"
	addDictionaryEntry  pssd.dicom.modality DOC "Document"
	addDictionaryEntry  pssd.dicom.modality ECG "Electrocardiography"
	addDictionaryEntry  pssd.dicom.modality EPS "Cardiac Electrophysiology"
	addDictionaryEntry  pssd.dicom.modality ES "Endoscopy"
	addDictionaryEntry  pssd.dicom.modality GM "General Microscopy"
	addDictionaryEntry  pssd.dicom.modality HC "Hard Copy"
	addDictionaryEntry  pssd.dicom.modality IO "Intra-oral Radiography"
	addDictionaryEntry  pssd.dicom.modality IVOCT "Intravascular Optical Coherence Tomography"
	addDictionaryEntry  pssd.dicom.modality KO "Key Object Selection"
	addDictionaryEntry  pssd.dicom.modality LS "Laser surface scan"
	addDictionaryEntry  pssd.dicom.modality MG "Mammography"
	addDictionaryEntry  pssd.dicom.modality MR "Magnetic Resonance"
	addDictionaryEntry  pssd.dicom.modality NM "Nuclear Medicine"
	addDictionaryEntry  pssd.dicom.modality OAM "Ophthalmic Axial Measurements"
	addDictionaryEntry  pssd.dicom.modality OCT "Optical Coherence Tomography (non-Ophthalmic)"
	addDictionaryEntry  pssd.dicom.modality OP "Ophthalmic Photography"
	addDictionaryEntry  pssd.dicom.modality OPV "Ophthalmic Visual Field"
	addDictionaryEntry  pssd.dicom.modality OT "Other"
	addDictionaryEntry  pssd.dicom.modality PLAN "Plan"
	addDictionaryEntry  pssd.dicom.modality PR "Presentation State"
	addDictionaryEntry  pssd.dicom.modality PT "Positron emission tomography (PET)"
	addDictionaryEntry  pssd.dicom.modality PX "Panoramic X-Ray"
	addDictionaryEntry  pssd.dicom.modality REG "Registration"
	addDictionaryEntry  pssd.dicom.modality RF "Radio Fluoroscopy"
	addDictionaryEntry  pssd.dicom.modality RG "Radiographic imaging (conventional film/screen)"
	addDictionaryEntry  pssd.dicom.modality RTDOSE "Radiotherapy Dose"
	addDictionaryEntry  pssd.dicom.modality RTIMAGE "Radiotherapy Image"
	addDictionaryEntry  pssd.dicom.modality RTPLAN "Radiotherapy Plan"
	addDictionaryEntry  pssd.dicom.modality RTSTRUCT "Radiotherapy Structure Set"
	addDictionaryEntry  pssd.dicom.modality RTRECORD "RT Treatment Record"
	addDictionaryEntry  pssd.dicom.modality SM "Slide Microscopy"
	addDictionaryEntry  pssd.dicom.modality SR "Structured Reporting Document"
	addDictionaryEntry  pssd.dicom.modality TG "Thermography"
	addDictionaryEntry  pssd.dicom.modality US "Ultrasound"
	addDictionaryEntry  pssd.dicom.modality VA "Visual Acuity"
	addDictionaryEntry  pssd.dicom.modality XA "X-Ray Angiography"
	addDictionaryEntry  pssd.dicom.modality XC "External-camera Photography"
}


proc createDict_human_name_prefixes { } {

	if { [xvalue exists [dictionary.exists :name pssd.human.name.prefix]] == "false" } {
		dictionary.create :name pssd.human.name.prefix :description "Human name prefixes" :case-sensitive true
	}
	addDictionaryEntry  pssd.human.name.prefix "Associate Professor"
	addDictionaryEntry  pssd.human.name.prefix "Dr."
	addDictionaryEntry  pssd.human.name.prefix "Mr."
	addDictionaryEntry  pssd.human.name.prefix "Mrs."
	addDictionaryEntry  pssd.human.name.prefix "Ms."
	addDictionaryEntry  pssd.human.name.prefix "Professor"
	addDictionaryEntry  pssd.human.name.prefix "Sir"	
}



#============================================================================#
proc createUpdatePSSDDicts { } {
        createDict_research_organization
	createDict_DICOM_Modalities
	createDict_human_name_prefixes
}

#============================================================================#
proc destroyPSSDDicts { } {

	set dicts { pssd.research.organization pssd.dicom.modality pssd.human.name.prefix }
	foreach dict $dicts {
		if { [xvalue exists [dictionary.exists :name $dict]] == "true" } {
			dictionary.destroy :name $dict
		}
	}

}
