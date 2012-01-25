# Run these scripts as user rep-admin
proc log { event msg } {

	server.log :app replication :event $event :msg "${msg}"

}

proc log_info { msg } {
	
	log "info" $msg
	
}

proc log_error { msg } {
	
	log "error" $msg
	
}

proc log_warning { msg } {
	
	log "warning" $msg
	
}

proc replicate { peer_name where { parts all } } {
	
	asset.replicate.to :peer < :name $peer_name > :parts $parts :where $where :cmode push
	
}

proc compareCids { cid1 cid2 } {
	
	set list1 [split $cid1 .]
	set list2 [split $cid2 .]
	set n1 [lindex $list1 [expr [llength $list1]-1]]
	set n2 [lindex $list2 [expr [llength $list2]-1]]
	if { $n1 == $n2 } {
		return 0
	}
	if { $n1 > $n2 } {
		return 1
	}
	if { $n1 < $n2 } {
		return -1
	}

}

proc sortCids { cids } {
	
	return [lsort -command compareCids $cids]
	
}

proc getLastSection { cid } {
    
    set list [split $cid .]
    return [lindex $list [expr [llength $list]-1]]
    
}

proc peer_dmput { route where } {

	peer $route asset.query :size infinity :action pipe :service -name asset.content.migrate < :action move :destination offline > :where "${where}"

}

proc peer_sync_deletion { route where } {
	
	if { [string first "asset has rid" $where] == -1 } {
		set where "(${where}) and (asset has rid)"
	}
	set size 100
	set idx 1
	set complete "false"
	while { ${complete} == "false" } {
		set r [peer $route asset.query ":size ${size} :idx ${idx} :action get-meta :where ${where}"]
		set ids [xvalues asset/@id ${r}]
		set rids [xvalues asset/rid ${r}]
		if { $ids != {} && $rids != {} } {
			set args ""
			foreach rid ${rids} {
				set args "${args} :id ${rid}"
			}
			set es [xvalues exists [asset.exists ${args}]]
			set args ""
			for { set i 0 } { ${i} < [llength ${es}] } { incr i } {
				set e [lindex ${es} ${i}]
				set id [lindex ${ids} ${i}]
				if { ${e} == "false" } {
					set args "${args} :id ${id}"
				}
			}
			if { ${args} != "" } {
				set args "${args} :members false"
				puts "Deleting assets(${args}) on peer ${route}..."
				peer $route asset.destroy $args
			}
		}
		set complete [xvalue cursor/total/@complete ${r}]
		set idx [xvalue cursor/next ${r}]
	}
	
}

proc pssd_peer_dmput { route cid } {
	
	set where "cid='${cid}' or cid starts with '${cid}'"
	peer_dmput $route ${where}
	
}

proc pssd_peer_sync_deletion { route cid } {
	
	set where "cid = '${cid}' or cid starts with '${cid}'"
	peer_sync_deletion $route $where
	
}

proc pssd_replicate_project { peer_name cid { dmf 0 } } {
	
	# sync deletion on the remote peer
	#set peer_uuid [xvalue "peer/@uuid" [server.peer.describe :name $peer_name]]
	#log_info "Synchronizing deletions for project ${cid} on the remote peer ${peer_name}(uuid=${peer_uuid})..."
	#pssd_peer_sync_deletion $peer_uuid $cid
	
	# replicate meta
	log_info "Replicating meta for project ${cid}..."
	set where "(cid = '${cid}' or cid starts with '${cid}')"
	replicate $peer_name $where "meta"
	
	# replicate content
	log_info "Replicating content for project ${cid}..."
	set where "(cid = '${cid}' or cid starts with '${cid}') and (asset has content)"
	set size 100
	set complete false
	set idx 1
	while { ${complete} == "false" } {
		set r [asset.query ":size ${size} :idx ${idx} :action get-cid :where ${where}"]
		set ccids [xvalues cid $r]
		foreach ccid $ccids {
			set status "unknown"
			if { $dmf != 0 } {
				set status [xvalue asset/state [asset.content.status :cid ${ccid}]]
				if { $status == "offline" } {
					log_info "DMF: dmgetting ${ccid}"
					om.pssd.dmf.get :id ${ccid}
				}
			}
			log_info "Replicating object ${ccid}"
			replicate $peer_name "cid = '${ccid}'" "all"
			if { $dmf != 0 } {
				if { $status == "offline" } {
					log_info "DMF: dmputting ${ccid}"
					om.pssd.dmf.put :id ${ccid}
				}
			}
		}
		set complete [xvalue cursor/total/@complete ${r}]
		set idx [xvalue cursor/next ${r}]
	}
	
	# put content offline on the remote peer
	if { $dmf != 0 } {
	   # Commented out as its the peer admin who should manage this.
	   #log_info "DMF: dmputting ${cid} on remote peer ${peer_name}(uuid=${peer_uuid})."
	   #pssd_peer_dmput $peer_uuid $cid
	}
}

proc pssd_project_cid_root { } {
    
    return [xvalue id\[@name='pssd.project'\] [citeable.named.id.describe]]
    
}

proc pssd_replicate_projects { peer_name projects { startFrom 1 } } {
    
    set projects [sortCids $projects]
    foreach project $projects {
        if { [getLastSection $project] >= $startFrom } {
            try {
                pssd_replicate_project $peer_name $project
            } catch {Throwable type msg trace_ } {
                log_error "Failed to replicate project ${project} to peer ${peer_name}. Error: ${msg}"
            }
        }
    }
    
}


proc pssd_replicate_all_projects { { peer_name cherax } } {
	set cid_root [pssd_project_cid_root]
	set projects [xvalues cid [asset.query :size infinity :action get-cid :where cid in '${cid_root}']]
	puts $cid_root
	puts $projects
	puts $peer_name
	pssd_replicate_projects $peer_name $projects
}


proc pssd_replicate_rsubjects { { peer_name cherax } } {
	set cid_root  [xvalue id\[@name='pssd.r-subject'\] [citeable.named.id.describe]]
	log_info "Replicating r-subjects..."
	asset.replicate.to :peer < :name cherax >  :where cid starts with '${cid_root}'
}

proc pssd_replicate_methods { { peer_name cherax } } {
	set cid_root  [xvalue id\[@name='pssd.method'\] [citeable.named.id.describe]]
	log_info "Replicating methods..."
	asset.replicate.to :peer < :name $peer_name >  :where cid starts with '${cid_root}'
}

proc pssd_replicate_users { { peer_name cherax } } {
	log_info "Replicating users..."
	asset.replicate.to :peer < :name $peer_name >  :where namespace >= /system/users
}

proc pssd_replicate_all { { peer_name cherax } } {
	pssd_replicate_methods $peer_name
	pssd_replicate_rsubjects $peer_name
	pssd_replicate_users $peer_name
	pssd_replicate_all_projects $peer_name
}

proc pssd_check_replication { { peer_name cherax } } {
	
	#
	# Check number of assets for each project on local and remote...
	#
	set project_cid_root [xvalue id\[@name='pssd.project'\] [citeable.named.id.describe]]
	set peer_uuid [xvalue "peer/@uuid" [server.peer.status :name $peer_name]]
	foreach cid [xvalues cid [asset.query :size infinity :action get-cid :where cid in '${project_cid_root}']] {
	    set ln [xvalue value [asset.query :where cid starts with '$cid' :action count]]
	    set rn [xvalue value [peer ${peer_uuid} asset.query :where cid starts with '$cid' :action count]]
	    if { $ln != $rn } {
	        puts "Project ${cid}: \t local = ${ln} \t\t\t ${peer_name} = ${rn}"
	    }	    
	}
	
	#
	# Check number of r-subjects on local and remote...
	#
	set rsubject_cid_root [xvalue id\[@name='pssd.r-subject'\] [citeable.named.id.describe]]
	set ln [xvalue value [asset.query :where cid in '${rsubject_cid_root}' :action count]]
	set rn [xvalue value [peer ${peer_uuid} asset.query :where cid in '${rsubject_cid_root}' :action count]]
	if { $ln != $rn } {
		puts "Number of r-subjects: \t local = ${ln} \t\t\t ${peer_name} = ${rn}"
	}
	
	#
	# Check number of methods on local and remote...
	#
	set method_cid_root [xvalue id\[@name='pssd.method'\] [citeable.named.id.describe]]
	set ln [xvalue value [asset.query :where cid in '${method_cid_root}' :action count]]
	set rn [xvalue value [peer ${peer_uuid} asset.query :where cid in '${method_cid_root}' :action count]]
	if { $ln != $rn } {
		puts "Number of methods: \t local = ${ln} \t\t\t ${peer_name} = ${rn}"
	}
	
}
