# This script detects when the package role structure changed from one
# that uses nif-user to one that does not (stable-2-4).  It destroys roles that
# have been re-restructured so that when the roles are re-created
# they will be in the correct structure.  Another like script (In essentials) 
# will fix up users
#
if { [xvalue exists [authorization.role.exists :role nif-user]] == "true" } {
	puts "pssd installer : the nif-user role exists; destroying old roles pssd.model.user, pssd.model.doc.user, federation-user, pssd.dicom-ingest"
	if { [xvalue exists [authorization.role.exists :role pssd.model.user]] == "true" } {
		puts "   pssd.model.user"
		authorization.role.destroy :role pssd.model.user
	}
	if { [xvalue exists [authorization.role.exists :role pssd.model.doc.user]] == "true" } {
		puts "   pssd.model.doc.user"
		authorization.role.destroy :role pssd.model.doc.user
	}
	if { [xvalue exists [authorization.role.exists :role federation-user]] == "true" } {
		puts "   federation-user"
		authorization.role.destroy :role federation-user
	}
	if { [xvalue exists [authorization.role.exists :role pssd.dicom-ingest]] == "true" } {
		puts "   pssd.dicom-ingest"
		authorization.role.destroy :role pssd.dicom-ingest
	}
}