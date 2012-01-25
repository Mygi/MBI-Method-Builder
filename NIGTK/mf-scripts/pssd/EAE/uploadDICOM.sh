#!/bin/sh 

######################################################

editMeta ()
{
   patient_id="${1}"
   study_id="${2}"
#
   find . -name *.dcm | while read file ; do
      newName="^""${patient_id}"
#      echo "file = " $file
#      echo "name= " $patient_id
#      echo "study = " $study_id
#
      Set_PatientName "${newName}" "${file}"
      Set_StudyID "${study_id}" "${file}"
   done
}

upload () 
{
   port=6666
   #dicomServer="cluster.neuroimaging.org.au"   # Cluster 
   #dicomServer="172.23.65.3"      # Soma     DICOM server
   dicomServer="localhost"
   
# Use pixelmed

   /usr/local/pixelmed-dcmutils/StoreSCU.sh -callingAETitle HFI-DICOM-TEST -calledAETitle HFI -host "${dicomServer}" -port "${port}"  */*.dcm

# Use DCMTK
#   storescu -aet HFI-DICOM-TEST -aec HFI "${dicomServer}" "${port}" */*.dcm

}
   



doit () {

curDir=`pwd`

# Parse

name="${1}"
patient_id="${2}"
control="${3}"
#
echo "Subject Name    : " $name
echo "   Subject   ID : " $patient_id
echo "   Control      : " $control

# Descend to MR data

cd "${name}"/MRI
if [ "${control}" = "N" ]; then
   day=`ls`
   cd "${day}"
fi

# Edit Meta-data and Upload

if [ -e "Longitudinal" ]; then
  echo "   Upload Longitudinal DICOM data"
  cd Longitudinal
  study_id="${patient_id}".1.1
  editMeta "${patient_id}" "${study_id}"
  upload "${server}" "${port}"
  cd ..
fi
#
if [ -e "Coronal" ]; then
  echo "   Upload Coronal  DICOM data"
  cd Coronal
  study_id="${patient_id}".1.2
  editMeta "${patient_id}" "${study_id}"
  upload "${server}" "${port}"
  cd ..
fi

cd "${curDir}"
}




######################################################

# You must be located in the directory above the 
# "Controls" and "EAE" directories
# Script auto-detects presence of Longitudinal and/or Coronal data
# Script auto-detects presence of Day-<N> layer
#
# Input:
#  <Proj_id> is the Project CID (excluding first two numbers).
#
###########

#
# Parse inputs
#
proj_id=${1}

# Controls first

cd Controls
control=Y
subj_id="${proj_id}".1
subj_name=MO0025MBH1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".2
subj_name=MO0026MBH1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".3
subj_name=MO0027MBH1  
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".4
subj_name=MO0028MBH1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".5
subj_name=MO0029MBH1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".6
subj_name=MO0030MBH1  
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".7
subj_name=MO0031MBH1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".8
subj_name=MO0032MBH1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".9
subj_name=MO0033MHB1  
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".10
subj_name=MO0034MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".11
subj_name=MO0053MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".12
subj_name=MO0054MHB1  
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".13
subj_name=MO0055MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".14
subj_name=MO0056MHB1  
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".15
subj_name=MO0057MHB1  
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".16
subj_name=MOOCHBC1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".17
subj_name=MOOCHBC2  
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".18
subj_name=MOOCHBC3
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".19
subj_name=MOOCHBC4
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".20
subj_name=MOOCHBC5
doit "${subj_name}" "${subj_id}" "${control}"

# Create EAE Subjects 

cd ..
cd EAE
control=N
#
subj_id="${proj_id}".21
subj_name=MO0009CHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".22
subj_name=MO0010CHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".23
subj_name=MO0014MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".24
subj_name=MO0015MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".25
subj_name=MO0017MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".26
subj_name=MO0018MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".27
subj_name=MO0019MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".28
subj_name=MO0020MBH1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".29
subj_name=MO0021MBH1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".30
subj_name=MO0022MBH1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".31
subj_name=MO0023MBH1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".32
subj_name=MO0024MBH1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".33
subj_name=MO0035MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".34
subj_name=MO0037MBH1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".35
subj_name=MO0040MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".36
subj_name=MO0042MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".37
subj_name=MO0045MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".38
subj_name=MO0046MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".39
subj_name=MO0047MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".40
subj_name=MO0048MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".41
subj_name=MO0049MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".42
subj_name=MO0050MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".43
subj_name=MO0052MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".44
subj_name=MO0058MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".45
subj_name=MO0059MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".46
subj_name=MO0060MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".47
subj_name=MO0061MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".48
subj_name=MO0062MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".49
subj_name=MO0063MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".50
subj_name=MO0064MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".51
subj_name=MO0065MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".52
subj_name=MO0066MHB1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".53
subj_name=MOOCHBE1
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".54
subj_name=MOOCHBE11
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".55
subj_name=MOOCHBE2
doit "${subj_name}" "${subj_id}" "${control}"
#
subj_id="${proj_id}".56
subj_name=MOOCHBE3
doit "${subj_name}" "${subj_id}" "${control}"
