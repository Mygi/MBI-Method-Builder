#=============================================================================
# This script creates core  Document Types for meta-data to be attached to PSSD
# objects.  These Document Types should be Method independent and hence reusable.
# Non-reusable and/or very specific Document Types created for specific Methods
# should not be included in this file.
#
#
# Document Type Object Model:
#
#                 Subject
#        Animal            Non-Animal
#   Human                Plant     Mineral
#
# Note: you need to create the dictionaries first because some of  the
# doc types requires them.
#=============================================================================

#=============================================================================
# Project-related Document Types: hfi.pssd.project
#
# These meta-data should be placed on the Project object
proc createDocType_pssd_project {} {

	asset.doc.type.update \
		:create true :type hfi.pssd.project \
		:label "Project" \
		:description "Document type for  project" \
		:definition < \
		:element -name facility-id -min-occurs 0 -max-occurs infinity \
		-type string -index true  < \
		:description "Facility ID of the project allocated by some other authority" \
		:attribute -name type -type enumeration -min-occurs 0  < \
		:restriction -base enumeration < \
		:value HFI \
		:value RCH \
		:value CNS \
		:value Other \
		> \
		> \
		> \
		:element -name "funding-id" -type "string" -index "true" -min-occurs "0" \
		< \
		  :description "An identifier for the funding source (e.g. ARC/LIEF)" \
		  :attribute -name "type" -type "enumeration" -min-occurs "0" \
		  < \
		    :restriction -base "enumeration" \
		    < \
		      :dictionary "nig.funding.organization" \
		    > \
		  > \
		> \
		:element -name keyword -type string -index true -min-occurs 0 -max-occurs infinity < \
		    :description "A keyword relevant to this Project." \
		> \
	    >
}

#=============================================================================
# Subject-related Document Types: hfi.pssd.subject, hfi.pssd.human.subject,
# hfi.pssd.animal.subject, hfi.pssd.identity, hfi.pssd.human.identity
#
# These meta-data should be placed on  the Subject object
# Should be paired with the human and animal specific Doc Types
proc createDocType_pssd_subject {} {

	asset.doc.type.update \
		:create true :type hfi.pssd.subject \
		:description "Document type for project-based subject" \
		:label "Subject" \
		:definition < \
		:element -name type  -min-occurs 1 -max-occurs 1 \
		-type enumeration -enumerated-values animal,non-animal \
		-index true -case-sensitive false < \
		:description "Animal or non-animal Subject" > \
		:element -name control -min-occurs 0 -max-occurs 1 -type boolean -index true  < \
		:description "Subject is a member of a control group" > \
		>

}

#=============================================================================
# Generally, human animal subjects are re-used and so these meta-data should
# be placed on the R-Subject object
# Generally, non-human animals subjects are not re-used and so these meta-data
# should be placed on the Subject object
# Should be paired with hfi.pssd.subject
proc createDocType_pssd_animal_subject {} {

	asset.doc.type.update \
		:create true :type hfi.pssd.animal.subject \
		:description "Document type for an animal (Humans included) subject" \
		:label "Subject Basics" \
		:definition < \
		:element -name species -min-occurs 0 -max-occurs 1 \
		-type enumeration  -dictionary nig.pssd.animal.species \
		-index true  -case-sensitive false < \
		:description "Species type of the animal" >  \
		:element -name gender -min-occurs 0 -max-occurs 1 \
		-type enumeration -enumerated-values male,female,other,unknown \
		-index true  -case-sensitive false < \
		:description "Gender of the subject" > \
		:element -name birthDate -min-occurs 0 -max-occurs 1 \
		-type date -index true  < \
		:description "Birth date of the subject"  \
		> \
		:element -name deathDate -min-occurs 0 -max-occurs 1 \
		-type date -index true < \
		:description "Death date of the subject" \
		> \
		:element -name age-at-death  -min-occurs 0 -max-occurs 1 \
		-type integer -min 0 -index true  < \
		:attribute -type enumeration -name units -enumerated-values "days,weeks" -min-occurs 0  \
		:description "Age of subject (days or weeks) at time of death (intended for non-human subjects)." \
		> \
		:element -name weight-at-death  -min-occurs 0 -max-occurs 1 \
		-type float -min 0.0 -index true  < \
		:attribute -type enumeration -name units -enumerated-values "g,Kg" -min-occurs 0  \
		:description "Weight of subject (g or Kg) at time of death (intended for non-human subjects." \
		> \
		>

}

#=============================================================================
proc createDocType_pssd_animal_disease {} {

	asset.doc.type.update \
		:create true :type hfi.pssd.animal.disease  \
		:description "Document type for animal subject (Humans included) disease" \
		:label "Subject disease" \
		:definition < \
		:element -name disease -min-occurs 0 -max-occurs 1 -index true \
		-type enumeration  -dictionary nig.pssd.subject.pathology \
		-case-sensitive false \
		< :description "Disease pathology of the subject" > \
		:element -name disease-state -type document -index true -min-occurs 0 -max-occurs infinity \
		< :description "Describe the state of the disease in the subject with time" > < \
		:element -name state -type enumeration -min-occurs 0 -max-occurs 1 -index true \
		-enumerated-values pre-symptomatic,symptomatic,asymptomatic \
		< :description "State of the disease at the specified time" > \
		:element -name time -type document -min-occurs 0 -max-occurs 1 -index true <\
		:element -name date -type date -min-occurs 0 -max-occurs 1 -index true \
		< :description "Date when current disease state was set" > \
		:element -name time-point -type integer -min-occurs 0 -max-occurs 1 -index true \
		< :description "Time-point (0 is baseline) when current disease state was set" > \
		> \
		>\
		>\

	}

#=============================================================================
# Generally, human subjects are re-used and so these meta-data should be placed
# on the R-Subject object
# Should be paired with hfi.pssd.subject
proc createDocType_pssd_human_subject {} {

	asset.doc.type.update \
		:create true :type hfi.pssd.human.subject \
		:description "Document type for a Human subject" \
		:label "Human Subject" \
		:definition < \
		:element -name handedness -min-occurs 0 -max-occurs 1 -type enumeration \
		-enumerated-values left,right,ambidextrous,unknown -index true \
		-case-sensitive false < \
		:description "Handedness of the subject" \
		> \
		:element -name height  -min-occurs 0 -max-occurs 1 \
		-type float -min 0.0 -index true  < \
		:attribute -type enumeration -name units -enumerated-values "m" -min-occurs 0  \
		:description "Height of subject (m)" \
		> \
		:element -name smoking  -min-occurs 0 -max-occurs 1 \
		-type enumeration -enumerated-values never,ex,social,current,unknown \
		-index true -case-sensitive false < \
		:description "Smoking habits." \
		> \
		:element -name alcohol -min-occurs 0 -max-occurs 1  -type float -min 0.0 -index true  < \
		:attribute -type enumeration -name units -enumerated-values "unit/day" -min-occurs 0  \
		:description "Average consumption of alcohol (unit/day). One unit = 10ml or 8g" \
		> \
		>

}

#=============================================================================
# Generally,     human subjects are     re-used and so these meta-data should be
# placed on the Identity  object
# Generally, non-human subjects are not re-used and so these meta-data should be
# placed on the Subject object
# This Document Type exists because the content does not need to be protected
# (unlike hfi.pssd.human.identity)
proc createDocType_pssd_identity {} {

	asset.doc.type.update \
		:create true :type hfi.pssd.identity \
		:description "Document type for subject identity" \
		:label "External Subject Identifier" \
		:definition < \
		:element -name id -min-occurs 0 -max-occurs infinity  -type string -index true  < \
		:description "Unique identifier for the subject allocated by some other authority for cross-referencing" \
		:attribute -name type -type enumeration -min-occurs 0 < \
		:restriction -base enumeration < \
		:value RCH \
		:value HFIAS \
		:value aMRIF \
		:value INF \
		:value VIBES \
		:value Other \
		> \
		> \
		> \
		>

}

#=============================================================================
# Generally, human subjects are  re-used and so these meta-data should be placed
# on the Identity object
proc createDocType_pssd_human_identity {} {

	asset.doc.type.update \
		:create true \
		:type hfi.pssd.human.identity \
		:label "Human Identification" \
		:description "Document type for human subject identity" \
		:definition < \
		:element -name prefix -type string -min-occurs 0 -max-occurs 1 -length 20 -label "Prefix" \
		:element -name first  -type string -min-occurs 1 -max-occurs 1 -length 40 -label "First" \
		:element -name middle -type string -min-occurs 0 -max-occurs 1 -length 100 -label "Middle" < \
		:description "If there are several 'middle' names then put them in this field" \
		> \
		:element -name last   -type string -min-occurs 1 -max-occurs 1 -length 40 -label "Last" \
		:element -name suffix -type string -min-occurs 0 -max-occurs 1 -length 20 -label "Suffix" \
		>

}

#=============================================================================
proc createDocType_pssd_human_name {} {

	asset.doc.type.update \
		:create true \
		:type hfi.pssd.human.name \
		:label "Human name" \
		:description "Document type for human name" \
		:definition < \
		:element -name prefix -type string -min-occurs 0 -max-occurs 1 -length 20 -label "Prefix (e.g. Dr.)" \
		:element -name first  -type string -min-occurs 1 -max-occurs 1 -length 40 -label "First" \
		:element -name middle -type string -min-occurs 0 -max-occurs 1 -length 100 -label "Middle" < \
		:description "If there are several 'middle' names then put them in this field" \
		> \
		:element -name last   -type string -min-occurs 1 -max-occurs 1 -length 40 -label "Last" \
		:element -name suffix -type string -min-occurs 0 -max-occurs 1 -length 20 -label "Suffix" \
		>

}

#=============================================================================
proc createDocType_pssd_human_education {} {

	asset.doc.type.update \
		:create true \
		:type hfi.pssd.human.education \
		:label "Human Education" \
		:description "Document type for human education" \
		:definition < \
		:element -name primary  -min-occurs 0 -max-occurs 1 \
		-type integer -min 0 -index true  < \
		:attribute -type enumeration -name units -enumerated-values "years" -min-occurs 0  \
		:description "Years of primary education" \
		> \
		:element -name secondary  -min-occurs 0 -max-occurs 1 \
		-type integer -min 0 -index true  < \
		:attribute -type enumeration -name units -enumerated-values "years" -min-occurs 0  \
		:description "Years of secondary education" \
		> \
		:element -name tertiary  -min-occurs 0 -max-occurs 1 \
		-type integer -min 0 -index true  < \
		:attribute -type enumeration -name units -enumerated-values "years" -min-occurs 0  \
		:description "Years of tertiary education" \
		> \
		:element -name higher  -min-occurs 0 -max-occurs 1 \
		-type integer -min 0 -index true  < \
		:attribute -type enumeration -name units -enumerated-values "years" -min-occurs 0  \
		:description "Years of higher (e.g. Masters, PhD)  education" \
		> \
		>

}

#=============================================================================
proc createDocType_pssd_human_contact {} {

	asset.doc.type.update \
		:create true \
		:type hfi.pssd.human.contact \
		:label "Human Contact" \
		:description "Document type for human contacts" \
		:definition < \
		:element -name doctor -type document -index true -min-occurs 0 -max-occurs infinity < \
		:description "Referring Doctor or other health-care professional" \
		:reference -name name -type document < :value "hfi.pssd.human.name" > \
		>\
		>

}

#=============================================================================
# TODO: Study-related Document Types: hfi.pssd.study
#proc createDocType_pssd_study {} {
#	asset.doc.type.update \
		:create true \
		:type hfi.pssd.study \
		:description "Generic document type for Study" \
		:label "Study" \
		:definition < \
		> \
		>
#}

#=============================================================================
proc createDocType_pssd_PET_study {} {

	asset.doc.type.update \
		:create true \
		:type hfi.pssd.PET.study \
		:description "Document type for PET Study" \
		:label "PET Study" \
		:definition < \
		:element -name tracer -min-occurs 1 -max-occurs 1 \
		-type enumeration  -dictionary nig.PET.tracer -index true -case-sensitive false < \
		:description "Tracer of the PET acquisition" \
		> \
		>

}

#============================================================================#
proc createPSSDCoreDocTypes { } {

	createDocType_pssd_project
	createDocType_pssd_subject
	createDocType_pssd_animal_subject
	createDocType_pssd_animal_disease
	createDocType_pssd_human_subject
	createDocType_pssd_identity
	createDocType_pssd_human_identity
	createDocType_pssd_human_name
	createDocType_pssd_human_education
	createDocType_pssd_human_contact
#	createDocType_pssd_study
	createDocType_pssd_PET_study

}

#============================================================================#
proc destroyPSSDCoreDocTypes { } {

	set doctypes { pssd.project pssd.subject pssd.animal.subject pssd.animal.disease \
					pssd.human.subject pssd.identity pssd.human.identity pssd.human.name \
					pssd.human.education pssd.human.contact pssd.study pssd.PET.study }

	foreach doctype $doctypes {
        destroyDocType $doctype "true"
	}

}

#============================================================================#
#                                                                            #
#============================================================================#
createPSSDCoreDocTypes
