
asset.doc.type.update :create true :type test.species :definition < \
	:element -name species -type string -max-occurs 1 \
>


asset.doc.type.update :create true :type test.mouse.identity :definition < \
	:element -name id -type string \
>

asset.doc.type.update :create true :type test.mouse.strain :definition < \
	:element -name strain -type string \
>


# ================================
# Brain scans and extraction.
# ================================

set namespace pssd/methods

set r [
om.pssd.method.create :namespace $namespace :name "MBA - Brain" :description "Mouse Brain Atlas, production of brain" \
    :step < \
        :name "Live Scan" \
        :description "Scan on live mouse" \
        :study < :type MR :metadata < :definition -requirement mandatory "test.mouse.mr" > > \
    > \
    :step < \
        :name "Kill & Perfuse" \
	:subject < :metadata < :definition "test.mouse.kill" > :metadata < :definition "test.mouse.perfuse" > > \
    > \
    :step < \
        :name "Skull Scan" \
        :study < :type MR :metadata < :definition "test.mouse.mr" > > \
    > \
    :step < \
        :name "Remove Brain" \
	:subject -part r < :metadata < :definition "test.brain.removal" > > \
    > \
    :step < \
        :name "Isolated Brain Scan" \
        :study < :type MR :metadata < :definition "test.mouse.mr" > > \
    > \
    :step < \
        :name "Store and Transfer" \
	:subject < :metadata < :definition -requirement optional "test.mouse.brain.transport" > > \
    > \
]

set mr_mid [xvalue id $r]

# ================================
# NISSL method
# ================================

set r [ \
om.pssd.method.create :namespace $namespace :name NISSL :description "Frank Nissl Histological Stain" \
    :step < \
        :name stain \
    > \
]

set nissl_mid [xvalue id $r]

# ================================
# Immuno Histo Chemistry method
# ================================

set r [ \
om.pssd.method.create :namespace $namespace :name Immunohistochemistry :description "Immunohistochemistry staining" \
    :step < \
        :name stain \
    > \
]

set ihc_mid [xvalue id $r]


# ================================
# Histology method.
# ================================

set r [ \
om.pssd.method.create :namespace $namespace :name "MBA - Histology" :description "Mouse Brain Atlas Histology" \
    :step < \
        :name "Processing" \
    > \
    :step < \
        :name "Histology/Blockface imaging/Sectioning" \
    > \
    :step < \
        :name "Staining" \
        :branch -type or < \
            :method < :id $nissl_mid > \
	    :method < :id $ihc_mid > \
        > \
    > \
]

set histo_mid [xvalue id $r]

# ================================
# Imaging Method
# ================================

set r [ \
om.pssd.method.create :namespace $namespace :name "MBA - Histology Imaging" :description "Mouse Brain Atlas Histology - imaging of slides" \
    :step < \
        :name "Imaging" \
    > \
]

set imaging_mid [xvalue id $r]

# ================================
# Mouse Brain Atlas - overall method
# ================================

om.pssd.method.for.subject.create :namespace $namespace :name "MBA" :description "Mouse Brain Atlas" \
    :subject < \
        :rsubject < \
	   :identity < \
		:metadata < :definition -requirement mandatory "test.mouse.identity" > \
           > \
	   :public < \
		:metadata < :definition -requirement mandatory "test.species" :value -xpath species -as constant mouse > \
           	:metadata < :definition -requirement mandatory "test.mouse.strain" > \
	   > \
        > \
    > \
    :step < \
        :name "Brain removal" \
        :method < :id $mr_mid > \
    > \
    :step < \
        :name "Histology" \
        :method < :id $histo_mid > \
    > \
    :step < \
        :name "Imaging" \
        :method < :id $imaging_mid > \
    >