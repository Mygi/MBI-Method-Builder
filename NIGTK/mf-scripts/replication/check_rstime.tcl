proc check_rstime { cid } {
	set idx 1
	set size 100
	set complete "false"
	set no_rstime_found {}
	while { ${complete} == "false" } {
		set r [asset.query ":size ${size} :idx ${idx} :action get-cid :where cid = '${cid}' or cid starts with '${cid}'"]
		set ccids [xvalues cid ${r}]
		foreach ccid ${ccids} {
			set rstime [xvalue asset/rstime [asset.get :cid ${ccid}]]
			if { ${rstime} == "" } {
				set no_rstime_found [lappend no_rstime_found ${ccid}]
			}
		}
		set complete [xvalue cursor/total/@complete ${r}]
		set idx [xvalue cursor/next ${r}]
	}
	foreach ccid ${no_rstime_found} {
		puts "${ccid} \t\t\t No rstime found."
	}
}