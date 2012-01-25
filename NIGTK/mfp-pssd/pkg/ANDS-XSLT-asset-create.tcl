#Create an asset holding the XSLT to convert the result of  om.pssd.project.metadata-harvest
# into the Melbourne University RIF CS schema.  EVentually, this will be scraped for the ANDS
# national data commons registry
#
# Although the meta-data are populated by domain independent and research domain dependent steps, the meta-data
# schema that is ultimately to be transformed is not research domain dependent. Therefore this XSLT
# is appropriate to the pssd package
################################################################################################################################################
proc install-XSLT-asset { } {

    set script        DarisToRIFCS.xsl
    set scriptNS      system/XSLT
    set label         DARIS-TO-MU-RIFCS

    # If already exists return
    if { [xvalue id [asset.query :where name='$script']] != "" } {
	return
    }

    #
    # create the asset
    #
    asset.create :url archive:///$script \
	:namespace -create yes $scriptNS \
	:label -create yes $label :label PUBLISHED \
	:name $script
}

#################################################################################################################################

install-XSLT-asset
