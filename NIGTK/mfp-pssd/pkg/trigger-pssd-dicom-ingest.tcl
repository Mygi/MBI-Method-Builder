##
## This script is triggered when an asset is modified in the PSSD namespace. It sends
## an email notification to the project team if this was a Study
##

# This is probably very very bad Tcl; please fix Wilson ! ; nebk
proc getProjectCID { cid } {
	set y [split $cid "."]
	set c1 [lindex $y 0]
	set c2 [lindex $y 1]
	set c3 [lindex $y 2]
	set projectCID "$c1.$c2.$c3"
	return $projectCID	
}

# Main
set asset_detail [asset.get :id $id]
set asset_model  [xvalue asset/model $asset_detail]

if { $asset_model == "om.pssd.study" } {
	set cid          [xvalue asset/cid   $asset_detail]
	set projectCID [getProjectCID $cid]
#		
	set subject "New PSSD DICOM data for Project $projectCID has been uploaded"
	set body  "Project : $projectCID \n Study   :  $cid  \n \n regards \n Mediaflux"
	om.pssd.project.mail.send :id $projectCID :use-notification -category data-upload true :subject $subject :message $body :async false
}