# This script detects when the package role structure changed from one
# that uses nif-user to one that does not (stable-2-4) and 
# fixes up user accounts with the new roles. 
#
proc hasDICOMRole { roles } {
    foreach role $roles {
	if { $role == "dicom-ingest" } {
	    return "true"
	}
    }
    return "false"
}




if { [xvalue exists [authorization.role.exists :role nif-user]] == "true" } {
    puts "essentials installer : the nif-user role exists; migrating users to new role structure"


    # Find and fix the users
    foreach name [xvalues actor/@name [actors.granted :role -type role nif-user :type user]] {
	puts "Migrating standard user $name"
	actor.grant :name $name :role -type role nig.pssd.model.user :type user
	actor.grant :name $name :role -type role pssd.model.user :type user
    }

    # Destroy nif-user role
    authorization.role.destroy :role nif-user

    # Now fix up DICOM users if they hold the dicom-ingest role directly
    foreach user [xvalues user [user.list :domain dicom]] {
	set name "dicom:$user"	
	set roles [xvalues actor/role [actor.describe :name $name :type user]]
	set t [hasDICOMRole $roles]
	if { $t == "true" } {
	    puts "Migrating DICOM user $name"
	    actor.revoke :name $name :role -type role dicom-ingest :type user
	    actor.grant :name $name :role -type role nig.pssd.dicom-ingest :type user
	    actor.grant :name $name :role -type role pssd.dicom-ingest :type user
	}
    }
}
