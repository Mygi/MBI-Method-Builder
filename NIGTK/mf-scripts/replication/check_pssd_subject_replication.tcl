proc check_pssd_subject_replication { peerUUID subjectCID } {
   set n1 [xvalue value [asset.query :action count :where cid starts with '${subjectCID}' or cid = '${subjectCID}']]
   set n2 [xvalue value [peer ${peerUUID} asset.query :action count :where cid starts with '${subjectCID}' or cid = '${subjectCID}']]
   if { $n1 != $n2 } {
      puts "subject ${subjectCID} is not sychronized. (local: ${n1}  --- remote(uuid=${peerUUID}): ${n2}"
   }
}

proc check_pssd_project_replication { peerUUID projectCID } {
   foreach subjectCID [xvalues cid [asset.query :action get-cid :size infinity :where cid in '${projectCID}']] {
       check_pssd_subject_replication $peerUUID $subjectCID
   }
}
