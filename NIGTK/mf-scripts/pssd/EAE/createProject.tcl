##################################################################################################################
# Script to upload some EAE data into the Data Archive
##################################################################################################################
#
# Create Project

proc createProject { method_id } {
   set r [ om.pssd.project.create \
     :description "Investigate axonal degeneration in murine optic nerve via EAE mice" \
     :name "EAE optic nerve degeneration in mice" \
     :namespace pssd \
     :meta < \
         :hfi.pssd.project < \
           :ethics-id -type "HFI AEC" HFI-123 \
           :funding-id NHMRC \
         >\
     >\
     :method < :id $method_id > \
     :member < :domain system :user manager :role project-administrator > \
     :member < :domain hfi :user nkilleen :role subject-administrator > \
  ]
   set pid  [xvalue id $r]
   return $pid
}

##################################################################################################################
# Create Subject 
proc createSubject { proj_id method_id subj_name age } {

    puts "proj_id, method_id, subj_name, age = ${proj_id}, ${method_id}, ${subj_name}, ${age}"

    if { $age == "X" } {

   set r [ om.pssd.subject.create \
     :description "EAE mouse" \
     :name $subj_name :pid $proj_id \
     :method $method_id \
     :public < \
       :hfi.pssd.subject < \
          :type animal \
          :control false \
       > \
       :hfi.pssd.animal.subject < \
          :species mouse \
          :gender male \
       > \
       :hfi.pssd.animal.disease < \
          :disease "Experimental Autoimmune Encephalomyelitis (EAE)" \
       > \
       :hfi.pssd.animal.genetics < \
          :strain <\
             :name "C57Black" \
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
   return "$sid $mid"

} else {
   set r [ om.pssd.subject.create \
     :description "EAE mouse" \
     :name $subj_name :pid $proj_id \
     :method $method_id \
     :public < \
       :hfi.pssd.subject < \
          :type animal \
          :control false \
       > \
       :hfi.pssd.animal.subject < \
          :species mouse \
          :gender male \
          :age -units days $age \
       > \
       :hfi.pssd.animal.disease < \
          :disease "Experimental Autoimmune Encephalomyelitis (EAE)" \
       > \
       :hfi.pssd.animal.genetics < \
          :strain <\
             :name "C57Black" \
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
   return "$sid $mid"

}

}

##################################################################################################################
# Create MR Study
proc createMRStudy { pid } {
   set r [ om.pssd.study.create \
        :description "MRI acquisition at aMRIF" \
        :pid $pid \
        :name "MRI Study" \
        :type MR
   ]
   set id  [xvalue id $r]
   return $id
}



###################################################################################################################
# Create OM  Study 
proc createOMStudy { pid } {
   set r [ om.pssd.study.create \
        :description "Optical Microscopy acquisition at HFI" \
        :pid $pid  \
        :name "OM Study" \
        :type OM
   ]
   set omid  [xvalue id $r]
   return $omid
}


###################################################################################################################
# Create and attach T2  MR DataSet
proc createT2DataSet { pid data_path size } {
   om.pssd.dataset.primary.create \
      :pid $pid  \
      :description "Longitudinal T2 image" \
      :in file:$data_path \
      :name "T2 image" \
      :type "dicom/series" \
      :subject < :state 1 > \
      :meta < :mf-dicom-series < :size $size > >
}

###################################################################################################################
# Create and attach DWI  MR DataSet
proc createDWIDataSet { pid data_path size } {
   om.pssd.dataset.primary.create \
     :pid $pid  \
     :description "Longitudinal DWI image" \
     :in file:$data_path \
     :name "DWI image" \
     :type "dicom/series" \
     :subject < :state 1 > \
     :meta < :mf-dicom-series < :size $size > >
}

# Specify meta ?

###################################################################################################################
# Create Microscopy  DataSet, which zips up high and low magnification folders
proc createOMDataSet { pid data_path } {
   om.pssd.dataset.primary.create \
     :pid $pid  \
     :description "High and low magnification OM images" \
     :in file:$data_path \
     :name "High and low magnification images" \
     :type "dataset/tiff" \
     :subject < :state 10 >
}
# Specify meta ?

###################################################################################################################

proc loadSubject { proj_id method_id subj_name MR_T2_path T2_SIZE MR_DWI_path DWI_SIZE OM_path age } {

# Create Subject

   set retval [ createSubject $proj_id $method_id $subj_name $age ]
   set subj_id [ lindex $retval 0 ]
   set ExMeth_id [ lindex $retval 1 ]
   puts "      Subject  ID = ${subj_id}"
   puts "      ExMethod ID = ${ExMeth_id}"

# Create MR Study 

   set MR_id [ createMRStudy $ExMeth_id ]
   puts "      MR Study  ID = ${MR_id}"

# Create MR Data Sets

   createT2DataSet $MR_id $MR_T2_path $T2_SIZE
   createDWIDataSet $MR_id $MR_DWI_path $DWI_SIZE

# Create OM Study

   if { $OM_path != "X" } {
      set OM_id [ createOMStudy $ExMeth_id ]
      puts "      OM Study  ID = ${OM_id}"

# Create DataSets

      createOMDataSet $OM_id $OM_path
   }
}


#####################################################################################
# E.g. In aterm, put "set method_id 1005.5.272"  and then source this script

if { ![info exists method_id] } {
    error "The variable 'method_id' has not been defined - the method_id is the CID of the method to be used."
}

# Set root data path
#set root_data_path   "/Users/nebk/Documents/Work/Neuroscience/Qizhu/"
set root_data_path   "/data/soma/users/nkilleen/data/Qizhu"

# Create Project

set proj_id [ createProject $method_id ]
puts "Project ID = ${proj_id}"

# Create Control Subject and load data

set age "X"
set subj_name "MO0025MBH1"
puts "  Subject Name ${subj_name}"
set MR_T2_path   "${root_data_path}/Controls/${subj_name}/MRI/Longitudinal/655361_542750/T2.zip"
set MR_DWI_path   "${root_data_path}/Controls/${subj_name}/MRI/Longitudinal/589825_534382/dwi.zip"
set T2_SIZE 2
set DWI_SIZE 3
set OM_path   "${root_data_path}/Controls/${subj_name}/Optical-Microscopy/content.zip"
loadSubject $proj_id $method_id $subj_name $MR_T2_path $T2_SIZE $MR_DWI_path $DWI_SIZE $OM_path $age

# Create Control Subject and load data

set subj_name "MO0026MBH1"
puts "  Subject Name ${subj_name}"
set MR_T2_path   "${root_data_path}/Controls/${subj_name}/MRI/Longitudinal/786433_601795/T2.zip"
set MR_DWI_path   "${root_data_path}/Controls/${subj_name}/MRI/Longitudinal/655361_813001/dwi.zip"
set T2_SIZE 2
set DWI_SIZE 3
set OM_path   "${root_data_path}/Controls/${subj_name}/Optical-Microscopy/content.zip"
loadSubject $proj_id $method_id $subj_name $MR_T2_path $T2_SIZE $MR_DWI_path $DWI_SIZE $OM_path $age


# Create Control Subject and load data

set subj_name "MO0031MBH1"
puts "  Subject Name ${subj_name}"
set MR_T2_path   "${root_data_path}/Controls/${subj_name}/MRI/Longitudinal/720897_605961/T2.zip"
set MR_DWI_path   "${root_data_path}/Controls/${subj_name}/MRI/Longitudinal/589825_731656/dwi.zip"
set T2_SIZE 2
set DWI_SIZE 3
set OM_path   "${root_data_path}/Controls/${subj_name}/Optical-Microscopy/content.zip"
loadSubject $proj_id $method_id $subj_name $MR_T2_path $T2_SIZE $MR_DWI_path $DWI_SIZE $OM_path $age

# Create EAE Subject and load data

set subj_name "MO0009CHB1"
set age "8"
puts "  Subject Name ${subj_name}"
set MR_T2_path   "${root_data_path}/EAE/${subj_name}/MRI/Day-8/Longitudinal/786433_407058/T2.zip"
set MR_DWI_path   "${root_data_path}/EAE/${subj_name}/MRI/Day-8/Longitudinal/720897_687043/dwi.zip"
set T2_SIZE 2
set DWI_SIZE 3
set OM_path "X"
loadSubject $proj_id $method_id $subj_name $MR_T2_path $T2_SIZE $MR_DWI_path $DWI_SIZE $OM_path $age
