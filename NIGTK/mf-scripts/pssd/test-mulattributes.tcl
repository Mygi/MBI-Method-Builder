proc createDocType-pssd-project {} {
	asset.doc.type.update \
             :create true :type hfi.pssd.project \
             :label "Project" \
             :description "Document type for  project" \
             :definition < \
		:element -name ethics-id  -min-occurs 0 -max-occurs infinity \
                         -type string -index true  < \
                   :description "Ethics approval identifier" \
                   :attribute -name type -type enumeration -min-occurs 0 < \
			:restriction -base enumeration < \
	            :value "HFI AEC" \
			    :value RCH \
			    :value NHMRC \
			    :value Other \
                         > \
                   > \
                > \
		:element -name facility-id -min-occurs 0 -max-occurs infinity \
                         -type string -index true  < \
                   :description "Facility ID of the project allocated by some other authority" \
		   :attribute -name type -type enumeration -min-occurs 0  < \
		       :restriction -base enumeration < \
			    :value HFI \
			    :value RCH \
			    :value CNS \
			    :value Other \
                        > \
                   >\
		   :attribute -name colour -type enumeration -min-occurs 0  < \
		       :restriction -base enumeration < \
			    :value Red \
			    :value Green \
			    :value Blue \
                        > \
                    > \
		   :attribute -name quality -type string -min-occurs 0  \
                > \
		:element -name funding-id  -min-occurs 0 -max-occurs infinity \
                         -type string -index true < \
                    :description "An identifier for the funding source (e.g. ARC/LIEF)"  \
                > \
            >
}

proc destroyDocType-pssd-project { force } {
	if { $force != "true" && $force != "false" } {
		set force "false"
	}
	if { [xvalue exists [asset.doc.type.exists :type hfi.pssd.project]] == "true" } {
		asset.doc.type.destroy :type hfi.pssd.project :force $force
	}
}

