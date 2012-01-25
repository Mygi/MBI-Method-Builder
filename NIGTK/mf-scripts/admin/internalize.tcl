proc list_series { project } {

	set collection ""
	foreach subject [ xvalues id [ asset.query :size infinity :where related to{had-by} (id=$project) ] ] {
		foreach study [ xvalues id [ asset.query :size infinity :where related to{had-by} (id=$subject) ] ] {
			foreach series [ xvalues id [ asset.query :size infinity :where related to{container} (id=$study) ] ] {
				set collection [ concat $collection $series ]
			}
		}
	}
	return $collection

}

proc count_series { project } {
	return [ llength [ list_series $project ] ]
}

proc list_external_series { project } {

	set collection ""
	foreach subject [ xvalues id [ asset.query :size infinity :where related to{had-by} (id=$project) ] ] {
		foreach study [ xvalues id [ asset.query :size infinity :where related to{had-by} (id=$subject) ] ] {
			foreach series [ xvalues id [ asset.query :size infinity :where related to{container} (id=$study) and content is external ] ] {
				set collection [ concat $collection $series ]
			}
		}
	}
	return $collection

}

proc count_external_series { project } {
	return [ llength [ list_external_series $project ] ]
}

proc list_internal_series { project } {

	set collection ""
	foreach subject [ xvalues id [ asset.query :size infinity :where related to{had-by} (id=$project) ] ] {
		foreach study [ xvalues id [ asset.query :size infinity :where related to{had-by} (id=$subject) ] ] {
			foreach series [ xvalues id [ asset.query :size infinity :where related to{container} (id=$study) and content is internal ] ] {
				set collection [ concat $collection $series ]
			}
		}
	}
	return $collection

}

proc count_internal_series { project } {
	return [ llength [ list_internal_series $project ] ]
}

proc list_offline_series { project } {

	set collection ""
	foreach subject [ xvalues id [ asset.query :size infinity :where related to{had-by} (id=$project) ] ] {
		foreach study [ xvalues id [ asset.query :size infinity :where related to{had-by} (id=$subject) ] ] {
			foreach series [ xvalues id [ asset.query :size infinity :where related to{container} (id=$study) and content is offline ] ] {
				set collection [ concat $collection $series ]
			}
		}
	}
	return $collection

}

proc count_offline_series { project } {
	return [ llength [ list_offline_series $project ] ]
}

proc list_online_series { project } {

	set collection ""
	foreach subject [ xvalues id [ asset.query :size infinity :where related to{had-by} (id=$project) ] ] {
		foreach study [ xvalues id [ asset.query :size infinity :where related to{had-by} (id=$subject) ] ] {
			foreach series [ xvalues id [ asset.query :size infinity :where related to{container} (id=$study) and content is online ] ] {
				set collection [ concat $collection $series ]
			}
		}
	}
	return $collection

}

proc count_external_series { project } {
	return [ llength [ list_external_series $project ] ]
}

proc internalize_series { project {method "move"} } {

	set count 0
	foreach subject [ xvalues id [ asset.query :size infinity :where related to{had-by} (id=$project) ] ] {
		foreach study [ xvalues id [ asset.query :size infinity :where related to{had-by} (id=$subject) ] ] {
			foreach series [ xvalues id [ asset.query :size infinity :where related to{container} (id=$study) and content is external ] ] {
				puts -nonewline "Internalizing asset(id=$series)..."
				asset.internalize :method $method :id $series
				puts "done."
				set count [ expr $count+1 ]
			}
		}
	}
	puts "Result: $count assets have been internalized."

}

proc summarize_series { project } {
	
	puts "\tSummary of Project(id=$project)\n"
	puts "\tInternal Series: [list_internal_series $project]\n"
	puts "\tExternal Series: [list_external_series $project]\n"
	puts "\tNumber of Internal Series: [count_internal_series $project]\n"
	puts "\tNumber of External Series: [count_external_series $project]\n"
	puts "\tTotal Number of Series: [count_series $project]\n"

}

proc internalize_all { {method "move"} } {

	foreach asset [ xvalues id [asset.query :size infinity :where content is external ] ] {
		asset.internalize :method $method :id $asset
	}

}

# summarize_series 763
# internalize_series 763 move
# summarize_series 763
