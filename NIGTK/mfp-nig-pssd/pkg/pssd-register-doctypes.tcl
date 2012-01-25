#============================================================================#
# Register generic meta-data with specific PSSD objects                      #
# This is domain-specific, but not method-specific meta-data                 #
#============================================================================#


set mtypeArgs ":mtype -requirement optional hfi.pssd.project"
set mtypeArgs "${mtypeArgs} :mtype -requirement optional hfi.pssd.ethics"
# Although pssd-project-harvest is from the PSSD package, we choose to use it 
# here. It would not be appropriate to make this a default document in the
# PSSD package.
set mtypeArgs "${mtypeArgs} :mtype -requirement optional pssd-project-harvest"

# Append to any pre-existing associations (e.g. set by pssd package)
set args ":append true :type project ${mtypeArgs}"
om.pssd.type.metadata.set $args

