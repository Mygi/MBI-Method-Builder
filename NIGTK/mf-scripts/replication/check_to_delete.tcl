proc check_to_delete { peer where } {
	
	if { [string first "asset has rid" $where] == -1 } {
		set where "(${where}) and (asset has rid)"
	}
	set size 100
	set idx 1
	set complete "false"
	while { ${complete} == "false" } {
		set r [peer $peer asset.query ":size ${size} :idx ${idx} :action get-meta :where ${where}"]
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
                                        server.log :app "check-to-delete" :event "info" :msg ":id ${id}" 
				}
			}
		}
		set complete [xvalue cursor/total/@complete ${r}]
		set idx [xvalue cursor/next ${r}]
	}
	
}


