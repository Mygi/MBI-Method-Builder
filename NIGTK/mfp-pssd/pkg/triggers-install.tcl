#
# Install the trigger script for /dicom namespace. The trigger script is triggered when NON-PSSD style
# DICOM data arrived in /dicom namespace. It sends notifications to system admins to notify the data
# arrivals in /dicom namespace.
#
################################################################################################################################################
proc installDICOMTrigger { package } {

    set script        trigger-dicom-ingest.tcl
    set scriptNS      system/triggers
    set dicomNS       dicom
    set label         [string toupper PACKAGE_$package]

    if { [xvalue exists [asset.namespace.exists :namespace $dicomNS]] == "false" } {
	puts "Warning: DICOM namespace: ${dicomNS} does not exist."
	return
    }

    #
    # create the trigger script asset
    #
    asset.create :url archive:///$script \
	:namespace -create yes $scriptNS \
	:label -create yes $label :label PUBLISHED \
	:name $script


    #
    # create the triggers
    #
    asset.trigger.post.create :namespace $dicomNS :event create :script -type ref ${scriptNS}/${script}
    asset.trigger.post.create :namespace $dicomNS :event modify :script -type ref ${scriptNS}/${script}
}

################################################################################################################################

# Install trigger for user notification in PSSD namespace of new Studies
#

proc installPSSDStudyTrigger { package } {

    set script        trigger-pssd-dicom-ingest.tcl
    set scriptNS      system/triggers
    set pssdNS        pssd
    set label         [string toupper PACKAGE_$package]

    if { [xvalue exists [asset.namespace.exists :namespace $pssdNS]] == "false" } {
	puts "Warning: PSSD namespace: ${pssdNS} does not exist."
	return
    }

    #
    # create the trigger script asset
    #
    asset.create :url archive:///$script \
	:namespace -create yes $scriptNS \
	:label -create yes $label :label PUBLISHED \
	:name $script

    #
    # create the trigger. We must trigger on modify because we don't users, who are pre-creating
    # Studies to get an email then....  The DICOM server will modify the Study when it sets meta-data
    # on it, whether the Study is pre-created or not.
#    asset.trigger.post.create :namespace $pssdNS :event create :script -type ref ${scriptNS}/${script}
    asset.trigger.post.create :namespace $pssdNS :event modify :script -type ref ${scriptNS}/${script}
}


################################################################################################################################


# Install trigger for user notification in PSSD namespace of new Subject created by the server
#

proc installPSSDSubjectTrigger { package } {

    set script        trigger-pssd-dicom-ingest-subject.tcl
    set scriptNS      system/triggers
    set pssdNS        pssd
    set label         [string toupper PACKAGE_$package]

    if { [xvalue exists [asset.namespace.exists :namespace $pssdNS]] == "false" } {
	puts "Warning: PSSD namespace: ${pssdNS} does not exist."
	return
    }

    #
    # create the trigger script asset
    #
    asset.create :url archive:///$script \
	:namespace -create yes $scriptNS \
	:label -create yes $label :label PUBLISHED \
	:name $script
    
    # create the trigger. The trigger script will work out whether to send the email or not
    asset.trigger.post.create :namespace $pssdNS :event create :script -type ref ${scriptNS}/${script}
}



### Main
source triggers-uninstall.tcl
#
installDICOMTrigger $package
installPSSDStudyTrigger $package
installPSSDSubjectTrigger $package
