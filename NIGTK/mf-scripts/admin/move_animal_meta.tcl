########################################################################################################################################
# pssd script
# Moves hfi.pssd.subject and hfi.pssd.animal.subject from ns=public to ns=private on specified subject
# This was a one-off to hide specific meta-data so Heena could do a blind analysis
#
#  Usage:  script.execute :in file:/path/move_animal_meta.tcl :arg name cid <citable id of subject>
########################################################################################################################################

proc retrofit { cid } {


# Get asset
    set assetDetail [asset.get :cid $cid]
    set id [xvalue asset/@id $assetDetail]
    puts $id

# Fetch detail
    set model [xvalue asset/model $assetDetail]
    if { $model != "om.pssd.subject" } {
		puts "Given object is not a subject"
		return
    }

# Move public to private

    set ns [xvalue asset/meta/hfi.pssd.subject/@ns $assetDetail]
    if { $ns == "pssd.public" } {
         nig.asset.doc.copy :from $id :to $id :action add :doc hfi.pssd.subject :namespace pssd.private
         asset.set :id $id :meta -action remove < :hfi.pssd.subject -ns pssd.public >
    }
    set ns [xvalue asset/meta/hfi.pssd.animal.subject/@ns $assetDetail]
    if { $ns == "pssd.public" } {
         nig.asset.doc.copy :from $id :to $id :action add :doc hfi.pssd.animal.subject :namespace pssd.private
         asset.set :id $id :meta -action remove < :hfi.pssd.animal.subject -ns pssd.public >
    }

}


##
## Main
##
retrofit $cid