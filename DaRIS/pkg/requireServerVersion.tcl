set requiredVersion @requiredVersion@
	
set a [string trimleft [lindex [split $requiredVersion . ] 0] 0]
set b [string trimleft [lindex [split $requiredVersion . ] 1] 0]
set c [string trimleft [lindex [split $requiredVersion . ] 2] 0]
set currentVersion [xvalue version [server.version]]
set sa [string trimleft [lindex [split $currentVersion . ] 0] 0]
set sb [string trimleft [lindex [split $currentVersion . ] 1] 0]
set sc [string trimleft [lindex [split $currentVersion . ] 2] 0]
set meet 1
if { $a > $sa } {
	set meet 0
}
	
if { $b > $sb } {
	set meet 0
}
	
if { $c > $sc } {
	set meet 0
}	
	
if { !$meet } {
	error "This package requires Mediaflux server ${requiredVersion} or higher. Found Mediaflux ${currentVersion}."
}
