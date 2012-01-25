##############################################################################
#                                                                            #
#                             Uninstaller Script                             #
#                                     for                                    #
#                           Mediaflux Package: daris                         #
#                                                                            #
##############################################################################

set label [string toupper PACKAGE_$package]

#
# Destroy namespace
#
set namespace /www/${package}
if { [xvalue exists [asset.namespace.exists :namespace ${namespace}]] == "true" } { 
	puts "Uninstalling package ${package} -- Removing namespace: ${namespace}"
	asset.namespace.destroy :namespace ${namespace}
}

#
# Destroy HTTP processor
#
set url /${package}
if { [xvalue exists [http.processor.exists :url ${url}]] == "true" } {
	puts "Uninstalling package ${package} -- Removing http processor: url=${url}"
	http.processor.destroy :url ${url}
}



