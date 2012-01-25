# prune the result assets of a query

proc prune { where } {

	set size 100
	set idx 1
	set complete "false"
	while { ${complete} == "false" } {
		set r [asset.query :idx ${idx} :size ${size} :where ${where}]
		puts -nonewline "pruning result from ${idx} [expr ${idx} + 99]..."
		foreach id [xvalues id ${r}] {
			asset.prune :id ${id}
		}
		puts "done"
		set complete [xvalue cursor/total/@complete ${r}]
		set idx [xvalue cursor/next ${r}]
	}	
}