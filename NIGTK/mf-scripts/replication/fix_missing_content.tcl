#
# A script to find the dataset assets missing content on the remote peer, and re-replicate them from the source peer.
# Note: it need to be executed on the source peer as the rep user (with rep permssions).
#
proc fix_missing_content { peer } {

    set uuid [xvalue peer/@uuid [server.peer.describe :name $peer]]
    foreach cid [xvalues cid [peer $uuid asset.query :where model='om.pssd.dataset' and ( asset hasno content ) :action get-cid :size infinity]] {
         puts "re-replicating ${cid} to ${peer}..."
         asset.replicate.to :peer < :name $peer > :cmode push :where cid='${cid}'
    }

}
