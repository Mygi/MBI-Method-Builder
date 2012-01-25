proc push { peer_name min max where } {

	for { set i $min } { $i<=$max } { incr i } {
		set to "'now-${i}day'"
		set j [expr {$i+1}]
		set from "'now-${j}day'"
		after 500
		puts "pushing assets modified on ${from}..."
		set w "(mtime>=${from} and mtime<${to}) and (${where})"
		
		asset.replicate.to :peer < :name ${peer_name} > :cmode push :parts all :where $w
		puts "done"
	}
}

proc push_to_cherax { min max } {
	
	push cherax $min $max "namespace>=/pssd"
}

proc push_to_vera059 { min max } {
	puts vera059 $min $max "(cid='1.5.39' or cid starts with '1.5.39' or cid='1.5.48' or cid starts with '1.5.48' or cid='1.5.50' or cid starts with '1.5.50')"
}