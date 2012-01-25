##
## Note: 1. You only need to modify the configuration section to configure the
##       the peers and authentication. You will need to run this script on
##       both peers to complete the configuration.
##
##       2. By default, the federation is configured to be uni-directional, i.e.
##       you can only make peer calls on the source peer to execute services 
##       on the dst peer. You can set fed_bidirectional to 1 to set the federation
##       to bidirectional, i.e. peers can make remote calls to each other. 
##
##       3. The peer-admin and rep-admin users contains only the permissions needed
##       to create peers for admin purposes (establish peers, replicate content).
##       The normal federated end-users needs their own permissions to
##       execute services on the remote peer. In other words, after execution of
##       this script, you still need to set up permissions for the users to make
##       remote calls (depends on the their need).
##
##       4. This setup assumes replication will be done with :cmode push.  For pull
##          mode (which is idem potent) additional setup must be done.
##
proc getServer { target servers } {

    foreach server $servers {
	if { [lindex $server 0] == $target } {
	    return $server
	}
    }
    return { "Server not found" }
    
}

#
# Servers that may be of interest to us for federation purposes
# the tag is an arbitrary string that we attach to peers so that
# when federating, subsets can be selected
# tags are :
#    pssd-user (peers users can federate with)
#    pssd-admin (peers for administration purposes only e.g. mirrors)
#    pssd-devel (peers which are development machines).
# Peers can have multiple tags

set servers                 { {soma    1004 172.23.65.3 http true 8443 pssd-user} \
			      {cluster 1005 cluster.neuroimaging.org.au  http true 8443 pssd-devel} \
	                      {cherax 1028 138.194.75.20 http true 8443 pssd-admin} \
	                      {nebk-laptop 101 192.168.20.59 http true 8443 pssd-devel} \
			      {vera059 1035 vera059.monash.its.edu http true 8443 pssd-admin} \
                            }

#
# Select desired source and destination peers for this execution run.
# 
set src soma
set server         [getServer $src $servers]
set src_name       [lindex $server 0]
set src_uuid       [lindex $server 1]
set src_host       [lindex $server 2]
set src_type       [lindex $server 3]
set src_ssl        [lindex $server 4]
set src_port       [lindex $server 5]
set src_tag        [lindex $server 6]
#
set dst vera059
set server         [getServer $dst $servers]
set dst_name       [lindex $server 0]
set dst_uuid       [lindex $server 1]
set dst_host       [lindex $server 2]
set dst_type       [lindex $server 3]
set dst_ssl        [lindex $server 4]
set dst_port       [lindex $server 5]
set dst_tag        [lindex $server 6]


# This user will be used only to establish the peer connection between hosts.
set peer_admin_user       peer-admin
set peer_admin_domain     system
set peer_admin_passwd     Peer_me8
set peer_admin_svc_perms      { {service server.ping ACCESS} \
                                {service server.identity ACCESS} \
                                {service server.peer.execute FEDERATE} }

# This user will be used for replicating content
set rep_admin_user        rep-admin
set rep_admin_domain      system
set rep_admin_passwd      Rep_me8

# Service and role permissions for the source and destination peers
# Some of these are needed by our wrapper admin scripts (see pssd-replication.tcl)
set src_rep_admin_svc_perms       { {service asset.replicate.to ACCESS} \
	                            {service server.uuid ACCESS} \
	                            {service server.log MODIFY} \
				    {service server.peer.list} \
				    {service server.peer.describe} \
				    {service om.pssd.dmf.get MODIFY } \
				    {service om.pssd.dmf.put MODIFY } }
}
set dst_rep_admin_svc_perms      { {service asset.archive.restore MODIFY} \
				   {service server.uuid ACCESS} \
	                           {service om.pssd.dmf.put MODIFY } }
}
# GIve enough roles so that when log in as rep-admin can function 
# Pssd.object.admin also required so that re-admin can overwrite existing
# assets in archive restores
set src_rep_admin_role_perms      { nif-user pssd.object.admin pssd.model.user pssd.subject.create }
set dst_rep_admin_role_perms      { nif-user pssd.object.admin pssd.model.user pssd.subject.create }

# set to 1, if the federation is bidirectional.
# 0 means we are only replicating from src to dst)
set fed_bidirectional   0

# =========================================================================== #
# PROC
# =========================================================================== #

# Create a user in the default (internal) authority
proc create_user { domain user passwd perms } {

    if { [xvalue exists [user.exists :domain $domain :user $user]] == "false" } {
	user.create :domain $domain :user $user :password $passwd
    }
    foreach perm $perms {
	set rsType     [lindex ${perm} 0]
	set rsName     [lindex ${perm} 1]
	set rsAccess   [lindex ${perm} 2]
	actor.grant :type user :name ${domain}:${user} \
	    :perm < :resource -type ${rsType} ${rsName} :access ${rsAccess} >
    }

}

# Create a user with a specified external authority
proc create_auth_user { authority domain user passwd perms } {
    if { [xvalue exists [authentication.domain.exists :authority $authority :domain $domain]] == "false" } {
	authentication.domain.create :authority $authority :domain $domain
    }

    if { [xvalue exists [authentication.user.exists :authority $authority :domain $domain :user $user]] == "false" } {
	authentication.user.create :authority $authority :domain $domain :user $user :password $passwd
    }
	
    foreach perm $perms {
	set rsType     [lindex ${perm} 0]
	set rsName     [lindex ${perm} 1]
	set rsAccess   [lindex ${perm} 2]
	actor.grant :type user :name ${authority}:${domain}:${user} \
	    :perm < :resource -type ${rsType} ${rsName} :access ${rsAccess} >
    }

}


# Add a peer 
proc add_peer { name host type ssl port tag auth_domain auth_user auth_passwd } {

	if { [xvalue exists [server.peer.exists :name $name]] == "false" } {
		server.peer.add :address < :host $host :type $type :ssl $ssl :port $port > \
		                :authentication < :domain $auth_domain :user $auth_user :password $auth_passwd > \
		                :name $name :tag $tag
	} 

}

# Grant a list of roles to the specicied user
proc grantRoles { user roles } {	
	foreach role $roles {
		actor.grant :name $user :type user :role -type role $role
	}
	
}


# =========================================================================== #
# MAIN
#
# Execute this script first on the destination server and then the source server
#
# Uni-directional:
#    Source peer:
#       User rep-admin (default authority)
#       Peer dest added
#    Dest peer:
#       User peer-admin (default authority)
#       User rep-admin (Source authority)
#
# Bi-directional plus:
#   src peer-admin
#   peer src added to dst
# =========================================================================== #

# On the sourve host
if { [xvalue uuid [server.uuid]] == "${src_uuid}" } {
	# Create source server system:rep-admin user in the default authority and grant needed roles
	create_user $rep_admin_domain $rep_admin_user $rep_admin_passwd $src_rep_admin_svc_perms
	grantRoles ${rep_admin_domain}:${rep_admin_user} $src_rep_admin_role_perms
	
	
	# Add the destination as a peer.  We need to specify the authentication for the remote host (we use the peer-admin user)
	add_peer $dst_name $dst_host $dst_type $dst_ssl $dst_port $dst_tag \
	         $peer_admin_domain $peer_admin_user $peer_admin_passwd
	
	# If bi-directional, we also need the system:peer-admin user in the default authority on the source host.
	if { $fed_bidirectional } {
		create_user $peer_admin_domain $peer_admin_user $peer_admin_passwd $peer_admin_svc_perms 
	}
}

# On the destination host
if { [xvalue uuid [server.uuid]] == "${dst_uuid}" } {
	# Create the system:peer-admin host in the default authority. The source host will use this user to establish peers.
	create_user $peer_admin_domain $peer_admin_user $peer_admin_passwd $peer_admin_svc_perms 

	# Create the system:rep-admin user in the source server authority (that's what it will authenticate as)
	create_auth_user ${src_uuid} $rep_admin_domain $rep_admin_user $rep_admin_passwd $dst_rep_admin_svc_perms 
	grantRoles ${rep_admin_domain}:${rep_admin_user} $dst_rep_admin_role_perms
	
	# If bi-directional, also add the source server as a peer.
	if { $fed_bidirectional } {
		add_peer $src_name $src_host $src_type $src_ssl $src_port $tag \
		         $peer_admin_domain $peer_admin_user $peer_admin_passwd
	}
}
