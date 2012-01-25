##############################################################################
#                                                                            #
#                               Installer Script                             #
#                                      for                                   #
#                           Mediaflux Package: daris                         #
#                                                                            #
##############################################################################

#
# Check server version requirements
#
source requireServerVersion.tcl

set label [string toupper PACKAGE_$package]
set entryPoint DaRIS.html

#
# Set up authentication
#
set domain www-public
set user www-public
if { [xvalue exists [authentication.domain.exists :domain www-public]] == "false" } {
	authentication.domain.create :domain www-public
	authentication.user.create :domain www-public :user www-public :password www-public
}


#
# Set up namespace
#
set namespace /www/${package}
if { [xvalue exists [asset.namespace.exists :namespace ${namespace}]] == "true" } {
	puts "Installing package ${package} -- Destroying existing namespace: ${namespace}" 
	asset.namespace.destroy :namespace ${namespace}	
}
puts "Installing package ${package} -- Creating namespace: ${namespace}" 
asset.namespace.create :namespace -all true ${namespace} :description "the namespace for daris web application"

#
# Import web contents
#
puts "Installing package ${package} -- Importing web contents to namespace: ${namespace}"
asset.import :url archive:///www.zip :namespace ${namespace} :label -create yes ${label} :label PUBLISHED :update true 


#
# Set up HTTP processor
#
set url /$package
if { [xvalue exists [http.processor.exists :url ${url}]] == "true" } {
	puts "Installing package ${package} -- Destroying existing http processor: url=${url}"
	http.processor.destroy :url ${url}
}
puts "Installing package ${package} -- Creating http processor: url=${url}"
http.processor.create :app daris :url ${url} :type asset :translate ${namespace} :authentication < :domain $domain :user $user > :entry-point $entryPoint
