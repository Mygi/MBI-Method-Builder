# 
# Procedure: change_acls
# Description: change the pssd.project.subject.admin.${id} acl from read to read-write to study & dataset assets
# Arguements: 
#             id   -    The citeable id of the project
#
proc change_acls { id } {
	set size 200
	set idx 1
	set result [asset.query :where cid starts with '${id}' and (model='om.pssd.study' or model='om.pssd.dataset') :size ${size} :idx ${idx}]
	set count [xvalue cursor/count ${result}]
	while { ${count} != "0" } {
		set assetIds [xvalues id ${result}]
		foreach assetId ${assetIds} {
			asset.acl.revoke :acl < :id ${assetId} :actor -type role pssd.project.subject.admin.${id} >
			asset.acl.grant :acl < :id ${assetId} :actor -type role pssd.project.subject.admin.${id} :access read-write :propagate true >
		}
		set idx [expr ${idx} + ${size}]
		set result [asset.query :where cid starts with '${id}' and (model='om.pssd.study' or model='om.pssd.dataset') :size ${size} :idx ${idx}]
		set count [xvalue cursor/count ${result}]
	}
}
