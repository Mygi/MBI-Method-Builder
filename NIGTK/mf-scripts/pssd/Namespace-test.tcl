# This script tests docment types with their own instead of global namespaces
# It creates a namespace, generates some doc types and then makes a Method
# You can then 
#   1.  use om.pssd.type.set :type project  :mtype nebk:project  to register the document type with the framework
#   2. use the portal to create a project and subject
#
use the portal to use these doc types and Methods
proc createNameSpace { name role } {
	
   if { [xvalue exists [asset.doc.namespace.exists :namespace $name]] == "false" } {
	asset.doc.namespace.create :namespace $name
        actor.grant :type role  :name $role :perm < :resource -type document:namespace $name :access ACCESS >
   }
}


proc createDocTypes { } {
	
   # FOr project
   asset.doc.type.update :create 1 :type nebk:project :definition < :element -type string -name name >
   # FOr subject
   asset.doc.type.update :create 1 :type nebk:subject :definition < :element -type string -name name >
	
  # Permissions
   actor.grant :type role :name nig.pssd.model.user :perm < :resource -type document nebk:project :access ACCESS >
   actor.grant :type role :name nig.pssd.model.user :perm < :resource -type document nebk:project :access PUBLISH >
  #
   actor.grant :type role :name nig.pssd.model.user :perm < :resource -type document nebk:subject :access ACCESS >
   actor.grant :type role :name nig.pssd.model.user :perm < :resource -type document nebk:subject :access PUBLISH >   	
}

proc getMethodId { methodName } {

	set methodId ""
	set r [om.pssd.method.describe]
	set n [xcount method $r]
	for { set i 0 } { $i < $n } { incr i } {
		set name [xvalue method\[${i}\]/name $r]
		if { $name == $methodName } {
			set methodId [xvalue method\[${i}\]/@id $r]
		}
	}
	return $methodId

}

# Method
proc createMethod { } {
	
	set name "Human-MRI-Namespace"
	set id [getMethodId $name]
	if { $id == "" } {
		set id [xvalue id \
			[om.pssd.method.for.subject.create \
				:namespace pssd/methods  \
				:name $name \
				:description "Human MRI acquisition with own DocType Namespace" \
				:subject < \
					:project < \
						:public < \
							:metadata < :definition -requirement mandatory "nebk:subject" > \
						> \
					> \
				> \
				:step < \
					:name "MRI acquisition" \
					:description "MRI acquisition of Human subject" \
					:study < :type MR > \
				> \				
			]
		]
	}
	return $id
	
}

# Create nebk namespace and grant access
createNameSpace nebk nig.pssd.model.user

# Create doc types and set permissions
createDocTypes

# Register Project meta
om.pssd.type.metadata.set :type project :mtype  nebk:project

# Create a Method using it
createMethod
