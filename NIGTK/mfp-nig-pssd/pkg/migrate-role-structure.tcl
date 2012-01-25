# This script detects when the package role structure changed from one
# that uses nif-user to one that does not (stable-2-4).  It destroys roles that
# have been re-restructured so that when the roles are re-created
# they will be in the correct structure.  
#
# It then fixes up user accounts with the new roles. 
#

if { [xvalue exists [authorization.role.exists :role nif-user]] == "true" } {
	puts "nig-pssd installer : the nif-user role exists; destroying old roles nig.pssd.model.user, nig.pssd.dicom-ingest"
	if { [xvalue exists [authorization.role.exists :role nig.pssd.model.user]] == "true" } {
		puts "   nig.pssd.model.user"
		authorization.role.destroy :role nig.pssd.model.user
	}
	if { [xvalue exists [authorization.role.exists :role pssd.model.doc.user]] == "true" } {
		puts "   nig.pssd.dicom-ingest"
		authorization.role.destroy :role nig.pssd.dicom-ingest
	}
}