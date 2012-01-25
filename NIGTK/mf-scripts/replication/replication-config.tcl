# =========================================================================== #
#                                                                             #
#   Note: this is the script to be executed on Mediaflux Servers to set up    #
#
# the peers on both sides. To customise the script, modify the configuration  #
# section. After customisation/configuration you can run:                     #                                                         
#                                                                             #
#       script.execute :in file:/path/to/configure-pssd-replication.tcl       #
#                                                                             #
# in aterm on both Mediaflux servers. It will create the peers and required   #
# authentication domains and perms.                                           #
#                                                                             #
# I [nebk] believe this script is deprecated (see                             #
#   federation/federation-admin-config.tcl)                                   #
# =========================================================================== #







###############################################################################
#                                                                             #
#   Section 1: Functions                                                      #
#                                                                             # 
###############################################################################

proc create_peer { name host port type ssl domain user password} {

	if { [xvalue exists [server.peer.exists :name $name]] == "false" } {
		server.peer.add :name $name \
			:address < :host $host :type $type :port $port :ssl $ssl > \
			:authentication < :domain $domain :user $user :password $password >
	}
	
	set uuid [xvalue "peer/@uuid" [server.peer.status :name $name]] 

	if { $uuid == "" } {
		error "Could not ascertain peer UUID"
	}
	
	return $uuid
	
}

proc create_auth { authority domain user } {

	# the authority is equal to the uuid of the peer
	if { [xvalue exists [authentication.domain.exists :authority $authority :domain $domain]] == "false" } {
		authentication.domain.create :authority $authority :domain $domain
		authentication.user.create :authority $authority :domain $domain :user $user
	}
	
}

proc grant_roles { authority domain user roles } {

	foreach role $roles {
		actor.grant :type user :name $authority:$domain:$user :role -type role $role
	}

}

proc grant_services { authority domain user services } {

	foreach sl $services {
		set service [lindex $sl 0]
		set access [lindex $sl 1]
		actor.grant :type user :name $authority:$domain:$user :perm < :access $access :resource -type service $service >
	}

}

proc setup_peer { name host port type ssl domain user password roles services } {
	
	# create peer
	set uuid [create_peer $name $host $port $type $ssl $domain $user $password]
	
	set authority $uuid
	
	# create auth domain & user
	create_auth $authority $domain $user
	
	# grant roles
	grant_roles $authority $domain $user $roles
	
	# grant services
	grant_services $authority $domain $user $services
	
}



###############################################################################
#                                                                             #
#   Section 2: Configuration                                                  #
#                                                                             #
###############################################################################

set src_peer_name             soma
set src_peer_host             dicom-soma.ee.unimelb.edu.au
set src_peer_port             8443
set src_peer_type             http
set src_peer_ssl              true
set src_peer_uuid             1004
set src_auth_domain           system
set src_auth_user             manager
set src_auth_passwd           Guess_me8
set src_auth_roles_on_src     { pssd.object.admin }
set src_auth_services_on_src  { {asset.get ACCESS} \
								{server.uuid ACCESS} }
set src_auth_roles_on_dst     { pssd.object.admin }
set src_auth_services_on_dst  { {asset.archive.restore MODIFY} \
								{asset.query ACCESS} \
								{asset.content.migrate MODIFY} \
								{asset.get ACCESS} \
								{asset.destroy MODIFY} \
								{asset.label.add MODIFY} \
								{server.uuid ACCESS} }

set dst_peer_name             cherax
set dst_peer_host             cherax.hpsc.csiro.au
set dst_peer_port             3673
set dst_peer_type             http
set dst_peer_ssl              true
set dst_peer_uuid             1028
set dst_auth_domain           system
set dst_auth_user             manager
set dst_auth_passwd           Guess_me8

set src_namespace             pssd
set job_name                  pssd





###############################################################################
#                                                                             #
#   Section 3: Main                                                           #
#                                                                             #
###############################################################################

set uuid [xvalue "uuid" [server.uuid]]
if { $uuid == $src_peer_uuid } {

	puts "Setting up dst_peer on src_peer... ..."

	# create dst_peer on src server
	set dst_peer_uuid [create_peer $dst_peer_name \
							$dst_peer_host $dst_peer_port $dst_peer_type $dst_peer_ssl \
							$dst_auth_domain $dst_auth_user $dst_auth_passwd]

	# create src_auth_domain and src_auth_user on src server
	create_auth $src_peer_uuid $src_auth_domain $src_auth_user
	
	# grant roles to src_auth_user on src server
	grant_roles $src_peer_uuid $src_auth_domain $src_auth_user $src_auth_roles_on_src
	
	# grant services to src_auth_user on src server
	grant_services $src_peer_uuid $src_auth_domain $src_auth_user $src_auth_services_on_src
	
} elseif { $uuid == $dst_peer_uuid } {

	puts "Setting up src_peer on dst_peer... ..."

	# create src_peer on dst server
	set src_peer_uuid [create_peer $src_peer_name \
							$src_peer_host $src_peer_port $src_peer_type $src_peer_ssl \
							$src_auth_domain $src_auth_user $src_auth_passwd]

		
	# create src_auth_domain and src_auth_user on src server
	create_auth $src_peer_uuid $src_auth_domain $src_auth_user
	
	# grant roles to src_auth_user on dst server
	grant_roles $src_peer_uuid $src_auth_domain $src_auth_user $src_auth_roles_on_dst
	
	# grant services to src_auth_user on dst server
	grant_services $src_peer_uuid $src_auth_domain $src_auth_user $src_auth_services_on_dst
	
	# set up a label to be used by deletion synchronisation
	if { [xvalue exists [asset.label.type.exists :label TO_SYNC_DEL]] == "false" } {
		asset.label.type.create :description "used for deletion synchronisations" :label TO_SYNC_DEL
	}

}


