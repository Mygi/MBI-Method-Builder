##################################################################################################################
# Script to upload some EAE data into the Data Archive
# source script name
##################################################################################################################
#
# Create Project

proc createProject { method_id project_id } {
   set pid ""
   if { $project_id == "-1" } {
     set r [ om.pssd.project.create \
       :description "Investigate axonal degeneration in murine optic nerve via EAE mice" \
       :name "EAE optic nerve degeneration in mice" \
       :namespace pssd \
       :meta < \
           :hfi.pssd.project < \
             :funding-id NHMRC \
           >\
       >\
       :method < :id $method_id > \
       :member < :domain hfi :user nkilleen  :role project-administrator > \
     ]
     set pid  [xvalue id $r]
   } else {
     set r [ om.pssd.project.create \
       :project-number $project_id \
       :description "Investigate axonal degeneration in murine optic nerve via EAE mice" \
       :name "EAE optic nerve degeneration in mice" \
       :namespace pssd \
       :meta < \
           :hfi.pssd.project < \
             :funding-id NHMRC \
           >\
       >\
       :method < :id $method_id > \
       :member < :domain hfi :user nkilleen  :role project-administrator > \
     ]
     set pid  [xvalue id $r]
   }
   return $pid
}

##################################################################################################################
# Create Subject 

proc createSubject { proj_id method_id subj_name control gender age } {

#
# Create the subject with the invariant subject meta-data 
#
   set r [ om.pssd.subject.create \
     :description "EAE mouse" \
     :name $subj_name :pid $proj_id \
     :method $method_id \
     :public < \
       :hfi.pssd.subject < \
          :type animal \
          :control $control \
       > \
       :hfi.pssd.animal.subject <\
          :species mouse \
          :age -units days $age \
          :gender $gender \
        > \
       :hfi.pssd.animal.disease <\
          :disease  "Experimental Autoimmune Encephalomyelitis (EAE)" \
        > \
       :hfi.pssd.animal.genetics < \
          :strain <\
             :name C57Black \
             :fraction 1.0 \
          >\
       > \
       :hfi.pssd.identity < \
          :id -type Other $subj_name  \
       > \
     > \
   ]

# Fish out the Subject ID and ExMethod ID
   set sid  [xvalue id $r]
   set mid  [xvalue id/@mid $r]


# Now stick on the Method and step-specific, subject-based  meta-data
# This is effectively what the PSSD Gui does interactively

# Step 1

   om.pssd.ex-method.subject.step.update \
      :id $mid \
      :step "1.1" \
      :state complete \
      :ps-meta <\
         :hfi.pssd.anaesthetic <\
            :method inhalation \
            :induction < \
               :agent isoflurane \
               :concentration 3 \
             > \
            :maintenance < \
               :agent isoflurane \
               :concentration <\
                 :min 0.5 \
                 :max 1.0 \
               > \
             > \
             :monitoring "respiratory rate" \
         >\
      >
#      
   om.pssd.ex-method.subject.step.update \
      :id $mid \
      :step "1.4" \
      :state complete \
      :ps-meta <\
         :hfi.pssd.recovery < :method "warming light" >\
      >
#
   om.pssd.ex-method.subject.step.update \
      :id $mid \
      :step "1.5" \
      :state complete \
      :ps-meta <\
         :hfi.pssd.animal.kill <\
            :route "intra-peritoneal injection" \
            :agent pentobarbitane \
            :concentration 100 \
         >\
      >

# Step 2

   om.pssd.ex-method.subject.step.update \
      :id $mid \
      :step "2.1" \
      :state complete \
      :ps-meta <\
         :hfi.pssd.EAE.perfusion <\
            :agent <\
               :name paraformaldehyde \
               :concentration 4 \
             >\
            :agent <\
               :name gluteraldehyde \
               :concentration 2.5 \
             >\
            :buffer <\
               :name "sodium cacodylate" \
               :concentration 0.1 \
             >\
          >\
      >
#
   om.pssd.ex-method.subject.step.update \
      :id $mid \
      :step "2.2" \
      :state complete \
      :ps-meta <\
         :hfi.pssd.EAE.optic-nerve.removal <\
            :method "retro-chiasmal trans-section" \
          >\
      >
#
   om.pssd.ex-method.subject.step.update \
      :id $mid \
      :step "2.3" \
      :state complete \
      :ps-meta <\
         :hfi.pssd.EAE.perfusion <\
            :agent < \
               :name paraformaldehyde \
               :concentration 4 \
             > \
            :agent < \
               :name gluteraldehyde \
               :concentration 2.5 \
             > \
            :buffer < \
               :name "sodium cacodylate" \
               :concentration 0.1 \
             > \
            :duration 24 \
         >\
      >
#
   om.pssd.ex-method.subject.step.update \
      :id $mid \
      :step "2.4" \
      :state complete \
      :ps-meta <\
         :hfi.pssd.EAE.perfusion <\
            :buffer < \
               :name "sodium cacodylate" \
               :concentration 0.1 \
             > \
            :duration 240 \
         >\
      >
#
   om.pssd.ex-method.subject.step.update \
      :id $mid \
      :step "2.5" \
      :state complete \
      :ps-meta <\
         :hfi.pssd.EAE.perfusion <\
            :agent < \
                :name "osmium tetraoxide" \
                :concentration 2 \
              > \
              :agent < \
                :name "potassium ferricyanide"  \
                :concentration 1.5 \
              > \
              :buffer < \
                :name "water" \
                :concentration 100 \
              > \
              :duration 2 \
         >\
      >

   return "$sid $mid"
}

##################################################################################################################
# Create MR Study

proc createMRStudy { ExMethID type step } {

    set id ""
    if { $type == "L" } {
       set r [ om.pssd.study.create \
           :name "Longitudinal MR" \
           :description "Longitudinal MRI acquisition at aMRIF" \
           :pid $ExMethID \
           :type MR \
           :step $step
      ]
      set id  [xvalue id $r]
   } else {
       set r [ om.pssd.study.create \
           :name "Coronal  MR" \
           :description "Coronal  MRI acquisition at aMRIF" \
           :pid $ExMethID \
           :type MR \
           :step $step
      ]
      set id  [xvalue id $r]
   }

    return $id
}



###################################################################################################################
# Create OM  Study 
proc createOMStudy { ExMethID type step } {

    set omid ""
    set mag ""
    if { $type == "H" } {
       set r [ om.pssd.study.create \
        :name "High Mag OM" \
        :description "High Magnification Optical Microscopy acquisition at HFI" \
        :pid $ExMethID  \
        :type OM \
        :step $step
      ]
      set omid  [xvalue id $r]
      set mag 100
    } else {
       set r [ om.pssd.study.create \
        :name "Low Mag OM" \
        :description "Low  Magnification Optical Microscopy acquisition at HFI" \
        :pid $ExMethID  \
        :type OM \
        :step $step
      ]
      set omid  [xvalue id $r]
      set mag 20
    }

# Method/step study meta-data (no PSSD function; need to write om.pssd.ex-method.study.step.update)

    set ns ${ExMethID}_${step}
    asset.set :cid $omid \
        :meta <\
           :hfi.pssd.EAE.microscopy -action add -ns $ns  <\
              :magnification $mag \
           >\
        >\

    return $omid
}
###################################################################################################################

proc loadSubject { proj_id method_id subj_name control gender age hasOM hasLong hasCoronal } {

# Create Subject and Method-based subjectmeta-data

   set retval [ createSubject $proj_id $method_id $subj_name $control $gender $age ]
   set subj_id [ lindex $retval 0 ]
   set ExMeth_id [ lindex $retval 1 ]
   puts "      Subject  ID = ${subj_id}"
   puts "      SubjectName = ${subj_name}"
   puts "          control = ${control}"
   puts "           gender = ${gender}"
   puts "      ExMethod ID = ${ExMeth_id}"

# We create all 4 Studies, regardless of whether the data exists for them
# or not.  If the data exists, and will be uploaded, we set the 
# Step that produces the study to complete.

# Create Longitudinal  MR Study 

   set step "1.2"
   set type "L"
   set MR_id [ createMRStudy $ExMeth_id $type $step ]
   if { $hasLong == "Y" } {
      om.pssd.ex-method.step.update :id $ExMeth_id :state complete :step $step
   }
   puts "      MR Study  ID = ${MR_id}"

# Create Coronal MR Study 

   set step "1.3"
   set type "C"
   set MR_id [ createMRStudy $ExMeth_id $type $step ]
   if { $hasCoronal == "Y" } {
      om.pssd.ex-method.step.update :id $ExMeth_id :state complete :step $step
   }
   puts "      MR Study  ID = ${MR_id}"

# Create Low Mag OM STudy

   set step "3.1.3"
   set type "L"
   set OM_id [ createOMStudy $ExMeth_id $type $step ]
   if { $hasOM == "Y" } {
      om.pssd.ex-method.step.update :id $ExMeth_id :state complete :step $step
   }
   puts "      Low res OM Study  ID = ${OM_id}"
#
   set step "3.1.4"
   set type "H"
   set OM_id [ createOMStudy $ExMeth_id $type $step ]
   if { $hasOM == "Y" } {
      om.pssd.ex-method.step.update :id $ExMeth_id :state complete :step $step
   }
   puts "      High Res OM Study  ID = ${OM_id}"

# Method subject meta-data
# Step 3 branch 1

   om.pssd.ex-method.subject.step.update \
      :id $ExMeth_id \
      :step "3.1.1" \
      :state complete \
      :ps-meta <\
         :hfi.pssd.EAE.optic-nerve.section <\
            :thickness 0.5 \
          >\
      >
#
   om.pssd.ex-method.subject.step.update \
      :id $ExMeth_id \
      :step "3.1.2" \
      :state complete \
      :ps-meta <\
         :hfi.pssd.EAE.stain  <\
            :agent < :name "methylene blue" >\
          >\
      >
  puts ""
}




#####################################################################################
# E.g. In aterm, put "set method_id 1005.5.272"  and then source this script

if { ![info exists method_id] } {
    error "The variable 'method_id' has not been defined - the method_id is the CID of the method to be used."
}

# Create Project

if { ![info exists project_id] } {
   puts " Project number not given; will auto-allocate next available CID"
   set project_id "-1"
}
set proj_id [ createProject $method_id $project_id ]
puts "Project ID = ${proj_id}"

# Create Control Subjects
# The controls have no age
set age "0"
set control true
#
set subj_name MO0025MBH1
set gender male
set hasOM Y
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0026MBH1
set gender male
set hasOM Y
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0027MBH1  
set gender male
set hasOM Y
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0028MBH1
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0029MBH1
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0030MBH1  
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0031MBH1
set gender male
set hasOM Y
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0032MBH1
set gender male
set hasOM Y
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0033MHB1  
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0034MHB1
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0053MHB1
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0054MHB1  
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0055MHB1
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0056MHB1  
set gender female
set hasOM N
set hasLong Y
set hasCoronal 
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0057MHB1  
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MOOCHBC1
set gender male
set hasOM N
set hasLong N
set hasCoronal Y
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MOOCHBC2  
set gender male
set hasOM N
set hasLong N
set hasCoronal Y
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MOOCHBC3
set gender male
set hasOM N
set hasLong N
set hasCoronal Y
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MOOCHBC4
set gender male
set hasOM N
set hasLong N
set hasCoronal Y
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MOOCHBC5
set gender male
set hasOM N
set hasLong N
set hasCoronal Y
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal

# Create EAE Subjects 

set control false
#
set subj_name MO0009CHB1
set age 8
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0010CHB1
set age 8
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0014MHB1
set age 8
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0015MHB1
set age 8
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0017MHB1
set age 20
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0018MHB1
set age 20
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0019MHB1
set age 20
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0020MBH1
set age 20
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0021MBH1
set age 20
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0022MBH1
set age 20
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0023MBH1
set age 20
set gender female
set hasOM N
set hasLong Y
set hasCoronal 
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0024MBH1
set age 20
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0035MHB1
set age 20
set gender female
set hasOM Y
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0037MBH1
set age 20
set gender female
set hasOM Y
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0040MHB1
set age 20
set gender male
set hasOM Y
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0042MHB1
set age 20
set gender male
set hasOM Y
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0045MHB1
set age 20
set gender male
set hasOM Y
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0046MHB1
set age 20
set gender male
set hasOM Y
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0047MHB1
set age 20
set gender male
set hasOM Y
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0048MHB1
set age 20
set gender male
set hasOM Y
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0049MHB1
set age 20
set gender female
set hasOM Y
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0050MHB1
set age 20
set gender female
set hasOM Y
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0052MHB1
set age 20
set gender male
set hasOM Y
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0058MHB1
set age 16
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0059MHB1
set age 16
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0060MHB1
set age 16
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0061MHB1
set age 16
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0062MHB1
set age 16
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0063MHB1
set age 12
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0064MHB1
set age 12
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0065MHB1
set age 12
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MO0066MHB1
set age 12
set gender female
set hasOM N
set hasLong Y
set hasCoronal N
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MOOCHBE1
set age 20
set gender male
set hasOM N
set hasLong N
set hasCoronal Y
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MOOCHBE11
set age 20
set gender male
set hasOM N
set hasLong N
set hasCoronal Y
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MOOCHBE2
set age 20
set gender male
set hasOM N
set hasLong N
set hasCoronal Y
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#
set subj_name MOOCHBE3
set age 20
set gender male
set hasOM N
set hasLong N
set hasCoronal Y
loadSubject $proj_id $method_id $subj_name $control $gender $age $hasOM $hasLong $hasCoronal
#