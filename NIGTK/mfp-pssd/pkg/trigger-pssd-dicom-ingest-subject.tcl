##
## This script is triggered when an asset is created in the PSSD namespace. If it is a Subject
## and it was created by the DICOM server, an email is sent to the Project team.
##

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

if { $asset_model == "om.pssd.subject" } {
	set cid          [xvalue asset/cid   $asset_detail]
	set domain       [xvalue asset/creator/domain $asset_detail ]
	
	if { $domain == "dicom" } {
	   set projectCID [getProjectCID $cid]		
   	   set subject "New PSSD Subject $cid has been auto-created" 
	   set body  "Project : $projectCID \n Subject   :  $cid  \n \n This Subject was auto-created because it did not exist. \n You must edit the meta-data with the portal to set human identity meta-data, if required. \n \n regards \n Mediaflux"

	   # Notify project admin(s)
	   om.pssd.project.mail.send :id $projectCID :project-role -explicit false subject-administrator \
		:subject $subject :message $body :async false
		
	   # Notify mediaflux admin as well.  This notification goes to the dicom_notification_recipient
           # that is set up to be the mediaflux admin
	   notification.generate :object -type dicom ingest :event results :data < :subject $subject  :body $body >
	}
}