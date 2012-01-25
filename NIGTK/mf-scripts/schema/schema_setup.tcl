# =========================================================================== #
#                               Configuration                                 #
# =========================================================================== #

set schema_name        "monash-pssd"

set schema_description "Schema for PSSD at Monash"

set schema_member_host "monash-pssd.monash.edu.au"

set admin_domain       "system"

set admin_user         "manager"

set admin_role         "system-administrator"

set admin_password     "Tell_me8"

set user_domain        "nig"

set pssd_store         "monash-pssd"

# =========================================================================== #
#                                  Procs                                      #
# =========================================================================== #

# function: check if the specified store (name) exists, and its type is file-system or dmf-file-system
proc store_exists { store } {
	set type [xvalue store\[@name='${store}'\]/type [asset.store.describe]]
	if { $type == "file-system" || $type == "dmf-file-system" } {
		return "true"
	}
	return "false"
}

# function: create a schema with specified name and description
proc create_schema { name description } {
	if { [xvalue exists [schema.exists :schema $name]] == "false" } {
		schema.create :name $name :description $description
	}
}

# function: add member host (associate a host with the specified schema)
proc add_member_host { schema host } {
	if { [xvalue exists [schema.member.exists :schema $schema :host $host]] == "false" } {
		schema.member.add :schema $schema :host $host
	}
}

# function: create role
proc create_role { role } {
	if { [xvalue exists [authorization.role.exists :role $role]] == "false" } {
		authorization.role.create :role $role
	}
}

# function: create authentication domain
proc create_domain { domain } {
	if { [xvalue exists [authentication.domain.exists :domain $domain]] == "false" } {
		authentication.domain.create :domain $domain
	}
}

# function: create user
proc create_user { domain user password } {
	if { [xvalue exists [user.exists :domain $domain :user $user]] == "false" } {
		user.create :domain $domain :user $user :password $password
	}
}

# function: create a file-system store
proc create_store { name { type "file-system" } } {
	if { [xvalue exists [asset.store.exists :name $name]] == "false" } {	
		asset.store.create :type $type :automount true :name $name
	}
}

# function: create a namespace
proc create_namespace { ns store } {
	if { [xvalue exists [asset.namespace.exists :namespace $ns]] == "false" } {
		asset.namespace.create :store $store :namespace $ns
	}
}

# =========================================================================== #
#                                 Main                                        #
# =========================================================================== #

# Create schema
create_schema $schema_name $schema_description
# Associate host with the schema
add_member_host $schema_name $schema_member_host
# Check if the specified store exists in the default/main schema
set store_exists_in_default_schema [store_exists $pssd_store]
# Get into the new schema
schema.set :schema $schema_name
# Create admin role
create_role $admin_role
# Grant permissions to the admin role
actor.grant :type role :name $admin_role :perm < :access * :resource -type * * >
# Create authentication domain for admin user
create_domain $admin_domain
# Create admin user
create_user $admin_domain $admin_user $admin_password
# Grant admin role to admin user
actor.grant :type user :name ${admin_domain}:${admin_user} :role -type role ${admin_role}
# Create domain for normal user
create_domain $user_domain
# Create store
if { $store_exists_in_default_schema == "true" } {
	puts "Could not create store. Store: ${pssd_store} already exists in the default/main schema. Please change the store name and re-run this script."
	schema.unset
	return
}
create_store $pssd_store
# Create pssd namespace associated with $pssd_store
create_namespace "pssd" $pssd_store
# Get out of the schema
schema.unset
