#============================================================================#
# This script create document types for use with Bruker format data.         #
#============================================================================#

#=============================================================================
proc createDocType_hfi_bruker_study { } {

	asset.doc.type.update \
		:create true :type hfi-bruker-study   \
		:description "Document for Bruker Paravision study" \
		:label "Bruker Study" \
		:definition < \
		:element -name id  -type string \
		-index true -min-occurs 0 -max-occurs 1 < \
		:description "Identifier of the study. It is the value of SUBJECT_study_nr in subject file." \
		> \
		:element -name uid -type string \
		   -index true -min-occurs 0 -max-occurs 1 < \
		   :description "Unique identifier of the bruker study. It is the value of SUBJECT_study_instance_uid in subject file." \
		> \
		:element -name "ingest" -type "document" -index "true" -min-occurs "0" -max-occurs "1" <\
		  :description "Ingest details." \
		  :element -name "date" -type "date" -index "true" -max-occurs "1" \
		     < :description "Date and time when the study was ingested." > \
		  :element -name "domain" -type "string" -index "true" -max-occurs "1" \
		     < :description "Domain of the user that ingested this study." > \
		  :element -name "user" -type "string" -index "true" -max-occurs "1" \
		     < :description "User that ingested this study." > \
	        > \
	    >
}

#=============================================================================
proc createDocType_hfi_bruker_series { } {

	asset.doc.type.update \
		:create true :type hfi-bruker-series   \
		:description "Document for Bruker Paravision study" \
		:label "Bruker Series" \
		:definition < \
		:element -name id  -type string \
		-index true -min-occurs 0 -max-occurs 1 < \
		:description "Identifier of the Bruker series. It is the value generated from (procno | (expno << 16))." \
		> \
		:element -name uid -type string \
		-index true -min-occurs 0 -max-occurs 1 < \
		:description "Unique identifier of the Bruker series. It is the value of RECO_base_image_uid in reco file." \
		> \
		:element -name protocol -type string \
		-index true -min-occurs 0 -max-occurs 1 < \
		:description "The protocol name of the Bruker series. " \
		> \
        :element -name acqTime -type string \
        -index true -min-occurs 0 -max-occurs 1 < \
        :description "The acquisition time. " \
        > \
		>

}

#============================================================================#
proc createPSSDDocTypesBruker {} {
	
	createDocType_hfi_bruker_study
	createDocType_hfi_bruker_series
	
}

#============================================================================#
proc destroyPSSDDocTypesBruker {} {

	set doctypes { hfi-bruker-series hfi-bruker-study }

	foreach doctype $doctypes {
        destroyDocType $doctype "true"
	}

}

#============================================================================#
#                                                                            #
#============================================================================#
createPSSDDocTypesBruker