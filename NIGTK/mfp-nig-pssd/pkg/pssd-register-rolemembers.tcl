#============================================================================#
# Register specific roles as available as 'role-members' when projects are   #
# created. The only role we use in this way was created in the 'dms' package #
# It is used for the FNI small animal facility team so that they can all be  #
# made specific project members by referring to this role (which they all    #
# have) If the role has already been added it is ignored.                    #
#============================================================================#

set roleMembers { hfi-amrif-user }

foreach role $roleMembers {

    authorization.role.create :ifexists ignore :role $role
    om.pssd.role-member-registry.add :role $role
    
}