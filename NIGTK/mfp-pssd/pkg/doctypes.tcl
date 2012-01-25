# ======================================
# Type: pssd-object
# ======================================

asset.doc.type.update :create true :type pssd-object \
	:description "Identifying information for all types of PSSD objects." \
	:tag pssd \
        :generated-by application \
	:definition < \
		:element -name type -type enumeration -index true < \
			:description "The PSSD type of the object." \
			:restriction -base enumeration < \
				:value "project" \
				:value "subject" \
				:value "method" \
				:value "ex-method" \
				:value "study" \
				:value "dataset" \
				:value "data-object" \
				:value "r-subject" \
			> \
		> \
		:element -name name -type string -min-occurs 0 -max-occurs 1 < \
			:description "Arbitrary name for the object." \
		> \
		:element -name description -type string -min-occurs 0 -max-occurs 1 < \
			:description "Arbitrary description, if any, for the object." \
		> \
	>


# =====================================
# Type: pssd-project
# ======================================

asset.doc.type.update :create true :type pssd-project \
    :description "Metadata for a PSSD project." \
    :tag pssd \
    :generated-by application \
    :definition < \
        :element -name method -type document -min-occurs 0 < \
            :element -name id -type citeable-id \
		    :element -name notes -type string -min-occurs 0 \
		> \
 	    :element -name data-use -type enumeration -index true -min-occurs 1 -max-occurs 1 < \
            :description "Specifies the type of consent for the use of data: 1) 'specific' means use the data only for the original specific intent, 2) 'extended' means use the data for related projects and 3) 'unspecified' means use the data for any research" \
   	    	:restriction -base enumeration < \
	    		:value "specific" \
	    		:value "extended" \
	    		:value "unspecified" \
	    	> \
         > \
    >


# =====================================
# Type: pssd-subject
# ======================================

asset.doc.type.update :create true :type pssd-subject \
    :description "Metadata for a PSSD subject." \
    :tag pssd \
    :generated-by application \
    :definition < \
        :element -name method -type citeable-id < \
            :description "The method of research being used for this subject." \
        > \
        :element -name r-subject -type citeable-id -min-occurs 0 -max-occurs 1 < \
            :description "The R-Subject, if any, that this subject represents." \
        > \
        :element -name data-use -type enumeration -min-occurs 0 -max-occurs 1 < \
        	:description "Specify how the data can be (re)used for this subject" \
	    	:restriction -base enumeration < \
	    		:value "specific" \
	    		:value "extended" \
	    		:value "unspecified" \
	    	> \
	    > \
    >


# =====================================
# Type: pssd-ex-method
# ======================================

asset.doc.type.update :create true :type pssd-ex-method \
    :description "Metadata for a PSSD method execution." \
    :tag pssd \
    :generated-by application \
    :definition < \
	:element -name method -type document -max-occurs 1 < \
        	:element -name id -type citeable-id -max-occurs 1 < \
	            :description "The top level method (identifier) being executed." \
        	> \
        	:element -name name -type string -max-occurs 1 < \
	            :description "The name of the sourced method." \
        	> \
        	:element -name description -type string -max-occurs 1 < \
	            :description "A description, if any, of the sourced methodd." \
        	> \
        	:element -name author -type citeable-id -min-occurs 0 < \
	            :description "The author of the sourced method." \
        	> \
	> \
        :element -type enumeration -name state -default incomplete -min-occurs 0  -max-occurs 1 < \
            :description "Overall state of execution." \
            :restriction -base enumeration < \
                :value "incomplete" \
                :value "waiting" \
                :value "complete" \
                :value "abandoned" \
            > \
	> \
	:element -name notes -type string -min-occurs 0 \
        :element -name step -type document -min-occurs 0 < \
            :attribute -name path -type citeable-id \
            :element -type enumeration -name state -default incomplete -min-occurs 0  -max-occurs 1 < \
                :description "State of execution." \
                :restriction -base enumeration < \
                    :value "incomplete" \
                    :value "waiting" \
                    :value "complete" \
                    :value "abandoned" \
                > \
	    > \
            :element -name notes -type string -min-occurs 0 -max-occurs 1 \
        > \
    >


# ======================================
# Type: pssd-study
# ======================================

asset.doc.type.update :create true :type pssd-study \
	:description "The prescribed metadata for a PSSD Study." \
	:tag pssd \
        :generated-by application \
	:definition < \
		:element -name type -type enumeration -max-occurs 1 < \
			:description "The general classification/type of study." \
			:restriction -base enumeration < \
				:dictionary pssd.study.types \
			> \
		> \
		:element -name method -type citeable-id -min-occurs 0 -max-occurs 1 < \
			:description "The identity of the ex-method that generated this study." \
			:attribute -name step -type citeable-id < \
				:description "The step within the ex-method that generated this study." \
			> \
		> \
	>


# ======================================
# Type: pssd-dataset
# ======================================

asset.doc.type.update :create true :type pssd-dataset \
	:description "The prescribed metadata for a PSSD Data Set." \
	:tag pssd \
        :generated-by application \
	:definition < \
		:element -name type -type enumeration -max-occurs 1 < \
			:description "There are two classes of data sets." \
			:restriction -base enumeration < \
				:value "primary" \
				:value "derivation" \
			> \
		> \
	>


# ======================================
# Type: pssd-transform
# ======================================

asset.doc.type.update :create true :type pssd-transform \
	:description "A transformation applied to the data set or data object. Can be used for acquisitions or derivations." \
	:tag pssd \
        :generated-by application \
	:definition < \
		:element -name id -type citeable-id -min-occurs 0 -max-occurs 1 < \
			:description "The identity of a common transformation method/protocol." \
		> \
		:element -name notes -type string -min-occurs 0 < \
			:description "Description, if any, of the transformation." \
		> \
	>


# =====================================
# Type: pssd-acquisition
# ======================================

asset.doc.type.update :create true :type pssd-acquisition \
	:description "Acquisition from a subject." \
	:tag pssd \
        :generated-by application \
	:definition < \
		:element -name subject -type citeable-id -max-occurs 1 < \
			:description "The identity of the subject from which this data set was acquired." \
                    :attribute -name state -type integer < \
			:description "The identity of the state the subject was in at the time of acquisition." \
			:restriction -base integer < \
				:minimum 1 \
			> \
		    > \
		> \
		:element -name method -type citeable-id -min-occurs 0 -max-occurs 1 < \
			:description "The identity of the ex-method that generated this data set." \
			:attribute -name step -type citeable-id < \
				:description "The step within the ex-method that generated this data set." \
			> \
		> \
	>


# =====================================
# Type: pssd-derivation
# ======================================

asset.doc.type.update :create true :type pssd-derivation \
	:description "Derivation from one or more other data sets." \
	:tag pssd \
        :generated-by application \
	:definition < \
		:element -name input -type citeable-id -min-occurs 0 -max-occurs infinity < \
			:description "The identity of a Data Set from which this set was derived." \
			:attribute -name vid -type string < \
				:description "The version/state of the data set." \
			> \
		> \
		:element -name method -type citeable-id -min-occurs 0 -max-occurs 1 < \
			:description "The identity of the ex-method that generated this data set." \
			:attribute -name step -type citeable-id < \
				:description "The step within the ex-method that generated this data set." \
			> \
		> \
	>


# =====================================
# Type: pssd-method
# ======================================

asset.doc.type.update :create true :type pssd-method \
    :description "Research method. Defines the metadata to be presented at any step." \
    :tag pssd \
    :generated-by application \
    :definition < \
    	:element -name version -type string -min-occurs 0 -max-occurs 1 < :description "A version string identifying the Method object structure. If absent, implies version 1.0" > \
        :element -name author -type citeable-id -min-occurs 0 \
        :element -name step -type document -min-occurs 0 < \
            :attribute -name id -type integer \
            :element -name name -type string -min-occurs 1 -max-occurs 1 \
            :element -name description -type string -min-occurs 0 -max-occurs 1 \
            :element -name subject -type document -min-occurs 0 -max-occurs 1 < \
                :attribute -name part -type enumeration -enumerated-values "p,r" -min-occurs 0 -default p \
                :element -name metadata -type document -min-occurs 0 -max-occurs infinity < \
                    :element -name definition -type string -min-occurs 1 -max-occurs 1 < \
                        :attribute -name requirement -type enumeration -enumerated-values "mandatory,optional" -min-occurs 0 -default mandatory \
                    > \
                    :element -name value -type document -min-occurs 0 -max-occurs 1 -ignore-descendants true \
                > \
	    > \
            :element -name study -type document -min-occurs 0 -max-occurs 1 < \
               :element -name type -type enumeration < \
                    :restriction -base enumeration < \
                        :dictionary pssd.study.types \
                    > \
               > \
	       :element -name "dicom" -type "document" -index "true" -min-occurs "0" -max-occurs "1" < \
	          :description "This element describes restrictions on the kinds of DICOM data that are compatible with this Study type." \
	          :element -name "modality" -type "enumeration" -index "true" -min-occurs "0" -max-occurs "1" < \
		    :description "DICOM modality string code.  This code is found in DICOM element (0008,0060)." \
		    :restriction -base "enumeration" < \
		      :dictionary "pssd.dicom.modality" \
		     > \
	           > \
	        > \
               :element -name metadata -type document -min-occurs 0 -max-occurs infinity < \
                   :element -name definition -type string -min-occurs 1 -max-occurs 1 < \
                       :attribute -name requirement -type enumeration -enumerated-values "mandatory,optional" -min-occurs 0 -default mandatory \
                   > \
                   :element -name value -type document -min-occurs 0 -max-occurs 1 -ignore-descendants true \
               > \
            > \
            :element -name method -type document -min-occurs 0 -max-occurs 1 < \
            	:element -name id -type citeable-id -min-occurs 1 -max-occurs 1 \
            	:element -name name -type string -min-occurs 0 -max-occurs 1 \
            	:element -name description -type string -min-occurs 0 -max-occurs 1 \
	        :reference -type element -name step -min-occurs 0 < :value ../.. > \
	    > \
            :element -name branch -type document -min-occurs 0 -max-occurs 1 < \
                :attribute -name type -type enumeration < \
                    :restriction -base enumeration < \
                        :value or \
                        :value and \
                    > \
                > \
                :element -name method -type document -max-occurs infinity < \
            	    :element -name id -type citeable-id -min-occurs 1 -max-occurs 1 \
             	    :element -name name -type string -min-occurs 0 -max-occurs 1 \
              	    :element -name description -type string -min-occurs 0 -max-occurs 1 \
	            :reference -type element -name step -min-occurs 0 < :value ../../.. > \
                > \
            > \
        > \
     >


# =====================================
# Type: pssd-method-subject
# ======================================

asset.doc.type.update :create true :type pssd-method-subject \
    :description "Metadata for the subject of a method. Applies for subject specific methods." \
    :tag pssd \
    :generated-by application \
    :definition < \
        :element -name public -type document -min-occurs 0 -max-occurs 1 < \
	    :element -name metadata -type document -min-occurs 1 -max-occurs infinity < \
                :element -name definition -type string -min-occurs 1 -max-occurs 1 < \
                    :attribute -name requirement -type enumeration -enumerated-values "mandatory,optional" -min-occurs 0 -default mandatory \
                > \
                :element -name value -type document -min-occurs 0 -max-occurs 1 -ignore-descendants true \
            > \
        > \
        :element -name private -type document -min-occurs 0 -max-occurs 1 < \
	    :element -name metadata -type document -min-occurs 1 -max-occurs infinity < \
                :element -name definition -type string -min-occurs 1 -max-occurs 1 < \
                    :attribute -name requirement -type enumeration -enumerated-values "mandatory,optional" -min-occurs 0 -default mandatory \
                > \
                :element -name value -type document -min-occurs 0 -max-occurs 1 -ignore-descendants true \
            > \
        > \
     >


# =====================================
# Type: pssd-method-rsubject
# ======================================

asset.doc.type.update :create true :type pssd-method-rsubject \
    :description "Metadata for the r-subject of a method. Applies for subject specific methods." \
    :tag pssd \
    :generated-by application \
    :definition < \
        :element -name identity -type document -min-occurs 1 -max-occurs 1 < \
	    :element -name metadata -type document -min-occurs 1 -max-occurs infinity < \
                 :element -name definition -type string -min-occurs 1 -max-occurs 1 < \
                     :attribute -name requirement -type enumeration -enumerated-values "mandatory,optional" -min-occurs 0 -default mandatory \
                 > \
                 :element -name value -type document -min-occurs 0 -max-occurs 1 -ignore-descendants true \
            > \
        > \
        :element -name public -type document -min-occurs 0 -max-occurs 1 < \
	    :element -name metadata -type document -min-occurs 1 -max-occurs infinity < \
                :element -name definition -type string -min-occurs 1 -max-occurs 1 < \
                    :attribute -name requirement -type enumeration -enumerated-values "mandatory,optional" -min-occurs 0 -default mandatory \
                > \
                :element -name value -type document -min-occurs 0 -max-occurs 1 -ignore-descendants true \
            > \
        > \
        :element -name private -type document -min-occurs 0 -max-occurs 1 < \
	    :element -name metadata -type document -min-occurs 1 -max-occurs infinity < \
                :element -name definition -type string -min-occurs 1 -max-occurs 1 < \
                    :attribute -name requirement -type enumeration -enumerated-values "mandatory,optional" -min-occurs 0 -default mandatory \
                > \
                :element -name value -type document -min-occurs 0 -max-occurs 1 -ignore-descendants true \
            > \
        > \
     >

# =====================================
# Type: pssd-state
# ======================================

#asset.doc.type.update :create true :type pssd-state \
#	:description "The state of a subject." \
#	:tag pssd \
#        :generated-by application \
#	:definition < \
#		:element -name state -type document -max-occurs infinity < \
#			:description "A unique state." \
#			:attribute -name id -type integer < \
#				:description "The identity of the state." \
#                                :restriction -base integer < \
#                                  :minimum 1 \
#                                > \
# 			> \
#			:element -name ex-method -type citeable-id -min-occurs 0 -max-occurs 1 < \
#                            :description "The executing method instance that caused this state change." \
#                        > \
#                        :element -name method -type citeable-id -min-occurs 0 -max-occurs 1 -recurse true < \
#                            :description "The method and step that resulted in this state. May contain other sub-methods." \
#                            :attribute -name step -type integer \
#                        > \
#			:element -name workflow -type document -min-occurs 0 -max-occurs 1 < \
#	  			:element -name status -type enumeration < \
#					:description "Status of the state." \
#					:restriction -base enumeration < \
#                        	     		:value "incomplete" \
#                        	     		:value "waiting" \
#                             			:value "complete" \
#                             			:value "abandoned" \
#					> \
#				> \
#				:element -name notes -type string -min-occurs 0 \
#			> \
#			:element -name meta -type document -ignore-descendants true -min-occurs 0 < \
#				:description "Arbitrary metadata for the state" \
#			> \
#		> \
#	>

#=====================================
# Type: pssd-role-member-registry
# One document per item
#======================================

asset.doc.type.update \
   :create true :type pssd-role-member-registry \
   :label "Role member registry" \
   :description "Document type for registering roles as project role-members" \
   :definition < \
       :element -name role -min-occurs 1 -max-occurs 1 -type string -index true  < \
             :description "A role that can be supplied as a role-member when creating PSSD projects" \
             :attribute -name id  -type string  -min-occurs 1  \
        > \
   >

#=====================================
# Type: pssd-dicom-server-registry
# One document per Application Entity
#======================================
   
asset.doc.type.update \
      :create true :type pssd-dicom-server-registry \
      :label "DICOM Application Entity server registry" \
      :description "Document type for registering DICOM Application Entities in the DICOM AE registry." \
      :definition < \
	  :element -name ae -min-occurs 1 -max-occurs 1 -type document -index true  < \
               :attribute -name name -type string -min-occurs 0 < :description "Convenience name" > \
	       :attribute -name type -type enumeration -enumerated-values "local,remote" -min-occurs 0 < :description "The type of this AE; local means this server" > \
	       :element -name host -type string -min-occurs 1 -max-occurs 1 -index true < :description "Host name or IP address " > \
	       :element -name port -type integer -min-occurs 1 -max-occurs 1 -index true < :description "Port number" >  \
	       :element -name aet -type string -min-occurs 1 -max-occurs 1 -index true < :description "The AETitle of the AE" > \
	   > \
      >
   
# =================================================================
# Type: pssd-notification
#   
# Instead of "data-upload" and other category elements, we could use
# a category attribute on each member. In that way, only the
# category enum would grow with new categories. Perhaps less
# obvious for users.
# ==================================================================

asset.doc.type.update :create true :type pssd-notification \
    :description "Metadata to describe how notifications can be made to project members." \
    :tag pssd \
    :generated-by user \
    :definition < \
	:element -name data-upload -index true -type document -min-occurs 0 -max-occurs 1 < \
 	    :description "Indicates who will receive an email when data are uploaded." \
            :element -name project-role -type enumeration  -min-occurs 0 -max-occurs infinity < \
                :description "Specifies users who hold a specific project role to receive an email." \
                :attribute -name explicit -type boolean -min-occurs 1 < \
                	:description "If true, users who explicitly hold the given role receive an email. If false, users who implicitly hold the role (e.g. an admin is also a member) receive email." \
           	 > \
           	 :restriction -base enumeration < \
                    :value "project-administrator" \
                    :value "subject-administrator" \
                    :value "member" \
                    :value "guest" \
    		 > \
            > \
            :element -name member -index true -type document -min-occurs 0 -max-occurs infinity < \
            	:description "Specifies a user member to receive email." \
	    	:element -name authority -type string -min-occurs 0 -max-occurs 1 < \
		    :attribute -name protocol -type string -min-occurs 0  \
		 > \
     	        :element -name domain -type string -min-occurs 1 -max-occurs 1 \
       	        :element -name user -type string -min-occurs 1 -max-occurs 1 \
     	    > \
            :element -name role-member -index true -type string -min-occurs 0 -max-occurs infinity < \
                :description "Specifies a project role member to receive email." \
	    > \
	    :element -name email -index true -type string -min-occurs 0 -max-occurs infinity < \
	        :description "Specifies a direct email address, whether project team member or not, to receive a message." \
	    > \
        > \
    >
    
    
# =================================================================
# Type: pssd-repository-description
#   
# This meta-data is for a static singleton object that describes
# attributes of the repository in the research context.
# ==================================================================

asset.doc.type.update :create true :type pssd-repository-description \
	:description "Metadata to describe the repository in the research context." \
	:tag pssd \
	:generated-by user \
	:definition < \
	    :element -name name -index true -type string -min-occurs 1 -max-occurs 1 < \
	        :description "The name of the repository" \
	        :attribute -name acronym -type string -min-occurs 0 < :description "Acronym for the repository name." > \
	    > \
	    :element -name custodian -index true -type document -min-occurs 1 -max-occurs 1 < \
		:description "The person responsible for the management of the repository." \
		:element -name email -index true -type string -min-occurs 0 -max-occurs 1 < \
		    :description "Specifies the custodian's email address." \
		> \
		:element -name prefix -type string -min-occurs 0 -max-occurs 1 -length 20 -label "Prefix" \
		:element -name first  -type string -min-occurs 1 -max-occurs 1 -length 40 -label "First" \
	        :element -name middle -type string -min-occurs 0 -max-occurs infinity -length 100 -label "Middle" < \
		    :description "If there are several 'middle' names then put them in this field" \
		> \
	        :element -name last   -type string -min-occurs 1 -max-occurs 1 -length 40 -label "Last" \
		:element -name address -type document -min-occurs 0 -max-occurs 1  < \
	           :element -name department -type string -min-occurs 0 -max-occurs 1 \
		   :element -name institution -type string -min-occurs 0 -max-occurs 1 \
		   :element -name physical-address -type string -min-occurs 0 -max-occurs infinity \
		> \
	    > \
	    :element -name location -index true -type document -min-occurs 1 -max-occurs 1 < \
	       :description "The physical location of the repository." \
	       :element -name institution -index true -type string -min-occurs 1 -max-occurs 1 \
	       :element -name department -index true -type string -min-occurs 0 -max-occurs 1 \
	       :element -name building -index true -type string -min-occurs 0 -max-occurs 1 \
	       :element -name precinct -index true -type string -min-occurs 0 -max-occurs 1 \
	    > \
	    :element -name rights -index true -type document -min-occurs 1 -max-occurs 1 < \
	       :description "A description of the rights process to gain access to collections in the repository." \
	       :element -name description -index true -type string -min-occurs 1 -max-occurs 1 \
	    > \
	    :element -name data-holdings -index true -type document -min-occurs 0 -max-occurs 1 < \
	       :description "Describes broadly the data holdings in the repository." \
	       :element -name description -index true -type string -min-occurs 1 -max-occurs 1 \
	       :element -name start-date -index true -type date -min-occurs 0 -max-occurs 1 < \
	          :description "The date on which the repository was activated and started managing data." \
	       > \
	    > \
	>