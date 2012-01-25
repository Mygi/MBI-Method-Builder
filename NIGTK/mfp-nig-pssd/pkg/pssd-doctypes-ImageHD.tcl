#=============================================================================
proc createDocType_pssd_time_point { } {

	asset.doc.type.update \
		:create true :type hfi.pssd.time-point \
		:description "Document type for time point" \
		:label "Time point" \
		:definition < \
		:element -name time-point -type integer -min-occurs 1 -max-occurs 1 -index true \
		< :description "Time-point index; 0 is baseline" >\
		:element -name date -type date -min-occurs 0 -max-occurs 1 -index true \
		< :description "Fiducial central date for time point" > \
		:element -name date-min -type date -min-occurs 0 -max-occurs 1 -index true \
		< :description "Start date for time point" > \
		:element -name date-max -type date -min-occurs 0 -max-occurs 1 -index true \
		< :description "End date for time point" > \
		>

}

#=============================================================================
proc createDocType_pssd_subject_exclusion { } {

	asset.doc.type.update \
		:create true :type hfi.pssd.subject.exclusion \
		:description "Document type for subject exclusion or withdrawal from Project" \
		:label "Subject exclusion or withdrawal" \
		:definition < \
		:element -name exclusion -type document -min-occurs 0 -max-occurs 1 -index true < \
		:description "Describes whether the subject has been excluded from the Project" \
		:element -name excluded -min-occurs 0 -max-occurs 1 -type boolean -index true <\
		:description "Has the subject been excluded from the Project"\
		>\
		:element -name date -type date -min-occurs 0 -max-occurs 1 -index true <\
		:description "The date when the subject was excluded from the Project"\
		>\
		:element -name reason -type string -min-occurs 0 -max-occurs 1 <\
		:description "The reason that the subject was excluded from the Project" \
		>\
		>\
		:element -name withdrawal -type document -min-occurs 0 -max-occurs 1 -index true < \
		:description "Describes whether the subject has withdrawn themselves from the Project" \
		:element -name withdrawn -min-occurs 0 -max-occurs 1 -type boolean -index true <\
		:description "Has the subject withdrawn from the Project"\
		>\
		:element -name date -type date -min-occurs 0 -max-occurs 1 -index true <\
		:description "The date when the subject withdrew from the Project"\
		>\
		:element -name reason -type string -min-occurs 0 -max-occurs 1 <\
		:description "The reason that the subject withdrew from the Project (see also exclusion)" \
		>\
		>\
		>

}

#=============================================================================
proc createDocType_pssd_ImageHD_combined {} {

	asset.doc.type.update \
		:create true :type hfi.pssd.ImageHD.combined  \
		:description "Document type for ImageHD combined results" \
		:label "ImageHD Combined Results" \
		:definition < \
		:element -name disease -min-occurs 1 -max-occurs 1 -index true \
		-type enumeration  -dictionary nig.pssd.subject.pathology \
		-case-sensitive false \
		< :description "Disease pathology of the subject" > \
		:element -name diagnosis -type document -index true -min-occurs 0 -max-occurs infinity \
		< :description "Diagnosis of subject" > < \
		:element -name state -type enumeration -min-occurs 0 -max-occurs 1 -index true \
		-enumerated-values pre-symptomatic,symptomatic \
		< :description "State of the disease at the specified time" > \
		:element -name date -type date -min-occurs 0 -max-occurs 1 -index true \
		< :description "Date of diagnosis." > \
		>\
		>

}

#============================================================================#
proc createPSSDDocTypesImageHD {} {

	createDocType_pssd_time_point
	createDocType_pssd_subject_exclusion
	createDocType_pssd_ImageHD_combined

}

#============================================================================#
proc destroyPSSDDocTypesImageHD {} {

	set doctypes { hfi.pssd.time-point hfi.pssd.subject.exclusion hfi.pssd.ImageHD.combined }
	foreach doctype $doctypes {
        destroyDocType $doctype "true"
	}

}

#============================================================================#
#                                                                            #
#============================================================================#
createPSSDDocTypesImageHD