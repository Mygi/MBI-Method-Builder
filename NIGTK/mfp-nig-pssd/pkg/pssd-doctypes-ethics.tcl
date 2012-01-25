#============================================================================#
# This script creates ethics-related  Document Types for meta-data to be     #
# attached to PSSD objects.                                                  #
#============================================================================#

#=============================================================================
proc createDocType_pssd_ethics { } {

	asset.doc.type.update \
		:create true :type hfi.pssd.ethics  \
		:description "Document type specifying ethics-related items" \
		:label "Consent" \
		:definition < \
		:element -name ethics-id  -min-occurs 0 -max-occurs infinity \
		-type string -index true  < \
		:description "Ethics approval identifier from specified organization" \
		:attribute -name type -type enumeration -min-occurs 0 < \
		:restriction -base enumeration < \
		:dictionary nig.ethics.organization \
		> \
		> \
		> \
		:element -name retain-min  -min-occurs 0 -max-occurs 1 \
		-type float -min 0.0 -index true  < \
		:attribute -type enumeration -name units -enumerated-values "year" -min-occurs 0  \
		:description "Minimum period to retain data; if unspecified, no minimum period." \
		> \
		:element -name retain-max  -min-occurs 0 -max-occurs 1 \
		-type float -min 0.0 -index true  < \
		:attribute -type enumeration -name units -enumerated-values "year" -min-occurs 0  \
		:description "Maximum period to retain data; if unspecified, no maximum period.  Action (destroy, extend) must be taken once the data expires." \
		> \
		>

}

#============================================================================#
proc createPSSDDocTypesEthics {} {

	createDocType_pssd_ethics

}

#============================================================================#
proc destroyPSSDDocTypesEthics {} {

	set doctypes { hfi.pssd.ethics }

	foreach doctype $doctypes {
        destroyDocType $doctype "true"
	}

}

#============================================================================#
#                                                                            #
#============================================================================#
createPSSDDocTypesEthics
