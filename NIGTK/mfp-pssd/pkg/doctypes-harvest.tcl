#=============================================================================
# This script creates  Document Types for ANDS meta-data harvesting
# The meta-data re not domain specific and so it is appropriate to be
# in the pssd package
#=============================================================================
# Project-related Document Types: hfi.pssd.project
#
# These meta-data should be placed on the Project object
proc create_harvest_doctypes {} {

	asset.doc.type.update \
		:create true :type pssd-project-harvest \
		:label "Project Harvest Authorisation" \
		:description "Document type to specify authorisation to harvest Project descriptions to Insitutional and ANDS registry." \
		:definition < \
		  :element -name allow-institutional -min-occurs 1 -max-occurs 1  -type boolean -index true  < \
		     :description "Allow meta-data from this Project to be harvested to an Institutional registry." \
		   > \
		  :element -name allow-ANDS -min-occurs 1 -max-occurs 1  -type boolean -index true  < \
		     :description "Allow meta-data from this Project to be harvested to an ANDS registry." \
	           > \
		  :element -name project-owner -min-occurs 1 -max-occurs infinity  -type document -index true  < \
		     :description "The project owner (e.g. Chief Investigator) who has carriage of ethical/funding processes." \
	   	     :element -name prefix -type enumeration -dictionary pssd.human.name.prefix -min-occurs 0 -max-occurs 1 -label "Prefix" \
		     :element -name first  -type string -min-occurs 1 -max-occurs 1  -label "First" \
		     :element -name middle -type string -min-occurs 0 -max-occurs 1  -label "Middle" < \
		        :description "If there are several 'middle' names then put them in this field" \
		      > \
		     :element -name last   -type string -min-occurs 1 -max-occurs 1  -label "Last" \
		     :element -name suffix -type string -min-occurs 0 -max-occurs 1  -label "Suffix" \
		     :element -name email -type string -min-occurs 0 -max-occurs 1 -label email \
		     :element -name institution -type document -min-occurs 0 -max-occurs infinity -index true  < \
		        :description "Name of the institution that the project owner is a member of." \
		        :element -name name -type enumeration -dictionary pssd.research.organization \
		           -min-occurs 0 -max-occurs 1  \
		        :element -name department -type string -min-occurs 0 -max-occurs 1 \
		      > \
		    > \
		      :element -name field-of-research -type enumeration -dictionary pssd.ANZSRC.Division-11.field-of-research \
		          -min-occurs 1 -max-occurs infinity -index true \
	                   < :description "Standard ANZSRC Field of Research (Medical and Health Sciences) classification" > \
	        >
}


# The portal does not handle reference documents yet
#:element -name project-owner -min-occurs 0 -max-occurs 1  -type document -index true  < \
#:reference -name name -type document < :value "hfi.pssd.human.name" > \
#:element -type string -name email \
#> \
#>


#============================================================================#
proc destroy_harvest_doctypes { } {

	set doctypes { pssd-project-harvest }

	foreach doctype $doctypes {
           destroyDocType $doctype "true"
	}

}

#============================================================================#

create_harvest_doctypes
