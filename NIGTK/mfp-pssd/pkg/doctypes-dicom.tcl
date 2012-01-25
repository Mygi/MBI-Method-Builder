#============================================================================#
# This script create document types for use with DICOM format data.  It      #
# is used to augment meta-data  defined in Mediaflux' native DICOM DocTypes  #
# mf-dicom-study, mf-dicom-series                                            #
#============================================================================#

#=============================================================================
proc createDocType_pssd_dicom_series { } {

	asset.doc.type.update \
		:create true :type hfi.pssd.dicom.series \
		:label "DICOM Series" \
		:description "Document type for DICOM Series supplementary meta-data" \
		:definition < \
		:element -name image_position_patient  -type document \
		-index true -min-occurs 0 -max-occurs infinity < \
		:description "Image Position (Patient) from tag (0020,0032)" \
		:element -name value -type float  -min-occurs 0 -max-occurs 3  -index true \
		< :description "Value (3 in total)" > \
		> \
		:element -name image_orientation_patient  -type document \
		-index true -min-occurs 0 -max-occurs infinity < \
		:description "Image Orientation (Patient) from tag (0020,0037)" \
		:element -name value -type float  -min-occurs 0 -max-occurs 6  -index true \
		< :description "Value (6 in total)" > \
		> \
		>

}


#============================================================================#
proc createPSSDDocTypesDicom {} {
	
	createDocType_pssd_dicom_series
	
}

#============================================================================#
proc destroyPSSDDocTypesDicom {} {

	set doctypes { hfi.pssd.dicom.series }

	foreach doctype $doctypes {
        destroyDocType $doctype "true"
	}

}

#============================================================================#
#                                                                            #
#============================================================================#
createPSSDDocTypesDicom