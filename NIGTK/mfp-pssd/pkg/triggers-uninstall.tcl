#
# Uninstall the trigger script for /dicom namespace. The trigger script is triggered when NON-PSSD style
# DICOM data arrived in /dicom namespace. It sends notifications to system admins to notify the data
# arrivals in /dicom namespace.
#

set script        trigger-dicom-ingest.tcl
set scriptNS      system/triggers
set dicomNS       dicom
set label         [string toupper PACKAGE_$package]

if { [xvalue exists [asset.namespace.exists :namespace $dicomNS]] == "false" } {
	puts "Warning: DICOM namespace: ${dicomNS} does not exist."
	return
}

#
# destroy the triggers
#
set ids [xvalues namespace/trigger/@id [asset.trigger.on.list :namespace $dicomNS]]
foreach id $ids {
    asset.trigger.destroy :tid $id :namespace $dicomNS
}

#
# destroy the trigger script asset
#
if { [ xvalue exists [ asset.exists :id path=${scriptNS}/${script} ] ] == "true" } {
	asset.destroy :id path=${scriptNS}/${script}
}



####################################################################################################################################################

#
# Uninstall the trigger scripts for /pssd namespace. 
#

set scriptNS      system/triggers
set pssdNS        pssd
set label         [string toupper PACKAGE_$package]

if { [xvalue exists [asset.namespace.exists :namespace $pssdNS]] == "false" } {
	puts "Warning: PSSD namespace: ${pssdNS} does not exist."
	return
}

#
# destroy the triggers
#
set ids [xvalues namespace/trigger/@id [asset.trigger.on.list :namespace $pssdNS]]
foreach id $ids {
    asset.trigger.destroy :tid $id :namespace $pssdNS
}

#
# destroy the trigger script assets
#
set script        trigger-pssd-dicom-ingest.tcl
if { [ xvalue exists [ asset.exists :id path=${scriptNS}/${script} ] ] == "true" } {
	asset.destroy :id path=${scriptNS}/${script}
}

set script        trigger-pssd-dicom-ingest-subject.tcl
if { [ xvalue exists [ asset.exists :id path=${scriptNS}/${script} ] ] == "true" } {
	asset.destroy :id path=${scriptNS}/${script}
}


