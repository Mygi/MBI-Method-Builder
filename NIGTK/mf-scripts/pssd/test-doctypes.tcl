# Use with either:
# asset.set  :id 1530 :meta -action add < :hfi.pssd.human.contact-test2 < :contact < :name < :first Howard :last Hughes > :relationship magnate >  > >

proc createDocType-test {} {
	asset.doc.type.update \
            :create true \
            :type hfi.pssd.human.contact-test \
            :label "Human contact" \
            :description "Document type for human contacts" \
            :definition < \
             :element -name contact -type document -min-occurs 0 -max-occurs infinity < \
                :description "Human contact" \
                :element -name name -type document -min-occurs 1 -max-occurs 1  < \
                   :element -name prefix -type string -min-occurs 0 -max-occurs 1 -length 20 -label "Prefix (e.g. Dr.)" \
                   :element -name first  -type string -min-occurs 1 -max-occurs 1 -length 40 -label "First" \
                   :element -name middle -type string -min-occurs 0 -max-occurs 1 -length 100 -label "Middle" \
                   :element -name last   -type string -min-occurs 1 -max-occurs 1 -length 40 -label "Last" \
                   :element -name suffix -type string -min-occurs 0 -max-occurs 1 -length 20 -label "Suffix" \
                 > \
               :element -name relationship -type enumeration  -dictionary hfi.pssd.human.relationship \
                   -min-occurs 0 -max-occurs 1 -label "Relationship to subject" \
               :element -name address -type document  -min-occurs 0 -max-occurs infinity -label "Address" < \
                   :element -name unit  -type string -min-occurs 0 -max-occurs 1 -length 20 -label "Unit or apartment number" \
                   :element -name street -type string -min-occurs 1 -max-occurs 1 -length 30 -label "Street Number and street" \
                   :element -name suburb  -type string -min-occurs 0 -max-occurs 1 -length 30 -label "Suburb" \
                   :element -name city -type string -min-occurs 1 -max-occurs 1 -length 30 -label "City" \
                   :element -name postcode  -type string -min-occurs 1 -max-occurs 1 -length 8 -label "Post code" \
                   :element -name country   -type string -min-occurs 1 -max-occurs 1 -length 8 -label "Country" \
               > \
               :element -name email  -type string -min-occurs 0 -max-occurs infinity -label "Email" \
               :element -name telephone  -type document  -min-occurs 0 -max-occurs infinity -label "Telephone" < \
                   :element -name mobile  -type string -min-occurs 0 -max-occurs 1 -length 20 -label "Mobile phone" \
                   :element -name home  -type string -min-occurs 0 -max-occurs 1 -length 20 -label "Home phone" \
                   :element -name business -type string -min-occurs 0 -max-occurs 1 -length 20 -label "Business hours phone" \
               > \
             > \
           > \
}



proc destroyDocType-pssd-human-contact-test { force } {
	if { $force != "true" && $force != "false" } {
		set force "false"
	}
	if { [xvalue exists [asset.doc.type.exists :type hfi.pssd.human.contact-test]] == "true" } {
		asset.doc.type.destroy :type hfi.pssd.human.contact-test :force $force
	}
	
}




proc createDocType-test2 {} {
	asset.doc.type.update \
            :create true \
            :type hfi.pssd.human.contact-test2 \
            :label "Describe contact information for a human" \
            :description "Document type for human contacts" \
            :definition < \
             :element -name contact -type document -min-occurs 0 -max-occurs infinity < \
               :description "Human contact" \
               :reference -name name -type document < :value "hfi.pssd.human.name" > \
               :element -name address  -type string -min-occurs 0 -max-occurs 1 -label "Address" \
               :element -name relationship -type string -min-occurs 0 -max-occurs 1 -label "Relationship" \
             > \
           > \
}



proc destroyDocType-pssd-human-contact-test2 { force } {
	if { $force != "true" && $force != "false" } {
		set force "false"
	}
	if { [xvalue exists [asset.doc.type.exists :type hfi.pssd.human.contact-test2]] == "true" } {
		asset.doc.type.destroy :type hfi.pssd.human.contact-test2 :force $force
	}
	
}


