##
## The proc to fix/delete the assets within the specified project on the remote peer with rids deleted locally or with conflicting cids (caused by re-using cids or changing cids)
##
proc fix_project_replication { peerUUID projectCID } {
    set size     100
    set idx      1
    set complete "false"
    while { $complete == "false" } {
        set r [peer $peerUUID asset.query :action get-meta :size $size :idx $idx :where "asset has rid and (cid='${projectCID}' or (cid starts with '${projectCID}'))"]
        set ids  [xvalues "asset/@id" $r]
        set rids [xvalues "asset/rid" $r]
        set cids [xvalues "asset/cid" $r]
        if { $ids != {} } {
            for { set i 0 } { $i < [llength $ids] } { incr i } {
                set id  [lindex $ids  $i]
                set cid [lindex $cids $i]
                set rid [lindex $rids $i]
                #
                # delete the asset on the remote peer if it does not exist on the local peer
                #
                if { [xvalue exists [asset.exists :id $rid]] == "false" } {
                    puts "deleting asset(cid=${cid}, rid=${rid}, id=${id}) on peer ${peerUUID}(Reason: does not exist locally)..."
                    peer $peerUUID asset.destroy :members false :id $id
                }
                #
                # delete the asset on the remote peer if its cid does not match the asset's cid on the local peer
                #
                if { [xvalue asset/cid [asset.get :id $rid]] != $cid } {
                    puts "deleting asset(cid=${cid}, rid=${rid}, id=${id}) on peer ${peerUUID}(Reason: conflicting cids)..."
                    peer $peerUUID asset.destroy :members false :id $id
                }
            }
        }
        set complete [xvalue "cursor/total/@complete" $r]
        set idx [xvalue "cursor/next" $r]
    }
}

##
## The proc to fix/delete all the assets on the remote peer with rids deleted locally or with conflicting cids (caused by re-using cids or changing cids)
##
proc fix_replication { peerUUID } {
    foreach cid [xvalues cid [asset.query :action get-cid :where "cid in '1.5'"]] {
        fix_project_replication $peerUUID $cid
    }
}
