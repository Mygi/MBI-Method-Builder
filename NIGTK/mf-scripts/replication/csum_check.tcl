proc check_and_destroy { peer_name where } {

	set peer_uuid [xvalue peer/@uuid [server.peer.describe :name ${peer_name}]]
	set uuid [xvalue uuid [server.uuid]]
	set size 10
	set idx 1
	set complete "false"
	while { ${complete} == "false" } {
		set r [peer ${peer_uuid} asset.query :idx $idx :size $size :where "(asset has rid) and (asset has content) and (${where})" :action get-meta]
		set count [xcount asset $r]
		if { $count>0 } { 
			set ids [xvalues asset/@id $r]
			set rids [xvalues asset/rid $r]
			set cids [xvalues asset/cid $r]
			set csums [xvalues asset/content/csum $r]
			for { set i 0 } { $i<$count } { incr i } {
				set id [lindex $ids $i]
				set cid [lindex $cids $i]
				set rid [lindex $rids $i]
				set csum [lindex $csums $i]
				if { [xvalue exists [asset.exists :id ${rid}]] == "true" } {
				if { "${rid}" == "1004.84541" } {
					server.log :app "fix_replication_to_${peer_name}" :event info :msg  "ooooooooooooooooooooooooooooooooo"
				}
					if { [string first $uuid $rid] == 0 } {
						set lr [asset.get :id $rid]
						set lcid [xvalue asset/cid $lr]
						set lcsum [xvalue asset/content/csum $lr]
						if { [string compare "${csum}" "${lcsum}"] != 0 } {
							server.log :app "fix_replication_to_${peer_name}" :event info :msg "Replica id=${id},cid=${cid} on ${peer_name} has invalid content(csum=${csum}). While primary id=${rid},cid=${lcid} has content(csum=${lcsum})."
							server.log :app "fix_replication_to_${peer_name}" :event info :msg  "Destroying id=${id},cid=${cid} on ${peer_name}..."
							peer ${peer_uuid} asset.destroy :id ${id}
							server.log :app "fix_replication_to_${peer_name}" :event info :msg "done"
							
							server.log :app "fix_replication_to_${peer_name}" :event info :msg "Re-replicating ${rid} to ${peer_name}..."
							asset.replicate.to :peer < :name ${peer_name} > :id $rid :parts all :cmode push 
							server.log :app "fix_replication_to_${peer_name}" :event info :msg "done"
						}
					}
				} else {
					server.log :app "fix_replication_to_${peer_name}" :event info :msg  "Replica id=${id},cid=${cid} on ${peer_name} does not exist on local primary server. Destroying..."
					peer ${peer_uuid} asset.destroy :id ${id}
					server.log :app "fix_replication_to_${peer_name}" :event info :msg "done"
				}
			}
		}
		set complete [xvalue cursor/total/@complete ${r}]
		set idx [xvalue cursor/next ${r}]
	}
}